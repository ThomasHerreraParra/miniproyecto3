package com.example.miniproyecto3.controller;

import com.example.miniproyecto3.storage.SavedShip;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
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

import javafx.scene.input.MouseButton;

public class GameController extends NavigationAdapter {

    @FXML private GridPane gridBoard;
    @FXML private GridPane enemyBoard;
    @FXML private VBox shipSelectionArea;
    @FXML private Label textFloat; // Referencia al mensaje flotante en la interfaz
    @FXML private Label lblWelcome;
    private final Map<String, Integer> shipCounts = new HashMap<>(); // Stock de barcos por tipo
    private Label activeCountLanbel = null;     //Variable que almacena el contador del barco actualmente siendo arrastrado
    private static final String SHOTS_SAVE_PATH = "shots.txt"; //Archivo plano para guardar los disparos
    private static final String PLAYER_SAVE_PATH = "player_board.txt"; //Costantes para guardar archivos
    private static final String ENEMY_SAVE_PATH  = "enemy_board.txt";
    private final List<SavedShip> playerShips = new ArrayList<>(); //Lista donde se ira almacenando los barcos del jugador
    private final List<SavedShip> enemyShips  = new ArrayList<>();
    private String selectedShipType = null; //Variable para manejar la seleccion por click
    private enum Direction {RIGHT, DOWN, LEFT, UP}  //Variable para la orientacion
    private Direction currentDirection = Direction.RIGHT; //Direccion por defecto del barco
    //Disparos
    private final Map<String, Integer> enemyRemainingParts = new HashMap<>();  // shipId → partes vivas
    private final Set<String>           firedCells         = new HashSet<>(); // "r,c" ya disparado
    private int                         enemyShipsAlive    = 0;


    @FXML
    public void initialize() {
        //Mostrar nickname
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
        enemyShipsAlive = 0;
        // Inicializar contadores
        shipCounts.put("fragata", 4);
        shipCounts.put("submarino", 2);
        shipCounts.put("destructor", 3);
        shipCounts.put("portaviones", 1);
        createGrid();
        createGridEnemy();
        loadShips();

        //Si el archivo guardado existe entonces carga los tableros cargados
        Path playerFile = Paths.get(PLAYER_SAVE_PATH);
        Path enemyFile  = Paths.get(ENEMY_SAVE_PATH);

        /* 1️⃣ Pintamos SOLO el tablero del jugador */
        if (Files.exists(playerFile)) {
            //Pintar, y ademas obtenemos la lista para ajustar contadores
            List<SavedShip> alreadyPlaced =
                    loadBoardFromFile(PLAYER_SAVE_PATH, gridBoard, true);   // paint = true
            playerShips.addAll(alreadyPlaced);

            //Restar del stock todos los barcos que ya estaban colocados
            for (SavedShip s : alreadyPlaced) {
                shipCounts.put(s.getType(), shipCounts.get(s.getType()) - 1);
            }
            refreshAllCounters();

            //Deshabilitamos seleccion si ya no queda ninguno
            boolean allPlaced = shipCounts.values().stream().allMatch(v -> v == 0);
            shipSelectionArea.setDisable(allPlaced);
        }

        /*Cargamos PERO NO pintamos el tablero enemigo */
        if (Files.exists(enemyFile)) {
            enemyShips.addAll(loadBoardFromFile(ENEMY_SAVE_PATH, null, false)); // paint=false
            //Inicializar cotador de partes vivas
            for (SavedShip s : enemyShips) {
                int size = switch (s.getType()) {
                    case "fragata"   -> 1;
                    case "submarino" -> 2;
                    case "destructor"-> 3;
                    case "portaviones"->4;
                    default          -> 1;
                };
                String shipId = s.getType() + "_" + s.getRow() + "_" + s.getCol(); // id único
                enemyRemainingParts.put(shipId, size);
            }
            enemyShipsAlive = enemyRemainingParts.size();
        }
        //Cargamos disparos previos
        loadShotsFromFile();
    }

