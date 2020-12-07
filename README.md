# ♛ Three-Player Chess ♚

This project is my submission for the CITS3001 unit at UWA where we were tasked to make AI agents to play three-player chess.

## Agents In This Directory

This repo contains 6 agents:
<ul>
    <li>max^n</li>
    <ul><li>Implements the Max^n game playing algorithm to make moves</li></ul>
    <li>mcts</li>
    <ul><li>Implements the Monte Carlo Tree Search algorithm to make moves</li></ul>
    <li>RandomAgent</li>
    <ul><li>Plays random moves</li></ul>
    <li>RandomAttack</li>
    <ul><li>Plays aggressively attacking players in its way. If no player in the way then it will play a random move</li></ul>
    <li>ManualAgent</li>
    <ul><li>Plays moves based on the board positions a user gives using the command line</li></ul>
    <li>GUIAgent</li>
    <ul><li>Allows player to make moves by clicking on the board</li></ul>
</ul>

## Compilation

The following command can be used to compile threeChess,
```
javac -d bin src/threeChess/*.java src/threeChess/agents/*.java src/threeChess/agents/strategy/*.java
```

This command should be run from the directory above the src directory, where
it will create a bin directory to compile the program's class files into.


## Running ThreeChess
The following command can be used to start a game
```
java -cp bin/ threeChess.ThreeChess  
```

# ♜ The Project ♜

See the parent repository at [drtnf/threeChess](https://github.com/drtnf/threeChess).
