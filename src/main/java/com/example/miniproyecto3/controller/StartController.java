package com.example.miniproyecto3.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputDialog;
import java.util.Optional;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class StartController extends NavigationAdapter {

    @FXML private Button btnPlay;
    @FXML private Button btnContinue;
    @FXML private Button btnNewGame;
    @FXML private Button btnInstructions;
    private static final String NICKNAME_PATH = "nickname.txt";
    private static final String PLAYER_SAVE_PATH = "player_board.txt";
    private static final String ENEMY_SAVE_PATH  = "enemy_board.txt";
    private static final String SHOTS_SAVE_PATH = "shots.txt";

    /** Indica si ya se pulsó “Jugar” ó “Nueva partida” en esta sesión */
    private static boolean hasOngoingGame = false;
    //Metodo auxiliar para solicitar y guardar el nickname
    private Optional<String> promptAndSaveNickname() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Ingrese Nickname");
        dialog.setHeaderText(null);
        dialog.setContentText("Por favor ingrese su nickname:");

        Optional<String> result = dialog.showAndWait()
                .map(String::trim)
                .filter(nick -> !nick.isBlank());

        result.ifPresent(nick -> {
            try {
                Files.writeString(Paths.get(NICKNAME_PATH), nick);
            } catch (IOException e) {
                new Alert(Alert.AlertType.ERROR,
                        "No se pudo guardar el nickname").showAndWait();
            }
        });

        return result;
    }


    @FXML
    private void initialize() {
        if (!hasOngoingGame) {
            // Si no hay partida en memoria, borra archivos viejos al iniciar
            try {
                Files.deleteIfExists(Paths.get(PLAYER_SAVE_PATH));
                Files.deleteIfExists(Paths.get(ENEMY_SAVE_PATH));
                Files.deleteIfExists(Paths.get(SHOTS_SAVE_PATH));
            } catch (IOException ex) {
                new Alert(Alert.AlertType.ERROR, "Error limpiando datos antiguos").showAndWait();
                ex.printStackTrace();
            }
        }

        boolean savedOnDisk = Files.exists(Paths.get(PLAYER_SAVE_PATH))
                && Files.exists(Paths.get(ENEMY_SAVE_PATH));

        btnContinue.setDisable(!(savedOnDisk || hasOngoingGame));
        btnNewGame.setDisable(!(savedOnDisk || hasOngoingGame));
        btnPlay.setDisable(hasOngoingGame);
        btnInstructions.setDisable(false);
    }

    @FXML
    private void onPlayClicked(ActionEvent event) {
        if (promptAndSaveNickname().isEmpty()) return;
        //borrar cualquier archivo viejo (tableros o disparos)
        try {
            Files.deleteIfExists(Paths.get(PLAYER_SAVE_PATH));
            Files.deleteIfExists(Paths.get(ENEMY_SAVE_PATH));
            Files.deleteIfExists(Paths.get(SHOTS_SAVE_PATH));
        } catch (IOException ex) {
            new Alert(Alert.AlertType.ERROR, "Error clearing previous game data").showAndWait();
            ex.printStackTrace();
            return;
        }

        //generar un nuevo tablero enemigo
        EnemyController.generateAndSaveEnemyBoard();


        //Marcamos que hay partida en memoria y vamos a la vista
        hasOngoingGame = true;
        goTo("/com/example/miniproyecto3/game-view.fxml", (Node) event.getSource());
    }

    @FXML
    private void handleContinue(ActionEvent event) {
        if (hasOngoingGame) {
            // vuelve a la partida en memoria
            goTo("/com/example/miniproyecto3/game-view.fxml", (Node) event.getSource());
            return;
        }
        // si no hay en memoria, intenta cargar desde disco
        Path p1 = Paths.get(PLAYER_SAVE_PATH);
        Path p2 = Paths.get(ENEMY_SAVE_PATH);
        if (Files.exists(p1) && Files.exists(p2)) {
            goTo("/com/example/miniproyecto3/game-view.fxml", (Node) event.getSource());
        } else {
            new Alert(Alert.AlertType.WARNING, "No saved game found.").showAndWait();
        }
    }

    @FXML
    private void handleNewGame(ActionEvent event) {
        if (promptAndSaveNickname().isEmpty()) return;
        // eliminar guardados en disco
        try {
            Files.deleteIfExists(Paths.get(PLAYER_SAVE_PATH));
            Files.deleteIfExists(Paths.get(ENEMY_SAVE_PATH));
            Files.deleteIfExists(Paths.get(SHOTS_SAVE_PATH));
        } catch (IOException ex) {
            new Alert(Alert.AlertType.ERROR, "Error deleting save files").showAndWait();
            ex.printStackTrace();
            return;
        }
        // generar nuevo tablero enemigo
        EnemyController.generateAndSaveEnemyBoard();
        hasOngoingGame = true;
        goTo("/com/example/miniproyecto3/game-view.fxml", (Node) event.getSource());
    }

    @FXML
    private void onInstructionsClicked(ActionEvent event) {
        goTo("/com/example/miniproyecto3/instructions-view.fxml", (Node) event.getSource());
    }

    @FXML
    private void onExitClicked(ActionEvent event) {
        System.exit(0);
    }
}
