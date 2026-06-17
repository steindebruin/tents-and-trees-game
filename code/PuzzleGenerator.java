package org.example;
import java.util.*;

public class PuzzleGenerator {

    private static final Random RANDOM = new Random();
    private static int hardIndex = 0;

    /**
     * Constructs a new GameBoard based on the selected difficulty. Easy and Medium puzzles are generated randomly.
     * For the Hard difficulty, there are five puzzles which are returned in a cyclical and sequential order.
     * @param diff the selected difficulty
     * @return a fully initialised GameBoard (with trees and tent limits)
     */
    public static GameBoard generate(Difficulty diff) {
        return diff == Difficulty.Hard ? getHardPuzzle() : generateRandom(diff);
    }

    /**
     * Creates a randomly generated puzzle board that satisfies all game constraints. It repeatedly attempts to place
     * trees and tents until it finds an instance with exactly one solution. Each attempt builds a grid by randomly
     * placing trees, then solving it with backtracking to verify that the solution is unique.
     * @param diff the difficulty level
     * @return a valid and uniquely solvable GameBoard
     */
    private static GameBoard generateRandom(Difficulty diff) {
        int size = diff.size;
        int treeCount = diff.treeCount;

        for (int attempt = 0; attempt < 5000; attempt++) {
            List<int[]> trees = generateTreePlacement(size, treeCount);
            if (trees == null) {
                continue;
            }

            CellState[][] grid = new CellState[size][size];
            for (int r = 0; r < size; r++) {
                Arrays.fill(grid[r], CellState.EMPTY);
            }
            for (int[] t : trees) {
                grid[t[0]][t[1]] = CellState.TREE;
            }

            List<List<int[]>> solutions = new ArrayList<>();
            placeTents(grid, trees, new ArrayList<>(), 0, solutions, size);

            if (solutions.size() == 1) {
                List<int[]> tents = solutions.get(0);
                int[] rowCounts = countByRow(size, tents);
                int[] colCounts = countByCol(size, tents);

                GameBoard board = new GameBoard(size, size, rowCounts, colCounts);
                for (int[] t : trees) {
                    board.setTree(t[0], t[1]);
                }
                return board;
            }
        }
        throw new RuntimeException("Failed to generate a unique puzzle.");
    }

    /**
     * Randomly places the specified number of trees on the grid.
     * @param size the dimension of the square grid
     * @param treeCount the number of trees that should be placed on the grid
     * @return
     */
    private static List<int[]> generateTreePlacement(int size, int treeCount) {
        for (int attempt = 0; attempt < 5000; attempt++) {
            CellState[][] grid = new CellState[size][size];
            for (int r = 0; r < size; r++) {
                Arrays.fill(grid[r], CellState.EMPTY);
            }
            List<int[]> trees = new ArrayList<>();
            int tries = 0;
            while (trees.size() < treeCount && tries++ < 5000) {
                int r = RANDOM.nextInt(size), c = RANDOM.nextInt(size);
                if (grid[r][c] == CellState.EMPTY) {
                    grid[r][c] = CellState.TREE;
                    trees.add(new int[]{r, c});
                }
            }

            if (trees.size() < treeCount) {
                continue;
            }
            if (trees.stream().allMatch(t -> hasEmptyNeighbour(grid, t[0], t[1], size))) {
                return trees;
            }
        }
        return null;
    }

    /**
     * Checks if a given tree cell has at least one empty adjacent neighbour.
     * @param grid the current grid state
     * @param r the row index of the tree
     * @param c the column index of the tree
     * @param size the board size
     * @return true if at least one neighbour is empty, false otherwise
     */
    private static boolean hasEmptyNeighbour(CellState[][] grid, int r, int c, int size) {
        for (int[] n : neighbours(r, c, size)) {
            if (grid[n[0]][n[1]] == CellState.EMPTY) {
                return true;
            }
        }
        return false;
    }

