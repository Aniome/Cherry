package com.app.cherry.controllers;

import atlantafx.base.controls.ModalPane;
import com.app.cherry.ModalPane.SettingsModal;
import com.app.cherry.RunApplication;
import com.app.cherry.controls.ApplicationContextMenu;
import com.app.cherry.controls.TabManager;
import com.app.cherry.controls.TreeViewItems.TreeCellFactory;
import com.app.cherry.controls.TreeViewItems.TreeItemCustom;
import com.app.cherry.controls.codearea.MixedArea;
import com.app.cherry.dao.FavoriteNotesDAO;
import com.app.cherry.dao.SettingsDAO;
import com.app.cherry.util.FileService;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class MainController{
    @FXML
    private TreeView<String> treeView;
    @FXML
    private TabPane tabPane;
    @FXML
    public SplitPane splitPane;
    @FXML
    ToggleButton filesManagerButton;
    @FXML
    ToggleButton searchButton;
    @FXML
    ToggleButton favoriteNotesButton;
    @FXML
    VBox vbox;
    @FXML
    ModalPane modalPane;

    Stage mainStage;
    Stage renameStage;
    public static String newFileName;
    boolean favoriteSelected = false;
    boolean filesManagerSelected = true;
    TreeItem<String> filesManagerRoot;
    final String fileIconName = "mdal-insert_drive_file";
    final String folderIconName = "mdal-folder_open";

    @FXML
    private void CloseWindow(MouseEvent event) {
        Platform.exit();
    }

    public void init(Stage mainStage){
        this.mainStage = mainStage;

        treeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        filesManagerRoot = new TreeItem<>("");
        treeView.setRoot(filesManagerRoot);
        treeView.setShowRoot(false);
        loadFilesInTreeview();
        sortTreeView();
        ApplicationContextMenu.createContextMenu(treeView, this, renameStage, tabPane);

        TreeCellFactory.build(treeView, this);

        configureToggleButtons();
        modalPane.hide();
        splitPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
    }

    public void afterShowing(){
        splitPane.setDividerPositions(SettingsDAO.getDividerPosition());
    }

    private void configureToggleButtons(){
        filesManagerButton.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
            if(!favoriteNotesButton.isSelected() && aBoolean && !searchButton.isSelected()){
                filesManagerButton.setSelected(true);
            }
        });
        favoriteNotesButton.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
            if(!filesManagerButton.isSelected() && aBoolean && !searchButton.isSelected()){
                favoriteNotesButton.setSelected(true);
            }
        });
        searchButton.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
            if(!filesManagerButton.isSelected() && aBoolean && !favoriteNotesButton.isSelected()){
                searchButton.setSelected(true);
            }
        });
    }

    private void loadFilesInTreeview(){
        List<Path> pathList = FileService.getListFiles();
        loadItemsInTree(pathList);
    }

    private TreeItemCustom creatingTreeItem(String str){
        if (str.contains(".md")) {
            str = str.replace(".md", "");
            return new TreeItemCustom(str, true, fileIconName);
        } else {
            return new TreeItemCustom(str, false, folderIconName);
        }
    }

    public void openRenameWindow(){
        RunApplication.showRenameWindow(treeView, tabPane);
    }

    public void loadDataOnFormOnClick(TreeItem<String> selectedItem){
        Tab tab = tabPane.getSelectionModel().getSelectedItem();
        tab.setContent(null);
        String filename = selectedItem.getValue();
        tab.setText(filename);
        TabManager tabManager = new TabManager();
        BorderPane borderPane = tabManager.createTab(tab);
        ObservableList<Node> childrens = borderPane.getChildren();
        for (Node children: childrens){
            if (children instanceof TextField){
                ((TextField) children).setText(filename);
            }
            if (children instanceof StackPane stackPane){
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
                    MixedArea.applyStylesPage(pageLength);
                    MixedArea.applyStyles(pageLength, codeAreaLength);
                } else {
                    MixedArea.applyStyles(0, codeAreaLength);
                }
            }
        }
        borderPane.setStyle("-fx-background-color: #282a36");
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
    private void createNote(){
        createFile(treeView.getRoot());
    }

    public void createFile(TreeItem<String> parent){
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
    private void createFolderInTree(){
        createFolder(treeView.getRoot());
    }

    public void createFolder(TreeItem<String> treeItem){
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
    private void showFiles(){
        if (filesManagerSelected){
            return;
        }
        treeView.setRoot(filesManagerRoot);
        favoriteSelected = false;
    }

    @FXML
    private void showFavorites(){
        if (favoriteSelected){
            return;
        }
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
        filesManagerSelected = false;
    }

    @FXML
    private void changeStorage(){
        RunApplication.showInitialWindow();
    }

    @FXML
    private void showSearch(){

    }

    @FXML
    private void settings(){
        SettingsModal settingsModal = new SettingsModal();
        settingsModal.build(modalPane, splitPane);
    }

    private void loadItemsInTree(List<Path> pathList){
        pathList = pathList.stream().map(path -> RunApplication.FolderPath.relativize(path)).toList();
        pathList.forEach(item -> {
            String[] path = item.toString().split("\\\\");
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

    private void sortTreeView(){
        SortedList<TreeItem<String>> content = treeView.getRoot().getChildren().sorted(Comparator.comparing(TreeItem::getValue));
        treeView.getRoot().getChildren().setAll(content);
    }
}