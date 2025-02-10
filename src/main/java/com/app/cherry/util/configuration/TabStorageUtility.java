package com.app.cherry.util.configuration;

import atlantafx.base.controls.Breadcrumbs;
import atlantafx.base.controls.Breadcrumbs.BreadCrumbItem;
import com.app.cherry.RunApplication;
import com.app.cherry.controllers.MainController;
import com.app.cherry.util.Alerts;
import com.app.cherry.util.structures.PathNote;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TabStorageUtility {
    public static void loadSavingTabs(ObservableList<Tab> tabs, TreeItem<String> root, MainController mainController) {
        //getting array saving tabs
        Optional<String[]> optionalSavingTabs = Optional.ofNullable(getSavingTabs());
        //if array is empty exit
        if (optionalSavingTabs.isEmpty()) {
            return;
        }

        boolean firstTab = true;

        String[] openedSavingTabs = optionalSavingTabs.get();
        String separator = RunApplication.separator.equals("/") ? "/" : "\\\\";

        //loading tabs
        for (String path : openedSavingTabs) {
            //checking if file exist
            File item = new File(path);
            if (!item.exists()) {
                continue;
            }

            //creating paths
            Path absolutePath = Path.of(path);
            String relativePath = RunApplication.folderPath.relativize(absolutePath).toString();
            String[] splitPath = relativePath.split(separator);

            //removing .md if path contains
            int lastInd = splitPath.length - 1;
            String lastElement = splitPath[lastInd];
            if (lastElement.endsWith(".md")) {
                splitPath[lastInd] = lastElement.replace(".md", "");
            }

            TreeItem<String> treeItem = root;
            TreeItem<String> findingTreeItem = null;
            boolean isContains;
            //getting treeItem going on the path
            for (int i = 0; i < lastInd + 1; i++) {
                String treeItemString = splitPath[i];
                ObservableList<TreeItem<String>> treeItemObservableList = treeItem.getChildren();
                isContains = false;
                for (TreeItem<String> childTreeItem : treeItemObservableList) {
                    if (childTreeItem.getValue().equals(treeItemString)) {
                        isContains = true;
                        treeItem = childTreeItem;
                        findingTreeItem = i == lastInd ? childTreeItem : null;
                    }
                }
                if (!isContains) {
                    break;
                }
            }

            String fileName = splitPath[lastInd];
            Tab tab;
            if (firstTab) {
                tab = tabs.getFirst();
                tab.setText(fileName);
                firstTab = false;
            } else {
                tab = new Tab(fileName);
                tabs.addFirst(tab);
            }

            if (item.isDirectory()) {

            } else {
                mainController.loadDataOnTab(fileName, absolutePath, findingTreeItem, tab);
            }
        }
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

            //saving tab name
            StringBuilder path = getPathFromTitle(topContainerChildren);

            openedTabs[i] = path.toString();
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

    @NotNull
    private static StringBuilder getPathFromTitle(ObservableList<Node> topContainerChildren) {
        HBox hBoxTitleBar = (HBox) topContainerChildren.get(1);

        boolean isFile = true;
        ObservableList<Node> hBoxTitleBarChildren = hBoxTitleBar.getChildren();
        if (hBoxTitleBarChildren.isEmpty())
            isFile = false;

        ObservableList<Node> vBoxCrumbsChildren = ((VBox) topContainerChildren.getFirst()).getChildren();
        Breadcrumbs<?> crumbs = (Breadcrumbs<?>) vBoxCrumbsChildren.getFirst();

        return getPathFromCrumbs(crumbs, isFile);
    }

    @NotNull
    private static StringBuilder getPathFromCrumbs(Breadcrumbs<?> crumbs, boolean isFile) {
        BreadCrumbItem<?> currentCrumb = crumbs.getSelectedCrumb();
        List<String> pathNoteList = new ArrayList<>();
        while (currentCrumb != null) {
            pathNoteList.add((String) currentCrumb.getValue());
            currentCrumb = (BreadCrumbItem<?>) currentCrumb.getParent();
        }
        StringBuilder path = new StringBuilder(RunApplication.folderPath.toString());
        pathNoteList.forEach(item -> path.append(RunApplication.separator).append(item));
        if (isFile) {
            path.append(".md");
        }
        return path;
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
