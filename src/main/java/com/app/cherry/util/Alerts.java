package com.app.cherry.util;

import javafx.scene.control.Alert;

public class Alerts {
    public static void CreateAndShowWarning(String message){
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
        alert.setTitle("Предупреждение");
        alert.setHeaderText(message);
        alert.showAndWait();
    }
    public static void CreateAndShowError(String message){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(message);
        alert.showAndWait();
    }
}
