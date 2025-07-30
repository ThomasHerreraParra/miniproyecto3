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
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Alert;
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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.image.Image;

import javafx.scene.input.MouseButton;
import javafx.scene.image.ImageView;

/**
 * Main game controller class that handles the naval battle game logic and UI interactions.
 */
public class GameController extends NavigationAdapter {

    @FXML private GridPane gridBoard;
    @FXML private GridPane enemyBoard;
    @FXML private VBox shipSelectionArea;
    @FXML private Label textFloat;
    @FXML private Label lblWelcome;
    private boolean playerTurn = false;
    @FXML private Label lblTurn;

    private final Set<String> firedByEnemy = new HashSet<>();
    private int playerShipsAlive;
    private final Map<String, Integer> playerRemainingParts = new HashMap<>();
    private final Map<String, Integer> shipCounts = new HashMap<>();
    private Label activeCountLanbel = null;
    private static final String SHOTS_SAVE_PATH = "shots.txt";
    private static final String ENEMY_SHOTS_SAVE_PATH = "enemy_shots.txt";
    private static final String PLAYER_SAVE_PATH = "player_board.txt";
    private static final String ENEMY_SAVE_PATH  = "enemy_board.txt";
    private final List<SavedShip> playerShips = new ArrayList<>();
    private final List<SavedShip> enemyShips  = new ArrayList<>();
    private String selectedShipType = null;
    private enum Direction {RIGHT, DOWN, LEFT, UP}
    private Direction currentDirection = Direction.RIGHT;
    private final Map<String, Integer> enemyRemainingParts = new HashMap<>();
    private final Set<String> firedCells = new HashSet<>();
    private int enemyShipsAlive = 0;

    /**
     * Checks if all ships have been placed on the board.
     * @return true if all ships are placed, false otherwise
     */
    private boolean allShipsPlaced() {
        return shipCounts.values().stream().allMatch(count -> count == 0);
    }

    /**
     * Initializes the game controller and sets up the game boards.
     */
    @FXML
    public void initialize() {
        cleanDynamicElements(gridBoard);
        cleanDynamicElements(enemyBoard);

        try {
            Path nickPath = Paths.get("nickname.txt");
            if (Files.exists(nickPath)) {
                String nick = Files.readString(nickPath).trim();
                lblWelcome.setText("Bienvenido, capitán " + nick);
            } else {
                lblWelcome.setText("Bienvenido, capitán");
            }
        } catch (IOException e) {
            lblWelcome.setText("Bienvenido, capitán");
        }

        firedCells.clear();
        enemyRemainingParts.clear();
        playerRemainingParts.clear();
        enemyShipsAlive = 0;
        playerShipsAlive = 0;

        shipCounts.put("fragata", 4);
        shipCounts.put("destructor", 3);
        shipCounts.put("submarino", 2);
        shipCounts.put("portaviones", 1);
        createGrid(gridBoard, false);
        createGrid(enemyBoard, true);
        loadShips();

        Path playerFile = Paths.get(PLAYER_SAVE_PATH);
        Path enemyFile  = Paths.get(ENEMY_SAVE_PATH);

        if (Files.exists(playerFile)) {
            List<SavedShip> alreadyPlaced = loadBoardFromFile(PLAYER_SAVE_PATH, gridBoard, true);
            playerShips.addAll(alreadyPlaced);

            System.out.println("Barcos del jugador cargados: " + playerShips.size());
            for (SavedShip s : alreadyPlaced) {
                shipCounts.put(s.getType(), shipCounts.get(s.getType()) - 1);
                System.out.println("Partes vivas del jugador: " + playerRemainingParts.size());
                boatSize(s, playerRemainingParts);
            }

            refreshAllCounters();
            boolean allPlaced = shipCounts.values().stream().allMatch(v -> v == 0);
            shipSelectionArea.setDisable(allPlaced);
        }

        if (Files.exists(enemyFile)) {
            enemyShips.addAll(loadBoardFromFile(ENEMY_SAVE_PATH, null, false));
            for (SavedShip s : enemyShips) {
                boatSize(s, enemyRemainingParts);
            }
            enemyShipsAlive = enemyRemainingParts.size();
        }

        loadShotsFromFile();
        loadEnemyShotsFromFile();

        enemyShipsAlive = countAliveShips(enemyShips, enemyRemainingParts);
        if (playerShips.isEmpty() || playerRemainingParts.isEmpty()) {
            System.out.println("⚠️ No hay barcos del jugador cargados o no se cargaron correctamente");
            return;
        }
        playerShipsAlive = countAliveShips(playerShips, playerRemainingParts);
        if (enemyShipsAlive == 0) {
            showEndgameDialog(true);
        } else if (playerShipsAlive == 0) {
            showEndgameDialog(false);
        }

        if (allShipsPlaced()) {
            playerTurn = true;
            updateTurnLabel();
            shipSelectionArea.setDisable(true);
        }
    }

