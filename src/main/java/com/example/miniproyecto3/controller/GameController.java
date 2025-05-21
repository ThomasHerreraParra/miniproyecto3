package com.example.miniproyecto3.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import com.example.miniproyecto3.ships.*;

import java.util.HashMap;
import java.util.Map;

public class GameController extends NavigationAdapter {

    @FXML
    private GridPane gridBoard;

    @FXML
    private VBox shipSelectionArea;

    // Stock de barcos por tipo
    private Map<String, Integer> shipCounts = new HashMap<>();




    @FXML
    public void initialize() {
        // Inicializar contadores
        shipCounts.put("fragata", 4);
        shipCounts.put("submarino", 2);
        shipCounts.put("destructor", 3);
        shipCounts.put("portaviones", 1);
        createGrid();
        loadShips();
    }

    public void handleBack(ActionEvent event) {
        goTo("/com/example/miniproyecto3/start-view.fxml", (Node) event.getSource());
    }

    private void createGrid() {
        int numRows = 10;
        int numCols = 10;
        int cellSize = 50;
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {

                StackPane cell = new StackPane();
                cell.setPrefSize(cellSize, cellSize);
                Rectangle background = new Rectangle(cellSize, cellSize);
                background.setFill(Color.web("#e6f7ff"));
                background.setStroke(Color.LIGHTGRAY);
                cell.getChildren().add(background);

                // Permitir aceptar drops
                cell.setOnDragOver(e -> {
                    if (e.getGestureSource() != cell && e.getDragboard().hasString()) {
                        e.acceptTransferModes(TransferMode.MOVE);
                    }
                    e.consume();
                });

                // Manejar drop
                cell.setOnDragDropped(e -> {
                    Dragboard db = e.getDragboard();
                    boolean success = false;
                    if (db.hasString()) {
                        String type = db.getString();
                        Ship ship = createShipFromString(type);
                        int size = ship.getSize();

                        // Obtener posición de la celda
                        Integer rowIdx = GridPane.getRowIndex(cell);
                        Integer colIdx = GridPane.getColumnIndex(cell);
                        int row0 = rowIdx == null ? 0 : rowIdx;
                        int col0 = colIdx == null ? 0 : colIdx;

                        // Verificar que quepa horizontalmente
                        if (col0 + size <= numCols) {
                            for (int i = 0; i < size; i++) {
                                StackPane target = getCellPaneAt(row0, col0 + i);
                                Rectangle part = new Rectangle(30, 30, ship.getParts()[i].getFill());
                                part.setStroke(Color.BLACK);
                                target.getChildren().add(part);
                            }
                            success = true;
                        }
                    }
                    e.setDropCompleted(success);
                    e.consume();
                });
                gridBoard.add(cell, col, row);
            }
        }
    }
    private Ship createShipFromString(String shipType) {
        return switch (shipType) {
            case "fragata"     -> new Fragata();
            case "submarino"   -> new Submarino();
            case "destructor"  -> new Destructor();
            case "portaviones" -> new Portaviones();
            default             -> null;
        };
    }
    private void loadShips() {
        addShipToSelection("fragata",     new Fragata());
        addShipToSelection("submarino",   new Submarino());
        addShipToSelection("destructor",  new Destructor());
        addShipToSelection("portaviones", new Portaviones());
    }
    private void addShipToSelection(String type, Ship ship) {
        HBox shipBox = new HBox(5);
        shipBox.setUserData(type);

        // Label con contador
        javafx.scene.control.Label countLabel = new javafx.scene.control.Label(shipCounts.get(type).toString());
        countLabel.setUserData("label");
        shipBox.getChildren().add(countLabel);

        Rectangle clone = new Rectangle(30, 30, (Color) ship.getParts()[0].getFill());
        clone.setStroke(Color.BLACK);
        clone.setUserData(type);

        clone.setOnDragDetected(event -> {
            int currentCount = Integer.parseInt(countLabel.getText());

            // No permitir arrastrar si no quedan barcos
            if (currentCount <= 0) {
                event.consume();
                return;
            }
            Dragboard db = clone.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(type);
            db.setContent(content);

            // Crear miniatura
            Rectangle preview = new Rectangle(30, 30, (Color) clone.getFill());
            WritableImage snapshot = preview.snapshot(new SnapshotParameters(), null);
            db.setDragView(snapshot);

            // Actualizar contador
            countLabel.setText(String.valueOf(currentCount - 1));

            // Actualizar stock interno
            shipCounts.put(type, currentCount - 1);

            event.consume();
        });
        shipBox.getChildren().add(clone);
        shipSelectionArea.getChildren().add(shipBox);
    }
    private StackPane getCellPaneAt(int row, int col) {
        for (Node n : gridBoard.getChildren()) {
            int rowIndex = GridPane.getRowIndex(n) == null ? 0 : GridPane.getRowIndex(n);
            int colIndex = GridPane.getColumnIndex(n) == null ? 0 : GridPane.getColumnIndex(n);
            if (rowIndex == row && colIndex == col) {
                return (StackPane) n;
            }
        }
        return null;
    }
}