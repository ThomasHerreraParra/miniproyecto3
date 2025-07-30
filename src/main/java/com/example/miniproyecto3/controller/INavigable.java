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

import javafx.scene.Node;

/**
 * Defines navigation behavior for controllers within the application.
 */
public interface INavigable {

    /**
     * Navigates to a new FXML view from a given source node.
     *
     * @param fxmlPath   The path to the target FXML file.
     * @param sourceNode The source node that triggered the navigation.
     */
    void goTo(String fxmlPath, Node sourceNode);
}
