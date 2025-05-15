package com.example.miniproyecto3.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;

public class StartController extends NavigationAdapter {

    @FXML
    private void onPlayClicked(ActionEvent event) {
        goTo("/com/example/miniproyecto3/game-view.fxml", (Node) event.getSource());
    }

    @FXML
    private void onInstructionsClicked(ActionEvent event) {
        goTo("/com/example/miniproyecto3/instructions-view.fxml", (Node) event.getSource());
    }

    @FXML
    private void onExitClicked(ActionEvent event) {
        System.out.println("Saliendo");
        System.exit(0);
    }
}
