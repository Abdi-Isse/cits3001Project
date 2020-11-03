package threeChess.agents;

import threeChess.*;

import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.Arrays;


class Move{
    public Position start;
    public Position end;
    
    public Move(){}
    public Move(Position s, Position e){
        start =s;
        end = e;
    }
}
class mxRecord{
    public int[] utilityArr;
    public Move move;
    public mxRecord(){}
    public mxRecord(int[] uR, Move mv){
        utilityArr = uR;
        move = mv;
    }
}
 class positionValue {
    public Position position;
    public int value;

    public positionValue() {
    }

    public positionValue(Position p, int v) {
      position = p;
      value = v;
    }
}


public class maxN extends Agent {

    private static final String name = "maxN";
    public ArrayList<Position> enemyAttackPositions = new ArrayList<Position>();  //this will store all positions attackable by the enemies. 
    int maxThink = 0;

    public maxN() {
    }


    public Position[] playMove(Board board) {
        Colour player = board.getTurn();
        int time = board.getTimeLeft(player);

        if(board.getMoveCount() <= 12){
          return initialSetup(board);
        }

        return startMaxn(board);

        /*
         if(maxThink == 2){
            maxThink = 0;
            return startMaxn(board);
         }

         else{
             maxThink ++;
            return startNormalMove(board);
         }
         */
    }
    /**
     * This method will start calculating attack/defense moves without a tree.
     * @param board the current game board.
     * @return A move without 
     */
    public Position[] startNormalMove(Board board){
      Colour player = board.getTurn();
      enemyAttackPositions = getEnemyAttackPositions(board, player);
      positionValue defensePV = checkDefense(board, player);
      Position[] attackPath = randomAttack(board);
  
      if (defensePV.position != null) {
        if (board.getPiece(attackPath[1]) != null) {    //if there is an attack target
          if (defensePV.value > board.getPiece(attackPath[1]).getValue()) { // if defense value is greater than
            // attack value
            Position[] safePosition = findSafePosition(board, player, defensePV.position);
            if(safePosition != null){
              return safePosition;
            }
          } 
          else {
            return attackPath;
          }
        }
        else{ //if there is no attack target but a dodgable move
          Position[] safePosition = findSafePosition(board, player, defensePV.position);
          if(safePosition != null){
            return safePosition;
          }
        }
  
      }
      return attackPath;
    }
    /**
     * This method will return opening moves.
     * @param board the current game board. 
     * @return The pre-established opening move.
     */
    public Position[] initialSetup(Board board){
      Colour player = board.getTurn();
      try{
        //---Move 2 knights forward---
        if(board.getPiece(Position.get(player, 2, 2)) == null){
          if(board.getPiece(Position.get(player, 0, 1)).getType() == PieceType.valueOf("KNIGHT")){
            return new Position[] {Position.get(player, 0, 1), Position.get(player, 2, 2)};
          }
        }
        if(board.getPiece(Position.get(player, 2, 5)) == null){
          if(board.getPiece(Position.get(player, 0, 6)).getType() == PieceType.valueOf("KNIGHT")){
            return new Position[] {Position.get(player, 0, 6), Position.get(player, 2, 5)};
          }
        }
        //---Move pawns
        if(board.getPiece(Position.get(player, 3, 0)) == null){
          if(board.getPiece(Position.get(player, 1, 0)).getType() == PieceType.valueOf("PAWN")){
            return new Position[] {Position.get(player, 1, 0), Position.get(player, 3, 0)};
          }
        }
        if(board.getPiece(Position.get(player, 3, 7)) == null){
          if(board.getPiece(Position.get(player, 1, 7)).getType() == PieceType.valueOf("PAWN")){
            return new Position[] {Position.get(player, 1, 7), Position.get(player, 3, 7)};
          }
        }
        if(board.getPiece(Position.get(player, 2, 1)) == null){
          if(board.getPiece(Position.get(player, 1, 1)).getType() == PieceType.valueOf("PAWN")){
            return new Position[] {Position.get(player, 1, 1), Position.get(player, 2, 1)};
          }
        }
        if(board.getPiece(Position.get(player, 2, 6)) == null){
          if(board.getPiece(Position.get(player, 1, 6)).getType() == PieceType.valueOf("PAWN")){
            return new Position[] {Position.get(player, 1, 6), Position.get(player, 2, 6)};
          }
        }
      }
      catch(Exception e){}
      return null;
  }

