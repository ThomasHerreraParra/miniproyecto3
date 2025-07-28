package com.example.miniproyecto3.controller;

import com.example.miniproyecto3.storage.SavedShip;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
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
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.geometry.Insets;

import javafx.scene.input.MouseButton;

public class GameController extends NavigationAdapter {

    @FXML private GridPane gridBoard;
    @FXML private GridPane enemyBoard;
    @FXML private VBox shipSelectionArea;
    @FXML private Label textFloat; // Referencia al mensaje flotante en la interfaz
    @FXML private Label lblWelcome;
    private boolean playerTurn = false;
    @FXML private Label lblTurn;
    // Para los disparos de la IA
    private final Set<String> firedByEnemy = new HashSet<>();
// Contador de barcos del jugador (inicialízalo igual que enemyShipsAlive)
    private int playerShipsAlive;

    private final Map<String, Integer> playerRemainingParts = new HashMap<>();
    private final Map<String, Integer> shipCounts = new HashMap<>(); // Stock de barcos por tipo
    private Label activeCountLanbel = null;     //Variable que almacena el contador del barco actualmente siendo arrastrado
    private static final String SHOTS_SAVE_PATH = "shots.txt"; //Archivo plano para guardar los disparos
    private static final String ENEMY_SHOTS_SAVE_PATH = "enemy_shots.txt";  //Disparos hechos hacia el enemigo
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
    private boolean allShipsPlaced() {
        return shipCounts.values().stream().allMatch(count -> count == 0);
    }

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
        playerRemainingParts.clear();
        enemyShipsAlive = 0;
        playerShipsAlive = 0;
        // Inicializar contadores
        shipCounts.put("fragata", 4);
        shipCounts.put("destructor", 3);
        shipCounts.put("submarino", 2);
        shipCounts.put("portaviones", 1);
        createGrid(gridBoard, false);  // tablero del jugador
        createGrid(enemyBoard, true);  // tablero del enemigo
        loadShips();

        //Si el archivo guardado existe entonces carga los tableros cargados
        Path playerFile = Paths.get(PLAYER_SAVE_PATH);
        Path enemyFile  = Paths.get(ENEMY_SAVE_PATH);

        /* 1️⃣ Pintamos SOLO el tablero del jugador */
        if (Files.exists(playerFile)) {
            //Pintar, y ademas obtenemos la lista para ajustar contadores
            List<SavedShip> alreadyPlaced = loadBoardFromFile(PLAYER_SAVE_PATH, gridBoard, true);   // paint = true
            playerShips.addAll(alreadyPlaced);

            System.out.println("Barcos del jugador cargados: " + playerShips.size());
            //Restar del stock todos los barcos que ya estaban colocados
            for (SavedShip s : alreadyPlaced) {
                shipCounts.put(s.getType(), shipCounts.get(s.getType()) - 1);
                System.out.println("Partes vivas del jugador: " + playerRemainingParts.size());

                boatSize(s, playerRemainingParts);
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
                boatSize(s, enemyRemainingParts);
            }
            enemyShipsAlive = enemyRemainingParts.size();
        }
        // Cargamos disparos previos
        loadShotsFromFile();
        loadEnemyShotsFromFile();


// ⬇️ AGREGA ESTO AQUÍ
        enemyShipsAlive = countAliveShips(enemyShips, enemyRemainingParts);
        if (playerShips.isEmpty() || playerRemainingParts.isEmpty()) {
            System.out.println("⚠️ No hay barcos del jugador cargados o no se cargaron correctamente");
            return; // Evita que se muestre "perdiste" por error
        }
        playerShipsAlive = countAliveShips(playerShips, playerRemainingParts); // o usa tu mapa playerRemainingParts
        if (enemyShipsAlive == 0) {
            showEndgameDialog(true);
        } else if (playerShipsAlive == 0) {
            showEndgameDialog(false);
        }

