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
 * Represents a ship placed on the player's board.
 * A ship has a name, size, orientation, and starting position.
 */
public class Ship {

    /** Name of the ship (e.g., "portaviones", "submarino") */
    private final String name;

    /** Length of the ship in grid cells */
    private final int size;

    /** Indicates whether the ship is placed horizontally */
    private boolean isHorizontal;

    /** Starting row (topmost cell) of the ship on the grid */
    private int startRow;

    /** Starting column (leftmost cell) of the ship on the grid */
    private int startCol;

    /**
     * Constructs a ship with a given name and size.
     * Ships are placed horizontally by default.
     *
     * @param name Name of the ship type.
     * @param size Number of cells the ship occupies.
     */
    public Ship(String name, int size) {
        this.name = name;
        this.size = size;
        this.isHorizontal = true;
    }

    /**
     * Gets the number of cells this ship occupies.
     *
     * @return Size of the ship.
     */
    public int getSize() {
        return size;
    }

    /**
     * Returns whether the ship is placed horizontally.
     *
     * @return {@code true} if horizontal, {@code false} if vertical.
     */
    public boolean isHorizontal() {
        return isHorizontal;
    }

    /**
     * Sets the orientation of the ship.
     *
     * @param horizontal {@code true} for horizontal, {@code false} for vertical.
     */
    public void setHorizontal(boolean horizontal) {
        isHorizontal = horizontal;
    }

    /**
     * Gets the starting row of the ship.
     *
     * @return Row index where the ship begins.
     */
    public int getStartRow() {
        return startRow;
    }

    /**
     * Sets the starting row of the ship.
     *
     * @param row Row index to set as start.
     */
    public void setStartRow(int row) {
        startRow = row;
    }

    /**
     * Gets the starting column of the ship.
     *
     * @return Column index where the ship begins.
     */
    public int getStartCol() {
        return startCol;
    }

    /**
     * Sets the starting column of the ship.
     *
     * @param col Column index to set as start.
     */
    public void setStartCol(int col) {
        startCol = col;
    }
}
