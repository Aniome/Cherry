package com.app.cherry.util.configuration;

import com.app.cherry.RunApplication;
import com.app.cherry.util.Alerts;
import com.app.cherry.util.structures.PathNote;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.ObservableList;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

public class SavingTabs {
    public static void loadSavingTabs(ObservableList<Tab> tabs) {
        Optional<Path[]> optionalSavingTabs = Optional.ofNullable(getSavingTabs());
        optionalSavingTabs.ifPresent(openedTabs -> {
            //removing empty tab
            tabs.remove(0);
            Path[] openedSavingTabs = optionalSavingTabs.get();
            for (Path path : openedSavingTabs) {
                Tab tab = new Tab(path.toString());
                tabs.add(tab);
            }
        });
    }

    private static Path[] getSavingTabs() {
        File openedTabsFile = checkExistingSavingOpenedTabs();

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            PathNote openedTabs = objectMapper.readValue(openedTabsFile, PathNote.class);
            return openedTabs.getPathNote();
        } catch (IOException e) {
            Alerts.createAndShowError(e.getMessage());
            return null;
        }
    }

    public static void setSavingTabs(TabPane tabPane) {

    }

    private static File checkExistingSavingOpenedTabs() {
        String settingsFolderPath = RunApplication.folderPath.toString() + RunApplication.separator + ".cherry";
        File settingsFolder = new File(settingsFolderPath);
        if (!settingsFolder.exists()) {
            settingsFolder.mkdir();
        }

        File openedTabsFile = new File(settingsFolderPath + RunApplication.separator + "openedTabs.json");
        if (!openedTabsFile.exists()) {
            return null;
        }
        return openedTabsFile;
    }
}
