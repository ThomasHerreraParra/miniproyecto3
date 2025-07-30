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
 * Represents the Submarino ship type, which occupies 3 cells on the board.
 */
public class Submarino extends Ship {

    /**
     * Constructs a Submarino ship with a fixed size of 3 cells.
     */
    public Submarino() {
        super(3);
    }

    /**
     * Returns the type of the ship.
     *
     * @return The string "submarino".
     */
    @Override
    public String getType() {
        return "submarino";
    }
}
