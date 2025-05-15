package com.example.miniproyecto3.controller;

import com.example.miniproyecto3.controller.NavigationAdapter;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;

public class InstructionsController extends NavigationAdapter {

    @FXML
    private void handleBack(ActionEvent event) {
        goTo("/com/example/miniproyecto3/start-view.fxml", (Node) event.getSource());
    }
}
