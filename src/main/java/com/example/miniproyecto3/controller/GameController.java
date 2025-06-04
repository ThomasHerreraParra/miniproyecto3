package com.example.miniproyecto3.controller;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Label;
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
import javafx.util.Duration;
import com.example.miniproyecto3.ships.*;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.input.MouseButton;

public class GameController extends NavigationAdapter {

    @FXML
    private GridPane gridBoard;

    @FXML
    private VBox shipSelectionArea;

    @FXML
    private Label textFloat; // Referencia al mensaje flotante en la interfaz

    // Stock de barcos por tipo
    private final Map<String, Integer> shipCounts = new HashMap<>();

    //Variable que almacena el contador del barco actualmente siendo arrastrado
    private Label activeCountLanbel = null;

    //Variable para manejar la seleccion por click
    private String selectedShipType = null;

    //Variable para la orientacion
    private enum Direction {
        RIGHT, DOWN, LEFT, UP
    }
    private Direction currentDirection = Direction.RIGHT; //Direccion por defecto del barco

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

                        //HU-1 El sistema debe validar que los barcos no se superpongan ni salgan del tablero
                        if (col0 + size <= numCols) {
                            boolean spaceAvailable = true;
                            for (int i = 0; i < size; i++) {
                                StackPane target = getCellPaneAt(row0, col0 + i);
                                boolean occupied = target.getChildren().stream()
                                        .anyMatch(n -> {
                                            if (n instanceof Rectangle rect) {
                                                Object data = rect.getUserData();
                                                return data == null || !"preview".equals(data);
                                            }
                                            return true; // cualquier otro tipo de nodo se considera ocupado
                                        });

                                if (occupied) {
                                    spaceAvailable = false;
                                    break;
                                }
                            }

                            if (spaceAvailable) {
                                for (int i = 0; i < size; i++) {
                                    StackPane target = getCellPaneAt(row0, col0 + i);
                                    Rectangle part = new Rectangle(30, 30, ship.getParts()[i].getFill());
                                    part.setStroke(Color.BLACK);
                                    part.setUserData("real"); // importante para marcarlo como real y no "preview"
                                    target.getChildren().add(part);
                                }
                                //Se actualiza el contador solo si se coloca correctamente el barco
                            if (activeCountLanbel != null) {
                                int currentCount = Integer.parseInt(activeCountLanbel.getText());
                                activeCountLanbel.setText(String.valueOf(currentCount - 1));
                                shipCounts.put(type, currentCount - 1);
                            }
                            success = true;
                            } else {
                                showFloatingMessage("Inválido: Espacio ocupado");
                            }
                        } else {
                            showFloatingMessage("Inválido: El barco se sale del tablero");
                        }
                    }
                    e.setDropCompleted(success);
                    e.consume();
                });
                //Permitir colocar barcos con click si hay uno seleccionado
                cell.setOnMouseClicked(e -> {
                    if (e.getButton() == MouseButton.SECONDARY) {
                        // Cambiar dirección al hacer clic derecho
                        switch (currentDirection) {
                            case RIGHT -> currentDirection = Direction.DOWN;
                            case DOWN  -> currentDirection = Direction.LEFT;
                            case LEFT  -> currentDirection = Direction.UP;
                            case UP    -> currentDirection = Direction.RIGHT;
                        }
                        previewShipPlacement(cell);
                        showFloatingMessage("Dirección actual: " + currentDirection);
                        return;
                    }

                    // Solo proceder si es clic izquierdo
                    if (e.getButton() != MouseButton.PRIMARY) return;

                    if (selectedShipType == null) {
                        showFloatingMessage("Selecciona un barco antes de colocarlo");
                        return;
                    }

                    int currentCount = shipCounts.getOrDefault(selectedShipType, 0);
                    if (currentCount <= 0) {
                        showFloatingMessage("No quedan barcos de este tipo");
                        return;
                    }

                    Ship ship = createShipFromString(selectedShipType);
                    if (ship == null) {
                        showFloatingMessage("Tipo de barco inválido");
                        return;
                    }

                    int size = ship.getSize();
                    Integer rowIdx = GridPane.getRowIndex(cell);
                    Integer colIdx = GridPane.getColumnIndex(cell);
                    int row0 = rowIdx == null ? 0 : rowIdx;
                    int col0 = colIdx == null ? 0 : colIdx;

                    // Validación y colocación según dirección
                    boolean canPlace = true;
                    for (int i = 0; i < size; i++) {
                        int r = row0, c = col0;
                        switch (currentDirection) {
                            case RIGHT -> c += i;
                            case LEFT  -> c -= i;
                            case DOWN  -> r += i;
                            case UP    -> r -= i;
                        }

                        if (r < 0 || r >= 10 || c < 0 || c >= 10) {
                            canPlace = false;
                            break;
                        }

                        StackPane target = getCellPaneAt(r, c);
                        if (target == null) {
                            canPlace = true;
                            break;
                        }
                        // Verifica si hay un barco real (ignorando las sombras "preview")
                        boolean ocupado = target.getChildren().stream()
                                .anyMatch(n -> {
                                    if (n instanceof Rectangle rect) {
                                        Object data = rect.getUserData();
                                        return "real".equals(data);
                                    }
                                    return true; // cualquier otro tipo de nodo se considera "ocupado"
                                });
                        if (ocupado) {
                            canPlace = false;
                            break;
                        }
                    }
                    if (canPlace) {
                        for (int i = 0; i < size; i++) {
                            int r = row0, c = col0;
                            switch (currentDirection) {
                                case RIGHT -> c += i;
                                case LEFT  -> c -= i;
                                case DOWN  -> r += i;
                                case UP    -> r -= i;
                            }

                            StackPane target = getCellPaneAt(r, c);
                            Rectangle part = new Rectangle(30, 30, ship.getParts()[i].getFill());
                            part.setStroke(Color.BLACK);
                            part.setUserData("real");
                            target.getChildren().add(part);
                        }

                        // Actualiza contador
                        currentCount = shipCounts.get(selectedShipType);
                        if (currentCount > 0) {
                            shipCounts.put(selectedShipType, currentCount - 1);
                            updateLabelCount(selectedShipType, currentCount - 1);
                        }
                    } else {
                        showFloatingMessage("Inválido: El barco se sale del tablero o el espacio está ocupado");
                    }
                });
                // Actualizar sombra al mover el mouse
                cell.setOnMouseMoved(e -> previewShipPlacement(cell));

                // También cuando entra el mouse
                cell.setOnMouseEntered(e -> previewShipPlacement(cell));

                // Limpiar sombra al salir
                cell.setOnMouseExited(e -> {
                    for (Node node : gridBoard.getChildren()) {
                        if (node instanceof StackPane stack) {
                            stack.getChildren().removeIf(child ->
                                    child instanceof Rectangle rect &&
                                            "preview".equals(rect.getUserData()));
                        }
                    }
                });

                gridBoard.add(cell, col, row);
            }
        }
    }

    private void previewShipPlacement(StackPane cell) {
        // Eliminar sombras previas
        for (Node node : gridBoard.getChildren()) {
            if (node instanceof StackPane stack) {
                stack.getChildren().removeIf(child ->
                        child instanceof Rectangle rect &&
                                "preview".equals(rect.getUserData()));
            }
        }

        if (selectedShipType == null) return;

        Ship ship = createShipFromString(selectedShipType);
        if (ship == null) return;

        int size = ship.getSize();
        Integer rowIdx = GridPane.getRowIndex(cell);
        Integer colIdx = GridPane.getColumnIndex(cell);
        int row0 = rowIdx == null ? 0 : rowIdx;
        int col0 = colIdx == null ? 0 : colIdx;

        for (int i = 0; i < size; i++) {
            int r = row0, c = col0;

            switch (currentDirection) {
                case RIGHT -> c += i;
                case LEFT  -> c -= i;
                case DOWN  -> r += i;
                case UP    -> r -= i;
            }

            if (r < 0 || r >= 10 || c < 0 || c >= 10) continue;

            StackPane target = getCellPaneAt(r, c);
            if (target != null) {
                Rectangle shadow = new Rectangle(30, 30, Color.LIGHTGRAY);
                shadow.setOpacity(0.4);
                shadow.setUserData("preview");
                target.getChildren().add(shadow);
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

        //Label con contador
        Label countLabel = new Label(shipCounts.get(type).toString());
        countLabel.setUserData("label");
        shipBox.getChildren().add(countLabel);

        Rectangle clone = new Rectangle(30, 30, (Color) ship.getParts()[0].getFill());
        clone.setStroke(Color.BLACK);
        clone.setUserData(type);

        //Selección de barco por clic
        clone.setOnMouseClicked(e -> {
            int currentCount = Integer.parseInt(countLabel.getText());
            if (currentCount <= 0) {
                showFloatingMessage("No quedan barcos de este tipo");
                e.consume();
                return;
            }

            if (selectedShipType != null && selectedShipType.equals(type)) {
                // Deseleccionar si ya estaba seleccionado
                selectedShipType = null;
                clone.setStroke(Color.BLACK);
                clone.setStrokeWidth(1);
            } else {
                // Deseleccionar todos los demás barcos
                for (Node node : shipSelectionArea.getChildren()) {
                    if (node instanceof HBox hbox) {
                        for (Node child : hbox.getChildren()) {
                            if (child instanceof Rectangle rect) {
                                rect.setStroke(Color.BLACK);
                                rect.setStrokeWidth(1);
                            }
                        }
                    }
                }
                // Seleccionar este barco
                selectedShipType = type;
                clone.setStroke(Color.DEEPSKYBLUE);
                clone.setStrokeWidth(3);
            }

            e.consume();
        });


        clone.setOnDragDetected(event -> {
            int currentCount = Integer.parseInt(countLabel.getText());

            //No permitir arrastrar si no quedan barcos
            if (currentCount <= 0) {
                event.consume();
                return;
            }
            //Se guarda temporalmente el contador para actualizarlo despues
            activeCountLanbel = countLabel;

            Dragboard db = clone.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(type);
            db.setContent(content);

            //Crear miniatura
            Rectangle preview = new Rectangle(30, 30, (Color) clone.getFill());
            WritableImage snapshot = preview.snapshot(new SnapshotParameters(), null);
            db.setDragView(snapshot);

            event.consume();
        });
        shipBox.getChildren().add(clone);
        shipSelectionArea.getChildren().add(shipBox);
    }

    private StackPane getCellPaneAt(int row, int col) {
        for (Node n : gridBoard.getChildren()) {
            if (n instanceof StackPane) {
            int rowIndex = GridPane.getRowIndex(n) == null ? 0 : GridPane.getRowIndex(n);
            int colIndex = GridPane.getColumnIndex(n) == null ? 0 : GridPane.getColumnIndex(n);
            if (rowIndex == row && colIndex == col) {
                return (StackPane) n;
            }
            }
        }
        return null;
    }

    //HU-1 Mostrar mensaje de error si no se puede colocar el barco
    private void showFloatingMessage(String message) {
        textFloat.setText(message);
        textFloat.setOpacity(1);

        FadeTransition fade = new FadeTransition(Duration.seconds(3), textFloat);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.play();
    }

    //Contador Visual cuando se coloca un barco por click
    private void updateLabelCount(String type, int newCount) {
        for (Node node : shipSelectionArea.getChildren()) {
            if (node instanceof HBox hbox && hbox.getUserData().equals(type)) {
                for (Node child : hbox.getChildren()) {
                    if (child instanceof Label label && "label".equals(label.getUserData())) {
                        label.setText(String.valueOf(newCount));
                        break;
                    }
                }
            }
        }
    }
}