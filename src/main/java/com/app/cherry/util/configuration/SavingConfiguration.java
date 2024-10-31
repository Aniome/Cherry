package com.app.cherry.util.configuration;

import com.app.cherry.RunApplication;
import com.app.cherry.controllers.MainController;
import com.app.cherry.dao.RecentPathsDAO;
import com.app.cherry.dao.SettingsDAO;
import com.app.cherry.util.HibernateUtil;
import javafx.stage.Stage;

public class SavingConfiguration {
    public static Stage initStage;
    public static Stage renameStage;
    public static Stage browserStage;
    public static Stage findStage;

    public static void observableMainStage(Stage stage, MainController mainController) {
        stage.setOnHiding((event) -> {
            RecentPathsDAO.addPath(RunApplication.FolderPath.toString());
            boolean isMaximized = stage.isMaximized();
            if (!isMaximized) {
                SettingsDAO.setHeight(stage.getHeight());
                SettingsDAO.setWidth(stage.getWidth());
            }
            SettingsDAO.setIsMaximized(isMaximized);
            SettingsDAO.setDividerPosition(mainController.splitPane.getDividerPositions()[0]);
            SettingsDAO.setPath(RunApplication.FolderPath.toString());

            closeWindow(renameStage);
            closeWindow(browserStage);
            closeWindow(findStage);

            if (initStage == null) {
                HibernateUtil.tearDown();
            }
        });
    }

    private static void closeWindow(Stage stage){
        if (stage != null)
            stage.close();
    }
}
