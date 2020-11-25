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
            //Reset counters
            setNodesExamined(0);
            setPrunedCounter(0);
            setReachedLeafNodes(0);
            setSearchDepth(0);

            //Build tree
            GameBoardState root = gameState;
            List<ObjectiveWrapper> possibleMoves  = AgentController.getAvailableMoves(root,PlayerTurn.PLAYER_ONE); //Player two is the AI, player one is human.
            long beforeTime = new Date().getTime();
            createChildrenStates(possibleMoves,root,0,PlayerTurn.PLAYER_ONE); //Recursively build tree
            long afterTime = new Date().getTime();
            long runTime = (afterTime - beforeTime);
            System.out.println("Time for building state tree: " + runTime + "ms");

            //Run minimaxAB on the built tree
            long beforeMinimaxTime = new Date().getTime();
            int val = minimaxAB(root);
            long afterMinimaxTime = new Date().getTime();
            long runTimeMinimax = (afterMinimaxTime - beforeMinimaxTime);
            System.out.println("Time to run minimax: " + runTimeMinimax + "ms");

            //Match a move with the return value of the minimaxAB
            ObjectiveWrapper theMove = null;
            List<GameBoardState> states = root.getChildStates();
            for(GameBoardState state : states) {
                if(state.utility == val){
                    theMove = state.getLeadingMove();
                    break;
                }
            }

            //Catch any case where the utility of a node and the return value of minimaxAB was not found, this happened very few times, so we lazily handle it here.s
            if(theMove == null) {
               theMove = states.get(0).getLeadingMove();
            }

            return new MoveWrapper(theMove);
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
    /**
     * Minimax algorithm with alpha beta-pruning from Russel & Norvig page. 170
     * @param node The root node of the tree
     * @return The utility of the optimal node to pick
     */
    private int minimaxAB(GameBoardState node) {
        int v = maxValue(node,Integer.MIN_VALUE,Integer.MAX_VALUE);
        return v;
    }

    private int maxValue(GameBoardState node, int alpha, int beta) {
        setNodesExamined(getNodesExamined()+1);
        if (node.getChildStates().isEmpty()) {
            return getUtility(node);
        }

        int v = Integer.MIN_VALUE;

        List<GameBoardState> children = node.getChildStates();
        for(GameBoardState child : children) {
            v = Math.max(v,minValue(child,alpha,beta));
            if(v >= beta) {
                setPrunedCounter(getPrunedCounter()+1);
                return v;
            }
            alpha = Math.max(alpha,v);
        }
        node.utility = v;
        return v;
    }

    private int minValue(GameBoardState node, int alpha, int beta) {
        setNodesExamined(getNodesExamined()+1);
        if (node.getChildStates().isEmpty()) {
            return getUtility(node);
        }

        int v = Integer.MAX_VALUE;

        List<GameBoardState> children = node.getChildStates();
        for(GameBoardState child : children) {
            v = Math.min(v,maxValue(child,alpha,beta));
            if (v <= alpha)  {
                setPrunedCounter(getPrunedCounter()+1);
                return v;
            }
            v = Math.min(beta,v);
        }
        node.utility = v;
        return v;
    }

    private int getUtility(GameBoardState node) {
        setReachedLeafNodes(getReachedLeafNodes()+1);
        return (node.getWhiteCount() - node.getBlackCount());
    }
}
