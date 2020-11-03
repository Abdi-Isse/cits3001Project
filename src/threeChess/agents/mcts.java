package threeChess.agents;

import threeChess.*;

import java.io.PrintStream;
import java.util.*;

/**
 * A node class that will allow us to implement the 4 phases of a monte carlo
 * tree search
 */
class MctsNode {
    private final Colour player;
    private final MctsNode parent;
    private int numSimulations = 0;
    private final Reward reward;
    private final LinkedList<MctsNode> children = new LinkedList<>();
    private final LinkedList<Position[]> unexploredMoves;
    private final Position[] moveUsedToGetToNode; // This is where we will store the moves used to get to the current
                                                  // child node

    /**
     * This will create a child node that links to a parent, has information about
     * the move used to get to the current node the state of the board after
     * performing said move
     * 
     * @param parent parent node
     * @param move   move used to get to child node
     * @param board  child nodes board state
     */
    public MctsNode(MctsNode parent, Position[] move, Board board) {
        player = board.getTurn();
        this.parent = parent;
        moveUsedToGetToNode = move;
        unexploredMoves = validMoves(board); // returns all the available moves currently on the board
        int blue = board.score(Colour.BLUE);
        int green = board.score(Colour.GREEN);
        int red = board.score(Colour.RED);
        reward = new Reward(blue, green, red);
    }

