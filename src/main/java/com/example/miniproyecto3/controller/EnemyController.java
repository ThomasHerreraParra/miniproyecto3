/**
 * Warship Dominions - Naval Battle Game
 *
 * Version: 1.0
 * License: OpenGL
 *
 * Authors:
 * - Yoel Steven Montoya (2416571)
 * - Andrés Felipe Muñoz (2415124)
 * - Thomas Herrera Parra (2417158)
 */


package com.example.miniproyecto3.controller;

import com.example.miniproyecto3.storage.SavedShip;
import com.example.miniproyecto3.ships.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Controller class responsible for initializing and managing the enemy board.
 * This includes randomly placing ships, saving and loading board state,
 * and displaying ship images according to orientation.
 */
public class EnemyController extends NavigationAdapter {

    @FXML
    private GridPane enemyBoard;

    private static final int GRID_SIZE = 10;
    private static final int CELL_SIZE = 50;
    private static final Color DEFAULT_COLOR = Color.web("#e6f7ff");
    private static final String SAVE_PATH = "enemy_board.txt";

    private final Map<String, Integer> shipCounts = Map.of(
            "fragata", 4,
            "destructor", 3,
            "submarino", 2,
            "portaviones", 1
    );

    /**
     * Initializes the enemy board. If a saved board exists, it loads and paints it.
     * Otherwise, it generates a new one randomly.
     */
    @FXML
    public void initialize() {
        createGrid();
        Path saveFile = Paths.get(SAVE_PATH);
        if (Files.exists(saveFile)) {
            paintSavedShips(loadBoard(saveFile));
        } else {
            placeShipsRandomly();
        }
    }

