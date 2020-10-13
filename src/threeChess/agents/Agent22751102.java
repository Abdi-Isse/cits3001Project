package threeChess.agents;

import threeChess.*;

import java.util.*;

class MctsNode {
    private final MctsNode parent;
    private int numSimulations = 0;
    private double reward;
    private final LinkedList<MctsNode> children = new LinkedList<>();
    private final LinkedList<Position[]> unexploredMoves;
    private final Position[] moveUsedToGetToNode; // This is where we will store the moves used to get to the current child node
    Board state; // Copy of board

    /**
     * This will create a child node that links to a parent, has information about the move used to get to the current node
     * the state of the board after performing said move
     * @param parent parent node
     * @param move  move used to get to child node
     * @param board child nodes board state
     */
    public MctsNode(MctsNode parent, Position[] move, Board board) {
        this.parent = parent;
        moveUsedToGetToNode = move;
        unexploredMoves = validMoves(board); // returns all the available moves currently on the board
        reward = getRewardForPlayer(board); // initialise reward
        state = board;
    }

    /**
     * This method will loop through all the current child nodes in the decision
     * tree and calculate all of their reward values in order to find the most most
     * promising child node to explore next
     * The MCTS algorithm is domain independent so the formula for calculating reward is as follows:
     * Vi + 2 * squareroot( (log N) / ni)
     * Where:
     *      Vi is the average reward/value of all nodes beneath it
     *      N is the number of times the parent node has been visited 
     *      ni is the number of times the child node i has been visited
     * 
     * @return The most promising child node in the decision tree
     */
    public MctsNode select() {
        MctsNode selectedNode = this;
        double max = Integer.MIN_VALUE;

        for (MctsNode child : getChildNodes()) { // for every child node, we calculate their UCT values and select the best one
            double uctValue = getUctValue(child);
            if (uctValue > max) {
                max = uctValue;
                selectedNode = child;
            }
        }

        return selectedNode; // returns the child node with the best uct value
    }

    /**
     * This is a method that will calculate and return the UCT Value for a child
     * node
     * 
     * @param child
     * @return The UCT value of the child node
     */
    private double getUctValue(MctsNode child) {
        double uctValue;

        if (child.getNumberOfSimulations() == 0) {
            uctValue = 1;
        } else {
            uctValue = (1.0 * getRewardForPlayer(child.state)) / (child.getNumberOfSimulations() * 1.0)
                    + (2 * (Math.sqrt((Math.log(getNumberOfSimulations() * 1.0) / child.getNumberOfSimulations()))));
        }
        Random r = new Random();
        uctValue += (r.nextDouble() / 10000000);
        return uctValue;
    }

    /**
     * This method takes as an input a Board and then examines all the available
     * positions currently on the board. After examining the available moves on the
     * board this method will play a random valid move and create a new child node
     * 
     * @param game
     * @return
     * @throws ImpossiblePositionException
     */
    public MctsNode expand(Board game) throws ImpossiblePositionException {
        if (!nodeCanBeExpanded()) {
            return this;
        }
        Random random = new Random();
        int moveToPlay = random.nextInt(unexploredMoves.size());

        Position[] move = unexploredMoves.remove(moveToPlay);
        System.out.println("This is the move we have in our linkedlist");
        System.out.println(move[0] + " " + move[1]);
        game.move(move[0], move[1]);
        MctsNode child = new MctsNode(this, move, game);
        return child;
    }

    public void backPropagate(int reward) {
        this.reward += reward;
        this.numSimulations++;
        if (parent != null) {
            parent.backPropagate(reward);
        }
    }

    public LinkedList<MctsNode> getChildNodes() {
        return children;
    }

    public int getNumberOfSimulations() {
        return numSimulations;
    }

    public double getRewardForPlayer(Board currentState) {
        int numMoves = currentState.getMoveCount();
        if (numMoves <= 2)
            return 0;
        int score = reward(currentState);
        return score;
    }

    public boolean nodeCanBeExpanded() {
        return unexploredMoves.size() > 0;
    }

    public MctsNode getMostVisitedNode() {
        int mostVisitCount = 0;
        MctsNode bestChild = null;

        for (MctsNode child : getChildNodes()) {
            if (child.getNumberOfSimulations() > mostVisitCount) {
                bestChild = child;
                mostVisitCount = child.getNumberOfSimulations();
            }
        }
        return bestChild;
    }

    public Position[] getMoveUsedToGetToNode() {
        return moveUsedToGetToNode;
    }

