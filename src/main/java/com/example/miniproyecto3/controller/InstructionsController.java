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

/**
 * Controller class for the instructions view.
 * Handles navigation back to the start view from the instructions screen.
 */
public class InstructionsController extends NavigationAdapter {

    /**
     * Handles the back button event.
     * Navigates to the start view when the user clicks "Back".
     *
     * @param event The action event triggered by the button click.
     */
    @FXML
    private void handleBack(ActionEvent event) {
        goTo("/com/example/miniproyecto3/start-view.fxml", (Node) event.getSource());
    }
}
