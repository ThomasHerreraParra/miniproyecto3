package com.example.miniproyecto3.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class InstructionsStage extends Stage {

    public InstructionsStage() {
        try {
            // Carga la vista de instrucciones desde el archivo FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/miniproyecto3/instructions-view.fxml"));
            Parent root = loader.load();

            // Crea la escena y configura el título
            Scene scene = new Scene(root);
            this.setScene(scene);
            this.setTitle("Instrucciones");
            //Pantalla completa
            this.setFullScreen(true);
            setScene(scene);
        } catch (IOException e) {
            e.printStackTrace(); // Muestra error si hay problema cargando el FXML
        }
    }
}