    /**
     * Cleans dynamic elements from the game board.
     * @param board The game board to clean
     */
    private void cleanDynamicElements(GridPane board) {
        if (board == null) return;

        for (Node node : new ArrayList<>(board.getChildren())) {
            if (node instanceof StackPane) {
                StackPane cell = (StackPane) node;
                cell.getChildren().removeIf(child ->
                        child instanceof ImageView ||
                                (child instanceof Rectangle && "real".equals(((Rectangle)child).getUserData()))
                );

                if (board == enemyBoard) {
                    Integer row = GridPane.getRowIndex(cell);
                    Integer col = GridPane.getColumnIndex(cell);
                    if (row != null && row > 0 && col != null && col > 0) {
                        cell.getProperties().put("row", row - 1);
                        cell.getProperties().put("col", col - 1);
                        cell.setOnMouseClicked(e -> handleShot(cell));
                    }
                }
            }
        }
    }

    /**
     * Calculates the size of a ship and stores it in the remaining parts map.
     * @param s The ship to calculate size for
     * @param playerRemainingParts The map to store remaining parts
     */
    private void boatSize(SavedShip s, Map<String, Integer> playerRemainingParts) {
        int size = switch (s.getType()) {
            case "fragata" -> 1;
            case "submarino" -> 3;
            case "destructor" -> 2;
            case "portaviones" -> 4;
            default -> 0;
        };
        String shipId = s.getType() + "_" + s.getRow() + "_" + s.getCol();
        playerRemainingParts.put(shipId, size);
    }

    /**
     * Updates the turn label based on whose turn it is.
     */
    private void updateTurnLabel() {
        String nick = "capitán";
        try {
            nick = "capitán " + Files.readString(Paths.get("nickname.txt")).trim();
        } catch (IOException e) { /* ignore */ }
        lblTurn.setText(playerTurn
                ? "Es tu turno de disparar, " + nick
                : "Turno de la máquina...");
    }

    /**
     * Handles back button action.
     * @param event The action event
     */
    public void handleBack(ActionEvent event) {
        goTo("/com/example/miniproyecto3/start-view.fxml", (Node) event.getSource());
    }

    /**
     * Handles enemy view button action.
     * @param event The action event
     */
    public void handlenemy(ActionEvent event) {
        goTo("/com/example/miniproyecto3/enemy-view.fxml", (Node) event.getSource());
    }

