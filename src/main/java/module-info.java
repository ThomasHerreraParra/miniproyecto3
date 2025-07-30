module com.example.miniproyecto3 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.desktop;

    opens com.example.miniproyecto3 to javafx.fxml;
    opens com.example.miniproyecto3.controller to javafx.fxml;

    exports com.example.miniproyecto3;
    exports com.example.miniproyecto3.model;
    exports com.example.miniproyecto3.view;
}
