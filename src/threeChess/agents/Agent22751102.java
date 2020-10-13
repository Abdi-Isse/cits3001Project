package threeChess.agents;

import threeChess.*;

import java.util.*;

class MctsNode {
    private final MctsNode parent;
    private int numSimulations = 0;
    private double reward;
    private final LinkedList<MctsNode> children = new LinkedList<>();
    private final Position[][] unexploredMoves;
    private final Position[] moveUsedToGetToNode; // This is where we will store the moves. First arg stores the piece
                                                  // and the next stores the start and end positions
    private int prevValue;
    Board state; // Copy of board
    Board previous;

    public MctsNode(MctsNode parent, Position[] move, Board board) {
        this.parent = parent;
        moveUsedToGetToNode = move;
        unexploredMoves = getAvailableMoves(board); // returns all the available moves currently on the board
        reward = getRewardForPlayer(board); // initialise reward
        state = board;
    }

    /**
     * This method will loop through all the current child nodes in the decision
     * tree and calculate all of their UCT values in order to find the most most
     * promising child node to explore next
     * 
     * @return The most promising child node in the decision tree
     */
    public MctsNode select() {
        MctsNode selectedNode = this;
        double max = Integer.MIN_VALUE;

        for (MctsNode child : getChildren()) { // for every child node, we calculate their UCT values and select the
                                               // best one
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
                    + (2*(Math.sqrt((Math.log(getNumberOfSimulations() * 1.0) / child.getNumberOfSimulations()))));
        }
        return uctValue;
    }

    /**
     * This method takes as an input a Board and then examines all the available
     * positions currently on the board After examining the available moves on the
     * board this method will play a random valid move and create a new child node
     * 
     * @param game
     * @return
     */
    public MctsNode expand(Board game) {
        if (!canExpand()) {
            return this;
        }
        Position[][] validMoves = getAvailableMoves(game);
        for(int i = 0; i < validMoves.length; ++i) {
            Position[] move = validMoves[i];
            MctsNode child = new MctsNode(this, move, game);
            children.add(child);
        }
        MctsNode child = children.get(0);
        return child;
    }

    public void backPropagate(int reward) {
        this.reward += reward;
        this.numSimulations++;
        if (parent != null) {
            parent.backPropagate(reward);
        }
    }

    public LinkedList<MctsNode> getChildren() {
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
        previous = currentState;
        return score;
    }

    public boolean canExpand() {
        return unexploredMoves.length > 0;
    }

    public MctsNode getMostVisitedNode() {
        int mostVisitCount = 0;
        MctsNode bestChild = null;

        for (MctsNode child : getChildren()) {
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
        if(previous == null) {
            previous = current;
        }
        int numMoves = current.getMoveCount();
        if (numMoves <= 2)
            return 0; // Our agent hasn't moved yet.
        // We don't need to reconstruct the board! We already have it!
        // Here we have 2 boards, previous and current.
        // We generate the reward value for a board position to be the value of our
        // pieces + captured pieces at the current turn,
        // minus the value of our pieces + captured pieces from the previous turn.
        int currentValue = 0;
        Set<Position> currentPositions = current.getPositions(current.getTurn());
        Piece[] currentPieces = new Piece[currentPositions.size()];
        int i = 0;
        for (Position position : currentPositions) {
            currentPieces[i] = current.getPiece(position);
            i++;
        }
        List<Piece> currentCaptured = current.getCaptured(current.getTurn());
        for (Piece piece : currentPieces)
            currentValue += piece.getValue();
        for (Piece piece : currentCaptured)
            currentValue += piece.getValue();

        int previousValue = 0;
        if (prevValue != 0)
            previousValue = prevValue;
        else { // Only calculate previous value if haven't already brought the previous value
               // over
            Set<Position> previousPositions = previous.getPositions(current.getTurn());
            Piece[] previousPieces = new Piece[previousPositions.size()];
            i = 0;
            for (Position position : previousPositions) {
                previousPieces[i] = previous.getPiece(position);
                i++;
            }
            List<Piece> previousCaptured = previous.getCaptured(current.getTurn());
            for (Piece piece : previousPieces)
                previousValue += piece.getValue();
            for (Piece piece : previousCaptured)
                previousValue += piece.getValue();
        }

        prevValue = currentValue;
        previous = current;
        System.out.println("MCTS calculates a prev value of : " + prevValue);
        System.out.println("MCTS calculates a reward value of : " + (currentValue - prevValue));
        return currentValue - previousValue;
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
        // Find all of our piece positions and all of the board spaces
        Position[] pieces = board.getPositions(board.getTurn()).toArray(new Position[0]);
        Position[] spaces = Position.values();
        ArrayList<Position[]> valid_moves = new ArrayList<>();
        // Enumerate over all possible move spaces for all pieces
        for (Position piece : pieces) {
            for (Position space : spaces) {
                // Start Position -> End Position, Piece -> Space
                Position[] currMove = new Position[] { piece, space };
                if (board.isLegalMove(piece, space) && !valid_moves.contains(currMove))
                    valid_moves.add(currMove);
            }
        }
        return valid_moves.toArray(new Position[0][0]);
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

    public int maxIterations = 20;

    public Position[] getMove(Board game) throws ImpossiblePositionException {
        try {
            gameCopy = (Board) game.clone();
        } catch (Exception e) {
        }
        MctsNode rootNode = new MctsNode(null, null, gameCopy);
        for (int iteration = 0; iteration < maxIterations; iteration++) {
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
        while (!node.canExpand() && !game.gameOver()) {
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
        setBoard(game);
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
        int numMoves = current.getMoveCount();
        if (numMoves <= 2)
            return 0; // Agent hasn't moved yet.
        int currentValue = 0;
        Set<Position> currentPositions = current.getPositions(current.getTurn());
        Piece[] currentPieces = new Piece[currentPositions.size()];
        int i = 0;
        for (Position position : currentPositions) {
            currentPieces[i] = current.getPiece(position);
            i++;
        }
        List<Piece> currentCaptured = current.getCaptured(current.getTurn());
        for (Piece piece : currentPieces)
            currentValue += piece.getValue();
        for (Piece piece : currentCaptured)
            currentValue += piece.getValue();

        int previousValue = 0;
        if (prevValue != 0)
            previousValue = prevValue;
        else {
            Set<Position> previousPositions = previous.getPositions(current.getTurn());
            Piece[] previousPieces = new Piece[previousPositions.size()];
            i = 0;
            for (Position position : previousPositions) {
                previousPieces[i] = previous.getPiece(position);
                i++;
            }
            List<Piece> previousCaptured = previous.getCaptured(current.getTurn());
            for (Piece piece : previousPieces)
                previousValue += piece.getValue();
            for (Piece piece : previousCaptured)
                previousValue += piece.getValue();
        }

        prevValue = currentValue;
        return currentValue - previousValue;
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
        currentState = board;
        currentReward = reward(board);
        if (board.gameOver())
            return null;
        try {
            Position[] bestPosition = getMove(board);
            previous = board;
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