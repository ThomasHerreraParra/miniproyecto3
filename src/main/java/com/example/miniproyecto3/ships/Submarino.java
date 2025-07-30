package com.example.miniproyecto3.ships;

import javafx.scene.paint.Color;

/**
 * Clase que representa el barco Submarino (3 casillas).
 */
public class Submarino extends Ship {

    /**
     * Constructor del submarino. Usa color verde oscuro.
     */
    public Submarino() {
        super(3);
    }
    public String getType() {
        return "submarino";
    }
}