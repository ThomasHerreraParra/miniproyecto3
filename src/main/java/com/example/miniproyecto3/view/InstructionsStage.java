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
 * Custom JavaFX Stage that displays the instructions screen to the player.
 */
public class InstructionsStage extends Stage {

    /**
     * Constructs a new InstructionsStage, loading and displaying the
     * instructions view from FXML in fullscreen mode.
     */
    public InstructionsStage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/miniproyecto3/instructions-view.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            this.setScene(scene);
            this.setTitle("Instrucciones");
            this.setFullScreen(true);
            setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