    /**
     * Creates the visual 10x10 grid with labels and background color.
     */
    private void createGrid() {
        for (int col = 0; col < GRID_SIZE; col++) {
            Label label = createLabel(String.valueOf(col + 1));
            enemyBoard.add(label, col + 1, 0);
        }
        for (int row = 0; row < GRID_SIZE; row++) {
            Label label = createLabel(String.valueOf((char) ('A' + row)));
            enemyBoard.add(label, 0, row + 1);
        }
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                StackPane cell = new StackPane();
                cell.setPrefSize(CELL_SIZE, CELL_SIZE);
                Rectangle background = new Rectangle(CELL_SIZE, CELL_SIZE);
                background.setFill(DEFAULT_COLOR);
                background.setStroke(Color.LIGHTGRAY);
                cell.getChildren().add(background);
                enemyBoard.add(cell, col + 1, row + 1);
            }
        }
    }

    /**
     * Creates a bold, centered label for rows and columns.
     * @param text Text to display.
     * @return A configured label node.
     */
    private Label createLabel(String text) {
        Label label = new Label(text);
        label.setMinSize(CELL_SIZE, CELL_SIZE);
        label.setAlignment(Pos.CENTER);
        label.setStyle("-fx-font-weight: bold;");
        return label;
    }

    /**
     * Randomly places all ships on the board and saves the result.
     */
    private void placeShipsRandomly() {
        Random rand = new Random();
        List<SavedShip> savedShips = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : shipCounts.entrySet()) {
            String type = entry.getKey();
            int count = entry.getValue();
            int size = getShipSize(type);

            int attempts = 0;
            while (count > 0 && attempts < 1000) {
                attempts++;
                boolean horizontal = rand.nextBoolean();
                int row = rand.nextInt(GRID_SIZE);
                int col = rand.nextInt(GRID_SIZE);

                if (canPlaceShip(row, col, size, horizontal)) {
                    String imagePath = switch (type) {
                        case "fragata"     -> "/com/example/miniproyecto3/assets/fragata.png";
                        case "destructor"  -> "/com/example/miniproyecto3/assets/destructor.png";
                        case "submarino"   -> "/com/example/miniproyecto3/assets/submarino.png";
                        case "portaviones" -> "/com/example/miniproyecto3/assets/portaviones.png";
                        default            -> null;
                    };

                    if (imagePath != null) {
                        paintShip(row, col, size, horizontal, imagePath);
                        savedShips.add(new SavedShip(type, row, col, horizontal));
                        count--;
                    }
                }
            }

            if (attempts >= 1000) {
                try {
                    throw new PlacementException("No se pudo colocar el barco de tipo: " + type);
                } catch (PlacementException e) {
                    System.err.println(e.getMessage());
                    new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
                }
            }
        }

        saveBoard(savedShips);
    }

    /**
     * Generates a new enemy board and writes the ship layout to a file.
     */
    public static void generateAndSaveEnemyBoard() {
        Random rand = new Random();
        List<SavedShip> savedShips = new ArrayList<>();
        boolean[][] occupied = new boolean[GRID_SIZE][GRID_SIZE];

        Map<String, Integer> shipCounts = Map.of(
                "fragata", 4,
                "submarino", 2,
                "destructor", 3,
                "portaviones", 1
        );

        for (Map.Entry<String, Integer> entry : shipCounts.entrySet()) {
            String type = entry.getKey();
            int count = entry.getValue();
            int size = switch (type) {
                case "fragata" -> 1;
                case "submarino" -> 3;
                case "destructor" -> 2;
                case "portaviones" -> 4;
                default -> 0;
            };

            int attempts = 0;
            while (count > 0 && attempts < 1000) {
                attempts++;
                boolean horizontal = rand.nextBoolean();
                int row = rand.nextInt(GRID_SIZE);
                int col = rand.nextInt(GRID_SIZE);

                if ((horizontal && col + size > GRID_SIZE) || (!horizontal && row + size > GRID_SIZE)) continue;

                boolean canPlace = true;
                for (int i = 0; i < size; i++) {
                    int r = row + (horizontal ? 0 : i);
                    int c = col + (horizontal ? i : 0);
                    if (occupied[r][c]) {
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
                    savedShips.add(new SavedShip(type, row, col, horizontal));
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

    /**
     * Saves the list of placed ships to file.
     * @param ships List of ships to serialize.
     */
    private void saveBoard(List<SavedShip> ships) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(SAVE_PATH))) {
            for (SavedShip ship : ships) {
                writer.write(ship.toString());
                writer.newLine();
            }
            System.out.println("Board saved to: " + SAVE_PATH);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks if a ship can be placed in the given location without overlapping.
     */
    private boolean canPlaceShip(int row, int col, int size, boolean horizontal) {
        for (int i = 0; i < size; i++) {
            int r = row + (horizontal ? 0 : i);
            int c = col + (horizontal ? i : 0);
            if (r >= GRID_SIZE || c >= GRID_SIZE) return false;

            StackPane cell = getCell(r, c);
            Rectangle rect = (Rectangle) cell.getChildren().getFirst();
            if (!rect.getFill().equals(DEFAULT_COLOR)) return false;
        }
        return true;
    }

    /**
     * Paints a ship on the board using an image stretched across multiple cells.
     */
    private void paintShip(int row, int col, int size, boolean horizontal, String imagePath) {
        Image image = new Image(getClass().getResourceAsStream(imagePath));
        ImageView shipView = new ImageView(image);

        if (horizontal) {
            shipView.setFitWidth(size * CELL_SIZE);
            shipView.setFitHeight(CELL_SIZE);
        } else {
            shipView.setFitWidth(CELL_SIZE);
            shipView.setFitHeight(size * CELL_SIZE);
        }

        shipView.setPreserveRatio(false);
        shipView.setSmooth(true);

        for (int i = 0; i < size; i++) {
            int r = row + (horizontal ? 0 : i);
            int c = col + (horizontal ? i : 0);
            StackPane cell = getCell(r, c);
            if (cell != null) cell.getChildren().clear();
        }

        StackPane imageContainer = new StackPane(shipView);
        imageContainer.setPickOnBounds(false);
        StackPane.setAlignment(shipView, Pos.CENTER);

        GridPane.setRowIndex(imageContainer, row + 1);
        GridPane.setColumnIndex(imageContainer, col + 1);
        GridPane.setRowSpan(imageContainer, horizontal ? 1 : size);
        GridPane.setColumnSpan(imageContainer, horizontal ? size : 1);

        enemyBoard.getChildren().add(imageContainer);
    }

    /**
     * Returns the StackPane representing the cell at a specific board coordinate.
     */
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

    /**
     * Gets the numeric size of a ship based on its type.
     */
    private int getShipSize(String type) {
        return switch (type) {
            case "fragata" -> 1;
            case "submarino" -> 3;
            case "destructor" -> 2;
            case "portaviones" -> 4;
            default -> throw new IllegalArgumentException("Tipo de barco inválido: " + type);
        };
    }

    /**
     * Creates an instance of a Ship subclass based on a string type.
     */
    private Ship createShipInstance(String type) {
        return switch (type) {
            case "fragata" -> new Fragata();
            case "submarino" -> new Submarino();
            case "destructor" -> new Destructor();
            case "portaviones" -> new Portaviones();
            default -> throw new IllegalArgumentException("Tipo de barco inválido: " + type);
        };
    }

    /**
     * Handles return to the game view.
     */
    public void returnBoard(ActionEvent event) {
        goTo("/com/example/miniproyecto3/game-view.fxml", (Node) event.getSource());
    }

    /**
     * Loads a list of ships from a saved file.
     */
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
     * Paints all ships previously saved in file onto the board using directional images.
     */
    private void paintSavedShips(List<SavedShip> ships) {
        for (SavedShip s : ships) {
            int size = getShipSize(s.getType());
            String direccion;

            if (s.isHorizontal()) {
                direccion = (s.getCol() + size <= GRID_SIZE) ? "derecha" : "izquierda";
            } else {
                direccion = (s.getRow() + size <= GRID_SIZE) ? "abajo" : "arriba";
            }

            String imagePath = "/com/example/miniproyecto3/assets/" + s.getType().toLowerCase() + direccion + ".png";
            paintShip(s.getRow(), s.getCol(), size, s.isHorizontal(), imagePath);
        }
    }

    /**
     * Returns the full image path for a ship based on orientation and direction.
     */
    private static String getImagePath(String type, boolean horizontal, int row, int col, int size) {
        String direction;
        if (horizontal) {
            direction = (col + size <= GRID_SIZE) ? "derecha" : "izquierda";
        } else {
            direction = (row + size <= GRID_SIZE) ? "abajo" : "arriba";
        }
        return "/com/example/miniproyecto3/assets/" + type.toLowerCase() + direction + ".png";
    }
}
