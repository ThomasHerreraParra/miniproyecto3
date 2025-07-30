/**
 * Warship Dominions - Naval Battle Game
 *
 * Version: 1.0
 * License: Educational Use Only
 *
 * Authors:
 * - Yoel Steven Montoya (2416571)
 * - Andrés Felipe Muñoz (2415124)
 * - Thomas Herrera Parra (2417158)
 */

package com.example.miniproyecto3.model;

/**
 * Represents the 10x10 game board composed of individual {@link Cell} objects.
 * Handles ship placement and validation logic.
 */
public class Board {

    /** Constant size of the board (10x10) */
    private final int size = 10;

    /** 2D matrix representing the cells of the board */
    private final Cell[][] grid;

    /**
     * Constructs an empty board by initializing each cell.
     */
    public Board() {
        grid = new Cell[size][size];
        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++)
                grid[i][j] = new Cell();
    }

    /**
     * Checks whether a ship can be placed at the specified position and orientation.
     *
     * @param ship The ship to be placed.
     * @param row Starting row index for the ship.
     * @param col Starting column index for the ship.
     * @param horizontal {@code true} if the ship is placed horizontally, {@code false} if vertically.
     * @return {@code true} if the ship can be placed, {@code false} otherwise.
     */
    public boolean canPlaceShip(Ship ship, int row, int col, boolean horizontal) {
        int length = ship.getSize();

        if (horizontal) {
            if (col + length > size) return false;
            for (int i = 0; i < length; i++) {
                if (grid[row][col + i].isOccupied()) return false;
            }
        } else {
            if (row + length > size) return false;
            for (int i = 0; i < length; i++) {
                if (grid[row + i][col].isOccupied()) return false;
            }
        }

        return true;
    }

    /**
     * Places a ship on the board at the given position and orientation if the placement is valid.
     *
     * @param ship The ship to place.
     * @param row Starting row index.
     * @param col Starting column index.
     * @param horizontal {@code true} for horizontal placement, {@code false} for vertical.
     * @return {@code true} if the ship was successfully placed, {@code false} otherwise.
     */
    public boolean placeShip(Ship ship, int row, int col, boolean horizontal) {
        if (!canPlaceShip(ship, row, col, horizontal)) return false;

        for (int i = 0; i < ship.getSize(); i++) {
            if (horizontal) {
                grid[row][col + i].occupy(ship);
            } else {
                grid[row + i][col].occupy(ship);
            }
        }

        ship.setStartRow(row);
        ship.setStartCol(col);
        ship.setHorizontal(horizontal);

        return true;
    }

    /**
     * Returns the full 2D grid of cells representing the board.
     *
     * @return The board grid.
     */
    public Cell[][] getGrid() {
        return grid;
    }
}
