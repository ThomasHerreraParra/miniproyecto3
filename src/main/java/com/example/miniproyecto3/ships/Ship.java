package com.example.miniproyecto3.ships;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;


//-----FALTA PULIR LAS FIGURAS 2D ESTO ES UNA BASE MUY GENERICA-----

/**
 * Clase abstracta que representa un barco en el juego.
 * Cada barco está compuesto por una cantidad de partes (rectángulos) que representan casillas del tablero.
 */
public abstract class Ship {
    protected int size; // Número de casillas que ocupa el barco
    protected Rectangle[] parts; // Array de rectángulos que representan las partes del barco

    /**
     * Constructor que inicializa las partes del barco con color y borde.
     * size es el número de casillas que ocupa el barco.
     * color es color de relleno del barco.
     */
    public Ship(int size, Color color) {
        this.size = size;
        this.parts = new Rectangle[size];

        // Crear cada parte del barco como un rectángulo de 30x30 píxeles
        for (int i = 0; i < size; i++) {
            Rectangle part = new Rectangle(30, 30); // Tamaño de una casilla
            part.setFill(color); // Color del barco
            part.setStroke(Color.BLACK); // Bordes negros para visibilidad
            this.parts[i] = part;
        }
    }

    /**
     * Devuelve el arreglo de rectángulos que componen el barco.
     * retorna el arreglo de partes (rectángulos).
     */
    public Rectangle[] getParts() {
        return parts;
    }

    /**
     * Devuelve el tamaño del barco (cantidad de casillas que ocupa).
     * retorna el número de casillas.
     */
    public int getSize() {
        return size;
    }
}
