package com.app.cherry.util;

import com.app.cherry.RunApplication;
import javafx.scene.control.Alert;

public class Alerts {

    public static void createAndShowWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(RunApplication.getResourceBundle().getString("AlertWarning"));
        alert.setHeaderText(message);
        alert.showAndWait();
    }

    public static void createAndShowError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(RunApplication.getResourceBundle().getString("AlertError"));
        alert.setHeaderText(message);
        alert.showAndWait();
    }
}
