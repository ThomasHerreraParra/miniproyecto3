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
 * Represents a single cell on the 10x10 game board.
 * Each cell can either be empty or occupied by a {@link Ship}.
 */
public class Cell {

    /** Indicates whether the cell is occupied by a ship */
    private boolean occupied;

    /** Reference to the ship occupying the cell */
    private Ship ship;

    /**
     * Constructs an empty cell (not occupied by any ship).
     */
    public Cell() {
        this.occupied = false;
    }

    /**
     * Returns whether the cell is currently occupied by a ship.
     *
     * @return {@code true} if the cell is occupied, {@code false} otherwise.
     */
    public boolean isOccupied() {
        return occupied;
    }

    /**
     * Marks the cell as occupied by a specific ship.
     *
     * @param ship The ship occupying this cell.
     */
    public void occupy(Ship ship) {
        this.occupied = true;
        this.ship = ship;
    }

    /**
     * Gets the ship that occupies this cell.
     *
     * @return The ship occupying the cell, or {@code null} if unoccupied.
     */
    public Ship getShip() {
        return ship;
    }
}
