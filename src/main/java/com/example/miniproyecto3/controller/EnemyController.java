package com.example.miniproyecto3.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import com.example.miniproyecto3.ships.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class EnemyController extends NavigationAdapter {

    @FXML
    private GridPane enemyBoard;

    private final Map<String, Integer> shipCounts = new HashMap<>();
    private final int GRID_SIZE = 10;
    private final int CELL_SIZE = 50;

    @FXML
    public void initialize() {
        shipCounts.put("fragata", 4);
        shipCounts.put("submarino", 2);
        shipCounts.put("destructor", 3);
        shipCounts.put("portaviones", 1);
        createGrid();
        placeShipsRandomly();
    }

    private void createGrid() {
        // Etiquetas de columna
        for (int col = 0; col < GRID_SIZE; col++) {
            Label label = new Label(String.valueOf(col + 1));
            label.setMinSize(CELL_SIZE, CELL_SIZE);
            label.setAlignment(Pos.CENTER);
            label.setStyle("-fx-font-weight: bold;");
            enemyBoard.add(label, col + 1, 0);
        }

        // Etiquetas de fila
        for (int row = 0; row < GRID_SIZE; row++) {
            Label label = new Label(String.valueOf((char) ('A' + row)));
            label.setMinSize(CELL_SIZE, CELL_SIZE);
            label.setAlignment(Pos.CENTER);
            label.setStyle("-fx-font-weight: bold;");
            enemyBoard.add(label, 0, row + 1);
        }

        // Celdas del tablero
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                StackPane cell = new StackPane();
                cell.setPrefSize(CELL_SIZE, CELL_SIZE);
                Rectangle background = new Rectangle(CELL_SIZE, CELL_SIZE);
                background.setFill(Color.web("#e6f7ff"));
                background.setStroke(Color.LIGHTGRAY);
                cell.getChildren().add(background);
                enemyBoard.add(cell, col + 1, row + 1);
            }
        }
    }

    private void placeShipsRandomly() {
        Random rand = new Random();

        for (Map.Entry<String, Integer> entry : shipCounts.entrySet()) {
            String shipName = entry.getKey();
            int count = entry.getValue();

            int size = switch (shipName) {
                case "fragata" -> 1;
                case "submarino" -> 2;
                case "destructor" -> 3;
                case "portaviones" -> 4;
                default -> 1;
            };

            int attempts = 0;
            while (count > 0 && attempts < 1000) {
                attempts++;

                boolean horizontal = rand.nextBoolean();
                int row = rand.nextInt(GRID_SIZE);
                int col = rand.nextInt(GRID_SIZE);

                if (canPlaceShip(row, col, size, horizontal)) {
                    Ship ship = switch (shipName) {
                        case "fragata" -> new Fragata();
                        case "submarino" -> new Submarino();
                        case "destructor" -> new Destructor();
                        case "portaviones" -> new Portaviones();
                        default -> null;
                    };

                    if (ship == null) continue;

                    Color color = (Color) ship.getParts()[0].getFill();

                    for (int i = 0; i < size; i++) {
                        int r = row + (horizontal ? 0 : i);
                        int c = col + (horizontal ? i : 0);
                        StackPane cell = getCell(r, c);
                        Rectangle rect = (Rectangle) cell.getChildren().get(0);
                        rect.setFill(color);
                    }

                    count--;
                }
            }

            if (attempts >= 1000) {
                System.out.println("No se pudo colocar todos los barcos de tipo: " + shipName);
            }
        }
    }

    private boolean canPlaceShip(int row, int col, int size, boolean horizontal) {
        for (int i = 0; i < size; i++) {
            int r = row + (horizontal ? 0 : i);
            int c = col + (horizontal ? i : 0);

            if (r >= GRID_SIZE || c >= GRID_SIZE) return false;

            StackPane cell = getCell(r, c);
            Rectangle rect = (Rectangle) cell.getChildren().get(0);
            if (!rect.getFill().equals(Color.web("#e6f7ff"))) return false;
        }
        return true;
    }

    private StackPane getCell(int row, int col) {
        for (Node node : enemyBoard.getChildren()) {
            Integer column = GridPane.getColumnIndex(node);
            Integer rowIndex = GridPane.getRowIndex(node);
            if (column == null || rowIndex == null) continue;
            if (column == col + 1 && rowIndex == row + 1 && node instanceof StackPane) {
                return (StackPane) node;
            }
        }
        throw new IllegalStateException("No se encontró la celda en [" + row + "," + col + "]");
    }

    public void returnBoard(ActionEvent event) {
        goTo("/com/example/miniproyecto3/game-view.fxml", (Node) event.getSource());
    }
}


