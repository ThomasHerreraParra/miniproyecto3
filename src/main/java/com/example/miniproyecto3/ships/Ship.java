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
package com.example.miniproyecto3.ships;

/**
 * Abstract base class representing a generic ship in the game.
 * Each ship has a defined size and must implement a method to return its type.
 */
public abstract class Ship {
    /** The number of cells the ship occupies on the board. */
    protected int size;

    /**
     * Constructs a ship with the specified size.
     *
     * @param size The number of cells the ship occupies.
     */
    public Ship(int size) {
        this.size = size;
    }

    /**
     * Returns the size of the ship (number of occupied cells).
     *
     * @return The size of the ship.
     */
    public int getSize() {
        return size;
    }

    /**
     * Returns the type of the ship (e.g., "fragata", "submarino").
     *
     * @return The ship's type as a lowercase string.
     */
    public abstract String getType();
}
