package com.app.cherry.util.configuration;

import atlantafx.base.theme.CupertinoLight;
import atlantafx.base.theme.Dracula;
import com.app.cherry.RunApplication;
import com.app.cherry.util.Alerts;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

public class ApplyConfiguration {
    private static double dividerPosition;

    public static double getDividerPosition() {
        return dividerPosition;
    }

    public static String theme;

    private static BorderPane borderPane;

    public static void setBorderPane(BorderPane borderPane) {
        ApplyConfiguration.borderPane = borderPane;
    }

    private static final String dark = "Dark";
    private static Scene mainScene;
    private static Rectangle rectangle;

    public static void build(Stage mainStage) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            SettingsData settingsData = objectMapper.readValue(new File(RunApplication.appPath +
                    "/settings.json"), SettingsData.class);

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
        String borderColor = buildBorderStyle();
        borderPane.setStyle(borderColor);
        ObservableList<Node> borderPaneChildren = borderPane.getChildren();
        for (Node borderPaneChild : borderPaneChildren) {
            if (borderPaneChild instanceof GridPane gridPane) {
                gridPane.setStyle(borderColor);
            }
        }
    }

    public static void applyThemeOnSettingsPage(VBox vBox, String style) {
        String vBoxStyle = style + buildBorderStyle();
        vBox.setStyle(vBoxStyle);
    }

    private static String buildBorderStyle() {
        String borderColor = "-fx-border-color: ";
        if (theme.equals(dark)) {
            borderColor = borderColor + "#685ab3;";
        } else {
            borderColor = borderColor + "#d1d1d6;";
        }
        return borderColor;
    }

    public static void setMainScene(Scene scene) {
        ApplyConfiguration.mainScene = scene;
        setThemeOnScene();
    }

    /* use after set main scene */
    public static void applyThemeOnMarkdownArea() {
        setThemeOnScene();
        setThemeOnRectangleLineNumber();
    }

    private static void setThemeOnScene() {
        ObservableList<String> mainSceneStylesheets = mainScene.getStylesheets();
        String darkTheme = Objects.requireNonNull
                (RunApplication.class.getResource("css/themes/dark.css")).toExternalForm();
        if (theme.equals(dark)) {
            mainSceneStylesheets.add(darkTheme);
        } else {
            mainSceneStylesheets.remove(darkTheme);
        }
    }

    public static void setRectangle(Rectangle rectangle) {
        ApplyConfiguration.rectangle = rectangle;
        setThemeOnRectangleLineNumber();
    }

    private static void setThemeOnRectangleLineNumber() {
        if (rectangle == null) return;
        if (theme.equals(dark)) {
            rectangle.setFill(Color.web("#282c34"));
        } else {
            rectangle.setFill(Color.TRANSPARENT);
        }
    }
}
