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
 * Custom JavaFX Stage that displays the main start screen of the game.
 */
public class StartStage extends Stage {

    /**
     * Constructs a new StartStage, loading the start view from its FXML file
     * and setting the stage to full screen.
     */
    public StartStage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/miniproyecto3/start-view.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            this.setScene(scene);
            this.setFullScreen(true);
            this.setTitle("Batalla Naval - Inicio");

            setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