    private int reward(Board current) {
        Colour player = current.getTurn();
        return current.score(player);
    }

    /**
     * Given a board position, returns a 2D array of all the valid moves that can be
     * performed from the current position by the player whose turn it is to move.
     * 
     * @param board the current state of the game.
     * @return a 2D array, where the second dimension is 2 elements long, indicating
     *         all the valid moves for the current player.
     */
    private Position[][] getAvailableMoves(Board board) {
        Position[] pieces = board.getPositions(board.getTurn()).toArray(new Position[0]);
        Position[] spaces = Position.values();
        ArrayList<Position[]> valid_moves = new ArrayList<>();
        for (Position piece : pieces) {
            for (Position space : spaces) {
                Position[] currMove = new Position[] { piece, space };
                if (board.isLegalMove(piece, space) && !valid_moves.contains(currMove))
                    valid_moves.add(currMove);
            }
        }
        return valid_moves.toArray(new Position[0][0]);
    }

    /**
     * This will convert getAvailableMoves board into a linkedlist for easier manipulation
     * @param board
     * @return
     */
    public LinkedList<Position[]> validMoves(Board board) {
        Position[][] validMoves = getAvailableMoves(board);
        LinkedList<Position[]> unexplored = new LinkedList<>();
        for (int i = 0; i < validMoves.length; ++i) {
            unexplored.add(validMoves[i]);
        }
        return unexplored;
    }
}

public class Agent22751102 extends Agent {

    private static final String name = "Agent22751102";
    Board gameCopy;
    Board currentState;
    Board state;
    Board previous = state;
    int prevValue;
    int currentReward;

    public Agent22751102() {

    }

    public int maxIterations = 1;

    public Position[] getMove(Board game) throws ImpossiblePositionException {
        MctsNode rootNode = new MctsNode(null, null, game);
        for (int iteration = 0; iteration < maxIterations; iteration++) {
            try {
                gameCopy = (Board) game.clone();
            } catch (CloneNotSupportedException e) {
                gameCopy = game;
            }
            MctsNode node = select(rootNode, gameCopy);
            node = node.expand(gameCopy);
            int reward = rollout(gameCopy);
            node.backPropagate(reward);
        }

        MctsNode mostVisitedChild = rootNode.getMostVisitedNode();
        return mostVisitedChild.getMoveUsedToGetToNode();
    }

    /**
     * This selects a node from the decision tree to evaulate
     * @param node which has information about different moves
     * @param game the board for said move
     * @return the node that we select 
     * @throws ImpossiblePositionException
     */
    private MctsNode select(MctsNode node, Board game) throws ImpossiblePositionException {
        while (!node.nodeCanBeExpanded() && !game.gameOver()) {
            node = node.select();
            Position[] move = node.getMoveUsedToGetToNode();
            if (move != null) {
                game.move(move[0], move[1]);
            }
        }

        return node;
    }

    /**
     * This function will simulate the current board till the end and then
     * return the reward value of the simulated board
     * @param game which is a board
     * @return reward for the board
     */
    private int rollout(Board game) {
        //TODO
        return reward(game);
    }

    /**
     * This method will take a board as an argument and calculate the reward value for
     * a board position. 
     * Reward value = value of players pieces + value of pieces captured
     * @param current which is the current board
     * @return reward which is an integer
     */
    private int reward(Board current) {
        Colour player = current.getTurn();
        return current.score(player);
    }

    /**
     * Play a move in the game. The agent is given a Board Object representing the
     * position of all pieces, the history of the game and whose turn it is. They
     * respond with a move represented by a pair (two element array) of positions:
     * the start and the end position of the move.
     * 
     * @param board The representation of the game state.
     * @return a two element array of Position objects, where the first element is
     *         the current position of the piece to be moved, and the second element
     *         is the position to move that piece to.
     **/
    public Position[] playMove(Board board) {
        // COMPLETE THIS
        if (board.gameOver())
            return null;
        try {
            Position[] bestPosition = getMove(board);
            return bestPosition;
        } catch (ImpossiblePositionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return new Position[] {};
    }

    /**
     * @return the Agent's name, for annotating game description.
     **/
    public String toString() {
        return name;
    }

    /**
     * Displays the final board position to the agent, if required for learning
     * purposes. Other a default implementation may be given.
     * 
     * @param finalBoard the end position of the board
     **/
    public void finalBoard(Board finalBoard) {
    }

}