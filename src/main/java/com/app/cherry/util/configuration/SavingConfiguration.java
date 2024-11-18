package com.app.cherry.util.configuration;

import com.app.cherry.RunApplication;
import com.app.cherry.controllers.MainController;
import com.app.cherry.controls.codearea.MarkdownArea;
import com.app.cherry.dao.RecentPathsDAO;
import com.app.cherry.util.Alerts;
import com.app.cherry.util.HibernateUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public class SavingConfiguration {
    public static Stage mainStage;
    public static Stage initStage;
    public static Stage renameStage;
    public static Stage browserStage;
    public static Stage findStage;
    public static String language;
    public static boolean preparationMainStage;

    public static void observableMainStage(Stage stage, MainController mainController) {
        mainStage = stage;
        stage.setOnHiding(windowEvent -> {
            closeWindow(renameStage);
            closeWindow(browserStage);
            closeWindow(findStage);

            saveConfiguration(stage, mainController);

            if (initStage == null) {
                HibernateUtil.tearDown();
            }
        });
    }

    public static void observableInitStage(Stage stage){
        initStage = stage;
        stage.setOnHiding(windowEvent -> {
            if (mainStage == null && !preparationMainStage) {
                HibernateUtil.tearDown();
            }
        });
    }

    private static void closeWindow(Stage stage){
        if (stage != null)
            stage.close();
    }

    private static void saveConfiguration(Stage stage, MainController mainController) {
        String path = RunApplication.folderPath.toString();
        RecentPathsDAO.addPath(path);

        SettingsData settingsData = generateSettingsData(stage, mainController, path);
        ObjectMapper objectMapper = new ObjectMapper();
        String settings = RunApplication.appPath + "/settings.json";
        try {
            objectMapper.writeValue(new File(settings), settingsData);
        } catch (IOException e) {
            Alerts.createAndShowError(e.getMessage());
        }
    }

    @NotNull
    private static SettingsData generateSettingsData(Stage stage, MainController mainController, String path) {
        SettingsData settingsData = new SettingsData();

        boolean isMaximized = stage.isMaximized();
        settingsData.setLastPath(path);
        if (!isMaximized) {
            settingsData.setHeight(stage.getHeight());
            settingsData.setWidth(stage.getWidth());
        }

        settingsData.setMaximized(isMaximized);
        settingsData.setDividerPosition(mainController.splitPane.getDividerPositions()[0]);
        settingsData.setTheme(ApplyConfiguration.theme);
        settingsData.setLanguage(language);
        settingsData.setFontSize(MarkdownArea.fontSize);
        return settingsData;
    }
}