    /**
     * This method will initiate max-n algorithm by getting all legit moves
     * and assess their return values.
     * The move with the largest utility for the current user will be returned. 
     * @param board get the current game board.
     * @return The best move possible, based on max-n assesment. 
     */
    public Position[] startMaxn(Board board){
      ArrayList<Move> actions = getActions(board, board.getTurn());   //Get all legal moves for current player
      int bestU = 0;
      ArrayList<mxRecord> allMX = new ArrayList<mxRecord>();
      for(Move action : actions){
          try{
              Board copyBoard = (Board)board.clone();
              copyBoard.move(action.start, action.end);
              int[] utilityArr = maxn(copyBoard, 0);
              mxRecord thisRecord = new mxRecord(utilityArr, action);
              allMX.add(thisRecord);

              if(board.getTurn() == Colour.RED){
                  if(utilityArr[0] >= bestU){
                      bestU = utilityArr[0];
                  }
              }
              else if(board.getTurn() == Colour.GREEN){
                  if(utilityArr[1] >= bestU){
                      bestU = utilityArr[1];
                  }
              }
              else{
                  if(utilityArr[2] >= bestU){
                      bestU = utilityArr[2];
                  }
              }
          }catch(Exception e){System.out.println("Failed copying board and making move");}
      }
      int minOtherU = Integer.MAX_VALUE;
      Move bestMove = null;
      for(mxRecord record : allMX){
          if(board.getTurn() == Colour.RED){
              if(record.utilityArr[0] == bestU && record.utilityArr[1] + record.utilityArr[2] <= minOtherU){
                  minOtherU = record.utilityArr[1] + record.utilityArr[2];
                  bestMove = record.move;
              }
          }
          else if(board.getTurn() == Colour.GREEN){
              if(record.utilityArr[1] == bestU && record.utilityArr[0] + record.utilityArr[2] <= minOtherU){
                  minOtherU = record.utilityArr[0] + record.utilityArr[2];
                  bestMove = record.move;
              }
          }
          else{
              if(record.utilityArr[2] == bestU && record.utilityArr[1] + record.utilityArr[0] <= minOtherU){
                  minOtherU = record.utilityArr[1] + record.utilityArr[0];
                  bestMove = record.move;
              }
          }
      }
      Position[] returnP = new Position[] {bestMove.start, bestMove.end};
      return returnP;
    }


    /**
     * This is the recursive method for running max-n algorithm.
     * If less than the level limit, it recursively calls maxn() on all the child moves.
     * If it reaches the level limit, it will return the scores (utilities) of all the players. 
     * @param curBoard get the current game board. 
     * @param level the current level of the tree.
     * @return An array of utilities {RED, GREEN, BLUE} which is the most beneficial to the current player
     */
     public int[] maxn(Board curBoard, int level){

      if(level >= 2 || curBoard.gameOver()){
          //return the list of utility for all players
          int utilityR = curBoard.score(Colour.RED); 
          int utilityG = curBoard.score(Colour.GREEN); 
          int utilityB = curBoard.score(Colour.BLUE); 
          int[] mxReturn = new int[] {utilityR, utilityG, utilityB};
          return mxReturn;
      }
      ArrayList<Move> actions = getActions(curBoard, curBoard.getTurn());

      int bestU = 0;
      ArrayList<mxRecord> allMX = new ArrayList<mxRecord>();
      for(Move action : actions){
          try{
              Board newBoard = (Board)curBoard.clone();
              newBoard.move(action.start, action.end);
              int[] utilityArr = maxn(newBoard, level+1);
              mxRecord thisRecord = new mxRecord(utilityArr, action);
              allMX.add(thisRecord);
              if(curBoard.getTurn() == Colour.RED){
                  if(utilityArr[0] >= bestU){
                      bestU = utilityArr[0];
                  }
              }
              else if(curBoard.getTurn() == Colour.GREEN){
                  if(utilityArr[1] >= bestU){
                      bestU = utilityArr[1];
                  }
              }
              else{
                  if(utilityArr[2] >= bestU){
                      bestU = utilityArr[2];
                  }
              }
          }
          catch(Exception e){System.out.println("Illegal move or failed copying board! Recursion failed!");}
      }
      int minOtherU = Integer.MAX_VALUE;
      int[] bestUtility = new int[3];
      for(mxRecord record : allMX){
          if(curBoard.getTurn() == Colour.RED){
              if(record.utilityArr[0] == bestU && record.utilityArr[1] + record.utilityArr[2] <= minOtherU){
                  minOtherU = record.utilityArr[1] + record.utilityArr[2];
                  bestUtility = record.utilityArr;
              }
          }
          else if(curBoard.getTurn() == Colour.GREEN){
              if(record.utilityArr[1] == bestU && record.utilityArr[0] + record.utilityArr[2] <= minOtherU){
                  minOtherU = record.utilityArr[0] + record.utilityArr[2];
                  bestUtility = record.utilityArr;
              }
          }
          else{
              if(record.utilityArr[2] == bestU && record.utilityArr[1] + record.utilityArr[0] <= minOtherU){
                  minOtherU = record.utilityArr[1] + record.utilityArr[0];
                  bestUtility = record.utilityArr;
              }
          }
      } 
      return bestUtility;
  }

