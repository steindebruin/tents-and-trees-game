# Tents and Trees

JavaFX implementation of the classic Tents and Trees logic puzzle. Made by Stein de Bruin, Erasmus School of Economics.

## Overview

Tents and Trees is a puzzle game where the player must place tents on a grid such that:

- Every tree has exactly one adjacent tent.
- No two tents touch, including diagonally.
- Row and column tent limits are satisfied.

The application provides an interactive graphical user interface built with JavaFX.

## Features

- Interactive puzzle gameplay
- Three difficulty levels:
    - Easy
    - Medium
    - Hard
- Left-click to place or remove tents
- Right-click to place or remove flowers (markers)
- Dynamic row and column hints
- Invalid move detection
- Win detection and victory screen
- Reset and exit confirmations
- Show/hide game rules
- Forest-themed user interface

## Puzzle Generation

- Easy and Medium puzzles are generated automatically.
- Generated puzzles are validated to ensure a unique solution.
- Hard mode contains a collection of predefined puzzles.

## Technologies

- Java 17
- JavaFX
- Maven

## Running the Project

### Using IntelliJ IDEA

1. Open the project.
2. Set the SDK to Java 17.
3. Open the Maven tool window.
4. Run:

```
javafx:run
```

### Using Maven

From the project root:

```bash
mvn clean javafx:run
```

## Project Structure

```text
tents-and-trees-game/

├── src/
│   ├── TentsAndTrees.java
│   ├── PuzzleGenerator.java
│   ├── GameBoard.java
│   ├── CellState.java
│   └── Difficulty.java

├── pom.xml
└── README.md
```

## Author

**Stein de Bruin** \
BSc Econometrics & Economics \
Erasmus University Rotterdam
