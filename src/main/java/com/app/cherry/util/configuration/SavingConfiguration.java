package com.app.cherry.util.configuration;

import com.app.cherry.RunApplication;
import com.app.cherry.controllers.MainController;
import com.app.cherry.dao.RecentPathsDAO;
import com.app.cherry.dao.SettingsDAO;
import com.app.cherry.util.HibernateUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.stage.Stage;

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
            RecentPathsDAO.addPath(RunApplication.folderPath.toString());
            boolean isMaximized = stage.isMaximized();
            SettingsData settingsData = new SettingsData();
            if (!isMaximized) {
                settingsData.setHeight(stage.getHeight());
                settingsData.setWidth(stage.getWidth());
            }
            SettingsDAO.setPath(RunApplication.folderPath.toString());

            settingsData.setMaximized(isMaximized);
            settingsData.setDividerPosition(mainController.splitPane.getDividerPositions()[0]);
            settingsData.setTheme(ApplyConfiguration.getTheme());
            settingsData.setLanguage(language);

            ObjectMapper objectMapper = new ObjectMapper();
            String settings = RunApplication.appPath + "/src/main/resources/com/app/cherry/settings.json";
            System.out.println(settings);
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
