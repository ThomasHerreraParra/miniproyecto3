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
package com.example.miniproyecto3.storage;

/**
 * Plain-old data transfer object (DTO) that stores the necessary data to reconstruct
 * a ship on the enemy board during game load.
 */
public class SavedShip {

    private final String type;         // Ship type: "fragata", "submarino", etc.
    private final int row;             // Starting row (0-based)
    private final int col;             // Starting column (0-based)
    private final boolean horizontal;  // Orientation: true = horizontal, false = vertical

    /**
     * Constructs a new SavedShip with its type, position, and orientation.
     *
     * @param type        The type of the ship.
     * @param row         Starting row (0-based).
     * @param col         Starting column (0-based).
     * @param horizontal  True if the ship is horizontal, false if vertical.
     */
    public SavedShip(String type, int row, int col, boolean horizontal) {
        this.type = type;
        this.row = row;
        this.col = col;
        this.horizontal = horizontal;
    }

    /**
     * Returns the ship type.
     *
     * @return Ship type as a string.
     */
    public String getType() {
        return type;
    }

    /**
     * Returns the starting row.
     *
     * @return The row where the ship starts.
     */
    public int getRow() {
        return row;
    }

    /**
     * Returns the starting column.
     *
     * @return The column where the ship starts.
     */
    public int getCol() {
        return col;
    }

    /**
     * Indicates whether the ship is placed horizontally.
     *
     * @return True if horizontal, false if vertical.
     */
    public boolean isHorizontal() {
        return horizontal;
    }

    /**
     * Serializes the ship as a CSV line: type,row,col,H|V
     *
     * @return A string representing the ship in CSV format.
     */
    @Override
    public String toString() {
        return type + "," + row + "," + col + "," + (horizontal ? "H" : "V");
    }

    /**
     * Creates a SavedShip object from a CSV line.
     * Expected format: type,row,col,H|V
     *
     * @param csv The CSV line.
     * @return A new SavedShip object parsed from the line.
     * @throws IllegalArgumentException If the CSV line is malformed.
     */
    public static SavedShip fromCsv(String csv) {
        String[] parts = csv.split(",");
        if (parts.length != 4) {
            throw new IllegalArgumentException("Malformed ship line: " + csv);
        }
        String type = parts[0];
        int row = Integer.parseInt(parts[1]);
        int col = Integer.parseInt(parts[2]);
        boolean horizontal = "H".equalsIgnoreCase(parts[3]);
        return new SavedShip(type, row, col, horizontal);
    }
}
