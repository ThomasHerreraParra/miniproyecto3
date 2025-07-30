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
package com.example.miniproyecto3.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Custom JavaFX Stage that displays the main game board.
 */
public class GameStage extends Stage {

    /**
     * Constructs a new GameStage, loading the FXML layout for the game board
     * and displaying it in fullscreen mode.
     */
    public GameStage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/miniproyecto3/game-view.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 600, 600);
            this.setScene(scene);
            this.setTitle("Batalla Naval - Tablero de juego");
            this.setFullScreen(true);
            setScene(scene);
            show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