    /**
     * This method finds the best attack position.
     * @param board the current game board
     * @return The best attack move which leads to highest gain. 
     */

    public Position[] randomAttack(Board board) {
        Colour playerCol = board.getTurn();
        Position[] allPositions = board.getPositions(playerCol).toArray(new Position[0]);
        Position start = null;
        Position end = null;
        Position legalStart = null;
        Position legalEnd = null;
    
        if (checkAttack(board)[0] != null) {
          start = checkAttack(board)[0];
          end = checkAttack(board)[1];
          return new Position[] { start, end };
        }
        // if there is no attack option available
        for (Position position : allPositions) {
          Piece thisPiece = board.getPiece(position);
          start = position;
          PieceType pieceType = thisPiece.getType();
          int stepRep = pieceType.getStepReps();
          Direction[][] legitSteps = pieceType.getSteps();
          for (Direction[] step : legitSteps) {
            end = start;
            for (int i = 0; i < stepRep; i++) {
              try {
                end = board.step(thisPiece, step, end);
                if (board.isLegalMove(start, end)) {
                  legalStart = start;
                  legalEnd = end;
                }
                if (board.isLegalMove(start, end) && !enemyAttackPositions.contains(end)) { // find a position where the
                                                                                            // enemy cannot attack
                  System.out.println("Moving: [" + start.toString() + ", " + end.toString() + " ]");
                  return new Position[] { start, end };
                }
              } catch (Exception e) {
              }
            }
          }
        }
        return new Position[] { legalStart, legalEnd };
      }
    

    /**
     * This method checks whether there is an attack move.
     * @return The best attack move possible, null if there is none.
     */
      public Position[] checkAttack(Board board) {
        Colour playerCol = board.getTurn();
        int curScore = board.score(playerCol);
        Position[] allPositions = board.getPositions(playerCol).toArray(new Position[0]);
        Position start = null;
        Position end = null;
        int highestScore = 0;

        for (Position position : allPositions) {
          Piece thisPiece = board.getPiece(position);
          PieceType pieceType = thisPiece.getType();
          int stepRep = pieceType.getStepReps();
          Direction[][] legitSteps = pieceType.getSteps();
          for (Direction[] step : legitSteps) {
            Position s = position;
            Position e = null;
            for (int i = 0; i < stepRep; i++) {
              try {
                e = board.step(thisPiece, step, s);
                if (board.isLegalMove(s, e)) {
                  Board copyBoard = (Board) board.clone();
                  copyBoard.move(s, e);
                  if (copyBoard.score(playerCol) > curScore && copyBoard.score(playerCol) > highestScore) {
                    start = s;
                    end = e;
                    highestScore = copyBoard.score(playerCol);
                    break;
                  }
                }
              } catch (Exception exc) {
                break;
              }
            }
          }
        }
        if (start == null) {
          return new Position[] { null };
        }
        return new Position[] { start, end };
      }
    

    /**
     * This method checks whether there is a defensive move.
     * @param board the current game board.
     * @param player the player to check defense for
     * @return the defensive move which leads to least loss.
     */
      public positionValue checkDefense(Board board, Colour player) {
        Position mostVulnerable = null;
        int vulnerability = 0;
        for (Position enemyAttack : enemyAttackPositions) {
          try {
            if (board.getPiece(enemyAttack).getColour() == player
                && board.getPiece(enemyAttack).getValue() > vulnerability) {
              vulnerability = board.getPiece(enemyAttack).getValue();
              mostVulnerable = enemyAttack;
            }
          } catch (Exception e) {
          }
        }
        positionValue pv = new positionValue(mostVulnerable, vulnerability);
        if (mostVulnerable != null) {
          System.out.println("Most Vulnerable position is :" + pv.position.toString() + " with value" + pv.value);
        }
        return pv;
      }

