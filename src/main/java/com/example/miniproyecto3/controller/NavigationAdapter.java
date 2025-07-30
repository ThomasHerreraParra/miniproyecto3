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

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Abstract base class for controllers that require navigation between views.
 * Implements the {@link INavigable} interface and provides a reusable method to switch scenes.
 */
public abstract class NavigationAdapter implements INavigable {

    /**
     * Loads the specified FXML file and sets it as the current scene in full screen.
     *
     * @param fxmlPath   The path to the FXML file to load.
     * @param sourceNode A node from the current scene, used to obtain the stage.
     */
    @Override
    public void goTo(String fxmlPath, Node sourceNode) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) sourceNode.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setFullScreen(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
