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
 * Represents a Fragata ship (1 cell in length).
 * This class extends the generic {@link Ship} class and sets the type and size.
 */
public class Fragata extends Ship {

    /**
     * Constructs a new Fragata with a fixed size of 1.
     */
    public Fragata() {
        super(1);
    }

    /**
     * Returns the type identifier of the ship.
     *
     * @return The string "fragata".
     */
    @Override
    public String getType() {
        return "fragata";
    }
}