    public void handleBack(ActionEvent event) {goTo("/com/example/miniproyecto3/start-view.fxml", (Node) event.getSource());}
    public void handlenemy(ActionEvent event) {goTo("/com/example/miniproyecto3/enemy-view.fxml", (Node) event.getSource());}


    // Nuevo metodo loadBoardFromFile con flag paint
    private List<SavedShip> loadBoardFromFile(String path, GridPane target, boolean paint) {
        List<SavedShip> ships = new ArrayList<>();

        try (BufferedReader br = Files.newBufferedReader(Paths.get(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length != 4) continue;

                String  type   = p[0];
                int     row    = Integer.parseInt(p[1]);
                int     col    = Integer.parseInt(p[2]);
                boolean horiz  = "H".equalsIgnoreCase(p[3]);

                /* Guardamos en memoria */
                ships.add(new SavedShip(type, row, col, horiz));

                /* Dibujamos solo si paint = true */
                if (!paint || target == null) continue;

                Ship model = createShipFromString(type);
                if (model == null) continue;

                int size  = model.getSize();
                Color color = (Color) model.getParts()[0].getFill();

                for (int i = 0; i < size; i++) {
                    int r = row + (horiz ? 0 : i);
                    int c = col + (horiz ? i : 0);

                    StackPane cell = getCellPaneAt(target, r, c);
                    if (cell == null) continue;

                    Rectangle part = new Rectangle(30,30,color);
                    part.setStroke(Color.BLACK);
                    part.setUserData("real");
                    cell.getChildren().add(part);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Error loading board from " + path).showAndWait();
        }
        return ships;
    }



    //Metodo para guardar el tablero del jugador
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

    //Ayudante del metodo de arrriba
    private SavedShip toSavedShip(String type, int rowClick, int colClick, int size, Direction dir) {
        boolean horizontal = (dir == Direction.RIGHT || dir == Direction.LEFT);
        int startRow = (dir == Direction.UP)    ? rowClick - size + 1 : rowClick;
        int startCol = (dir == Direction.LEFT)  ? colClick - size + 1 : colClick;
        return new SavedShip(type, startRow, startCol, horizontal);
    }


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


    private void createGrid() {
        int numRows = 10;
        int numCols = 10;
        int cellSize = 50;

        // Etiquetas de columna (1–10)
        for (int col = 0; col < numCols; col++) {
            Label label = new Label(String.valueOf(col + 1));
            label.setMinSize(cellSize, cellSize);
            label.setAlignment(Pos.CENTER);
            label.setStyle("-fx-font-weight: bold;");
            gridBoard.add(label, col + 1, 0);
        }

        // Etiquetas de fila (A–J)
        for (int row = 0; row < numRows; row++) {
            Label label = new Label(String.valueOf((char) ('A' + row)));
            label.setMinSize(cellSize, cellSize);
            label.setAlignment(Pos.CENTER);
            label.setStyle("-fx-font-weight: bold;");
            gridBoard.add(label, 0, row + 1);
        }

        // Crear celdas del tablero
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                StackPane cell = new StackPane();
                cell.setPrefSize(cellSize, cellSize);
                Rectangle background = new Rectangle(cellSize, cellSize);
                background.setFill(Color.web("#e6f7ff"));
                background.setStroke(Color.LIGHTGRAY);
                cell.getChildren().add(background);

                // Drag over
                cell.setOnDragOver(e -> {
                    if (e.getGestureSource() != cell && e.getDragboard().hasString()) {
                        e.acceptTransferModes(TransferMode.MOVE);
                    }
                    e.consume();
                });

                // Drag dropped
                cell.setOnDragDropped(e -> {
                    Dragboard db = e.getDragboard();
                    boolean success = false;
                    if (db.hasString()) {
                        String type = db.getString();
                        Ship ship = createShipFromString(type);
                        int size = ship.getSize();

                        Integer rowIdx = GridPane.getRowIndex(cell);
                        Integer colIdx = GridPane.getColumnIndex(cell);
                        int row0 = rowIdx == null ? 0 : rowIdx - 1;
                        int col0 = colIdx == null ? 0 : colIdx - 1;

                        boolean spaceAvailable = true;

                        for (int i = 0; i < size; i++) {
                            int r = row0, c = col0;
                            switch (currentDirection) {
                                case RIGHT -> c += i;
                                case LEFT  -> c -= i;
                                case DOWN  -> r += i;
                                case UP    -> r -= i;
                            }

                            if (r < 0 || r >= numRows || c < 0 || c >= numCols) {
                                spaceAvailable = false;
                                break;
                            }

                            StackPane target = getCellPaneAt(r, c);
                            boolean occupied = target.getChildren().stream()
                                    .anyMatch(n -> {
                                        if (n instanceof Rectangle rect) {
                                            Object data = rect.getUserData();
                                            return data == null || !"preview".equals(data);
                                        }
                                        return true;
                                    });
                            if (occupied) {
                                spaceAvailable = false;
                                break;
                            }
                        }

                        if (spaceAvailable) {
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

                            if (activeCountLanbel != null) {
                                int currentCount = Integer.parseInt(activeCountLanbel.getText());
                                activeCountLanbel.setText(String.valueOf(currentCount - 1));
                                shipCounts.put(type, currentCount - 1);
                            }

                            SavedShip ss = toSavedShip(type, row0, col0, size, currentDirection);
                            playerShips.add(ss);
                            savePlayerBoard();
                            success = true;
                        } else {
                            showFloatingMessage("Inválido: El barco se sale del tablero o el espacio está ocupado");
                        }
                    }
                    e.setDropCompleted(success);
                    e.consume();
                });

                // Click izquierdo y derecho
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
                            canPlace = false; break;
                        }

                        StackPane target = getCellPaneAt(r, c);
                        if (target == null) {canPlace = false;break;}

                        boolean ocupado = target.getChildren().stream().anyMatch(n -> {
                                    if (n instanceof Rectangle rect) {
                                        Object data = rect.getUserData();
                                        return "real".equals(data);
                                    }
                                    return true;
                                });
                        if (ocupado) {canPlace = false;break;}
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

                        if (currentCount > 0) {
                            shipCounts.put(selectedShipType, currentCount - 1);
                            updateLabelCount(selectedShipType, currentCount - 1);
                        }

                        //Guardar en playerShips y en el archivo
                        SavedShip ss = toSavedShip(selectedShipType, row0, col0, size, currentDirection);
                        playerShips.add(ss);
                        savePlayerBoard();
                    } else {
                        showFloatingMessage("Inválido: El barco se sale del tablero o el espacio está ocupado");
                    }
                });

                // Sombra
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

                gridBoard.add(cell, col + 1, row + 1);
            }
        }
    }

    private void createGridEnemy(){
        int numRows = 10;
        int numCols = 10;
        int cellSize = 50;

        // Etiquetas de columna (1–10)
        for (int col = 0; col < numCols; col++) {
            Label label = new Label(String.valueOf(col + 1));
            label.setMinSize(cellSize, cellSize);
            label.setAlignment(Pos.CENTER);
            label.setStyle("-fx-font-weight: bold;");
            enemyBoard.add(label, col + 1, 0);
        }

        // Etiquetas de fila (A–J)
        for (int row = 0; row < numRows; row++) {
            Label label = new Label(String.valueOf((char) ('A' + row)));
            label.setMinSize(cellSize, cellSize);
            label.setAlignment(Pos.CENTER);
            label.setStyle("-fx-font-weight: bold;");
            enemyBoard.add(label, 0, row + 1);
        }

        // Crear celdas del tablero
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                StackPane cell = new StackPane();
                cell.setPrefSize(cellSize, cellSize);
                Rectangle background = new Rectangle(cellSize, cellSize);
                background.setFill(Color.web("#e6f7ff"));
                background.setStroke(Color.LIGHTGRAY);
                cell.getChildren().add(background);
                enemyBoard.add(cell, col + 1, row + 1);
                cell.setOnMouseClicked(e -> handleShot(cell));

            }
        }
    }

    //Encargado de pum pum
    private void handleShot(StackPane cell) {
        // Coordenadas 0-based
        int row = GridPane.getRowIndex(cell) - 1;
        int col = GridPane.getColumnIndex(cell) - 1;
        String key = row + "," + col;

        // Ya disparado
        if (firedCells.contains(key)) return;
        firedCells.add(key);

        /*¿Hay un barco en esa casilla? */
        Optional<SavedShip> hitShip = enemyShips.stream().filter(s -> {
            int size = switch (s.getType()) {
                case "fragata"   -> 1;
                case "submarino" -> 2;
                case "destructor"-> 3;
                case "portaviones"->4;
                default          -> 1;
            };
            for (int i = 0; i < size; i++) {
                int r = s.getRow() + (s.isHorizontal() ? 0 : i);
                int c = s.getCol() + (s.isHorizontal() ? i : 0);
                if (r == row && c == col) return true;
            }
            return false;
        }).findFirst();

        if (hitShip.isEmpty()) {
            /* --------------- AGUA --------------- */
            drawMiss(cell);
            saveShotsToFile(); //Guardamos aunque sea agua
            // aquí pasarías turno al oponente
            return;
        }

        /* --------------- TOCADO / HUNDIDO --------------- */
        SavedShip ship = hitShip.get();
        String shipId = ship.getType() + "_" + ship.getRow() + "_" + ship.getCol();
        int partsLeft = enemyRemainingParts.get(shipId) - 1;
        enemyRemainingParts.put(shipId, partsLeft);

        if (partsLeft == 0) {
            // HUNDIDO
            drawSunk(ship);
            enemyShipsAlive--;
            if (enemyShipsAlive == 0) {
                showFloatingMessage("¡Has ganado!");
            }
            //jugador puede volver a disparar
        } else {
            //TOCADO
            drawHit(cell);
            // jugador puede volver a disparar
        }
        saveShotsToFile();
    }

    //Guardamos los disparos
    private void saveShotsToFile() {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(SHOTS_SAVE_PATH))) {
            for (String shot : firedCells) {
                writer.write(shot);  // Formato: "row,col"
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Cargamos los disparos guardados
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

                // Dibujar efecto del disparo según si fue acierto o agua
                Optional<SavedShip> hitShip = enemyShips.stream().filter(s -> {
                    int size = switch (s.getType()) {
                        case "fragata" -> 1;
                        case "submarino" -> 2;
                        case "destructor" -> 3;
                        case "portaviones" -> 4;
                        default -> 1;
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
                    if (remaining == -1) continue; // no control
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
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }



    //Funciones unicas de dibujo (temporales, se tienen que reemplazar por imagenes ilustrativas de cada una)-----------------------
    private void drawMiss(StackPane cell) {
        Label x = new Label("X");
        x.setStyle("-fx-text-fill: black; -fx-font-weight: bold;");
        cell.getChildren().add(x);
    }

    private void drawHit(StackPane cell) {
        Rectangle r = new Rectangle(30, 30, Color.RED);
        r.setOpacity(0.7);
        cell.getChildren().add(r);
    }

    private void drawSunk(SavedShip s) {
        int size = switch (s.getType()) {
            case "fragata"   -> 1;
            case "submarino" -> 2;
            case "destructor"-> 3;
            case "portaviones"->4;
            default          -> 1;
        };
        for (int i = 0; i < size; i++) {
            int r = s.getRow() + (s.isHorizontal() ? 0 : i);
            int c = s.getCol() + (s.isHorizontal() ? i : 0);
            StackPane cell = getCellPaneAt(enemyBoard, r, c);
            Rectangle part = new Rectangle(30, 30, Color.GREY);
            part.setOpacity(0.8);
            cell.getChildren().add(part);
        }
    }
//-----------------------------------------------------------------------------------

    // Eliminar sombras previas
    private void clearPreviews() {
        for (Node node : gridBoard.getChildren()) {
            if (node instanceof StackPane stack) {
                stack.getChildren().removeIf(child ->
                        child instanceof Rectangle rect && "preview".equals(rect.getUserData())
                );
            }
        }
    }


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
                if (rowIndex == row + 1 && colIndex == col + 1) {
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

    // --- NEW ---
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
}