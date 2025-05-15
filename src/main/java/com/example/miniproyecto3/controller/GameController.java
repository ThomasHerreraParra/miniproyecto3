package com.example.miniproyecto3.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import com.example.miniproyecto3.ships.*;

public class GameController extends NavigationAdapter {

    @FXML
    private GridPane gridBoard;

    @FXML
    private VBox shipSelectionArea;

    /**
     * Inicializa el tablero de juego y carga los barcos disponibles para arrastrar.
     */
    @FXML
    public void initialize() {
        createGrid();
        loadShips();
    }

    /**
     * Maneja el evento de regreso al menú principal.
     */
    public void handleBack(ActionEvent event) {
        goTo("/com/example/miniproyecto3/start-view.fxml", (Node) event.getSource());
    }





    /**
     * Crea la cuadrícula de 10x10 del tablero con celdas interactivas para el drag and drop.
     */
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

                //------------------FALTA ARREGLAR TODA ESTA VAINA-----------------------
                //SE ARRASTRAN TODOS LOS CUADRADOS Y SOLO TIENE QUE SER UNO (UN BARCO) Y QUITARSE DE LA BARRA LATERAL
                //DERECHA Y QUE QUEDEN LOS SOBRANTES OBVIAMENTE, SI SON 4 SE QUITA UNO ARRASTRANDO Y QUEDAN 3.


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

                    if (db.hasString() && db.getString().equals("ship")) {
                        // Aquí debes crear una NUEVA instancia del barco visualmente
                        Ship newShip = createShipFromString("ship"); // puedes mejorar esto si usas IDs específicos
                        if (newShip != null) {
                            HBox newShipBox = new HBox(5);
                            for (Rectangle part : newShip.getParts()) {
                                newShipBox.getChildren().add(part);
                            }

                            cell.getChildren().add(newShipBox);
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
    //Metodo auxiliar para no diferenciar entre tipos
    private Ship createShipFromString(String shipType) {
        // Puedes cambiar esto para tener más control
        return new Fragata(); // por ejemplo, crea un barco de 1 casilla para testear
    }


    /**
     * Carga las instancias de barcos disponibles en el panel lateral para ser arrastrados.
     */
    private void loadShips() {
        addShipToSelection(new Fragata());
        addShipToSelection(new Submarino());
        addShipToSelection(new Destructor());
        addShipToSelection(new Portaviones());
    }

    /**
     * Agrega un barco visualmente a la zona de selección y configura su arrastrabilidad.
     */
    private void addShipToSelection(Ship ship) {
        // Contenedor visual del barco en el panel derecho (plantilla)
        HBox shipBox = new HBox(5);

        // Agrega visualmente las partes (rectángulos) del barco
        for (Rectangle part : ship.getParts()) {
            Rectangle clone = new Rectangle(part.getWidth(), part.getHeight(), (Color) part.getFill());
            shipBox.getChildren().add(clone);
        }

        // Evento de arrastrar detectado desde la plantilla
        shipBox.setOnDragDetected(event -> {
            Dragboard db = shipBox.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString("ship");  // solo usamos un identificador, no objetos no serializables
            db.setContent(content);

            // Creamos una copia visual para arrastrar (no el original del panel)
            Ship newShip = createShipCopy(ship);  // << función que clona el tipo de barco
            HBox dragVisual = new HBox(5);
            for (Rectangle part : newShip.getParts()) {
                Rectangle clone = new Rectangle(part.getWidth(), part.getHeight(), (Color) part.getFill());
                dragVisual.getChildren().add(clone);
            }

            WritableImage snapshot = dragVisual.snapshot(null, null);
            db.setDragView(snapshot);

            // Eliminamos la plantilla del panel (solo se arrastra una vez)
            shipSelectionArea.getChildren().remove(shipBox);

            event.consume();
        });

        // Añadir al área de selección
        shipSelectionArea.getChildren().add(shipBox);
    }
    private Ship createShipCopy(Ship original) {
        if (original instanceof Fragata) return new Fragata();
        if (original instanceof Submarino) return new Submarino();
        if (original instanceof Destructor) return new Destructor();
        if (original instanceof Portaviones) return new Portaviones();
        return null;
    }


    //HASTA ACA SE TIENE QUE CORRIGIR EL CODIGO, EL CORCHETE DE ABAJO ES LO MEJOR QUE HAY DE ESTE CONTROLLER.


}