        // Si venimos de una partida guardada ya lista...
        if (allShipsPlaced()) {
            playerTurn = true;
            updateTurnLabel();
            shipSelectionArea.setDisable(true);
        }
    }

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

    private void updateTurnLabel() {
        String nick = "capitán";
        try {
            nick = "capitán " + Files.readString(Paths.get("nickname.txt")).trim();
        } catch (IOException e) { /* ignorar */ }
        lblTurn.setText(playerTurn
                ? "Es tu turno de disparar, " + nick
                : "Turno de la máquina...");
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

    private void createGrid(GridPane targetGrid, boolean isEnemy) {
        int numRows = 10;
        int numCols = 10;
        int cellSize = 50;

        // Etiquetas de columna (1–10)
        for (int col = 0; col < numCols; col++) {
            Label label = new Label(String.valueOf(col + 1));
            label.setMinSize(cellSize, cellSize);
            label.setAlignment(Pos.CENTER);
            label.setStyle("-fx-font-weight: bold;");
            targetGrid.add(label, col + 1, 0);
        }

        // Etiquetas de fila (A–J)
        for (int row = 0; row < numRows; row++) {
            Label label = new Label(String.valueOf((char) ('A' + row)));
            label.setMinSize(cellSize, cellSize);
            label.setAlignment(Pos.CENTER);
            label.setStyle("-fx-font-weight: bold;");
            targetGrid.add(label, 0, row + 1);
        }

        // Celdas del tablero
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                StackPane cell = new StackPane();
                cell.setPrefSize(cellSize, cellSize);

                Rectangle background = new Rectangle(cellSize, cellSize);
                background.setFill(Color.web("#e6f7ff"));
                background.setStroke(Color.LIGHTGRAY);
                cell.getChildren().add(background);

                if (isEnemy) {
                    // Guardar coordenadas para el disparo
                    cell.getProperties().put("row", row);
                    cell.getProperties().put("col", col);
                    cell.setOnMouseClicked(e -> handleShot(cell));
                } else {
                    // Comportamiento drag and drop
                    setupPlayerCellBehavior(cell);
                }

                targetGrid.add(cell, col + 1, row + 1);
            }
        }
    }

    private void setupPlayerCellBehavior(StackPane cell) {
        int numRows = 10, numCols = 10;
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
                                    return !"preview".equals(data);
                                }
                                return true;
                            });
                    if (occupied) {
                        spaceAvailable = false;
                        break;
                    }
                }

                if (spaceAvailable) {
                    placeShipOnBoard(ship, size, row0, col0);

                    if (activeCountLanbel != null) {
                        int currentCount = Integer.parseInt(activeCountLanbel.getText());
                        activeCountLanbel.setText(String.valueOf(currentCount - 1));
                        shipCounts.put(type, currentCount - 1);
                    }

                    SavedShip ss = toSavedShip(type, row0, col0, size, currentDirection);
                    playerShips.add(ss);
                    savePlayerBoard();

                    // Después de playerShips.add(ss); y savePlayerBoard();
                    if (allShipsPlaced()) {
                        // Deshabilita el área de selección
                        shipSelectionArea.setDisable(true);

                        // Inicializa la vida de tus barcos y arranca el turno
                        playerShipsAlive = playerShips.size();
                        playerTurn = true;
                        updateTurnLabel();
                    }
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
                boolean shipsLefts = shipCounts.values().stream().anyMatch(c -> c > 0);
                if (shipsLefts) {
                    showFloatingMessage("Selecciona un barco antes de colocarlo");
                }
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
                placeShipOnBoard(ship, size, row0, col0);

                shipCounts.put(selectedShipType, currentCount - 1);
                updateLabelCount(selectedShipType, currentCount - 1);

                //Guardar en playerShips y en el archivo
                SavedShip ss = toSavedShip(selectedShipType, row0, col0, size, currentDirection);
                playerShips.add(ss);
                savePlayerBoard();

                // ¡Lanzamos la partida si fue el último barco!
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

    }

    private void placeShipOnBoard(Ship ship, int size, int row0, int col0) {
        for (int i = 0; i < size; i++) {
            int r = row0, c = col0;
            switch (currentDirection) {
                case RIGHT -> c += i;
                case LEFT  -> c -= i;
                case DOWN  -> r += i;
                case UP    -> r -= i;
            }

            StackPane target = getCellPaneAt(r, c);
            if (target == null) {
                throw new NullPointerException("Intento de colocar parte de un barco fuera del tablero.");
            }
            Rectangle part = new Rectangle(30, 30, ship.getParts()[i].getFill());
            part.setStroke(Color.BLACK);
            part.setUserData("real");
            target.getChildren().add(part);
        }
    }