      /**
     * This method finds the safest position for a player to move.
     * @param board the current game board.
     * @param player the plyaer we need to find the safe position for.
     * @param position the position on which the piece needs to be saved.
     * @return the defensive move which leads to least loss.
     */
      public Position[] findSafePosition(Board board, Colour player, Position position) {
        Direction[][] steps = board.getPiece(position).getType().getSteps();
        int stepReps = board.getPiece(position).getType().getStepReps();
        Piece p = board.getPiece(position);
        Position start = position;
        Position safeEnd = null;
        for (Direction[] step : steps) {
          Position end = start;
          for (int i = 0; i < stepReps; i++) {
            try {
              end = board.step(p, step, end);
              if (board.isLegalMove(start, end)) {
                if (!enemyAttackPositions.contains(end)) {
                  safeEnd = end;
                }
              }
              break;
            } catch (Exception e) {
            }
          }
        }
        if(safeEnd == null) return null;
        return new Position[] { start, safeEnd };
      }

    /**
     * This method finds the enemy colours of the player
     * @return an array of enemies colours of the player
     */
      public Colour[] getEnemies(Colour player) {
        Colour[] enemies = new Colour[2];
        for (Colour color : Colour.values()) {
          if (color != player) {
            if (enemies[0] == null) {
              enemies[0] = color;
            }
            enemies[1] = color;
          }
        }
        return enemies;
      }
    
    /**
     * This method loop through all enemy pieces and get their
     * attackable positions.
     * @param board the current game board.
     * @param player the player we need to get attackable positions for. 
     * @return an Arraylist of all enemy attackable positions.
     */
      public ArrayList<Position> getEnemyAttackPositions(Board board, Colour player) {
        ArrayList<Position> enemyAttPositions = new ArrayList<Position>();
        Colour[] enemies = getEnemies(player);
        ArrayList<Position[]> enemyPositions = new ArrayList<Position[]>();
        enemyPositions.add(board.getPositions(enemies[0]).toArray(new Position[0]));
        enemyPositions.add(board.getPositions(enemies[1]).toArray(new Position[0]));
        for (Position[] enemyPosition : enemyPositions) {
          for (Position position : enemyPosition) {
            Position start = position;
            Piece p = board.getPiece(position);
            int stepRep = p.getType().getStepReps();
            Direction[][] steps = p.getType().getSteps();
            if (p.getType() != PieceType.PAWN) { // if not a PAWN, it attacks everything on its path
              for (Direction[] step : steps) {
                Position end = start;
                for (int i = 0; i < stepRep; i++) {
                  try {
                    end = board.step(p, step, end);
                    if (board.getPiece(end) == null) {
                      if (!enemyAttPositions.contains(end)) {
                        enemyAttPositions.add(end);
                      }
                    } else if (board.getPiece(end) != null) {
                      enemyAttPositions.add(end);
                      break;
                    }
    
                  } catch (Exception e) {
                  }
                }
              }
            } else {
              for (Direction[] step : steps) { // if it is a PAWN, there's special consideration
                Position end = start;
                Direction[] pawnForward = new Direction[] { Direction.FORWARD };
                Direction[] pawnForward2 = new Direction[] { Direction.FORWARD, Direction.FORWARD };
                if (!Arrays.equals(step, pawnForward) && !Arrays.equals(step, pawnForward2)) {
                  try {
                    end = board.step(p, step, end);
                    if (!enemyAttPositions.contains(end)) {
                      enemyAttPositions.add(end);
                    }
                  } catch (Exception e) {
                  }
                }
              }
            }
    
          }
        }
        return enemyAttPositions;
      }
    

      /**
     * This method get all legal moves of the current player.
     * @param board the current game board.
     * @param player the player we need to get attackable positions for. 
     * @return an Arraylist of all legal moves
     */
    public ArrayList<Move> getActions(Board board, Colour player){
            //get all actions and put into the ArrayList
            ArrayList<Move> allMoves = new ArrayList<Move>();
            Position[] allPositions = board.getPositions(player).toArray(new Position[0]);
            for(Position position : allPositions){
                Position start = position;
                int stepReps = board.getPiece(position).getType().getStepReps();
                Direction[][] allSteps = board.getPiece(position).getType().getSteps();
                ArrayList<Position> existEnds = new ArrayList<Position>();
                for(Direction[] step : allSteps){
                    Position end = start;
                    for(int i = 0; i < stepReps; i++){
                        try{
                            end = board.step(board.getPiece(position), step, end);
                            if(board.isLegalMove(start, end)){
                                if(!existEnds.contains(end)){
                                    allMoves.add(new Move(start, end));
                                    existEnds.add(end);
                                }  
                            }
                        }catch(Exception e){}   
                    }
                }
            }
            return allMoves;
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
