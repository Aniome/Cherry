package com.app.cherry.util.configuration;

import com.app.cherry.RunApplication;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.scene.control.TabPane;

import java.io.File;

public class SavingTabs {
    public static void getSavingTabs() {
        String settingsFolderPath = RunApplication.folderPath.toString() + RunApplication.separator + ".cherry";
        File settingsFolder = new File(settingsFolderPath);
        if (!settingsFolder.exists()) {
            settingsFolder.mkdir();
        }
        ObjectMapper objectMapper = new ObjectMapper();
    }

    public static void setSavingTabs(TabPane tabPane) {

    }
}
