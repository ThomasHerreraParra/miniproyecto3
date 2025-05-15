package com.example.miniproyecto3.model;

// Clase que representa el tablero de juego de 10x10
public class Board {
    private final int size = 10;       // Tamaño fijo del tablero: 10x10
    private final Cell[][] grid;       // Matriz de celdas del tablero

    // Constructor: inicializa todas las celdas del tablero
    public Board() {
        grid = new Cell[size][size];
        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++)
                grid[i][j] = new Cell(); // Cada celda empieza vacía
    }

    // Verifica si un barco se puede colocar en una posición y orientación dadas
    public boolean canPlaceShip(Ship ship, int row, int col, boolean horizontal) {
        int length = ship.getSize();

        // Si está en horizontal, verificar que no se pase del borde derecho
        if (horizontal) {
            if (col + length > size) return false;
            for (int i = 0; i < length; i++) {
                if (grid[row][col + i].isOccupied()) return false; // Ya hay un barco
            }
        } else {
            // Si está en vertical, verificar que no se pase del borde inferior
            if (row + length > size) return false;
            for (int i = 0; i < length; i++) {
                if (grid[row + i][col].isOccupied()) return false;
            }
        }

        // Si pasa todas las validaciones, el barco se puede colocar
        return true;
    }

    // Coloca el barco en el tablero si es válido. Devuelve true si lo coloca.
    public boolean placeShip(Ship ship, int row, int col, boolean horizontal) {
        if (!canPlaceShip(ship, row, col, horizontal)) return false;

        for (int i = 0; i < ship.getSize(); i++) {
            if (horizontal) {
                grid[row][col + i].occupy(ship); // Ocupa celdas horizontalmente
            } else {
                grid[row + i][col].occupy(ship); // Ocupa celdas verticalmente
            }
        }

        // Guarda la posición inicial y orientación del barco
        ship.setStartRow(row);
        ship.setStartCol(col);
        ship.setHorizontal(horizontal);

        return true;
    }

    // Devuelve el tablero completo (matriz de celdas)
    public Cell[][] getGrid() {
        return grid;
    }
}
