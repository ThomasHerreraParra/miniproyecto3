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
 * Represents a Destroyer ship (2 cells in length).
 * This class extends the generic {@link Ship} class and sets the type and size.
 */
public class Destructor extends Ship {

    /**
     * Constructs a new Destroyer with a fixed size of 2.
     */
    public Destructor() {
        super(2);
    }

    /**
     * Returns the type identifier of the ship.
     *
     * @return The string "destructor".
     */
    @Override
    public String getType() {
        return "destructor";
    }
}
