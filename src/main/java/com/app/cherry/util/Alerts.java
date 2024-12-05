package com.app.cherry.util;

import com.app.cherry.RunApplication;
import javafx.scene.control.Alert;

import java.util.ResourceBundle;

public class Alerts {

    public static void createAndShowWarning(String message) {
        ResourceBundle resourceBundle = RunApplication.resourceBundle;
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(resourceBundle.getString("AlertWarning"));
        alert.setHeaderText(message);
        alert.showAndWait();
    }

    public static void createAndShowError(String message) {
        ResourceBundle resourceBundle = RunApplication.resourceBundle;
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(resourceBundle.getString("AlertError"));
        alert.setHeaderText(message);
        alert.showAndWait();
    }
}