    /**
     * Attempts to place one tent next to each tree using recursive backtracking. If multiple valid full placements
     * are found, the recursion stops early to detect non-unique puzzles.
     * @param grid the current grid
     * @param trees list of tree positions
     * @param placed list of tents placed so far
     * @param idx index of the current tree being processed
     * @param solutions list to store all valid solutions
     * @param size the size of the board
     */
    private static void placeTents(CellState[][] grid, List<int[]> trees, List<int[]> placed, int idx,
                                   List<List<int[]>> solutions, int size) {
        if (solutions.size() > 1) {
            return;
        }
        if (idx == trees.size()) {
            solutions.add(new ArrayList<>(placed));
            return;
        }

        for (int[] n : shuffled(neighbours(trees.get(idx)[0], trees.get(idx)[1], size))) {
            int r = n[0], c = n[1];
            if (grid[r][c] == CellState.EMPTY && !adjacentToTent(placed, r, c)) {
                grid[r][c] = CellState.TENT;
                placed.add(new int[]{r, c});
                placeTents(grid, trees, placed, idx + 1, solutions, size);
                grid[r][c] = CellState.EMPTY;
                placed.remove(placed.size() - 1);
            }
        }
    }

    /**
     * Helper method for checking whether a cell is adjacent to any already placed tent
     * @param tents current list of tents placed
     * @param r candidate row index
     * @param c candidate column index
     * @return true if another tent touches this cell, false otherwise
     */
    private static boolean adjacentToTent(List<int[]> tents, int r, int c) {
        for (int[] t : tents) {
            if (Math.abs(t[0] - r) <= 1 && Math.abs(t[1] - c) <= 1) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the list of orthogonal neighbour coordinates (up, down, left, and right) for a given cell position.
     * @param r the row index
     * @param c the column index
     * @param size the grid size
     * @return the list of neighbour coordinates
     */
    private static List<int[]> neighbours(int r, int c, int size) {
        List<int[]> list = new ArrayList<>();
        if (r > 0) {
            list.add(new int[]{r - 1, c});
        }
        if (r < size - 1) {
            list.add(new int[]{r + 1, c});
        }
        if (c > 0) {
            list.add(new int[]{r, c - 1});
        }
        if (c < size - 1) {
            list.add(new int[]{r, c + 1});
        }
        return list;
    }

    /**
     * Shuffles a list of coordinate pairs in order to randomise generation order.
     * @param list the list of coordinates
     * @return the same list, but shuffled
     */
    private static List<int[]> shuffled(List<int[]> list) {
        Collections.shuffle(list, RANDOM);
        return list;
    }

    /**
     * Counts the number of tents per row for the given list of tent positions.
     * @param size the board size
     * @param tents the list of tent coordinates
     * @return an array of row counts
     */
    private static int[] countByRow(int size, List<int[]> tents) {
        int[] counts = new int[size];
        for (int[] t : tents) {
            counts[t[0]]++;
        }
        return counts;
    }

    /**
     * Counts the number of tents per column for the given list of tent positions.
     * @param size the board size
     * @param tents the list of tent coordinates
     * @return an array of column counts
     */
    private static int[] countByCol(int size, List<int[]> tents) {
        int[] counts = new int[size];
        for (int[] t : tents) {
            counts[t[1]]++;
        }
        return counts;
    }

    /**
     * List of predefined GameBoards used for hard difficulty as random generation takes too long.
     */
    private static final List<GameBoard> HARD_PUZZLES = List.of(
            hardPuzzle1(), hardPuzzle2(), hardPuzzle3(), hardPuzzle4(), hardPuzzle5()
    );

    /**
     * Returns the next predefined puzzle from the list. When the end of the list is reached, it cycles back to the
     * first puzzle such that a player never gets the same previous puzzle.
     * @return a predefined Hard puzzle
     */
    private static GameBoard getHardPuzzle() {
        GameBoard board = HARD_PUZZLES.get(hardIndex);
        hardIndex = (hardIndex + 1) % HARD_PUZZLES.size();
        return board;
    }

    /**
     * These all create a different Hard puzzle layout.
     * @return a puzzle with Hard difficulty
     */
    private static GameBoard hardPuzzle1() {
        int size = 15;
        int[] row = {5,1,4,2,4,2,2,4,1,4,3,3,3,3,4};
        int[] col = {1,6,1,4,3,3,3,3,4,3,2,3,2,4,3};
        int[][] trees = {
                {0,2},{0,4},{0,7},{0,8},{1,13},{2,2},{2,11},{2,14},
                {3,5},{3,6},{3,8},{3,9},{4,0},{4,5},{4,9},{4,11},{4,13},
                {5,5},{6,1},{6,3},{6,7},{6,10},{6,14},{7,4},{8,0},{9,3},
                {9,5},{9,8},{9,12},{10,10},{11,0},{11,2},{11,4},{11,7},
                {11,13},{11,14},{12,5},{12,6},{12,13},{13,0},{13,10},
                {13,12},{13,14},{14,4},{14,8}
        };
        return makeBoard(size, row, col, trees);
    }

    private static GameBoard hardPuzzle2() {
        int size = 15;
        int[] row = {5,2,3,3,3,2,4,2,4,1,3,3,4,1,5};
        int[] col = {2,5,1,4,3,2,6,2,1,5,1,4,2,4,3};
        int[][] trees = {
                {0,1},{0,3},{0,5},{0,14},{1,2},{1,3},{1,9},{1,10},
                {2,0},{2,9},{3,1},{3,6},{3,10},{4,7},{4,12},{4,13},{4,14},
                {5,4},{5,5},{5,10},{6,2},{6,8},{7,0},{7,4},{7,9},{7,12},
                {8,5},{9,3},{9,6},{9,11},{10,10},{10,13},{11,2},{11,3},
                {11,5},{11,7},{11,9},{11,13},{12,14},{13,4},{13,10},
                {13,14},{14,2},{14,7},{14,10}
        };
        return makeBoard(size, row, col, trees);
    }

    private static GameBoard hardPuzzle3() {
        int size = 15;
        int[] row = {4,1,6,0,4,2,5,1,5,2,3,4,1,2,5};
        int[] col = {4,2,2,4,3,4,2,1,4,1,6,1,4,2,5};
        int[][] trees = {
                {0,14},{1,2},{1,4},{1,10},{2,8},{2,11},{3,1},{3,3},
                {3,5},{3,10},{3,12},{3,14},{4,0},{4,4},{5,5},{5,14},{6,4},
                {6,6},{6,7},{6,8},{6,11},{7,1},{7,3},{7,14},{8,7},{8,9},
                {9,5},{9,6},{9,11},{9,14},{10,0},{11,1},{11,2},{11,5},
                {11,7},{11,10},{11,11},{11,14},{13,2},{13,10},{13,14},
                {14,5},{14,7},{14,8},{14,13}
        };
        return makeBoard(size, row, col, trees);
    }

    private static GameBoard hardPuzzle4() {
        int size = 15;
        int[] row = {4,3,4,2,3,3,4,2,3,3,3,3,3,2,5};
        int[] col = {5,2,2,5,1,4,2,2,5,2,2,4,3,0,6};
        int[][] trees = {
                {0,7},{0,13},{1,0},{1,2},{1,6},{1,11},{1,12},{1,14},
                {3,1},{3,7},{3,8},{3,14},{4,3},{4,9},{4,10},{5,0},{5,2},
                {5,6},{5,7},{5,9},{5,13},{6,5},{6,10},{6,14},{7,1},{7,2},
                {7,11},{8,7},{9,1},{9,9},{10,1},{10,8},{10,10},{10,13},
                {10,14},{11,0},{11,3},{11,4},{11,7},{11,14},{14,1},
                {14,4},{14,5},{14,9},{14,11},{14,13}
        };
        return makeBoard(size, row, col, trees);
    }

    private static GameBoard hardPuzzle5() {
        int size = 15;
        int[] row = {5,2,3,3,3,3,1,4,4,2,4,1,5,2,3};
        int[] col = {5,2,4,3,4,2,2,3,3,2,3,2,4,0,6};
        int[][] trees = {
                {0,4},{0,5},{0,7},{0,8},{0,10},{0,13},{1,0},{1,3},
                {2,2},{2,5},{2,9},{2,12},{3,2},{3,12},{4,14},{5,0},{5,8},
                {6,0},{6,4},{6,14},{7,13},{8,1},{8,3},{8,5},{8,6},{8,8},
                {8,10},{8,11},{9,6},{9,9},{10,0},{10,11},{10,14},{11,2},
                {11,3},{11,10},{12,0},{12,5},{12,7},{12,9},{12,12},
                {12,13},{14,3},{14,5},{14,8}
        };
        return makeBoard(size, row, col, trees);
    }

    private static GameBoard makeBoard(int size, int[] row, int[] col, int[][] trees) {
        GameBoard board = new GameBoard(size, size, row, col);
        for (int[] t : trees) board.setTree(t[0], t[1]);
        return board;
    }
}
