package com.app.cherry.util.configuration;

import atlantafx.base.controls.Breadcrumbs;
import atlantafx.base.controls.Breadcrumbs.BreadCrumbItem;
import com.app.cherry.RunApplication;
import com.app.cherry.util.Alerts;
import com.app.cherry.util.structures.PathNote;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SavingTabs {
    public static void loadSavingTabs(ObservableList<Tab> tabs) {
        Optional<String[]> optionalSavingTabs = Optional.ofNullable(getSavingTabs());
        optionalSavingTabs.ifPresent(openedSavingTabs -> {
            for (String path : openedSavingTabs) {
                Tab tab = new Tab(path);
                tabs.addFirst(tab);
            }
        });
    }

    private static String[] getSavingTabs() {
        File openedTabsFile = checkExistingSavingOpenedTabs();
        if (openedTabsFile == null) {
            return null;
        }

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            PathNote openedTabs = objectMapper.readValue(openedTabsFile, PathNote.class);
            return openedTabs.getPathNote();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public static void saveTabs(ObservableList<Tab> tabs) {
        //+ is not counting
        int size = tabs.size() - 1;
        String[] openedTabs = new String[size];
        for (int i = 0; i < size; i++) {
            Tab tab = tabs.get(i);

            BorderPane borderPaneContent = (BorderPane) tab.getContent();
            VBox vBoxTopContainer = (VBox) borderPaneContent.getTop();
            if (vBoxTopContainer == null) {
                continue;
            }

            ObservableList<Node> topContainerChildren = vBoxTopContainer.getChildren();
            if (topContainerChildren.isEmpty()) {
                continue;
            }

            ObservableList<Node> vBoxCrumbsChildren = ((VBox) topContainerChildren.getFirst()).getChildren();
            @SuppressWarnings("unchecked")
            Breadcrumbs<String> crumbs = (Breadcrumbs<String>) vBoxCrumbsChildren.getFirst();
            BreadCrumbItem<String> currentCrumb = crumbs.getSelectedCrumb();

            List<String> pathNoteList = new ArrayList<>();
            while (currentCrumb != null) {
                pathNoteList.add(currentCrumb.getValue());
                currentCrumb = (BreadCrumbItem<String>) currentCrumb.getParent();
            }
            pathNoteList = pathNoteList.reversed();
            StringBuilder stringBuilder = new StringBuilder(RunApplication.folderPath.toString());
            pathNoteList.forEach(item -> stringBuilder.append(RunApplication.separator).append(item));

            //saving tab name
//            HBox hBoxTitleBar = (HBox) topContainerChildren.get(1);
//
//            ObservableList<Node> hBoxTitleBarChildren = hBoxTitleBar.getChildren();
//            if (hBoxTitleBarChildren.isEmpty())
//                continue;
//
//            TextField noteName = (TextField) hBoxTitleBarChildren.getFirst();
//            String noteNameString = noteName.getText();

            openedTabs[i] = stringBuilder.toString();
        }
        //empty tab
        if (openedTabs.length == 1 && openedTabs[0] == null) {
            return;
        }
        ObjectMapper objectMapper = new ObjectMapper();
        PathNote openedTabsPathNote = new PathNote(openedTabs);
        try {
            String savingPath = RunApplication.folderPath.toString() + RunApplication.separator + ".cherry"
                    + RunApplication.separator + "openedTabs.json";
            objectMapper.writeValue(new File(savingPath), openedTabsPathNote);
        } catch (IOException e) {
            Alerts.createAndShowError(e.getMessage());
        }
    }

    private static File checkExistingSavingOpenedTabs() {
        String settingsFolderPath = RunApplication.folderPath.toString() + RunApplication.separator + ".cherry";
        File settingsFolder = new File(settingsFolderPath);
        if (!settingsFolder.exists()) {
            if (!settingsFolder.mkdir())
                return null;
        }

        File openedTabsFile = new File(settingsFolderPath + RunApplication.separator + "openedTabs.json");
        if (!openedTabsFile.exists()) {
            return null;
        }
        return openedTabsFile;
    }
}
