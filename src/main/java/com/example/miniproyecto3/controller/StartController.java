package com.example.miniproyecto3.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class StartController extends NavigationAdapter {
    @FXML
    private javafx.scene.control.Button btnNewGame;
    private static final String PLAYER_SAVE_PATH = "player_board.txt";
    private static final String ENEMY_SAVE_PATH  = "enemy_board.txt";

    //Este metodo se ejecuta cada vez que vuelves al menú.  Habilita “Nueva Partida” solamente
    // si EXISTEN los dos archivos .txt de guardado.
    @FXML
    private void initialize() {
        boolean saved = Files.exists(Paths.get(PLAYER_SAVE_PATH)) &&
                Files.exists(Paths.get(ENEMY_SAVE_PATH));
        btnNewGame.setDisable(!saved);          // habilita si hay partida guardada
    }
    @FXML
    private void onPlayClicked(ActionEvent event) {
        btnNewGame.setDisable(false);  //Habilita el botón al hacer clic en "Jugar"
        goTo("/com/example/miniproyecto3/game-view.fxml", (Node) event.getSource());
    }

    @FXML
    private void onInstructionsClicked(ActionEvent event) {
        goTo("/com/example/miniproyecto3/instructions-view.fxml", (Node) event.getSource());
    }

    @FXML
    private void handleContinue(ActionEvent event) {
        Path enemyPath = Paths.get("enemy_board.txt");
        Path playerPath = Paths.get("player_board.txt");

        if (Files.exists(enemyPath) && Files.exists(playerPath)) {
            goTo("/com/example/miniproyecto3/game-view.fxml", (Node) event.getSource());
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Saved Game");
            alert.setHeaderText(null);
            alert.setContentText("No saved game found.");
            alert.showAndWait();
        }
    }


    @FXML
    private void onExitClicked(ActionEvent event) {
        System.out.println("Saliendo");
        System.exit(0);
    }

    //Nuevo Metodo
    @FXML
    private void handleNewGame(ActionEvent event) {

        // 1) borrar archivos guardados, si existen
        try {
            Files.deleteIfExists(Paths.get(PLAYER_SAVE_PATH));
            Files.deleteIfExists(Paths.get(ENEMY_SAVE_PATH));
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Error deleting save files").showAndWait();
            ex.printStackTrace();
            return;
        }

        // 2) generar un NUEVO tablero enemigo y guardarlo
        EnemyController.generateAndSaveEnemyBoard();

        // 3) ir a la vista del juego; GameController se encargará de inicializar todo
        goTo("/com/example/miniproyecto3/game-view.fxml", (Node) event.getSource());
    }
}


