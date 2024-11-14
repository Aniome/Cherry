package com.app.cherry.controllers;

import atlantafx.base.controls.ModalPane;
import com.app.cherry.ModalPane.SettingsModal;
import com.app.cherry.RunApplication;
import com.app.cherry.controls.ApplicationContextMenu;
import com.app.cherry.controls.TabManager;
import com.app.cherry.controls.TreeViewItems.TreeCellFactory;
import com.app.cherry.controls.TreeViewItems.TreeItemCustom;
import com.app.cherry.controls.codearea.MarkdownArea;
import com.app.cherry.dao.FavoriteNotesDAO;
import com.app.cherry.util.configuration.ApplyConfiguration;
import com.app.cherry.util.io.FileService;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class MainController{
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

    public void loadDataOnFormOnClick(TreeItem<String> selectedItem){
        ApplyConfiguration.listLineNumber.clear();
        Tab tab = tabPane.getSelectionModel().getSelectedItem();
        tab.setContent(null);
        String filename = selectedItem.getValue();
        tab.setText(filename);
        TabManager tabManager = new TabManager();
        BorderPane borderPane = tabManager.createTab(tab);
        ObservableList<Node> childrens = borderPane.getChildren();
        for (Node children: childrens) {
            if (children instanceof TextField) {
                ((TextField) children).setText(filename);
            }
            if (children instanceof StackPane stackPane) {
                ObservableList<Node> stackPaneChildrens = stackPane.getChildren();

                @SuppressWarnings("unchecked")
                VirtualizedScrollPane<CodeArea> virtualizedScrollPane =
                        (VirtualizedScrollPane<CodeArea>) stackPaneChildrens.getFirst();
                CodeArea codeArea = virtualizedScrollPane.getContent();

                final String text = FileService.readFile(selectedItem);
                int length = text.length();
                if (length > 0) {
                    char lastChar = text.charAt(length - 1);
                    if (lastChar == '\n'){
                        int i;
                        for (i = length - 1; i >= 0; i--) {
                            if (text.charAt(i) != '\n'){
                                break;
                            }
                        }
                        codeArea.appendText(text.substring(0,i));
                        codeArea.appendText(text.substring(i,length));
                        //codeArea.insertText(0, text);
                    } else {
                        codeArea.replaceText(0,0, text);
                    }
                }
                //codeArea.textProperty().addListener((observableValue, s, t1) -> FileService.writeFile(selectedItem, codeArea));

                int codeAreaLength = codeArea.getParagraphs().size();
                int pageLength = 80;
                if (codeAreaLength > pageLength){
                    MarkdownArea.applyStylesPage(pageLength);
                    MarkdownArea.applyStyles(pageLength, codeAreaLength);
                } else {
                    MarkdownArea.applyStyles(0, codeAreaLength);
                }
            }
        }
        tab.setContent(borderPane);
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
        if (newNote == null){
            return;
        }
        String name = newNote.getName().replace(".md", "");
        TabManager.addTab(name, tabPane);
        parent.getChildren().add(new TreeItemCustom(name, true, fileIconName));
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
            if (node instanceof TextField) {
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
        ListView<String> listView = new ListView<>();
        HBox hBox = new HBox(searchField);
        hBox.setPadding(new Insets(10));
        vboxChildren.addAll(hBox, listView);
        VBox.setVgrow(listView, Priority.ALWAYS);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            listView.getItems().clear();
            ObservableList<TreeItem<String>> rootList = treeView.getRoot().getChildren();
            ObservableList<String> listViewItems = listView.getItems();
            for (TreeItem<String> item : rootList) {
                if (item.getValue().contains(newValue)) {
                    listViewItems.add(item.getValue());
                }

            }

            Path startPath = Paths.get("path/to/start/directory");
            try {
                Files.walkFileTree(startPath, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        // Обработка найденного файла
                        System.out.println(file.toString());
                        return FileVisitResult.CONTINUE;
                    }
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        // Обработка директории перед входом в нее
                        System.out.println("Entering directory: " + dir.toString());
                        return FileVisitResult.CONTINUE;
                    }
                    @Override public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                        // Обработка ошибок при посещении файла
                        System.err.println("Failed to visit file: " + file.toString());
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                System.out.println("Failed to visit directory: " + e.toString());
            }
        });
    }

    private void findRecursive(TreeItem<String> item, String findingValue) {

    }

    @FXML
    private void showFavorites() {
        treeView.setRoot(new TreeItem<>(""));
        List<Path> pathList = new LinkedList<>();
        Objects.requireNonNull(FavoriteNotesDAO.getFavoriteNotes()).forEach(item -> {
            String path = item.getPathNote();
            if (FileService.checkExists(path)){
                pathList.add(Paths.get(path));
            } else {
                FavoriteNotesDAO.deleteFavoriteNote(item.getId());
            }
        });
        loadItemsInTree(pathList);
    }

    @FXML
    private void changeStorage(){
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
            String[] path = item.toString().split(RunApplication.separator);
            ObservableList<TreeItem<String>> rootList = treeView.getRoot().getChildren();
            //check tree contains file
            TreeItem<String> containedItem = null;
            for (TreeItem<String> i: rootList){
                if (path[0].equals(i.getValue()))
                    containedItem = i;
            }
            //if tree contains file
            TreeItemCustom addedItem;
            //added path in root tree
            if (containedItem != null){
                TreeItem<String> treeItem = containedItem;
                boolean isContained = false;
                for (int i = 1; i < path.length; i++){
                    ObservableList<TreeItem<String>> treeList = treeItem.getChildren();
                    for (TreeItem<String> treeListItem: treeList){
                        //check subtree for file existence
                        if (path[i].equals(treeListItem.getValue())){
                            treeItem = treeListItem;
                            isContained = true;
                            break;
                        }
                    }
                    if (isContained){
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
                for (String str: path){
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
        SortedList<TreeItem<String>> content = treeView.getRoot().getChildren().sorted(Comparator.comparing(TreeItem::getValue));
        treeView.getRoot().getChildren().setAll(content);
    }
}