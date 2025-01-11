package com.app.cherry.controllers;

import atlantafx.base.controls.ModalPane;
import com.app.cherry.ModalPane.SettingsModal;
import com.app.cherry.RunApplication;
import com.app.cherry.controls.ApplicationContextMenu;
import com.app.cherry.controls.TabManager;
import com.app.cherry.controls.TreeViewItems.TreeCellFactory;
import com.app.cherry.controls.TreeViewItems.TreeItemCustom;
import com.app.cherry.controls.codearea.MarkdownArea;
import com.app.cherry.controls.listViewItems.ListCellItemSearch;
import com.app.cherry.dao.FavoriteNotesDAO;
import com.app.cherry.util.Alerts;
import com.app.cherry.util.configuration.ApplyConfiguration;
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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
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
    public static String newFileName;
    TreeItem<String> filesManagerRoot;
    final String fileIconName = "mdal-insert_drive_file";
    final String folderIconName = "mdal-folder_open";
    ArrayList<Node> fileManagerVbox;
    ArrayList<SearchListViewItem> searchListViewItems;
    public static CodeArea codeArea;

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
        ApplicationContextMenu.createContextMenu(treeView, this, renameStage, tabPane);

        filesManagerButton.getStyleClass().remove("radio-button");
        searchButton.getStyleClass().remove("radio-button");
        favoriteNotesButton.getStyleClass().remove("radio-button");
        fileManagerVbox = new ArrayList<>();
        searchListViewItems = new ArrayList<>();

        TreeCellFactory.build(treeView, this);

        modalPane.hide();
        splitPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        setTheme();
    }

    private void setTheme() {
        ApplyConfiguration.setLeftPanelBorderPane(leftPanelBorderPane);
        ApplyConfiguration.applyThemeOnMainPage();
    }

    public void afterShowing() {
        splitPane.setDividerPositions(ApplyConfiguration.getDividerPosition());
    }

    private void loadFilesInTreeview() {
        List<Path> pathList = FileService.getListFiles();
        loadItemsInTree(pathList);
    }

    private TreeItemCustom creatingTreeItem(String str) {
        if (str.contains(".md")) {
            str = str.replace(".md", "");
            return new TreeItemCustom(str, true, fileIconName);
        } else {
            return new TreeItemCustom(str, false, folderIconName);
        }
    }

    public void openRenameWindow() {
        RunApplication.showRenameWindow(treeView, tabPane);
    }

    public void loadDataOnFormOnClick(TreeItem<String> selectedItem) {
        String filename = selectedItem.getValue();
        loadDataOnFormOnClick(filename, null, selectedItem);
    }

    public void loadDataOnFormOnClick(Path path, String filename) {
        loadDataOnFormOnClick(filename, path, null);
    }

    private void loadDataOnFormOnClick(String filename, Path path, TreeItem<String> selectedItem) {
        ApplyConfiguration.listStackPaneLineNumber.clear();
        Tab tab = tabPane.getSelectionModel().getSelectedItem();
        tab.setContent(null);
        tab.setText(filename);

        TabManager tabManager = new TabManager();
        tab.setContent(tabManager.createTab(tab, selectedItem));

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
            MarkdownArea.applyStylesPage(pageLength);
            MarkdownArea.applyStyles(pageLength, codeAreaLength);
        } else {
            MarkdownArea.applyStyles(0, codeAreaLength);
        }

        Thread thread = new Thread(() -> {
            Platform.runLater(() -> {
                codeArea.textProperty().addListener((observableValue, s, t1) -> {
                    //FileService.writeFile(selectedItem, t1);
                });
            });
        });
        thread.start();
    }

    //Creates a tab and gives focus to it
    @FXML
    private Tab addTab() {
        Tab tab = new Tab(RunApplication.resourceBundle.getString("EmptyTab"));
        tab.setContent(TabManager.createEmptyTab());
        TabManager.selectTab(tab, tabPane);
        return tab;
    }

    //Event on the note creation button
    @FXML
    private void createNote() {
        createFile(treeView.getRoot());
    }

    public void createFile(TreeItem<String> parent) {
        File newNote = FileService.createFileMarkdown(parent);
        if (newNote == null) {
            return;
        }
        String name = newNote.getName().replace(".md", "");
        TreeItemCustom newTreeItem = new TreeItemCustom(name, true, fileIconName);
        TabManager.addTab(name, tabPane, newTreeItem);
        parent.getChildren().add(newTreeItem);
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
                new TreeItemCustom(folder.getName(), false, folderIconName);
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
        settingsModal.build(modalPane, splitPane);
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
            //if tree contains file
            TreeItemCustom addedItem;
            //added path in root tree
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
}