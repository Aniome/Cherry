package com.app.cherry.util.configuration;

import atlantafx.base.theme.CupertinoLight;
import atlantafx.base.theme.Dracula;
import com.app.cherry.RunApplication;
import com.app.cherry.util.Alerts;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

public class ApplyConfiguration {
    @Getter
    private static double dividerPosition;
    @Getter
    @Setter
    private static String theme;
    @Setter
    private static BorderPane borderPane;
    private static final String dark = "Dark";

    public static void build(Stage mainStage) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            SettingsData settingsData = objectMapper.readValue(
                    RunApplication.class.getResource("settings.json"), SettingsData.class);

            if (settingsData.language.equals("en")) {
                RunApplication.resourceBundle = ResourceBundle.getBundle("local/text", Locale.ENGLISH);
                SavingConfiguration.language = "en";
            } else {
                RunApplication.resourceBundle = ResourceBundle.getBundle("local/text",
                        Locale.of("ru"));
                SavingConfiguration.language = "ru";
            }

            if (settingsData.theme.equals(dark)) {
                theme = dark;
                Application.setUserAgentStylesheet(new Dracula().getUserAgentStylesheet());
            } else {
                theme = "Light";
                Application.setUserAgentStylesheet(new CupertinoLight().getUserAgentStylesheet());
            }

            mainStage.setHeight(settingsData.height);
            mainStage.setWidth(settingsData.width);

            mainStage.setMaximized(settingsData.maximized);

            dividerPosition = settingsData.dividerPosition;
        } catch (IOException e) {
            Alerts.createAndShowWarning(e.getMessage());
        }
    }

    public static void applyThemeOnMainPage() {
        String borderColor = "-fx-border-color: ";
        if (theme.equals(dark)) {
            borderColor = borderColor + "#685ab3;";
        } else {
            borderColor = borderColor + "#d1d1d6;";
        }
        borderPane.setStyle(borderColor);
        ObservableList<Node> borderPaneChildren = borderPane.getChildren();
        for (Node borderPaneChild : borderPaneChildren) {
            if (borderPaneChild instanceof GridPane gridPane) {
                gridPane.setStyle(borderColor);
            }
        }
    }
}
