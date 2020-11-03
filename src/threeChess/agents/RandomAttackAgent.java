package threeChess.agents;

import threeChess.*;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.Arrays;

public class RandomAttackAgent extends Agent {

  private static final String name = "RandomAttack agent";

  class Move {
    public Position start;
    public Position end;
    public int gain;

    public Move() {
    }

    public Move(Position s, Position e, int gain) {
      start = s;
      end = e;
      this.gain = gain;
    }
  }

  static class positionValue {
    public Position position;
    public int value;

    public positionValue() {
    }

    public positionValue(Position p, int v) {
      position = p;
      value = v;
    }
  }

  public static ArrayList<Position> enemyAttackPositions = new ArrayList<Position>();

  public RandomAttackAgent() {
  }

  public Position[] playMove(Board board) {
    Colour player = board.getTurn();
    enemyAttackPositions = getEnemyAttackPositions(board, player);

    // -----Initial setup
    if (board.getMoveCount() <= 15) {
      try {
        // ---Move 2 knights forward---
        if (board.getPiece(Position.get(player, 2, 2)) == null) {
          if (board.getPiece(Position.get(player, 0, 1)).getType() == PieceType.valueOf("KNIGHT")) {
            return new Position[] { Position.get(player, 0, 1), Position.get(player, 2, 2) };
          }
        }
        if (board.getPiece(Position.get(player, 2, 5)) == null) {
          if (board.getPiece(Position.get(player, 0, 6)).getType() == PieceType.valueOf("KNIGHT")) {
            return new Position[] { Position.get(player, 0, 6), Position.get(player, 2, 5) };
          }
        }
        // ---Move pawns
        if (board.getPiece(Position.get(player, 3, 0)) == null) {
          if (board.getPiece(Position.get(player, 1, 0)).getType() == PieceType.valueOf("PAWN")) {
            return new Position[] { Position.get(player, 1, 0), Position.get(player, 3, 0) };
          }
        }
        if (board.getPiece(Position.get(player, 3, 7)) == null) {
          if (board.getPiece(Position.get(player, 1, 7)).getType() == PieceType.valueOf("PAWN")) {
            return new Position[] { Position.get(player, 1, 7), Position.get(player, 3, 7) };
          }
        }
        if (board.getPiece(Position.get(player, 2, 1)) == null) {
          if (board.getPiece(Position.get(player, 1, 1)).getType() == PieceType.valueOf("PAWN")) {
            return new Position[] { Position.get(player, 1, 1), Position.get(player, 2, 1) };
          }
        }
        if (board.getPiece(Position.get(player, 2, 6)) == null) {
          if (board.getPiece(Position.get(player, 1, 6)).getType() == PieceType.valueOf("PAWN")) {
            return new Position[] { Position.get(player, 1, 6), Position.get(player, 2, 6) };
          }
        }
      } catch (Exception e) {
      }
    }
    positionValue defensePV = checkDefense(board, player);
    Position[] attackPath = randomAttack(board);
    try {
      System.out.println(
          "Maximum attack value is: " + board.getPiece(attackPath[1]).getValue() + "at" + attackPath[1].toString());
    } catch (Exception e) {
    }

    if (defensePV.position != null) {
      System.out.println("Vulnerable value is: " + defensePV.value);
      System.out.println("Vulnerable position is: " + defensePV.position.toString());
      if (board.getPiece(attackPath[1]) != null) {    //if there is an attack target
        if (defensePV.value > board.getPiece(attackPath[1]).getValue()) { // if defense value is greater than
          // attack value
          Position[] safePosition = findSafePosition(board, player, defensePV.position);
          if(safePosition != null){
            System.out.print("Moving to safe position: " + safePosition[0] + " " + safePosition[1]);
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
          System.out.print("Moving to safe position: " + safePosition[0] + " " + safePosition[1]);
          return safePosition;
        }
      }

    }
    return attackPath;
  }

  public static Position[] randomAttack(Board board) {
    Colour playerCol = board.getTurn();
    Position[] allPositions = board.getPositions(playerCol).toArray(new Position[0]);
    Position start = null;
    Position end = null;
    Position legalStart = null;
    Position legalEnd = null;
    // System.out.println("The current utility is: "+ utility(board, playerCol));

    if (checkAttack(board)[0] != null) {
      start = checkAttack(board)[0];
      end = checkAttack(board)[1];
      System.out.println("Now attacking");
      System.out.println("Moving: [" + start.toString() + ", " + end.toString() + " ]");
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
    System.out.println("Move from here!");
    System.out.println("Moving: [" + start.toString() + ", " + end.toString() + " ]");
    return new Position[] { legalStart, legalEnd };

  }

  public static Position[] checkAttack(Board board) {
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
      System.out.println("Not attackable");
      return new Position[] { null };
    } else {

      return new Position[] { start, end };
    }
  }

  public static positionValue checkDefense(Board board, Colour player) {

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

  public static Position[] findSafePosition(Board board, Colour player, Position position) {
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

  // Get colors of the enemies
  public static Colour[] getEnemies(Colour player) {
    Colour[] enemies = new Colour[2];
    for (Colour color : Colour.values()) {
      if (color != player) {
        if (enemies[0] == null) {
          enemies[0] = color;
        }
        enemies[1] = color;
      }
    }
    System.out.println("Enemies are: " + enemies[0].toString() + enemies[1].toString());
    return enemies;
  }

  public static ArrayList<Position> getEnemyAttackPositions(Board board, Colour player) {
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
                    // System.out.println(end);
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

  public String toString() {
    return name;
  }

  public void finalBoard(Board finalBoard) {
  }

}