//-----METODOS DE CHEQUEO DE IMPACTOS Y HUNDIDOS EN EL TABLERO DEL JUGADOR

    /** Devuelve true si la IA ha acertado en (row,col) sobre el jugador */
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
                    // Reducir partes vivas del barco
                    String shipId = s.getType() + "_" + s.getRow() + "_" + s.getCol();
                    int rem = playerRemainingParts.getOrDefault(shipId, size) - 1;
                    playerRemainingParts.put(shipId, rem);
                    return true;
                }
            }
        }
        return false;
    }

    /** Devuelve true si tras este impacto acabamos de hundir el barco al que pertenece (row,col) */
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
    //-------------------------------

    //Dibujar barco hundido en el tablero del jugador
    /** Dibuja el barco hundido (todas sus casillas) sobre gridBoard */
    private void drawSunkOnPlayer() {
        // Encuentra la SavedShip cuyo id coincide con el que acabamos de hundir
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
    //-------------

    //Encargado de pum pum
    private void handleShot(StackPane cell) {
        // ❌ Impedir disparos si no se han colocado todos los barcos
        if (!allShipsPlaced()) {
            try {
                String nick = Files.readString(Paths.get("nickname.txt")).trim();
                showFloatingMessage("Primero ponga todos los barcos, capitan " + nick);
            } catch (IOException e) {
                showFloatingMessage("Primero ponga todos los barcos, capitán");
            }
            return;
        }
        if (!playerTurn) return; //Bloqueamos si no es turno

        if (!cell.getProperties().containsKey("row") || !cell.getProperties().containsKey("col")) {
            throw new IllegalStateException("La celda no tiene coordenadas asignadas");
        }

        // Coordenadas 0-based
        int row = (int) cell.getProperties().get("row");
        int col = (int) cell.getProperties().get("col");
        String key = row + "," + col;

        // Ya disparado
        if (firedCells.contains(key)) return;
        firedCells.add(key);

        /*¿Hay un barco en esa casilla? */
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
            /* --------------- AGUA --------------- */
            drawMiss(cell);
        } else {
            // TOCADO / HUNDIDO
            SavedShip ship = hitShip.get();
            String shipId = ship.getType() + "_" + ship.getRow() + "_" + ship.getCol();
            int partsLeft = enemyRemainingParts.get(shipId) - 1;
            enemyRemainingParts.put(shipId, partsLeft);

            if (partsLeft == 0) {
                drawSunk(ship);
                enemyShipsAlive--;
                if (enemyShipsAlive == 0) {
                    showEndgameDialog(true); //GANASTE
                    return;  // fin de juego
                }
            } else {
                drawHit(cell);
            }
        }
        saveShotsToFile();

        // ————————— Cambio de turno SIEMPRE —————————
        playerTurn = false;
        updateTurnLabel();
        PauseTransition pause = new PauseTransition(Duration.seconds(1));
        pause.setOnFinished(e -> enemyTurn());
        pause.play();
    }

    //Metodo de disparos de la maquina
    private void enemyTurn(){
        // Elegir aleatoriamente una celda válida del tablero del jugador
        int row, col;
        String key;
        do {
            row = new Random().nextInt(10);
            col = new Random().nextInt(10);
            key = row + "," + col;
        } while (firedByEnemy.contains(key));  // firedByEnemy: Set<String> que guardas por separado
        firedByEnemy.add(key);

        // Obtener la StackPane correspondiente en gridBoard
        StackPane cell = getCellPaneAt(gridBoard, row, col);
        boolean hit = checkPlayerShipHit(row, col); // implementa lógica similar a handleShot

        if (hit) {
            drawHit(cell);     // puedes crear versiones para el jugador
            if (checkIfPlayerShipSunk(row, col)) {
                drawSunkOnPlayer();
                playerShipsAlive--;
                if (playerShipsAlive == 0) {
                    showEndgameDialog(false); //PERDISTE
                    return;
                }
            }
        } else {
            drawMiss(cell);
        }

        saveEnemyShotsToFile();
        // Devolver el turno al jugador
        playerTurn = true;
        updateTurnLabel();
    }

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
//SISTEMA DE GUARDADO DE DISPAROS Y CARGADO DE LA IA.

    /** Guarda en disco los disparos de la IA en firedByEnemy */
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

    /** Carga los disparos de la IA al arrancar la pantalla */
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
                // Dibuja el resultado en gridBoard
                StackPane cell = getCellPaneAt(gridBoard, row, col);
                boolean hit = checkPlayerShipHit(row, col);
                if (hit) {
                    // si ya estaba hundido no queremos dibujar doble,
                    // pero para simplificar, usamos drawHit:
                    drawHit(cell);
                } else {
                    drawMiss(cell);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

//------------------

    //Guardamos los disparos (ESTO ES DEL JUGADOR)
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
        } catch (IOException | NumberFormatException ex) {
            ex.printStackTrace();
            showFloatingMessage("Error al cargar los disparos");
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
            case "submarino" -> 3;
            case "destructor"-> 2;
            case "portaviones"->4;
            default          -> 0;
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
            default             -> throw new IllegalArgumentException("Tipo de barco inválido: " + shipType); // ← no marcada;
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

        Rectangle clone = new Rectangle(30, 30, ship.getParts()[0].getFill());
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
            Rectangle preview = new Rectangle(30, 30, clone.getFill());
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

    //METODO AUXILIAR
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