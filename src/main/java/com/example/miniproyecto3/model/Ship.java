package com.example.miniproyecto3.model;

// Esta clase representa un barco del jugador
public class Ship {
    private final String name;  // Nombre del barco (ej. "Portaaviones")
    private final int size;     // Tamaño del barco en número de celdas
    private boolean isHorizontal; // Indica si el barco está en orientación horizontal
    private int startRow;       // Fila inicial donde comienza el barco
    private int startCol;       // Columna inicial donde comienza el barco

    // Constructor que define el nombre y tamaño del barco
    public Ship(String name, int size) {
        this.name = name;
        this.size = size;
        this.isHorizontal = true; // Por defecto, el barco es horizontal
    }


    public int getSize() { return size; }

    public boolean isHorizontal() { return isHorizontal; }

    public void setHorizontal(boolean horizontal) { isHorizontal = horizontal; }

    public int getStartRow() { return startRow; }

    public void setStartRow(int row) { startRow = row; }

    public int getStartCol() { return startCol; }

    public void setStartCol(int col) { startCol = col; }
}
