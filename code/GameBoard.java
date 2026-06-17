package org.example;
public class GameBoard {
    private final CellState[][] grid;
    private final int[] rowLimits, colLimits;

    /**
     * Constructs a new GameBoard with the specified dimensions and limits for tents.
     * @param rows the number of rows of the board
     * @param cols the number of columns of the board
     * @param rowLimits the row tent limit
     * @param colLimits the column tent limit
     */
    public GameBoard(int rows, int cols, int[] rowLimits, int[] colLimits) {
        this.grid = new CellState[rows][cols];
        this.rowLimits = rowLimits;
        this.colLimits = colLimits;
        clearAllPlacements();
    }

    /**
     * Changes the state of the cell to tree.
     * @param r the specific row index
     * @param c the specific column index
     */
    public void setTree(int r, int c) {
        grid[r][c] = CellState.TREE;
    }

    /**
     * Handles the tent placing logic. If the cell has a tent, it becomes empty. If it is empty, it becomes a tent.
     * Cells with trees or flowers cannot be turned into a tent.
     * @param r the row index
     * @param c the column index
     */
    public void placeTent(int r, int c) {
        if (grid[r][c] == CellState.TREE || grid[r][c] == CellState.FLOWER) {
            return;
        }
        grid[r][c] = (grid[r][c] == CellState.TENT) ? CellState.EMPTY : CellState.TENT;
    }

    /**
     * Handles the flowers placing logic. If the cell currently has flowers, it becomes empty. If it is empty, it
     * becomes flowers. Cells with trees or tents cannot be marked as flowers.
     * @param r
     * @param c
     */
    public void placeFlowers(int r, int c) {
        if (grid[r][c] == CellState.TREE || grid[r][c] == CellState.TENT) {
            return;
        }
        grid[r][c] = (grid[r][c] == CellState.FLOWER) ? CellState.EMPTY : CellState.FLOWER;
    }

    /**
     * Returns the current state of the cell.
     * @param r the row index
     * @param c the column index
     * @return the CellState (enum) of the specific cell
     */
    public CellState getState(int r, int c) {
        return grid[r][c];
    }

    /**
     * Gives the tent limit (required number of tents) for a specific row.
     * @param r the row index
     * @return the limit
     */
    public int getRowLimit(int r) {
        return rowLimits[r]; }

    /**
     * Gives the tent limit (required number of tents) for a specific column.
     * @param c the column index
     * @return the limit
     */
    public int getColLimit(int c) {
        return colLimits[c];
    }

    /**
     * Removes all placements on the board (tents and flowers). Note that trees stay in place.
     * Used when resetting the game.
     */
    public void clearAllPlacements() {
        for (int r = 0; r < grid.length; r++) {
            for (int c = 0; c < grid[0].length; c++) {
                if (grid[r][c] != CellState.TREE) {
                    grid[r][c] = CellState.EMPTY;
                }
            }
        }
    }

    /**
     * Checks whether a tent at the given position violates the game logic. Specifically, it checks the adjacency rules.
     * Two tents cannot be adjacent in any of the eight surrounding cells.
     * @param r the row index of the tent
     * @param c the column index of the tent
     * @return true if this tent touches another, false otherwise
     */
    public boolean isTentInvalid(int r, int c) {
        if (grid[r][c] != CellState.TENT) {
            return false;
        }
        for (int[] off : neighborOffsets(true)) {
            int rr = r + off[0], cc = c + off[1];
            if (inBounds(rr, cc) && grid[rr][cc] == CellState.TENT) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines whether the puzzle is solved correctly, according to the game rules.
     * @return true if solved, false otherwise
     */
    public boolean isSolved() {
        for (int r = 0; r < grid.length; r++) {
            if (countTentsInRow(r) != rowLimits[r]) {
                return false;
            }
        }
        for (int c = 0; c < grid[0].length; c++) {
            if (countTentsInCol(c) != colLimits[c]) {
                return false;
            }
        }
        for (int r = 0; r < grid.length; r++) {
            for (int c = 0; c < grid[0].length; c++) {
                if (isTentInvalid(r, c)) {
                    return false;
                }
                if (grid[r][c] == CellState.TENT && !hasAdjacentTree(r, c)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Helper method for checking whether a tent at a specific location has at least one adjacent tree.
     * @param r the row index
     * @param c the column index
     * @return true if there is an adjacent tree
     */
    private boolean hasAdjacentTree(int r, int c) {
        for (int[] off : neighborOffsets(false)) {
            int rr = r + off[0], cc = c + off[1];
            if (inBounds(rr, cc) && grid[rr][cc] == CellState.TREE) {
                return true;
            }
        }
        return false;
    }

    /**
     * Helper method for counting the number of tents currently placed in a row.
     * @param r the row index
     * @return the number of tents in that row
     */
    public int countTentsInRow(int r) {
        int count = 0;
        for (CellState s : grid[r]) {
            if (s == CellState.TENT) {
                count++;
            }
        }
        return count;
    }

    /**
     * Helper method for counting the number of tents currently placed in a column.
     * @param c the column index
     * @return the number of tents in that column
     */
    public int countTentsInCol(int c) {
        int count = 0;
        for (CellState[] row : grid) {
            if (row[c] == CellState.TENT) {
                count++;
            }
        }
        return count;
    }

    /**
     * Utility helper method for checking whether a cell position is valid within the valid board boundaries.
     * @param r the row index
     * @param c the column index
     * @return true if the cell is within bounds of the grid, false otherwise
     */
    private boolean inBounds(int r, int c) {
        return r >= 0 && c >= 0 && r < grid.length && c < grid[0].length;
    }

    /**
     * Returns an array of coordinate offsets for checking neighbouring cells.
     * @param diagonal true if diagonal directions should be included, false for orthogonal only
     * @return an array of integer offsets representing neighbour directions
     */
    private int[][] neighborOffsets(boolean diagonal) {
        return diagonal ? new int[][]{{-1,-1},{-1,0},{-1,1},{0,-1},{0,1},{1,-1},{1,0},{1,1}}
                        : new int[][]{{-1,0},{1,0},{0,-1},{0,1}};
    }
}
