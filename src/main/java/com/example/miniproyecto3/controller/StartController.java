/**
 * Warship Dominions - Naval Battle Game
 *
 * Version: 1.0
 * License: Educational Use Only
 *
 * Authors:
 * - Yoel Steven Montoya (2416571)
 * - Andrés Felipe Muñoz (2415124)
 * - Thomas Herrera Parra (2417158)
 */

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

/**
 * Controller for the main start menu of the game.
 * Handles nickname entry, new game creation, continuing previous games,
 * and navigation to instructions or exit.
 */
public class StartController extends NavigationAdapter {

    @FXML private Button btnPlay;
    @FXML private Button btnContinue;
    @FXML private Button btnNewGame;
    @FXML private Button btnInstructions;

    private static final String NICKNAME_PATH = "nickname.txt";
    private static final String PLAYER_SAVE_PATH = "player_board.txt";
    private static final String ENEMY_SAVE_PATH  = "enemy_board.txt";
    private static final String SHOTS_SAVE_PATH = "shots.txt";
    private static final String ENEMY_SHOTS_SAVE_PATH = "enemy_shots.txt";

    private boolean hasOngoingGame;

    /**
     * Prompts the user to enter a nickname and saves it to disk.
     *
     * @return An Optional containing the nickname if successfully entered and saved.
     */
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

    /**
     * Initializes the state of the main menu by checking saved game files
     * and adjusting button states accordingly.
     */
    @FXML
    private void initialize() {
        boolean savedGameExists =
                Files.exists(Paths.get(NICKNAME_PATH)) &&
                        Files.exists(Paths.get(PLAYER_SAVE_PATH)) &&
                        Files.exists(Paths.get(ENEMY_SAVE_PATH));

        if (!savedGameExists) {
            try {
                Files.deleteIfExists(Paths.get(PLAYER_SAVE_PATH));
                Files.deleteIfExists(Paths.get(ENEMY_SAVE_PATH));
                Files.deleteIfExists(Paths.get(SHOTS_SAVE_PATH));
                Files.deleteIfExists(Paths.get(ENEMY_SHOTS_SAVE_PATH));
            } catch (IOException ex) {
                new Alert(Alert.AlertType.ERROR, "Error limpiando datos antiguos").showAndWait();
                ex.printStackTrace();
            }
        }

        if (savedGameExists) {
            hasOngoingGame = true;
        }

        boolean savedOnDisk = Files.exists(Paths.get(PLAYER_SAVE_PATH))
                && Files.exists(Paths.get(ENEMY_SAVE_PATH));

        btnContinue.setDisable(!(savedOnDisk || hasOngoingGame));
        btnPlay.setDisable(hasOngoingGame);
        btnInstructions.setDisable(false);
        btnNewGame.setDisable(!(savedOnDisk || hasOngoingGame));
    }

    /**
     * Handles the "Play" button click. Prompts for nickname, clears previous saves,
     * generates a new enemy board and navigates to the game view.
     *
     * @param event Action event triggered by the button click.
     */
    @FXML
    private void onPlayClicked(ActionEvent event) {
        if (promptAndSaveNickname().isEmpty()) return;

        try {
            Files.deleteIfExists(Paths.get(PLAYER_SAVE_PATH));
            Files.deleteIfExists(Paths.get(ENEMY_SAVE_PATH));
            Files.deleteIfExists(Paths.get(SHOTS_SAVE_PATH));
        } catch (IOException ex) {
            new Alert(Alert.AlertType.ERROR, "Error clearing previous game data").showAndWait();
            ex.printStackTrace();
            return;
        }

        EnemyController.generateAndSaveEnemyBoard();
        hasOngoingGame = true;
        goTo("/com/example/miniproyecto3/game-view.fxml", (Node) event.getSource());
    }

    /**
     * Handles the "Continue" button click. Loads an existing game from memory or disk.
     *
     * @param event Action event triggered by the button click.
     */
    @FXML
    private void handleContinue(ActionEvent event) {
        if (hasOngoingGame) {
            goTo("/com/example/miniproyecto3/game-view.fxml", (Node) event.getSource());
            return;
        }

        Path p1 = Paths.get(PLAYER_SAVE_PATH);
        Path p2 = Paths.get(ENEMY_SAVE_PATH);
        if (Files.exists(p1) && Files.exists(p2)) {
            goTo("/com/example/miniproyecto3/game-view.fxml", (Node) event.getSource());
        } else {
            new Alert(Alert.AlertType.WARNING, "No saved game found.").showAndWait();
        }
    }

    /**
     * Disables the "Continue" button from other controllers.
     * Typically called when save data is deleted.
     */
    public void disableContinueButton() {
        btnContinue.setDisable(true);
    }

    /**
     * Handles the "New Game" button click. Prompts for nickname, deletes all
     * save data and generates a new enemy board.
     *
     * @param event Action event triggered by the button click.
     */
    @FXML
    private void handleNewGame(ActionEvent event) {
        if (promptAndSaveNickname().isEmpty()) return;

        try {
            Files.deleteIfExists(Paths.get(PLAYER_SAVE_PATH));
            Files.deleteIfExists(Paths.get(ENEMY_SAVE_PATH));
            Files.deleteIfExists(Paths.get(SHOTS_SAVE_PATH));
            Files.deleteIfExists(Paths.get(ENEMY_SHOTS_SAVE_PATH));
        } catch (IOException ex) {
            new Alert(Alert.AlertType.ERROR, "Error deleting save files").showAndWait();
            ex.printStackTrace();
            return;
        }

        EnemyController.generateAndSaveEnemyBoard();
        hasOngoingGame = true;
        goTo("/com/example/miniproyecto3/game-view.fxml", (Node) event.getSource());
    }

    /**
     * Handles the "Instructions" button click. Navigates to the instructions view.
     *
     * @param event Action event triggered by the button click.
     */
    @FXML
    private void onInstructionsClicked(ActionEvent event) {
        goTo("/com/example/miniproyecto3/instructions-view.fxml", (Node) event.getSource());
    }

    /**
     * Handles the "Exit" button click. Terminates the application.
     *
     * @param event Action event triggered by the button click.
     */
    @FXML
    private void onExitClicked(ActionEvent event) {
        System.exit(0);
    }
}
