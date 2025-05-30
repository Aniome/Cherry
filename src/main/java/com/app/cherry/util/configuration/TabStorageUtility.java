package com.app.cherry.util.configuration;

import com.app.cherry.RunApplication;
import com.app.cherry.controllers.MainController;
import com.app.cherry.controls.FolderOfFiles;
import com.app.cherry.controls.TabBuilder;
import com.app.cherry.util.Alerts;
import com.app.cherry.util.structures.PathNote;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

public class TabStorageUtility {
    public static void loadSavingTabs(TabPane tabPane, TreeView<String> treeView, MainController mainController) {
        //getting array saving tabs
        Optional<PathNote> optionalSavingTabs = Optional.ofNullable(getSavingTabs());
        //if an array is empty, then exit
        if (optionalSavingTabs.isEmpty()) {
            return;
        }
        PathNote savingTabs = optionalSavingTabs.get();

        //getting variables from an object
        String[] openedSavingTabs = savingTabs.getPathNote();
        int selectedTabIndex = savingTabs.getSelectedIndex();

        //setting initialize variables
        int openedSavingTabsLength = openedSavingTabs.length - 1;
        ObservableList<Tab> tabs = tabPane.getTabs();
        boolean isFirstTab = true;
        String separator = RunApplication.getSeparator().equals("/") ? "/" : "\\\\";

        //loading tabs
        for (int i = openedSavingTabsLength; i >= 0; i--) {
            if (openedSavingTabs[i] == null) {
                Tab emptyTab;
                String tabName = RunApplication.getResourceBundle().getString("EmptyTab");
                if (isFirstTab) {
                    emptyTab = tabs.getFirst();
                    emptyTab.setText(tabName);
                    isFirstTab = false;
                } else {
                    emptyTab = new Tab(tabName);
                    tabs.addFirst(emptyTab);
                }
                TabBuilder.buildEmptyTab(emptyTab);
                continue;
            }

            //checking if file exists
            File item = new File(openedSavingTabs[i]);
            if (!item.exists()) continue;

            //creating paths
            Path absolutePath = Path.of(openedSavingTabs[i]);
            String relativePath = RunApplication.folderPath.relativize(absolutePath).toString();
            String[] splitPath = relativePath.split(separator);

            //removing .md if a file is Markdown
            int lastInd = splitPath.length - 1;
            String lastElement = splitPath[lastInd];
            if (lastElement.endsWith(".md")) splitPath[lastInd] = lastElement.replace(".md", "");

            TreeItem<String> currentTreeItem = treeView.getRoot();
            TreeItem<String> findingTreeItem = null;
            //searching treeItem in the treeview
            for (int j = 0; j < lastInd + 1; j++) {
                String treeItemString = splitPath[j];
                ObservableList<TreeItem<String>> treeItemObservableList = currentTreeItem.getChildren();
                boolean isContains = false;
                for (TreeItem<String> childTreeItem : treeItemObservableList) {
                    if (childTreeItem.getValue().equals(treeItemString)) {
                        isContains = true;
                        currentTreeItem = childTreeItem;
                        findingTreeItem = j == lastInd ? childTreeItem : null;
                    }
                }
                if (!isContains) {
                    break;
                }
            }

            String fileName = splitPath[lastInd];
            Tab tab;
            if (isFirstTab) {
                tab = tabs.getFirst();
                tab.setText(fileName);
                isFirstTab = false;
            } else {
                tab = new Tab(fileName);
                tabs.addFirst(tab);
            }

            if (item.isDirectory()) {
                FolderOfFiles.buildFolderTab(tab, findingTreeItem, relativePath, treeView);
            } else {
                mainController.loadDataOnTab(fileName, absolutePath, findingTreeItem, tab);
            }
        }

        tabPane.getSelectionModel().select(selectedTabIndex);
    }

    private static PathNote getSavingTabs() {
        File openedTabsFile = checkExistingSavingOpenedTabs();
        if (openedTabsFile == null) {
            return null;
        }

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(openedTabsFile, PathNote.class);
        } catch (IOException e) {
            return null;
        }
    }

    public static void saveTabs(TabPane tabPane) {
        ObservableList<Tab> tabs = tabPane.getTabs();
        //+ is not counting
        int tabsSize = tabs.size() - 1;
        String[] openedTabs = new String[tabsSize];
        for (int i = 0; i < tabsSize; i++) {
            Tab tab = tabs.get(i);

            Node tabContent = tab.getContent();
            if (tabContent instanceof AnchorPane) {
                openedTabs[i] = null;
                continue;
            }

            BorderPane borderPaneContent = (BorderPane) tabContent;
            VBox vBoxTopContainer = (VBox) borderPaneContent.getTop();
            Node center = borderPaneContent.getCenter();
            boolean isFile = !(center instanceof FlowPane);

            //if empty tab
            if (vBoxTopContainer == null) continue;

            ObservableList<Node> topContainerChildren = vBoxTopContainer.getChildren();

            //getting a path to the tab
            StringBuilder path = getPathFromTitle(topContainerChildren, isFile);

            openedTabs[i] = path.toString();
        }

        int selectedIndex = tabPane.getSelectionModel().getSelectedIndex();

        ObjectMapper objectMapper = new ObjectMapper();
        PathNote openedTabsPathNote = new PathNote(openedTabs, selectedIndex);
        try {
            String savingPath = RunApplication.folderPath.toString() + RunApplication.getSeparator() + ".cherry"
                    + RunApplication.getSeparator() + "openedTabs.json";
            objectMapper.writeValue(new File(savingPath), openedTabsPathNote);
        } catch (IOException e) {
            Alerts.createAndShowError(e.getMessage());
        }
    }

    @NotNull
    private static StringBuilder getPathFromTitle(ObservableList<Node> topContainerChildren, boolean isFile) {
        ObservableList<Node> vBoxCrumbsChildren = ((HBox) topContainerChildren.getFirst()).getChildren();
        StringBuilder path = new StringBuilder(RunApplication.folderPath.toString());
        for (Node node : vBoxCrumbsChildren) {
            if (node instanceof Button button) {
                path.append(RunApplication.getSeparator()).append(button.getText());
            }
        }
        if (isFile) {
            path.append(".md");
        }
        return path;
    }

    private static File checkExistingSavingOpenedTabs() {
        String settingsFolderPath = RunApplication.folderPath.toString() + RunApplication.getSeparator() + ".cherry";
        File settingsFolder = new File(settingsFolderPath);
        if (!settingsFolder.exists()) {
            if (!settingsFolder.mkdir())
                return null;
        }

        File openedTabsFile = new File(settingsFolderPath + RunApplication.getSeparator() + "openedTabs.json");
        if (!openedTabsFile.exists()) {
            return null;
        }
        return openedTabsFile;
    }
}
