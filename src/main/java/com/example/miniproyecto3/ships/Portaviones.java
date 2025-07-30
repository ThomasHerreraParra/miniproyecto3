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
 * Represents a Portaviones ship (4 cells in length).
 * This class extends the generic {@link Ship} class and defines the size and type.
 */
public class Portaviones extends Ship {

    /**
     * Constructs a new Portaviones ship with a fixed size of 4.
     */
    public Portaviones() {
        super(4);
    }

    /**
     * Returns the type identifier of the ship.
     *
     * @return The string "portaviones".
     */
    @Override
    public String getType() {
        return "portaviones";
    }
}
