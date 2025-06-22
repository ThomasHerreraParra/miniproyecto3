package com.example.miniproyecto3.storage;

/**
 * Plain‑old DTO that stores the information needed to rebuild a ship
 * on the enemy board.
 */
public class SavedShip {

    private final String type;   // e.g. "frigate", "submarine"
    private final int row;       // starting row (0‑based)
    private final int col;       // starting column (0‑based)
    private final boolean horizontal; // true = horizontal, false = vertical

    public SavedShip(String type, int row, int col, boolean horizontal) {
        this.type = type;
        this.row = row;
        this.col = col;
        this.horizontal = horizontal;
    }

    public String getType() {
        return type;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public boolean isHorizontal() {
        return horizontal;
    }

    /**
     * Serialises the ship as CSV: type,row,col,H|V
     */
    @Override
    public String toString() {
        return type + "," + row + "," + col + "," + (horizontal ? "H" : "V");
    }

    /**
     * Convenience factory that builds a SavedShip from a CSV line.
     * (Úsalo en el futuro cuando implementes loadBoard).
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

