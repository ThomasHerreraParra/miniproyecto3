package com.example.miniproyecto3.ships;

public abstract class Ship {
    protected int size;
    public abstract String getType();
    public Ship(int size) {
        this.size = size;
    }

    public int getSize() {
        return size;
    }
}
