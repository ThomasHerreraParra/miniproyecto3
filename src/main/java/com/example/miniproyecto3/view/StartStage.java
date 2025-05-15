package com.example.miniproyecto3.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class StartStage extends Stage {

    public StartStage() {
        try {
            // Carga el archivo FXML de la pantalla de inicio
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/miniproyecto3/start-view.fxml"));
            Parent root = loader.load();

            // Crea y muestra la escena
            Scene scene = new Scene(root);
            this.setScene(scene);
            //Pantalla Completa
            this.setFullScreen(true);
            this.setTitle("Batalla Naval - Inicio");

            setScene(scene);
        } catch (IOException e) {
            e.printStackTrace(); // Muestra el error si el archivo FXML no se carga correctamente
        }
    }
}
