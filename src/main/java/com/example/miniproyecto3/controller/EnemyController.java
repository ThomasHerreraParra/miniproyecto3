package com.example.miniproyecto3.controller;

import com.example.miniproyecto3.storage.SavedShip;
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;   // << new



public class EnemyController extends NavigationAdapter {

    @FXML
    private GridPane enemyBoard;
    private final Map<String, Integer> shipCounts = new HashMap<>();
    private final int GRID_SIZE = 10;
    private final int CELL_SIZE = 50;
    private static final String SAVE_PATH = "enemy_board.txt";


    @FXML
    public void initialize() {
        shipCounts.put("fragata", 4);
        shipCounts.put("submarino", 3);
        shipCounts.put("destructor", 2);
        shipCounts.put("portaviones", 1);
        createGrid();
        Path saveFile = Paths.get(SAVE_PATH);
        if (Files.exists(saveFile)) {
            // board already saved → just paint it
            List<SavedShip> ships = loadBoard(saveFile);
            paintSavedShips(ships);
        } else {
            placeShipsRandomly();  //Tambien guarda
        }
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
        List<SavedShip> savedShips = new ArrayList<>();   // <‑‑ lista donde iremos guardando


        for (Map.Entry<String, Integer> entry : shipCounts.entrySet()) {
            String shipName = entry.getKey();
            int count = entry.getValue();

            int size = switch (shipName) {
                case "fragata" -> 1;
                case "submarino" -> 3;
                case "destructor" -> 2;
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

                    // Guardamos la nave para persistencia
                    savedShips.add(new SavedShip(shipName, row, col, horizontal));
                    count--;
                }
            }

            if (attempts >= 1000) {
                System.out.println("No se pudo colocar todos los barcos de tipo: " + shipName);
            }
        }
        // 🔒 Persistimos el tablero en disco
        saveBoard(SAVE_PATH, savedShips);
    }

    //nuevo metodo publico para generar un nuevo archivo desde GameController
    public static void generateAndSaveEnemyBoard() {
        final int GRID_SIZE = 10;
        Map<String, Integer> shipCounts = Map.of(
                "fragata", 4,
                "submarino", 3,
                "destructor", 2,
                "portaviones", 1
        );

        Random rand = new Random();
        List<SavedShip> savedShips = new ArrayList<>();
        boolean[][] occupied = new boolean[GRID_SIZE][GRID_SIZE];

        for (Map.Entry<String, Integer> entry : shipCounts.entrySet()) {
            String shipName = entry.getKey();
            int count = entry.getValue();
            int size = switch (shipName) {
                case "fragata" -> 1;
                case "submarino" -> 3;
                case "destructor" -> 2;
                case "portaviones" -> 4;
                default -> 1;
            };

            int attempts = 0;
            while (count > 0 && attempts < 1000) {
                attempts++;
                boolean horizontal = rand.nextBoolean();
                int row = rand.nextInt(GRID_SIZE);
                int col = rand.nextInt(GRID_SIZE);

                // ✅ NUEVA validación de espacio antes del loop
                if ((horizontal && col + size > GRID_SIZE) || (!horizontal && row + size > GRID_SIZE)) {
                    continue;
                }

                boolean canPlace = true;
                for (int i = 0; i < size; i++) {
                    int r = row + (horizontal ? 0 : i);
                    int c = col + (horizontal ? i : 0);
                    if (r >= GRID_SIZE || c >= GRID_SIZE || occupied[r][c]) {
                        canPlace = false;
                        break;
                    }
                }

                if (canPlace) {
                    for (int i = 0; i < size; i++) {
                        int r = row + (horizontal ? 0 : i);
                        int c = col + (horizontal ? i : 0);
                        occupied[r][c] = true;
                    }
                    savedShips.add(new SavedShip(shipName, row, col, horizontal));
                    count--;
                }
            }
        }

        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(SAVE_PATH))) {
            for (SavedShip ship : savedShips) {
                writer.write(ship.toString());
                writer.newLine();
            }
            System.out.println("Nuevo tablero enemigo guardado en " + SAVE_PATH);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



     /* Escribe la instantánea del tablero en un archivo de texto sin formato (CSV).    */
    private void saveBoard(String filePath, List<SavedShip> ships) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (SavedShip ship : ships) {
                writer.write(ship.toString());
                writer.newLine();
            }
            System.out.println("Board saved to: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
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

    private List<SavedShip> loadBoard(Path file) {
        List<SavedShip> ships = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(file)) {
            String line;
            while ((line = br.readLine()) != null) {
                ships.add(SavedShip.fromCsv(line));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ships;
    }

    /**
     * Re‑draws the enemy grid from the list we just read.
     */
    private void paintSavedShips(List<SavedShip> ships) {

        for (SavedShip s : ships) {
            // 1) size lookup
            int size = switch (s.getType()) {
                case "fragata"     -> 1;
                case "submarino"   -> 3;
                case "destructor"  -> 2;
                case "portaviones" -> 4;
                default            -> 1;
            };

            // 2) obtain the colour the same way you already do
            Ship model = switch (s.getType()) {
                case "fragata"     -> new Fragata();
                case "submarino"   -> new Submarino();
                case "destructor"  -> new Destructor();
                case "portaviones" -> new Portaviones();
                default            -> null;
            };
            if (model == null) continue;
            Color colour = (Color) model.getParts()[0].getFill();

            // 3) paint every cell of that ship
            for (int i = 0; i < size; i++) {
                int r = s.getRow() + (s.isHorizontal() ? 0 : i);
                int c = s.getCol() + (s.isHorizontal() ? i : 0);

                Rectangle rect = (Rectangle) getCell(r, c).getChildren().get(0);
                rect.setFill(colour);
            }
        }
    }
}