    /**
     * This method will loop through all the current child nodes in the decision
     * tree and calculate all of their reward values in order to find the most
     * promising child node to explore next. The MCTS algorithm is domain
     * independent so the formula for calculating reward that we will use is as
     * follows: Vi + 1 * squareroot(2*(log N) / ni) Where; Vi is the average
     * reward/value of root nodes child nodes N: is the number of times the parent
     * node has been visited ni: is the number of times the child node i has been
     * visited
     * 
     * @return The most promising child node in the decision tree
     */
    public MctsNode select() {
        MctsNode selectedNode = this;
        double max = Integer.MIN_VALUE;

        for (MctsNode child : getChildNodes()) { // for every child node, we calculate their UCT values and select the
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
     * @param child node
     * @return The UCT value of the child node
     */
    private double getUctValue(MctsNode child) {
        double uctValue;

        if (child.getNumberOfSimulations() == 0) {
            uctValue = 1;
        } else {
            uctValue = (child.getRewardForPlayer(getPlayer())) / (child.getNumberOfSimulations() * 1.0)
                    + ((Math.sqrt(2 * (Math.log(getNumberOfSimulations()) / child.getNumberOfSimulations()))));
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
        game.move(move[0], move[1]);
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

    /**
     * Given a board position, returns a 2D array of all the valid moves that can be
     * performed from the current position by the player whose turn it is to move.
     * 
     * @param board the current state of the game.
     * @return a 2D array, where the second dimension is 2 elements long, indicating
     *         all the valid moves for the current player.
     */
    public Position[][] getAvailableMoves(Board board) {
        Position[] pieces = board.getPositions(board.getTurn()).toArray(new Position[0]);
        Position[] spaces = Position.values();
        ArrayList<Position[]> validmoves = new ArrayList<>();
        for (Position piece : pieces) {
            for (Position space : spaces) {
                Position[] currentmove = new Position[] { piece, space };
                if (board.isLegalMove(piece, space) && !validmoves.contains(currentmove))
                    validmoves.add(currentmove);
            }
        }
        return validmoves.toArray(new Position[0][0]);
    }

    /**
     * This will convert getAvailableMoves board into a linkedlist for easier 
     * manipulation in our monte carlo tree searches
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

    /**
     * This method allows us to get the player whose turn it is 
     * @return player whose turn it is 
     */
    public Colour getPlayer() {
        return player;
    }

    /**
     * This method allows us to access the reward for a player inside of our reward hashmap
     * @param player whose reward we want to find out 
     * @return reward for said player
     */
    public double getRewardForPlayer(Colour player) {
        return reward.getRewardForPlayer(player);
    }

    /**
     * This method will allow us to access al the children of a node
     * @return a linked list of all the child nodes
     */
    public LinkedList<MctsNode> getChildNodes() {
        return children;
    }

    /**
     * This method allows us to find out the number of simulations in our monte carlo tree search
     * @return number of simulations 
     */
    public int getNumberOfSimulations() {
        return numSimulations;
    }

    /**
     * This method will allow us to determine whether a node can be expanded or not
     * @return boolean of whether we can expand a node or not 
     */
    public boolean nodeCanBeExpanded() {
        return unexploredMoves.size() > 0;
    }

    /**
     * This method will allow us to find out what the most visited node is in our monte
     * carlo tree search
     * @return MctsNode that has been visited the most
     */
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

    /**
     * This method will allow us to find out the move that was used to get to a node
     * @return move that was used to get to a node 
     */
    public Position[] getMoveUsedToGetToNode() {
        return moveUsedToGetToNode;
    }
}

/**
 * A reward class that will help us keep track of the rewards for each player
 */
class Reward {

    HashMap<Colour, Integer> rewards = new HashMap<>();

    /**
     * Will insert the rewards for each respective player inside of our hasmap
     * 
     * @param reward for player 1
     * @param reward for player 2
     * @param reward for player 3
     */
    public Reward(int reward1, int reward2, int reward3) {
        rewards.put(Colour.BLUE, reward1);
        rewards.put(Colour.GREEN, reward2);
        rewards.put(Colour.RED, reward3);
    }

    /**
     * This method will allow us to retrieve rewards
     * 
     * @return returns the hashmap of rewards
     */
    public HashMap<Colour, Integer> getReward() {
        return rewards;
    }

    /**
     * This method will allow us to retrieve the reward for a specific player
     * 
     * @param player the colour of the player we want to retrieve reward for
     * @return the value of this players reward
     */
    public int getRewardForPlayer(Colour player) {
        return rewards.get(player);
    }

    /**
     * This method will allow us to add reward values inside of the hashmap of
     * rewards for all the players
     * 
     * @param reward the reward to insert for each player
     */
    public void addReward(Reward reward) {
        rewards.put(Colour.BLUE, rewards.get(Colour.BLUE) + reward.getRewardForPlayer(Colour.BLUE));
        rewards.put(Colour.GREEN, rewards.get(Colour.GREEN) + reward.getRewardForPlayer(Colour.GREEN));
        rewards.put(Colour.RED, rewards.get(Colour.RED) + reward.getRewardForPlayer(Colour.RED));
    }

}

public class mcts extends Agent {

    private static final String name = "mcts";
    Board gameCopy;

    public mcts() {

    }

    public int maxIterations = 200; // This number specifies the number of simulations our monte carlo tree search performs

    /**
     * This method will begin our monte carlo tree search and return the best move that we should make on the board
     * @param game the current state of the board
     * @return the best move to make after running monte carlo tree search on the board
     * @throws ImpossiblePositionException
     */
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
            Reward reward = rollout(gameCopy);
            node.backPropagate(reward);

        }
        MctsNode mostVisitedChild = rootNode.getMostVisitedNode();
        return mostVisitedChild.getMoveUsedToGetToNode();
    }

    /**
     * This selects a node from the decision tree to evaluate
     * 
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
     * This function will make a copy of the game board and simulate the board using 
     * random agents playing against each other all the way till the end and then return
     * the reward value of the simulated board
     * 
     * @param game which is a board
     * @return reward for the board
     */
    private Reward rollout(Board board) {
        Board gameCopy;
        try {
            gameCopy = (Board) board.clone();
        } catch (CloneNotSupportedException e) {
            gameCopy = board;
        }
        Agent random = new RandomAgent();
        while (!gameCopy.gameOver()) {
            Position[] move = random.playMove(gameCopy);
            Position start = move[0];
            Position end = move[1];
            try {
                gameCopy.move(start, end, 1);
            } catch (ImpossiblePositionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            ;
        }
        int blue = 0;
        int green = 0;
        int red = 0;
        if (gameCopy.getWinner() == Colour.BLUE) {
            blue += 100;
        }
        if (gameCopy.getLoser() == Colour.BLUE) {
            blue -= 10000;
        }
        if (gameCopy.getWinner() == Colour.RED) {
            red += 100;
        }
        if (gameCopy.getLoser() == Colour.RED) {
            red -= 10000;
        }
        if (gameCopy.getWinner() == Colour.GREEN) {
            green += 100;
        }
        if (gameCopy.getLoser() == Colour.GREEN) {
            green -= 10000;
        }
        Reward result = new Reward(blue, green, red); // Result of simulated game
        return result;
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
        Colour player = board.getTurn();
        // -----Initial setup
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