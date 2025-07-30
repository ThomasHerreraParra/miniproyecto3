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
 * Custom JavaFX Stage that loads and displays the enemy board view.
 */
public class EnemyStage extends Stage {

    /**
     * Constructs a new EnemyStage, loading the FXML layout and initializing
     * the window in fullscreen mode.
     */
    public EnemyStage() {
        try {
            // Load the FXML for the enemy board
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/miniproyecto3/enemy-view.fxml"));
            Parent root = loader.load();

            // Create and assign the scene
            Scene scene = new Scene(root, 600, 600);
            this.setScene(scene);
            this.setTitle("Batalla Naval - Tablero enemigo");
            this.setFullScreen(true);
            setScene(scene);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
