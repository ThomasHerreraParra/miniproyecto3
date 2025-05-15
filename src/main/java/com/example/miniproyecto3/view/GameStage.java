package com.example.miniproyecto3.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class GameStage extends Stage {

    public GameStage() {
        try {
            // Cargamos el archivo FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/miniproyecto3/game-view.fxml"));
            Parent root = loader.load();

            // Creamos y mostramos la escena
            Scene scene = new Scene(root, 600, 600);
            this.setScene(scene);
            this.setTitle("Batalla Naval - Tablero de juego");
            //Pantalla completa
            this.setFullScreen(true);
            setScene(scene);
            show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
