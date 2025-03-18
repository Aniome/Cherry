package com.app.cherry.util.configuration;

import atlantafx.base.theme.CupertinoLight;
import atlantafx.base.theme.Dracula;
import com.app.cherry.RunApplication;
import com.app.cherry.controls.codearea.MarkdownArea;
import com.app.cherry.util.Alerts;
import com.app.cherry.util.structures.SettingsData;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Set;

public class ApplyConfiguration {
    private static final String DARK = "Dark";
    private static double dividerPosition;
    public static String theme;
    private static BorderPane leftPanelBorderPane;
    private static Scene mainScene;

    public static void loadAndApplySettings(Stage mainStage) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            SettingsData settingsData = objectMapper.readValue(new File(RunApplication.getAppPath() +
                    RunApplication.getSeparator() + "settings.json"), SettingsData.class);

            Path folderPath = Paths.get(settingsData.getLastPath());
            boolean isValidFolder = Files.exists(folderPath) && Files.isExecutable(folderPath)
                    && Files.isDirectory(folderPath);
            if (isValidFolder) {
                RunApplication.folderPath = folderPath;
            } else {
                RunApplication.folderPath = null;
            }

            if (settingsData.getLanguage().equals("en")) {
                RunApplication.setResourceBundle(ResourceBundle.getBundle("local/text", Locale.ENGLISH));
                SavingConfiguration.language = "en";
            } else {
                RunApplication.setResourceBundle(ResourceBundle.getBundle("local/text",
                        Locale.of("ru")));
                SavingConfiguration.language = "ru";
            }

            if (settingsData.getTheme().equals(DARK)) {
                theme = DARK;
                Application.setUserAgentStylesheet(new Dracula().getUserAgentStylesheet());
            } else {
                theme = "Light";
                Application.setUserAgentStylesheet(new CupertinoLight().getUserAgentStylesheet());
            }

            mainStage.setHeight(settingsData.getHeight());
            mainStage.setWidth(settingsData.getWidth());

            mainStage.setMaximized(settingsData.isMaximized());

            dividerPosition = settingsData.getDividerPosition();

            MarkdownArea.fontSize = settingsData.getFontSize();
        } catch (FileNotFoundException e) {
            applyDefaultSettings(mainStage);
        } catch (IOException e) {
            Alerts.createAndShowWarning(e.getMessage());
        }
    }

    private static void applyDefaultSettings(Stage mainStage) {
        RunApplication.folderPath = null;

        RunApplication.setResourceBundle(ResourceBundle.getBundle("local/text", Locale.ENGLISH));
        SavingConfiguration.language = "en";

        theme = DARK;
        Application.setUserAgentStylesheet(new Dracula().getUserAgentStylesheet());

        mainStage.setWidth(1280);
        mainStage.setHeight(720);

        dividerPosition = 0.13;

        MarkdownArea.fontSize = 22;
    }

    public static void applyThemeOnLeftPanelInMainPage() {
        String borderColor = buildBorderStyle();
        leftPanelBorderPane.setStyle(borderColor);
        ObservableList<Node> borderPaneChildren = leftPanelBorderPane.getChildren();
        for (Node borderPaneChild : borderPaneChildren) {
            if (borderPaneChild instanceof GridPane gridPane) {
                gridPane.setStyle(borderColor);
            }
        }
    }

    public static void applyThemeOnSettingsPage(VBox vBox, String style) {
        vBox.setStyle(style + buildBorderStyle());
    }

    public static String buildBorderStyle() {
        return "-fx-border-color: " + getBorderColor() + ";";
    }

    public static String getBorderColor() {
        if (theme.equals(DARK))
            return "#685ab3";
        else
            return "#d1d1d6";
    }

    public static void changeThemeCssOnMainScene() {
        ObservableList<String> mainSceneStylesheets = mainScene.getStylesheets();
        String darkTheme = Objects.requireNonNull
                (RunApplication.class.getResource("css/themes/dark.css")).toExternalForm();
        if (theme.equals(DARK)) {
            mainSceneStylesheets.add(darkTheme);
        } else {
            mainSceneStylesheets.remove(darkTheme);
        }
    }

    public static void applyThemeOnRectangleBackgroundLineNumber(Rectangle rectangle) {
        rectangle.setFill(getColorBackground());
    }

    public static void updateThemeOnStackPaneBackgroundLineNumber(ObservableList<Tab> tabs) {
        Color rectangelColor = getColorBackground();
        for (Tab tab : tabs) {
            if (tab.getText().equals("+"))
                return;
            BorderPane borderPaneContent = (BorderPane) tab.getContent();
            if (borderPaneContent == null)
                return;

            Node borderPaneCenter = borderPaneContent.getCenter();
            if (borderPaneCenter instanceof Label) {
                borderPaneContent.setBackground(new Background(new BackgroundFill(rectangelColor, CornerRadii.EMPTY,
                        Insets.EMPTY)));
            }
            if (borderPaneCenter instanceof StackPane stackPane) {
                ObservableList<Node> virtualizedScrolledObservableList = stackPane.getChildren();
                virtualizedScrolledObservableList.forEach(virtualizedScrolledObservable -> {
                    var virtualizedScrollPane = (VirtualizedScrollPane<?>) virtualizedScrolledObservable;
                    CodeArea codeArea = (CodeArea) virtualizedScrollPane.getContent();
                    //VirtualFlow -> Navigator -> ParagraphBox
                    ObservableList<Node> virtualFlowList = codeArea.getChildrenUnmodifiable();
                    virtualFlowList.forEach(virtualFlow -> {
                        Set<Node> lineNumberStackPaneSet = virtualFlow.lookupAll(".stackPaneGraphicFactory");
                        lineNumberStackPaneSet.forEach(lineNumberStackPaneNode -> {
                            StackPane lineNumberStackPane = (StackPane) lineNumberStackPaneNode;
                            ObservableList<Node> stackPaneChildren = lineNumberStackPane.getChildren();
                            for (Node stackPaneChild : stackPaneChildren) {
                                if (stackPaneChild instanceof Rectangle rectangle) {
                                    rectangle.setFill(rectangelColor);
                                }
                            }
                        });
                    });
                });
            }
        }
    }

    public static void updateThemeOnTopContainer(ObservableList<Tab> tabs) {
        String borderColor = ApplyConfiguration.getBorderColor();
        for (Tab tab : tabs) {
            if (tab.getText().equals("+"))
                continue;
            BorderPane borderPaneContent = (BorderPane) tab.getContent();
            if (borderPaneContent == null)
                return;

            VBox vBoxTopContainer = (VBox) borderPaneContent.getTop();
            if (vBoxTopContainer == null) {
                continue;
            }
            //top right bottom left
            vBoxTopContainer.setStyle("-fx-border-color: transparent " + borderColor + borderColor + " transparent;");
        }
    }

    public static Color getColorBackground() {
        if (theme.equals(DARK))
            return Color.web("#282a36");
        else
            return Color.WHITE;
    }

    ////////////////////////////////////////////////////////////////
    //Under only getters and setters

    public static void setLeftPanelBorderPane(BorderPane borderPane) {
        ApplyConfiguration.leftPanelBorderPane = borderPane;
    }

    public static void setMainSceneAndSetTheme(Scene scene) {
        ApplyConfiguration.mainScene = scene;
    }

    public static double getDividerPosition() {
        return dividerPosition;
    }
}
