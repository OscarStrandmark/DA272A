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

        //Build tree

        //GameBoardState root = new GameBoardState(gameState.getGameBoard(),true); //Create root state object.
        //root.setPlayerTurn(BoardCellState.WHITE); //Not set when creating copy using copy constructor, bad framework
        GameBoardState root = gameState;
        List<ObjectiveWrapper> possibleMoves  = AgentController.getAvailableMoves(root,PlayerTurn.PLAYER_ONE); //Player two is the AI, player one is human.

        long beforeTime = new Date().getTime();
        createChildrenStates(possibleMoves,root,0,PlayerTurn.PLAYER_ONE); //Recursively build tree
        long afterTime = new Date().getTime();

        double runTime = (afterTime - beforeTime)/1000;
        System.out.println("Time for building state tree: " + runTime + "s");

        //Val is the alpha value of best move.
        long beforeMinimaxTime = new Date().getTime();
        int val = minimax(root,0,true,Integer.MIN_VALUE,Integer.MAX_VALUE);
        long afterMinimaxTime = new Date().getTime();

        double runTimeMinimax = (afterMinimaxTime - beforeMinimaxTime) / 1000;
        System.out.println("Time to run minimax: " + runTimeMinimax + "s");

        //Get the ObjectiveWrapper for the move.
        ObjectiveWrapper theMove = null;
        List<GameBoardState> states = root.getChildStates();
        for(GameBoardState state : states) {
            if(state.utility == val){
                theMove = state.getLeadingMove();
                break;
            }
        }

        MoveWrapper move = new MoveWrapper(theMove);

        return move;
    }

    private void createChildrenStates (List<ObjectiveWrapper> moves, GameBoardState parent, int depth,PlayerTurn turn) {
        if(depth <= AISettings.MAX_TREE_DEPTH) { //Check for depth of tree to stop building it.
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
        if(depth <= AISettings.MAX_TREE_DEPTH) { //Stop at depth defined in setting file

            if(node.getChildStates().isEmpty()) { //If current node is a leaf, return value of leaf.
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
                    if(beta <= alpha) break;
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
                    if(beta <= alpha) break;
                }
                node.utility = beta;
                return beta;
            }
        }
        return (node.getWhiteCount() - node.getBlackCount()); //This is a node at max defined depth, return as if it was a leaf node.
    }
}
