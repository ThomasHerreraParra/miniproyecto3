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

package com.example.miniproyecto3;

import com.example.miniproyecto3.view.StartStage;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Entry point for the Warship Dominions game.
 * This class initializes and launches the JavaFX application.
 */
public class Main extends Application {

    /**
     * Initializes and displays the main start stage of the game.
     *
     * @param primaryStage The primary stage for this application, not used directly.
     */
    @Override
    public void start(Stage primaryStage) {
        StartStage startStage = new StartStage();
        startStage.show();
    }

    /**
     * Launches the JavaFX application.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
