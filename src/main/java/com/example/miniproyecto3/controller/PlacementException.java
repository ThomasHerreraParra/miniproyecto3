package com.example.miniproyecto3.controller;

/**
 * Excepción personalizada que se lanza cuando no se puede colocar un barco
 * después de varios intentos.
 */
public class PlacementException extends Exception {
    public PlacementException(String message) {
        super(message);
    }
}
