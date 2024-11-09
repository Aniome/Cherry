package com.app.cherry.util.configuration;

import com.app.cherry.RunApplication;
import com.app.cherry.controllers.MainController;
import com.app.cherry.controls.codearea.MarkdownArea;
import com.app.cherry.dao.RecentPathsDAO;
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

    public static void observableMainStage(Stage stage, MainController mainController) {
        mainStage = stage;
        stage.setOnHiding(windowEvent -> {
            String path = RunApplication.folderPath.toString();
            RecentPathsDAO.addPath(path);

            SettingsData settingsData = createSettingsData(stage, mainController, path);

            ObjectMapper objectMapper = new ObjectMapper();
            String settings = RunApplication.appPath + "/settings.json";
            try {
                objectMapper.writeValue(new File(settings), settingsData);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            closeWindow(renameStage);
            closeWindow(browserStage);
            closeWindow(findStage);

            if (initStage == null) {
                HibernateUtil.tearDown();
            }
        });
    }

    @NotNull
    private static SettingsData createSettingsData(Stage stage, MainController mainController, String path) {
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

    public static void observableInitStage(Stage stage){
        initStage = stage;
        stage.setOnHiding(windowEvent -> {
            if (mainStage == null) {
                HibernateUtil.tearDown();
            }
        });
    }

    private static void closeWindow(Stage stage){
        if (stage != null)
            stage.close();
    }
}
