package com.example.miniproyecto3;

import com.example.miniproyecto3.view.StartStage;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {


    @Override
    public void start(Stage primaryStage) {
        StartStage startStage = new StartStage();
        startStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
