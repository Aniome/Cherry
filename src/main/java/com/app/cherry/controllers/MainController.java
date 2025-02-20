package com.app.cherry.controllers;

import atlantafx.base.controls.ModalPane;
import com.app.cherry.ModalPane.SettingsModal;
import com.app.cherry.RunApplication;
import com.app.cherry.controls.ApplicationContextMenu;
import com.app.cherry.controls.TabBuilder;
import com.app.cherry.controls.TreeViewItems.TreeCellFactory;
import com.app.cherry.controls.TreeViewItems.TreeItemCustom;
import com.app.cherry.controls.codearea.MarkdownArea;
import com.app.cherry.controls.listViewItems.ListCellItemSearch;
import com.app.cherry.dao.FavoriteNotesDAO;
import com.app.cherry.util.Alerts;
import com.app.cherry.util.configuration.ApplyConfiguration;
import com.app.cherry.util.configuration.TabStorageUtility;
import com.app.cherry.util.icons.IconConfigurer;
import com.app.cherry.util.io.FileService;
import com.app.cherry.util.structures.SearchListViewItem;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import org.fxmisc.richtext.CodeArea;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class MainController {
    @FXML
    private TreeView<String> treeView;
    @FXML
    private TabPane tabPane;
    @FXML
    public SplitPane splitPane;
    @FXML
    RadioButton filesManagerButton;
    @FXML
    RadioButton searchButton;
    @FXML
    RadioButton favoriteNotesButton;
    @FXML
    VBox vbox;
    @FXML
    ModalPane modalPane;
    @FXML
    BorderPane leftPanelBorderPane;

    Stage renameStage;
    TreeItem<String> filesManagerRoot;
    ArrayList<Node> fileManagerVbox;
    ArrayList<SearchListViewItem> searchListViewItems;

    @FXML
    private void CloseWindow(MouseEvent event) {
        Platform.exit();
    }

    public void initialize() {
        treeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        filesManagerRoot = new TreeItem<>("");
        treeView.setRoot(filesManagerRoot);
        treeView.setShowRoot(false);
        loadFilesInTreeview();
        sortTreeView();
        ApplicationContextMenu.buildTreeViewContextMenu(treeView, this, renameStage, tabPane);

        filesManagerButton.getStyleClass().remove("radio-button");
        searchButton.getStyleClass().remove("radio-button");
        favoriteNotesButton.getStyleClass().remove("radio-button");
        fileManagerVbox = new ArrayList<>();
        searchListViewItems = new ArrayList<>();

        TreeCellFactory.build(treeView, this);

        modalPane.hide();
        splitPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        applyThemeOnLeftPanel();
        TabStorageUtility.loadSavingTabs(tabPane, treeView.getRoot(), this);
    }

    private void applyThemeOnLeftPanel() {
        ApplyConfiguration.setLeftPanelBorderPane(leftPanelBorderPane);
        ApplyConfiguration.applyThemeOnLeftPanelInMainPage();
    }

    public void setDividerPositionAfterShowing() {
        splitPane.setDividerPositions(ApplyConfiguration.getDividerPosition());
    }

    private void loadFilesInTreeview() {
        loadItemsInTree(FileService.getListFiles());
    }

    private TreeItemCustom creatingTreeItem(String folderItem) {
        if (folderItem.contains(".md")) {
            folderItem = folderItem.replace(".md", "");
            return new TreeItemCustom(folderItem, true, IconConfigurer.getFileIcon());
        } else {
            return new TreeItemCustom(folderItem, false, IconConfigurer.getFolderIcon(16));
        }
    }

    public void openRenameWindow() {
        RunApplication.showRenameWindow(treeView.getSelectionModel().getSelectedItem(),
                tabPane.getSelectionModel().getSelectedItem());
    }

    //Loading on click treeItem
    public void loadDataOnFormOnClick(TreeItem<String> selectedItem) {
        String filename = selectedItem.getValue();
        loadDataOnFormOnClick(filename, null, selectedItem);
    }

    //Loading data in listView
    public void loadDataOnFormOnClick(Path path, String filename) {
        loadDataOnFormOnClick(filename, path, null);
    }

    public void loadDataOnFormOnClick(String filename, Path path, TreeItem<String> selectedItem) {
        Tab tab = tabPane.getSelectionModel().getSelectedItem();
        loadDataOnTab(filename, path, selectedItem, tab);
    }

    public void loadDataOnTab(String filename, Path path, TreeItem<String> selectedItem, Tab tab) {
        tab.setContent(null);
        tab.setText(filename);

        TabBuilder tabBuilder = new TabBuilder();
        StackPane markdownArea = MarkdownArea.createMarkdownArea(selectedItem, tabBuilder);
        tab.setContent(tabBuilder.buildTabContent(tab, selectedItem, markdownArea));
        CodeArea codeArea = tabBuilder.getCodeArea();

        String text;
        if (path == null)
            text = FileService.readFile(selectedItem);
        else
            text = FileService.readFile(path);
        int length = text.length();
        if (length > 0) {
            if (text.endsWith("\n")) {
                int i;
                for (i = length - 1; i >= 0; i--) {
                    if (text.charAt(i) != '\n') {
                        break;
                    }
                }
                codeArea.appendText(text.substring(0, i));
                codeArea.appendText(text.substring(i, length));
            } else {
                codeArea.replaceText(0, 0, text);
            }
        }

        int codeAreaLength = codeArea.getParagraphs().size();
        int pageLength = 80;
        if (codeAreaLength > pageLength) {
            MarkdownArea.applyStylesPage(codeArea, pageLength);
            MarkdownArea.applyStyles(pageLength, codeAreaLength, codeArea);
        } else {
            MarkdownArea.applyStyles(0, codeAreaLength, codeArea);
        }

        Circle circleUnsavedChanges = (Circle) tab.getGraphic();
        final boolean[] isUnsavedChanges = {false};
        codeArea.textProperty().addListener((observableValue, s, t1) -> {
            if (isUnsavedChanges[0]) return;
            circleUnsavedChanges.setOpacity(1);
            isUnsavedChanges[0] = true;
        });
    }

    //Creates a tab and gives focus to it
    @FXML
    private Tab addTab() {
        Tab tab = new Tab(RunApplication.resourceBundle.getString("EmptyTab"));
        TabBuilder.buildEmptyTab(tab);
        TabBuilder.selectTab(tab, tabPane);
        return tab;
    }

    //Event on the note creation button
    @FXML
    private void createNote() {
        createNote(treeView.getRoot());
    }

    public void createNote(TreeItem<String> parent) {
        File newNote = FileService.createFileMarkdown(parent);
        if (newNote == null) {
            return;
        }
        String fileName = newNote.getName().replace(".md", "");
        TreeItemCustom newTreeItem = new TreeItemCustom(fileName, true, IconConfigurer.getFileIcon());
        parent.getChildren().add(newTreeItem);
        new TabBuilder().addTab(fileName, tabPane, newTreeItem);
        sortTreeView();
    }

    @FXML
    private void createFolderInTree() {
        createFolder(treeView.getRoot());
    }

    public void createFolder(TreeItem<String> treeItem) {
        File folder = FileService.createFolderMarkdown(treeItem);
        if (folder == null) {
            return;
        }
        TreeItemCustom folderTreeItem =
                new TreeItemCustom(folder.getName(), false, IconConfigurer.getFolderIcon(16));
        treeItem.getChildren().add(folderTreeItem);
        sortTreeView();
    }

    @FXML
    private void showFiles() {
        ObservableList<Node> listVbox = vbox.getChildren();
        boolean afterSearch = false;
        for (Node node : listVbox) {
            if (node instanceof ListView) {
                afterSearch = true;
                break;
            }
        }
        if (afterSearch) {
            listVbox.clear();
            listVbox.addAll(fileManagerVbox);
            fileManagerVbox.clear();
        }
        treeView.setRoot(filesManagerRoot);
    }

    @FXML
    private void showSearch() {
        ObservableList<Node> vboxChildren = vbox.getChildren();
        fileManagerVbox.addAll(vboxChildren);
        vboxChildren.clear();

        TextField searchField = new TextField();
        ListView<SearchListViewItem> listView = new ListView<>();
        HBox hBox = new HBox(searchField);
        hBox.setPadding(new Insets(10));
        vboxChildren.addAll(hBox, listView);
        VBox.setVgrow(listView, Priority.ALWAYS);

        ResourceBundle resourceBundle = RunApplication.resourceBundle;
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty())
                return;
            listView.getItems().clear();
            searchListViewItems.clear();
            ObservableList<SearchListViewItem> listViewItems = listView.getItems();
            try {
                Files.walkFileTree(RunApplication.folderPath, new SimpleFileVisitor<>() {
                    @NotNull
                    @Override
                    public FileVisitResult visitFile(Path file, @NotNull BasicFileAttributes attrs) {
                        String pathString = file.toString();
                        int ind = pathString.lastIndexOf(RunApplication.separator);
                        pathString = pathString.substring(ind + 1);
                        if (pathString.contains(newValue)) {
                            if (pathString.contains(".md")) {
                                pathString = pathString.replace(".md", "");
                            }
                            listViewItems.add(new SearchListViewItem(pathString, file));
                        }
                        return FileVisitResult.CONTINUE;
                    }

                    @NotNull
                    @Override
                    public FileVisitResult visitFileFailed(Path file, @NotNull IOException exc) {
                        Alerts.createAndShowWarning(resourceBundle.getString("FailedVisitFile") + " "
                                + file.toString());
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                Alerts.createAndShowError(resourceBundle.getString("FailedVisitDirectory") + " " + e);
            }

            listView.setCellFactory(lvItem -> {
                ListCellItemSearch listCellItem = new ListCellItemSearch();
                listCellItem.setOnMouseClicked(event -> {
                    SearchListViewItem item = listCellItem.getItem();
                    String searchResult = item.searchText;
                    if (searchResult == null || searchResult.isEmpty()) {
                        return;
                    }
                    loadDataOnFormOnClick(item.path, item.searchText);
                });
                return listCellItem;
            });
        });
    }

    @FXML
    private void showFavorites() {
        treeView.setRoot(new TreeItem<>(""));
        List<Path> pathList = new LinkedList<>();
        Objects.requireNonNull(FavoriteNotesDAO.getFavoriteNotes()).forEach(item -> {
            String path = item.getPathNote();
            if (FileService.checkExists(path)) {
                pathList.add(Paths.get(path));
            } else {
                FavoriteNotesDAO.deleteFavoriteNote(item.getId());
            }
        });
        loadItemsInTree(pathList);
    }

    @FXML
    private void changeStorage() {
        RunApplication.showInitialWindow();
    }

    @FXML
    private void help() {
        RunApplication.showHelpStage();
    }

    @FXML
    private void settings() {
        SettingsModal settingsModal = new SettingsModal();
        ObservableList<Tab> tabs = tabPane.getTabs();
        settingsModal.build(modalPane, splitPane, tabs);
    }

    private void loadItemsInTree(List<Path> pathList) {
        pathList = pathList.stream().map(path -> RunApplication.folderPath.relativize(path)).toList();
        pathList.forEach(item -> {
            String separator = "\\\\";
            if (item.toString().contains("/")) {
                separator = "/";
            }
            String[] path = item.toString().split(separator);
            ObservableList<TreeItem<String>> rootList = treeView.getRoot().getChildren();

            //check tree contains file
            TreeItem<String> containedItem = null;
            for (TreeItem<String> i : rootList) {
                if (path[0].equals(i.getValue()))
                    containedItem = i;
            }

            TreeItemCustom addedItem;
            //3 options:
            //1. If tree root already contains first part path
            //2. If tree root don't contain first part path and path long
            //3. If tree root don't contain path
            if (containedItem != null) {
                TreeItem<String> treeItem = containedItem;
                boolean isContained = false;
                for (int i = 1; i < path.length; i++) {
                    ObservableList<TreeItem<String>> treeList = treeItem.getChildren();
                    for (TreeItem<String> treeListItem : treeList) {
                        //check subtree for file existence
                        if (path[i].equals(treeListItem.getValue())) {
                            treeItem = treeListItem;
                            isContained = true;
                            break;
                        }
                    }
                    if (isContained) {
                        isContained = false;
                        continue;
                    }
                    //add item in tree
                    addedItem = creatingTreeItem(path[i]);
                    treeList.add(addedItem);
                }
            } else if (path.length > 1) {
                //creating tree hierarchy
                TreeItem<String> treeItem = treeView.getRoot();
                TreeItem<String> newTreeItem;
                for (String str : path) {
                    newTreeItem = creatingTreeItem(str);
                    treeItem.getChildren().add(newTreeItem);
                    treeItem = newTreeItem;
                }
            } else {
                //adding file in tree
                addedItem = creatingTreeItem(path[0]);
                rootList.add(addedItem);
            }
        });
    }

    private void sortTreeView() {
        SortedList<TreeItem<String>> content = treeView.getRoot().getChildren()
                .sorted(Comparator.comparing(TreeItem::getValue));
        treeView.getRoot().getChildren().setAll(content);
    }

    public TabPane getTabPane() {
        return tabPane;
    }
}