    /**
     * Loads a game board from file.
     * @param path The file path to load from
     * @param target The target grid pane
     * @param paint Whether to paint the ships on the board
     * @return List of loaded ships
     */
    private List<SavedShip> loadBoardFromFile(String path, GridPane target, boolean paint) {
        List<SavedShip> ships = new ArrayList<>();

        try (BufferedReader br = Files.newBufferedReader(Paths.get(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length != 4) continue;

                String type = p[0];
                int row = Integer.parseInt(p[1]);
                int col = Integer.parseInt(p[2]);
                boolean horiz = "H".equalsIgnoreCase(p[3]);

                ships.add(new SavedShip(type, row, col, horiz));

                if (!paint || target == null) continue;

                StackPane baseCell = getCellPaneAt(target, row, col);
                if (baseCell == null) {
                    System.err.println("Celda base no encontrada en [" + row + "," + col + "]");
                    continue;
                }

                int size = getShipSize(type);
                String direction = horiz ?
                        (col + size <= 9 ? "derecha" : "izquierda") :
                        (row + size <= 9 ? "abajo" : "arriba");

                String imagePath = "/com/example/miniproyecto3/assets/" + type.toLowerCase() + direction + ".png";

                try {
                    Image image = new Image(getClass().getResourceAsStream(imagePath));
                    ImageView shipView = new ImageView(image);
                    shipView.setPreserveRatio(true);

                    if (horiz) {
                        shipView.setFitWidth(size * 30);
                        shipView.setFitHeight(30);
                    } else {
                        shipView.setFitWidth(30);
                        shipView.setFitHeight(size * 30);
                    }

                    StackPane container = new StackPane(shipView);
                    container.setPickOnBounds(false);

                    GridPane.setRowIndex(container, row + 1);
                    GridPane.setColumnIndex(container, col + 1);
                    GridPane.setRowSpan(container, horiz ? 1 : size);
                    GridPane.setColumnSpan(container, horiz ? size : 1);

                    target.getChildren().add(container);

                    for (int i = 0; i < size; i++) {
                        int r = row + (horiz ? 0 : i);
                        int c = col + (horiz ? i : 0);
                        StackPane cell = getCellPaneAt(target, r, c);
                        if (cell != null) {
                            Rectangle marker = new Rectangle(30, 30, Color.TRANSPARENT);
                            marker.setUserData("real");
                            cell.getChildren().add(marker);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error al cargar imagen: " + imagePath);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return ships;
    }

    /**
     * Gets the size of a ship based on its type.
     * @param type The ship type
     * @return The size of the ship
     */
    private int getShipSize(String type) {
        return switch (type.toLowerCase()) {
            case "fragata"     -> 1;
            case "destructor"  -> 2;
            case "submarino"   -> 3;
            case "portaviones" -> 4;
            default -> throw new IllegalArgumentException("Tipo de barco inválido: " + type);
        };
    }

    /**
     * Paints a ship on the game grid.
     * @param grid The grid to paint on
     * @param row The starting row
     * @param col The starting column
     * @param size The ship size
     * @param horizontal Whether the ship is horizontal
     * @param imagePath The path to the ship image
     */
    private void paintShipOnGrid(GridPane grid, int row, int col, int size, boolean horizontal, String imagePath) {
        try {
            Image image = new Image(getClass().getResourceAsStream(imagePath));
            ImageView shipView = new ImageView(image);
            shipView.setPreserveRatio(true);

            if (horizontal) {
                shipView.setFitWidth(size * 30);
                shipView.setFitHeight(30);
            } else {
                shipView.setFitWidth(30);
                shipView.setFitHeight(size * 30);
            }

            StackPane baseCell = getCellPaneAt(grid, row, col);
            if (baseCell == null) return;

            baseCell.getChildren().removeIf(child ->
                    child instanceof ImageView ||
                            (child instanceof Rectangle && "real".equals(((Rectangle)child).getUserData()))
            );

            StackPane shipContainer = new StackPane(shipView);
            shipContainer.setPickOnBounds(false);

            GridPane.setRowIndex(shipContainer, row + 1);
            GridPane.setColumnIndex(shipContainer, col + 1);
            GridPane.setRowSpan(shipContainer, horizontal ? 1 : size);
            GridPane.setColumnSpan(shipContainer, horizontal ? size : 1);

            grid.getChildren().add(shipContainer);

            for (int i = 0; i < size; i++) {
                int r = row + (horizontal ? 0 : i);
                int c = col + (horizontal ? i : 0);
                StackPane cell = getCellPaneAt(grid, r, c);
                if (cell != null) {
                    Rectangle marker = new Rectangle(30, 30, Color.TRANSPARENT);
                    marker.setUserData("real");
                    cell.getChildren().add(marker);
                }
            }
        } catch (Exception e) {
            System.err.println("Error al pintar barco: " + e.getMessage());
        }
    }

    /**
     * Saves the player's board to file.
     */
    private void savePlayerBoard() {
        try (BufferedWriter w = Files.newBufferedWriter(Paths.get(PLAYER_SAVE_PATH))) {
            for (SavedShip s : playerShips) {
                w.write(s.toString());
                w.newLine();
            }
            System.out.println("Player board saved → " + PLAYER_SAVE_PATH);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Converts ship parameters to a SavedShip object.
     * @param type The ship type
     * @param rowClick The row position
     * @param colClick The column position
     * @param size The ship size
     * @param dir The ship direction
     * @return The created SavedShip object
     */
    private SavedShip toSavedShip(String type, int rowClick, int colClick, int size, Direction dir) {
        boolean horizontal = (dir == Direction.RIGHT || dir == Direction.LEFT);
        int startRow = (dir == Direction.UP)    ? rowClick - size + 1 : rowClick;
        int startCol = (dir == Direction.LEFT)  ? colClick - size + 1 : colClick;
        return new SavedShip(type, startRow, startCol, horizontal);
    }

    /**
     * Gets the StackPane cell at specified coordinates.
     * @param board The game board
     * @param row The row index
     * @param col The column index
     * @return The StackPane cell or null if not found
     */
    private StackPane getCellPaneAt(GridPane board, int row, int col) {
        for (Node n : board.getChildren()) {
            if (n instanceof StackPane sp) {
                Integer r = GridPane.getRowIndex(sp);
                Integer c = GridPane.getColumnIndex(sp);
                if (r != null && c != null && r == row + 1 && c == col + 1) {
                    return sp;
                }
            }
        }
        return null;
    }

    /**
     * Creates the game grid.
     * @param targetGrid The target grid pane
     * @param isEnemy Whether it's the enemy grid
     */
    private void createGrid(GridPane targetGrid, boolean isEnemy) {
        if (targetGrid.getChildren().size() > 20) {
            return;
        }

        int numRows = 10;
        int numCols = 10;
        int cellSize = 50;

        for (int col = 0; col < numCols; col++) {
            Label label = new Label(String.valueOf(col + 1));
            label.setMinSize(cellSize, cellSize);
            label.setAlignment(Pos.CENTER);
            label.setStyle("-fx-font-weight: bold;");
            targetGrid.add(label, col + 1, 0);
        }

        for (int row = 0; row < numRows; row++) {
            Label label = new Label(String.valueOf((char) ('A' + row)));
            label.setMinSize(cellSize, cellSize);
            label.setAlignment(Pos.CENTER);
            label.setStyle("-fx-font-weight: bold;");
            targetGrid.add(label, 0, row + 1);
        }

        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                StackPane cell = new StackPane();
                cell.setPrefSize(cellSize, cellSize);

                Rectangle background = new Rectangle(cellSize, cellSize);
                background.setFill(Color.web("#e6f7ff"));
                background.setStroke(Color.LIGHTGRAY);
                cell.getChildren().add(background);

                if (isEnemy) {
                    cell.getProperties().put("row", row);
                    cell.getProperties().put("col", col);
                    cell.setOnMouseClicked(e -> handleShot(cell));
                } else {
                    setupPlayerCellBehavior(cell);
                }

                targetGrid.add(cell, col + 1, row + 1);
            }
        }
    }

    /**
     * Sets up player cell behavior including click and hover events.
     * @param cell The cell to set up
     */
    private void setupPlayerCellBehavior(StackPane cell) {
        cell.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                switch (currentDirection) {
                    case RIGHT -> currentDirection = Direction.DOWN;
                    case DOWN -> currentDirection = Direction.LEFT;
                    case LEFT -> currentDirection = Direction.UP;
                    case UP -> currentDirection = Direction.RIGHT;
                }
                previewShipPlacement(cell);
                showFloatingMessage("Dirección actual: " + currentDirection);
                return;
            }

            if (e.getButton() != MouseButton.PRIMARY) return;

            if (selectedShipType == null) {
                boolean shipsLeft = shipCounts.values().stream().anyMatch(c -> c > 0);
                if (shipsLeft) showFloatingMessage("Selecciona un barco antes de colocarlo");
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
            int row0 = rowIdx == null ? 0 : rowIdx - 1;
            int col0 = colIdx == null ? 0 : colIdx - 1;

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
                    canPlace = false;
                    break;
                }

                boolean ocupado = target.getChildren().stream().anyMatch(n -> {
                    if (n instanceof Rectangle rect) {
                        Object data = rect.getUserData();
                        return "real".equals(data);
                    }
                    return true;
                });

                if (ocupado) {
                    canPlace = false;
                    break;
                }
            }

            if (canPlace) {
                placeShipOnBoard(ship, size, row0, col0);
                shipCounts.put(selectedShipType, currentCount - 1);
                updateLabelCount(selectedShipType, currentCount - 1);

                SavedShip ss = toSavedShip(selectedShipType, row0, col0, size, currentDirection);
                playerShips.add(ss);
                savePlayerBoard();

                if (allShipsPlaced()) {
                    shipSelectionArea.setDisable(true);
                    playerShipsAlive = playerShips.size();
                    playerTurn = true;
                    updateTurnLabel();
                }

            } else {
                showFloatingMessage("Inválido: El barco se sale del tablero o el espacio está ocupado");
            }
        });

        cell.setOnMouseMoved(e -> previewShipPlacement(cell));
        cell.setOnMouseEntered(e -> previewShipPlacement(cell));
        cell.setOnMouseExited(e -> {
            for (Node node : gridBoard.getChildren()) {
                if (node instanceof StackPane stack) {
                    stack.getChildren().removeIf(child ->
                            child instanceof Rectangle rect &&
                                    "preview".equals(rect.getUserData()));
                }
            }
        });
    }

    /**
     * Places a ship on the game board.
     * @param ship The ship to place
     * @param size The ship size
     * @param row0 The starting row
     * @param col0 The starting column
     */
    private void placeShipOnBoard(Ship ship, int size, int row0, int col0) {
        String type = ship.getClass().getSimpleName().toLowerCase();
        String imagePath = getImagePath(type, currentDirection);
        Image image = new Image(getClass().getResourceAsStream(imagePath));
        ImageView shipView = new ImageView(image);

        shipView.setPreserveRatio(true);

        if (currentDirection == Direction.RIGHT || currentDirection == Direction.LEFT) {
            shipView.setFitWidth(size * 30);
            shipView.setFitHeight(30);
        } else {
            shipView.setFitWidth(30);
            shipView.setFitHeight(size * 30);
        }

        StackPane container = new StackPane(shipView);
        container.setPickOnBounds(false);

        int baseRow = row0;
        int baseCol = col0;
        switch (currentDirection) {
            case LEFT  -> baseCol = col0 - size + 1;
            case UP    -> baseRow = row0 - size + 1;
        }

        GridPane.setRowIndex(container, baseRow + 1);
        GridPane.setColumnIndex(container, baseCol + 1);
        GridPane.setRowSpan(container,
                (currentDirection == Direction.RIGHT || currentDirection == Direction.LEFT) ? 1 : size);
        GridPane.setColumnSpan(container,
                (currentDirection == Direction.RIGHT || currentDirection == Direction.LEFT) ? size : 1);

        gridBoard.getChildren().add(container);

        for (int i = 0; i < size; i++) {
            int r = row0, c = col0;
            switch (currentDirection) {
                case RIGHT -> c += i;
                case LEFT  -> c -= i;
                case DOWN  -> r += i;
                case UP    -> r -= i;
            }

            StackPane cell = getCellPaneAt(r, c);
            if (cell != null) {
                cell.getChildren().removeIf(n -> n instanceof Rectangle);
                Rectangle rect = new Rectangle(30, 30, Color.TRANSPARENT);
                rect.setUserData("real");
                cell.getChildren().add(rect);
            }
        }
    }

    /**
     * Checks if the AI hit a player ship at specified coordinates.
     * @param row The row to check
     * @param col The column to check
     * @return true if hit, false otherwise
     */
    private boolean checkPlayerShipHit(int row, int col) {
        for (SavedShip s : playerShips) {
            int size = switch (s.getType()) {
                case "fragata"    -> 1;
                case "submarino"  -> 3;
                case "destructor" -> 2;
                case "portaviones"-> 4;
                default           -> 0;
            };
            for (int i = 0; i < size; i++) {
                int r = s.getRow() + (s.isHorizontal() ? 0 : i);
                int c = s.getCol() + (s.isHorizontal() ? i : 0);
                if (r == row && c == col) {
                    String shipId = s.getType() + "_" + s.getRow() + "_" + s.getCol();
                    int rem = playerRemainingParts.getOrDefault(shipId, size) - 1;
                    playerRemainingParts.put(shipId, rem);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a player ship was sunk at specified coordinates.
     * @param row The row to check
     * @param col The column to check
     * @return true if sunk, false otherwise
     */
    private boolean checkIfPlayerShipSunk(int row, int col) {
        for (SavedShip s : playerShips) {
            int size = switch (s.getType()) {
                case "fragata"    -> 1;
                case "submarino"  -> 3;
                case "destructor" -> 2;
                case "portaviones"-> 4;
                default           -> 0;
            };
            for (int i = 0; i < size; i++) {
                int r = s.getRow() + (s.isHorizontal() ? 0 : i);
                int c = s.getCol() + (s.isHorizontal() ? i : 0);
                if (r == row && c == col) {
                    String shipId = s.getType() + "_" + s.getRow() + "_" + s.getCol();
                    return playerRemainingParts.getOrDefault(shipId, 1) == 0;
                }
            }
        }
        return false;
    }

    /**
     * Draws a sunk ship on the player's board.
     */
    private void drawSunkOnPlayer() {
        for (SavedShip s : playerShips) {
            String shipId = s.getType() + "_" + s.getRow() + "_" + s.getCol();
            if (enemyRemainingParts.getOrDefault(shipId, 1) == 0) {
                int size = switch (s.getType()) {
                    case "fragata"    -> 1;
                    case "submarino"  -> 3;
                    case "destructor" -> 2;
                    case "portaviones"-> 4;
                    default           -> 0;
                };
                for (int i = 0; i < size; i++) {
                    int r = s.getRow() + (s.isHorizontal() ? 0 : i);
                    int c = s.getCol() + (s.isHorizontal() ? i : 0);
                    StackPane cell = getCellPaneAt(gridBoard, r, c);
                    Rectangle part = new Rectangle(30, 30, Color.GREY);
                    part.setOpacity(0.8);
                    cell.getChildren().add(part);
                }
                break;
            }
        }
    }

    /**
     * Draws an explosion effect on a cell.
     * @param cell The cell to draw on
     */
    private void drawExplosion(StackPane cell) {
        Image explosionImage = new Image(getClass().getResourceAsStream("/com/example/miniproyecto3/assets/explosion.png"));
        ImageView explosionView = new ImageView(explosionImage);
        explosionView.setFitWidth(30);
        explosionView.setFitHeight(30);
        cell.getChildren().add(explosionView);

        PauseTransition pause = new PauseTransition(Duration.seconds(0.5));
        pause.setOnFinished(e -> cell.getChildren().remove(explosionView));
        pause.play();
    }

    /**
     * Handles a shot on the enemy board.
     * @param cell The cell that was shot
     */
    private void handleShot(StackPane cell) {
        if (!allShipsPlaced()) {
            try {
                String nick = Files.readString(Paths.get("nickname.txt")).trim();
                showFloatingMessage("Primero ponga todos los barcos, capitan " + nick);
            } catch (IOException e) {
                showFloatingMessage("Primero ponga todos los barcos, capitán");
            }
            return;
        }
        if (!playerTurn) return;

        if (!cell.getProperties().containsKey("row") || !cell.getProperties().containsKey("col")) {
            throw new IllegalStateException("La celda no tiene coordenadas asignadas");
        }

        int row = (int) cell.getProperties().get("row");
        int col = (int) cell.getProperties().get("col");
        String key = row + "," + col;

        drawExplosion(cell);

        if (firedCells.contains(key)) return;
        firedCells.add(key);

        Optional<SavedShip> hitShip = enemyShips.stream().filter(s -> {
            int size = switch (s.getType()) {
                case "fragata"   -> 1;
                case "submarino" -> 3;
                case "destructor"-> 2;
                case "portaviones"->4;
                default          -> 0;
            };
            for (int i = 0; i < size; i++) {
                int r = s.getRow() + (s.isHorizontal() ? 0 : i);
                int c = s.getCol() + (s.isHorizontal() ? i : 0);
                if (r == row && c == col) return true;
            }
            return false;
        }).findFirst();

        if (hitShip.isEmpty()) {
            drawMiss(cell);
        } else {
            SavedShip ship = hitShip.get();
            String shipId = ship.getType() + "_" + ship.getRow() + "_" + ship.getCol();
            int partsLeft = enemyRemainingParts.get(shipId) - 1;
            enemyRemainingParts.put(shipId, partsLeft);

            if (partsLeft == 0) {
                drawSunk(ship);
                enemyShipsAlive--;
                if (enemyShipsAlive == 0) {
                    showEndgameDialog(true);
                    return;
                }
            } else {
                drawHit(cell);
            }
        }
        saveShotsToFile();

        playerTurn = false;
        updateTurnLabel();
        PauseTransition pause = new PauseTransition(Duration.seconds(1));
        pause.setOnFinished(e -> enemyTurn());
        pause.play();
    }

    /**
     * Handles the AI's turn.
     */
    private void enemyTurn() {
        try {
            int row, col;
            String key;
            int attempts = 0;

            do {
                row = new Random().nextInt(10);
                col = new Random().nextInt(10);
                key = row + "," + col;
                attempts++;

                if (attempts > 100) {
                    System.err.println("Demasiados intentos fallidos de disparo de la IA");
                    playerTurn = true;
                    updateTurnLabel();
                    return;
                }
            } while (firedByEnemy.contains(key));

            firedByEnemy.add(key);

            StackPane cell = getCellPaneAt(gridBoard, row, col);
            if (cell == null) {
                System.err.println("Error: Celda nula en [" + row + "," + col + "]");
                rebuildGridIfNeeded();
                playerTurn = true;
                updateTurnLabel();
                return;
            }

            boolean hit = checkPlayerShipHit(row, col);

            if (hit) {
                drawHit(cell);
                if (checkIfPlayerShipSunk(row, col)) {
                    drawSunkOnPlayer();
                    playerShipsAlive--;
                    if (playerShipsAlive == 0) {
                        showEndgameDialog(false);
                        return;
                    }
                }
            } else {
                drawMiss(cell);
            }

            saveEnemyShotsToFile();
            playerTurn = true;
            updateTurnLabel();
        } catch (Exception e) {
            System.err.println("Error en enemyTurn: " + e.getMessage());
            playerTurn = true;
            updateTurnLabel();
        }
    }

    /**
     * Rebuilds the grid if needed.
     */
    private void rebuildGridIfNeeded() {
        System.out.println("Reconstruyendo grilla...");
        cleanDynamicElements(gridBoard);
        if (Files.exists(Paths.get(PLAYER_SAVE_PATH))) {
            playerShips.clear();
            playerShips.addAll(loadBoardFromFile(PLAYER_SAVE_PATH, gridBoard, true));
        }
    }

    /**
     * Shows the endgame dialog.
     * @param playerWon Whether the player won
     */
    private void showEndgameDialog(boolean playerWon) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(playerWon ? "¡Victoria!" : "Derrota");

        Label msg = new Label(playerWon ? "¡Ganaste! :)" : "¡Perdiste! :(");
        msg.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        HBox botones = getHBox(dialog);
        botones.setAlignment(Pos.CENTER);

        VBox layout = new VBox(20, msg, botones);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);

        dialog.setScene(new Scene(layout));
        dialog.setOnHidden(e -> {
            deleteGameFiles();
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/miniproyecto3/start-view.fxml"));
                Parent root = loader.load();
                StartController startCtrl = loader.getController();
                startCtrl.disableContinueButton();

                Scene scene = new Scene(root);
                Stage primary = (Stage) gridBoard.getScene().getWindow();
                primary.setScene(scene);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        dialog.show();
    }

    /**
     * Deletes all game files.
     */
    private void deleteGameFiles() {
        String[] files = {"player_board.txt", "enemy_board.txt", "shots.txt", "enemy_shots.txt", "nickname.txt"};
        for (String file : files) {
            try {
                Files.deleteIfExists(Paths.get(file));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Creates an HBox with menu and exit buttons.
     * @param dialog The dialog to close
     * @return The created HBox
     */
    private HBox getHBox(Stage dialog) {
        Button btnMenu = new Button("Volver al menú");
        Button btnSalir = new Button("Salir del juego");

        btnMenu.setOnAction(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/miniproyecto3/start-view.fxml"));
                Parent root = loader.load();
                StartController startCtrl = loader.getController();
                startCtrl.disableContinueButton();

                Scene scene = new Scene(root);
                Stage primary = (Stage) gridBoard.getScene().getWindow();
                primary.setScene(scene);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            dialog.close();
        });

        btnSalir.setOnAction(e -> Platform.exit());

        HBox botones = new HBox(10, btnMenu, btnSalir);
        return botones;
    }

    /**
     * Saves AI shots to file.
     */
    private void saveEnemyShotsToFile() {
        try (BufferedWriter w = Files.newBufferedWriter(Paths.get(ENEMY_SHOTS_SAVE_PATH))) {
            for (String shot : firedByEnemy) {
                w.write(shot);
                w.newLine();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Loads AI shots from file.
     */
    private void loadEnemyShotsFromFile() {
        Path p = Paths.get(ENEMY_SHOTS_SAVE_PATH);
        if (!Files.exists(p)) return;
        try (BufferedReader r = Files.newBufferedReader(p)) {
            String line;
            while ((line = r.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length != 2) continue;
                int row = Integer.parseInt(parts[0]);
                int col = Integer.parseInt(parts[1]);
                String key = row + "," + col;
                firedByEnemy.add(key);
                StackPane cell = getCellPaneAt(gridBoard, row, col);
                boolean hit = checkPlayerShipHit(row, col);
                if (hit) {
                    drawHit(cell);
                } else {
                    drawMiss(cell);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Saves player shots to file.
     */
    private void saveShotsToFile() {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(SHOTS_SAVE_PATH))) {
            for (String shot : firedCells) {
                writer.write(shot);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads player shots from file.
     */
    private void loadShotsFromFile() {
        Path path = Paths.get(SHOTS_SAVE_PATH);
        if (!Files.exists(path)) return;

        try (BufferedReader br = Files.newBufferedReader(path)) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length != 2) continue;
                int row = Integer.parseInt(p[0]);
                int col = Integer.parseInt(p[1]);
                String key = row + "," + col;
                firedCells.add(key);

                Optional<SavedShip> hitShip = enemyShips.stream().filter(s -> {
                    int size = switch (s.getType()) {
                        case "fragata" -> 1;
                        case "submarino" -> 3;
                        case "destructor" -> 2;
                        case "portaviones" -> 4;
                        default -> 0;
                    };
                    for (int i = 0; i < size; i++) {
                        int r = s.getRow() + (s.isHorizontal() ? 0 : i);
                        int c = s.getCol() + (s.isHorizontal() ? i : 0);
                        if (r == row && c == col) return true;
                    }
                    return false;
                }).findFirst();

                if (hitShip.isEmpty()) {
                    drawMiss(getCellPaneAt(enemyBoard, row, col));
                } else {
                    String shipId = hitShip.get().getType() + "_" + hitShip.get().getRow() + "_" + hitShip.get().getCol();
                    int remaining = enemyRemainingParts.getOrDefault(shipId, -1);
                    if (remaining == -1) continue;
                    remaining--;
                    enemyRemainingParts.put(shipId, remaining);

                    if (remaining == 0) {
                        drawSunk(hitShip.get());
                        enemyShipsAlive--;
                    } else {
                        drawHit(getCellPaneAt(enemyBoard, row, col));
                    }
                }
            }
        } catch (IOException | NumberFormatException ex) {
            ex.printStackTrace();
            showFloatingMessage("Error al cargar los disparos");
        }
    }

    /**
     * Draws a miss marker on a cell.
     * @param cell The cell to mark
     */
    private void drawMiss(StackPane cell) {
        Label x = new Label("X");
        x.setStyle("-fx-text-fill: black; -fx-font-weight: bold;");
        cell.getChildren().add(x);
    }

    /**
     * Draws a hit marker on a cell.
     * @param cell The cell to mark
     */
    private void drawHit(StackPane cell) {
        Rectangle redOverlay = new Rectangle(30, 30, Color.RED);
        redOverlay.setOpacity(0.6);
        cell.getChildren().add(redOverlay);
    }

    /**
     * Draws a sunk ship on the enemy board.
     * @param ship The sunk ship to draw
     */
    private void drawSunk(SavedShip ship) {
        int size = switch (ship.getType()) {
            case "fragata"    -> 1;
            case "destructor" -> 2;
            case "submarino"  -> 3;
            case "portaviones"-> 4;
            default -> 0;
        };

        boolean horizontal = ship.isHorizontal();
        int startRow = ship.getRow();
        int startCol = ship.getCol();

        for (int i = 0; i < size; i++) {
            int r = startRow + (horizontal ? 0 : i);
            int c = startCol + (horizontal ? i : 0);
            StackPane cell = getCellPaneAt(enemyBoard, r, c);
            if (cell != null) {
                cell.getChildren().clear();
            }
        }

        String imgPath = "/com/example/miniproyecto3/assets/" + ship.getType() +
                (horizontal ? "derecha.png" : "abajo.png");
        ImageView shipImage = new ImageView(new Image(getClass().getResourceAsStream(imgPath)));
        shipImage.setFitWidth(horizontal ? 30 * size : 30);
        shipImage.setFitHeight(horizontal ? 30 : 30 * size);
        shipImage.setOpacity(0.8);

        enemyBoard.getChildren().add(shipImage);
        GridPane.setRowIndex(shipImage, startRow + 1);
        GridPane.setColumnIndex(shipImage, startCol + 1);
        if (horizontal) {
            GridPane.setColumnSpan(shipImage, size);
            GridPane.setRowSpan(shipImage, 1);
        } else {
            GridPane.setColumnSpan(shipImage, 1);
            GridPane.setRowSpan(shipImage, size);
        }

        GridPane.setHalignment(shipImage, HPos.CENTER);
        GridPane.setValignment(shipImage, VPos.CENTER);

        for (int i = 0; i < size; i++) {
            int r = startRow + (horizontal ? 0 : i);
            int c = startCol + (horizontal ? i : 0);
            StackPane cell = getCellPaneAt(enemyBoard, r, c);
            if (cell != null) {
                Rectangle grey = new Rectangle(30, 30, Color.GREY);
                grey.setOpacity(0.5);
                cell.getChildren().add(grey);
            }
        }
    }

    /**
     * Clears ship placement previews from the board.
     */
    private void clearPreviews() {
        for (Node node : gridBoard.getChildren()) {
            if (node instanceof StackPane stack) {
                stack.getChildren().removeIf(child ->
                        child instanceof Rectangle rect && "preview".equals(rect.getUserData())
                );
            }
        }
    }

    /**
     * Shows a preview of ship placement.
     * @param cell The cell to preview placement on
     */
    private void previewShipPlacement(StackPane cell) {
        clearPreviews();
        if (selectedShipType == null) return;
        Ship ship = createShipFromString(selectedShipType);
        if (ship == null) return;
        int size = ship.getSize();
        Integer rowIdx = GridPane.getRowIndex(cell);
        Integer colIdx = GridPane.getColumnIndex(cell);
        int row0 = rowIdx == null ? 0 : rowIdx - 1;
        int col0 = colIdx == null ? 0 : colIdx - 1;

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

    /**
     * Gets the image path for a ship type and direction.
     * @param type The ship type
     * @param direction The ship direction
     * @return The image path
     */
    public static String getImagePath(String type, Direction direction) {
        String direccion = switch (direction) {
            case RIGHT -> "derecha";
            case LEFT -> "izquierda";
            case UP -> "arriba";
            case DOWN -> "abajo";
        };

        return "/com/example/miniproyecto3/assets/" + type.toLowerCase() + direccion + ".png";
    }

    /**
     * Creates a ship object from a string type.
     * @param shipType The ship type
     * @return The created ship
     */
    private Ship createShipFromString(String shipType) {
        return switch (shipType) {
            case "fragata"     -> new Fragata();
            case "submarino"   -> new Submarino();
            case "destructor"  -> new Destructor();
            case "portaviones" -> new Portaviones();
            default             -> throw new IllegalArgumentException("Tipo de barco inválido: " + shipType);
        };
    }

    /**
     * Loads all ship types into the selection area.
     */
    private void loadShips() {
        addShipToSelection("fragata",     new Fragata());
        addShipToSelection("submarino",   new Submarino());
        addShipToSelection("destructor",  new Destructor());
        addShipToSelection("portaviones", new Portaviones());
    }

    /**
     * Adds a ship to the selection area.
     * @param type The ship type
     * @param ship The ship object
     */
    private void addShipToSelection(String type, Ship ship) {
        HBox shipBox = new HBox(5);
        shipBox.setUserData(type);

        Label countLabel = new Label(shipCounts.get(type).toString());
        countLabel.setUserData("label");
        shipBox.getChildren().add(countLabel);

        String imagePath = "/com/example/miniproyecto3/assets/" + type.toLowerCase() + "abajo.png";
        Image image = new Image(getClass().getResourceAsStream(imagePath));
        ImageView preview = new ImageView(image);
        preview.setFitWidth(30);
        preview.setFitHeight(30);
        preview.setUserData(type);

        preview.setOnMouseClicked(e -> {
            int currentCount = Integer.parseInt(countLabel.getText());
            if (currentCount <= 0) {
                showFloatingMessage("No quedan barcos de este tipo");
                e.consume();
                return;
            }

            if (selectedShipType != null && selectedShipType.equals(type)) {
                selectedShipType = null;
                preview.setStyle("");
            } else {
                for (Node node : shipSelectionArea.getChildren()) {
                    if (node instanceof HBox hbox) {
                        for (Node child : hbox.getChildren()) {
                            if (child instanceof ImageView iv) {
                                iv.setStyle("");
                            }
                        }
                    }
                }
                selectedShipType = type;
                preview.setStyle("-fx-effect: dropshadow(three-pass-box, deepskyblue, 10, 0, 0, 0);");
            }

            e.consume();
        });

        preview.setOnDragDetected(event -> {
            int currentCount = Integer.parseInt(countLabel.getText());
            if (currentCount <= 0) {
                event.consume();
                return;
            }

            activeCountLanbel = countLabel;
            Dragboard db = preview.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(type);
            db.setContent(content);

            WritableImage snapshot = new WritableImage(30, 30);
            preview.snapshot(null, snapshot);
            db.setDragView(snapshot);

            event.consume();
        });

        shipBox.getChildren().add(preview);
        shipSelectionArea.getChildren().add(shipBox);
    }

    /**
     * Gets a cell at specified coordinates.
     * @param row The row index
     * @param col The column index
     * @return The StackPane cell or null if not found
     */
    private StackPane getCellPaneAt(int row, int col) {
        for (Node n : gridBoard.getChildren()) {
            if (n instanceof StackPane) {
                int rowIndex = GridPane.getRowIndex(n) == null ? 0 : GridPane.getRowIndex(n);
                int colIndex = GridPane.getColumnIndex(n) == null ? 0 : GridPane.getColumnIndex(n);
                if (rowIndex == row + 1 && colIndex == col + 1) {
                    return (StackPane) n;
                }
            }
        }
        return null;
    }

    /**
     * Shows a floating message on the screen.
     * @param message The message to show
     */
    private void showFloatingMessage(String message) {
        textFloat.setText(message);
        textFloat.setOpacity(1);

        FadeTransition fade = new FadeTransition(Duration.seconds(3), textFloat);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.play();
    }

    /**
     * Updates the count label for a ship type.
     * @param type The ship type
     * @param newCount The new count
     */
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

    /**
     * Refreshes all ship counters in the selection area.
     */
    private void refreshAllCounters() {
        for (Node node : shipSelectionArea.getChildren()) {
            if (node instanceof HBox hbox) {
                String type = (String) hbox.getUserData();
                for (Node child : hbox.getChildren()) {
                    if (child instanceof Label lbl && "label".equals(lbl.getUserData())) {
                        lbl.setText(String.valueOf(shipCounts.get(type)));
                    }
                }
            }
        }
    }

    /**
     * Counts alive ships from a list.
     * @param ships The list of ships
     * @param remainingParts The map of remaining parts
     * @return The count of alive ships
     */
    private int countAliveShips(List<SavedShip> ships, Map<String, Integer> remainingParts) {
        int alive = 0;
        for (SavedShip s : ships) {
            String id = s.getType() + "_" + s.getRow() + "_" + s.getCol();
            if (remainingParts.getOrDefault(id, 0) > 0) {
                alive++;
            }
        }
        return alive;
    }
}