package threeChess.agents;

import threeChess.*;

import java.util.*;

class Reward {
    HashMap<Integer, Integer> rewards = new HashMap<>();
    public Reward(int reward1, int reward2, int reward3){
        rewards.put(0, reward1);
        rewards.put(1, reward2);
        rewards.put(2, reward3);
    }

    public HashMap<Integer, Integer> getReward(){
        return rewards;
    }

    public int getRewardForPlayer(int player){
        return rewards.get(player);
    }

    public void addReward(Reward reward) {
        rewards.put(0, rewards.get(0) + reward.getRewardForPlayer(0));
        rewards.put(1, rewards.get(1) + reward.getRewardForPlayer(1));
        rewards.put(2, rewards.get(2) + reward.getRewardForPlayer(2));
    }
}

class MctsNode {
    private final MctsNode parent;
    private int numSimulations = 0;
    private Reward reward;
    private final LinkedList<MctsNode> children = new LinkedList<>();
    private final Position[][] unexploredMoves;
    private final Colour player;
    private final Position[][] moveUsedToGetToNode;
    private int prevValue;
    Board s;

    public MctsNode(MctsNode parent, Position[][] move, Board board) {
        this.parent = parent;
        moveUsedToGetToNode = move;
        unexploredMoves = getAvailableMoves(board);
        reward = new Reward(0,0,0);
        player = board.getTurn();
    }

    public MctsNode select() {
        MctsNode selectedNode = this;
        double max = Integer.MIN_VALUE;

        for (MctsNode child : getChildren()) {
            double uctValue = getUctValue(child);

            if (uctValue > max) {
                max = uctValue;
                selectedNode = child;
            }
        }

        return selectedNode;
    }

    private double getUctValue(MctsNode child) {
        double uctValue;

        if (child.getNumberOfSimulations() == 0) {
            uctValue = 1;
        } else {
            uctValue = (1.0 * child.getRewardForPlayer(getPlayer())) / (child.getNumberOfSimulations() * 1.0)
                    + (Math.sqrt(2 * (Math.log(getNumberOfSimulations() * 1.0) / child.getNumberOfSimulations())));
        }

        Random r = new Random();
        uctValue += (r.nextDouble() / 10000000);
        return uctValue;
    }

    public MctsNode expand(Board game) {
        if (!canExpand()) {
            return this;
        }
        Position[] pieces = game.getPositions(game.getTurn()).toArray(new Position[0]);
        Random random = new Random();
        Position start = pieces[0];

        Position move = unexploredMoves.remove(moveIndex);
        game.makeMove(move);
        MctsNode child = new MctsNode(this, move, game);
        children.add(child);
        return child;
    }

    public void backPropagate(Reward reward) {
        this.reward.addReward(reward);
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

    public int getPlayer() {
        return player;
    }

    public double getRewardForPlayer(int player) {
        return reward.getRewardForPlayer(player);
    }

    public boolean canExpand() {
        return unexploredMoves.size() > 0;
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

    public Position getMoveUsedToGetToNode() {
        return moveUsedToGetToNode;
    }

    private int reward(Board current) {
        int numMoves = current.getMoveCount();
        if (numMoves <= 2)
            return 0; // Our agent hasn't moved yet.
        // We don't need to reconstruct the board! We already have it!
        Board previous = s;
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

    public Agent22751102() {

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