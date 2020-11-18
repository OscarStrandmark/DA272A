package main;

import ABMinimax.AISettings;
import ABMinimax.Node;
import com.eudycontreras.othello.capsules.AgentMove;
import com.eudycontreras.othello.capsules.MoveWrapper;
import com.eudycontreras.othello.capsules.ObjectiveWrapper;
import com.eudycontreras.othello.controllers.Agent;
import com.eudycontreras.othello.controllers.AgentController;
import com.eudycontreras.othello.enumerations.BoardCellState;
import com.eudycontreras.othello.enumerations.PlayerTurn;
import com.eudycontreras.othello.models.GameBoard;
import com.eudycontreras.othello.models.GameBoardState;

import java.util.List;
import java.util.Date;

public class AIAgent extends Agent {

    public AIAgent(PlayerTurn playerTurn) {
        super(playerTurn);
    }

    public AIAgent(String agentName) {
        super(agentName);
    }

    public AIAgent(String agentName, PlayerTurn playerTurn) {
        super(agentName, playerTurn);
    }

    @Override
    public AgentMove getMove(GameBoardState gameState) {

        if(!AgentController.isTerminal(gameState,PlayerTurn.PLAYER_ONE)) { //If not terminal, do move
            setNodesExamined(0);
            setPrunedCounter(0);
            setReachedLeafNodes(0);
            setSearchDepth(0);

            //Build tree

            //GameBoardState root = new GameBoardState(gameState.getGameBoard(),true); //Create root state object.
            //root.setPlayerTurn(BoardCellState.WHITE); //Not set when creating copy using copy constructor, bad framework
            GameBoardState root = gameState;
            List<ObjectiveWrapper> possibleMoves  = AgentController.getAvailableMoves(root,PlayerTurn.PLAYER_ONE); //Player two is the AI, player one is human.

            long beforeTime = new Date().getTime();
            createChildrenStates(possibleMoves,root,0,PlayerTurn.PLAYER_ONE); //Recursively build tree
            long afterTime = new Date().getTime();

            long runTime = (afterTime - beforeTime);
            System.out.println("Time for building state tree: " + runTime + "ms");

            int alpha = Integer.MIN_VALUE;
            int beta  = Integer.MAX_VALUE;


            //Val is the alpha value of best move.
            long beforeMinimaxTime = new Date().getTime();
            int val = minimax(root,0,true,alpha,beta);
            long afterMinimaxTime = new Date().getTime();

            long runTimeMinimax = (afterMinimaxTime - beforeMinimaxTime);
            System.out.println("Time to run minimax: " + runTimeMinimax + "ms");

            //Get the ObjectiveWrapper for the move.
            ObjectiveWrapper theMove = null;
            List<GameBoardState> states = root.getChildStates();
            for(GameBoardState state : states) {
                if(state.utility == val){
                    theMove = state.getLeadingMove();
                    break;
                }
            }

            if(theMove == null) {
                System.out.println("move was null");
            }

            MoveWrapper move = new MoveWrapper(theMove);

            return move;
        }

        return new MoveWrapper(null); //If no possible move, return null move to skip turn.
    }

    private void createChildrenStates (List<ObjectiveWrapper> moves, GameBoardState parent, int depth,PlayerTurn turn) {
        if(depth > getSearchDepth()) setSearchDepth(depth);
        if(depth < AISettings.MAX_TREE_DEPTH) { //Check for depth of tree to stop building it.
            GameBoard board = parent.getGameBoard(); //All the children will have the same parent board, so get it outside the loop to save time.
            for(ObjectiveWrapper move : moves) {
                GameBoardState child = AgentController.getNewState(parent,move); //Create child state, leading move and parent is set inside this
                parent.addChildState(child);

                //Alternate which player plays a piece
                if(turn == PlayerTurn.PLAYER_ONE) {
                    List<ObjectiveWrapper> possibleMoves = AgentController.getAvailableMoves(child,PlayerTurn.PLAYER_TWO); //Get all possible moves on child board.
                    createChildrenStates(possibleMoves,child,depth+1,PlayerTurn.PLAYER_TWO); //Recursively build tree, depth + 1 to stop infinite recursion
                } else {
                    List<ObjectiveWrapper> possibleMoves = AgentController.getAvailableMoves(child,PlayerTurn.PLAYER_ONE); //Get all possible moves on child board.
                    createChildrenStates(possibleMoves,child,depth+1,PlayerTurn.PLAYER_ONE); //Recursively build tree, depth + 1 to stop infinite recursion
                }
            }
        }
    }

    private int minimax(GameBoardState node, int depth, boolean isMaximisingPlayer, int alpha, int beta) {
        setNodesExamined(getNodesExamined() + 1);
        if(depth < AISettings.MAX_TREE_DEPTH) { //Stop at depth defined in setting file

            if(node.getChildStates().isEmpty()) { //If current node is a leaf, return value of leaf.
                setReachedLeafNodes(getReachedLeafNodes() + 1);
                return (node.getWhiteCount() - node.getBlackCount()); //The higher white count the higher utility
            }

            //Get best alpha value from children and assign to node
            if(isMaximisingPlayer) { //MAX
                int bestValue = Integer.MIN_VALUE;
                List<GameBoardState> children = node.getChildStates();
                for(GameBoardState child : children) {
                    int value = minimax(child,depth+1,false,alpha,beta);
                    bestValue = Math.max(bestValue,value);
                    alpha = Math.max(alpha,bestValue);
                    if(beta <= alpha) {
                        setPrunedCounter(getPrunedCounter() + 1);
                        break;
                    }
                }
                node.utility = alpha;
                return alpha;
            } else { //MIN, get best beta value from children and assign to node.
                int bestValue = Integer.MAX_VALUE;
                List<GameBoardState> children = node.getChildStates();
                for(GameBoardState child : children) {
                    int value = minimax(child,depth+1,true,alpha,beta);
                    bestValue = Math.min(bestValue,value);
                    beta = Math.min(beta,bestValue);
                    if(beta <= alpha) {
                        setPrunedCounter(getPrunedCounter() + 1);
                        break;
                    }
                }
                node.utility = beta;
                return beta;
            }
        }
        setReachedLeafNodes(getReachedLeafNodes() + 1);
        return (node.getWhiteCount() - node.getBlackCount()); //This is a node at max defined depth, return as if it was a leaf node.
    }


    private int minimax2(GameBoardState node, boolean isMaximisingPlayer, int alpha, int beta) {
        int v = maxValue(node,alpha,beta);
        return v;
    }

    private int maxValue(GameBoardState node, int alpha, int beta) {

        if (node.getChildStates().isEmpty()) {
            return (node.getWhiteCount() - node.getBlackCount());
        }

        int v = Integer.MIN_VALUE;

        List<GameBoardState> children = node.getChildStates();
        for(GameBoardState child : children) {
            v = Math.max(v,minValue(child,alpha,beta));
            if(v >= beta) {
                node.utility = v;
                System.out.println("pruned");
                return v;
            }
            v = Math.max(alpha,v);
        }
        node.utility = v;
        return v;
    }

    private int minValue(GameBoardState node, int alpha, int beta) {

        if (node.getChildStates().isEmpty()) {
            return (node.getWhiteCount() - node.getBlackCount());
        }

        int v = Integer.MAX_VALUE;

        List<GameBoardState> children = node.getChildStates();
        for(GameBoardState child : children) {
            v = Math.min(v,maxValue(child,alpha,beta));
            if (v <= alpha)  {
                node.utility = v;
                System.out.println("pruned");
                return v;
            }
            v = Math.min(beta,v);
        }
        node.utility = v;
        return v;
    }
}
