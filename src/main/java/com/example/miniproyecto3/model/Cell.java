package com.example.miniproyecto3.model;

// Esta clase representa una celda del tablero
public class Cell {
    private boolean occupied; // Indica si la celda está ocupada por un barco
    private Ship ship;        // Referencia al barco que ocupa la celda

    // Constructor por defecto: la celda está vacía
    public Cell() {
        this.occupied = false;
    }

    // Devuelve si la celda está ocupada
    public boolean isOccupied() { return occupied; }

    // Marca la celda como ocupada por un barco
    public void occupy(Ship ship) {
        this.occupied = true;
        this.ship = ship;
    }

    // Devuelve el barco que ocupa la celda (si hay uno)
    public Ship getShip() { return ship; }
}
