package com.example.miniproyecto3.ships;

import javafx.scene.paint.Color;

/**
 * Clase que representa el barco Fragata (1 casilla).
 */
public class Fragata extends Ship {

    /**
     * Constructor de la fragata. Usa color gris.
     */
    public Fragata() {
        super(1);
    }
    public String getType() {
        return "fragata";
    }
}