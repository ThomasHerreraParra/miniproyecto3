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

package com.example.miniproyecto3.controller;

/**
 * Custom exception thrown when a ship cannot be placed on the board
 * after several failed attempts.
 */
public class PlacementException extends Exception {

    /**
     * Constructs a new PlacementException with the specified detail message.
     *
     * @param message The detail message explaining the cause of the exception.
     */
    public PlacementException(String message) {
        super(message);
    }
}
