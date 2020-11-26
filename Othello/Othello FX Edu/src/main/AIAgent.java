package main;

import ABMinimax.AISettings;
import com.eudycontreras.othello.capsules.AgentMove;
import com.eudycontreras.othello.capsules.MoveWrapper;
import com.eudycontreras.othello.capsules.ObjectiveWrapper;
import com.eudycontreras.othello.controllers.Agent;
import com.eudycontreras.othello.controllers.AgentController;
import com.eudycontreras.othello.enumerations.PlayerTurn;
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

            //Run minimax algorithm and time its execution
            long beforeTime = new Date().getTime();
            int val = minimaxTreeBuild(gameState,0,true,Integer.MIN_VALUE,Integer.MAX_VALUE); //Recursively build tree
            long afterTime = new Date().getTime();
            long runTime = (afterTime - beforeTime);
            System.out.println("Time: " + runTime + "ms");


            //Match a move with the return value of the minimaxAB
            ObjectiveWrapper theMove = null;
            List<GameBoardState> states = gameState.getChildStates();
            for(GameBoardState state : states) {
                if(state.utility == val){
                    theMove = state.getLeadingMove();
                    break;
                }
            }

            //Catch any case where the utility of a node and the return value of minimaxAB was not found, this happened very few times (~1/500), so we lazily handle it here.
            //Might be some
            if(theMove == null) {
               theMove = states.get(0).getLeadingMove();
            }

            return new MoveWrapper(theMove);
        }

        return new MoveWrapper(null); //If no possible move, return null move to skip turn.
    }

    /**
     * A amalgamation of the tree building algorithm and the alpha-beta-minimax algorithm.
     * @param node parent node whose children will be traversed
     * @param depth depth of parent node
     * @param isMaximizing true if maximizing player
     * @param alpha alpha-value
     * @param beta beta-value
     * @return utility value of
     */
    private int minimaxTreeBuild(GameBoardState node, int depth, boolean isMaximizing, int alpha, int beta) {
        //Set display-boxes
        setSearchDepth(depth);
        setNodesExamined(getNodesExamined()+1);

        int v = 0;

        //If leaf node, return utility value
        if(depth == AISettings.MAX_TREE_DEPTH) return getUtility(node);

        if(isMaximizing) {
            List<ObjectiveWrapper> possibleMoves  = AgentController.getAvailableMoves(node,PlayerTurn.PLAYER_ONE); //Get all possible moves for max player
            v = Integer.MIN_VALUE;
            for (ObjectiveWrapper move : possibleMoves) { //Iterate over all possible moves for the max player
                GameBoardState child = AgentController.getNewState(node,move); //Get the board state if the current move is played
                v = Math.max(v,minimaxTreeBuild(child,depth+1,false,alpha,beta));
                if(v >= beta) { //Prune
                    setPrunedCounter(getPrunedCounter()+1); //Increment counter
                    node.utility = v; //Set utility of node so that we can see what move to pick after algorithm has ran
                    return v;
                }
                node.addChildState(child);
                alpha = Math.max(alpha,v);
            }
            node.utility = v;
        }

        //Same as above if-condition, but for the other player
        else if (!isMaximizing){
            List<ObjectiveWrapper> possibleMoves = AgentController.getAvailableMoves(node,PlayerTurn.PLAYER_TWO);
            v = Integer.MIN_VALUE;
            for(ObjectiveWrapper move : possibleMoves) {
                GameBoardState child = AgentController.getNewState(node,move);
                v = Math.min(v,minimaxTreeBuild(child,depth+1,true,alpha,beta));
                if(v <= alpha) {
                    setPrunedCounter(getPrunedCounter()+1);
                    node.utility = v;
                    return v;
                }
                node.addChildState(child);
                beta = Math.min(beta,v);
            }
            node.utility = v;
        }

        return v;
    }

    private int getUtility(GameBoardState node) {
        setReachedLeafNodes(getReachedLeafNodes()+1);
        return (node.getWhiteCount() - node.getBlackCount());
    }
}
