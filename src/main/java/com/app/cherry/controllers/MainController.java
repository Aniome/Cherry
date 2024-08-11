package com.app.cherry.controllers;

import com.app.cherry.controls.TabManager;
import com.app.cherry.dao.FavoriteNotesDAO;
import com.app.cherry.util.FileService;
import com.app.cherry.RunApplication;
import com.app.cherry.controls.EmptyExpandedTreeItem;
import com.app.cherry.controls.TreeCellFactory;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class MainController{
    @FXML
    private TreeView<String> treeView;
    @FXML
    private TabPane tabPane;
    @FXML
    private SplitPane splitPane;
    @FXML
    private GridPane gridPane;
    @FXML
    ToggleButton filesManagerButton;
    @FXML
    ToggleButton searchButton;
    @FXML
    ToggleButton favoriteNotesButton;
    @FXML
    VBox vbox;
    @FXML
    private Tab plusTab;

    final double renameWidth = 600;
    final double renameHeight = 250;
    Stage mainStage;
    Stage renameStage;
    public static String newFileName;
    TreeCellFactory treeCellFactory;
    boolean favoriteSelected = false;
    boolean filesManagerSelected = true;
    TreeItem<String> filesManagerRoot;

    @FXML
    private void CloseWindow(MouseEvent event) {
        Platform.exit();
    }

    public void init(Stage mainStage, RunApplication runApplication){
        this.mainStage = mainStage;

        treeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        filesManagerRoot = new TreeItem<>("");
        treeView.setRoot(filesManagerRoot);
        treeView.setShowRoot(false);
        loadFilesInTreeview();
        sortTreeView();
        CreateContextMenu.createContextMenu(treeView, this, renameStage, tabPane);

        treeCellFactory = new TreeCellFactory(treeView, this);

        splitPane.setDividerPositions(0.12);
        splitPane.widthProperty().addListener((observableValue, number, t1) -> splitPane.setDividerPositions(0.12));

        filesManagerButton.setSelected(true);

        configureToggleButtons();
        createScalable();
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

    private EmptyExpandedTreeItem creatingTreeItem(String str){
        if (str.contains(".md")) {
            str = str.replace(".md", "");
            return new EmptyExpandedTreeItem(str, true);
        } else {
            return new EmptyExpandedTreeItem(str, false);
        }
    }

    private void createScalable(){
        ObservableList<Node> children = gridPane.getChildren();
        gridPane.heightProperty().addListener((observableValue, number, t1) -> {
            double newHeight = (t1.doubleValue() / 10) * 8;
            for (Node button: children)
                ((ToggleButton)button).setPrefHeight(newHeight);
        });
        gridPane.widthProperty().addListener((observableValue, number, t1) -> {
            double newWidth = (t1.doubleValue() / 10) * 2;
            for (Node button: children)
                ((ToggleButton)button).setPrefWidth(newWidth);
        });
    }

    public void openModalWindow(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(RunApplication.class.getResource("fxmls/rename-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), renameWidth, renameHeight);
            Stage stage = new Stage();
            RunApplication.setIcon(stage);
            stage.setResizable(false);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(mainStage);
            stage.setOnHiding((event) -> {
                if (newFileName == null) {
                    return;
                }
                TreeItem<String> selectedItem = treeView.getSelectionModel().getSelectedItem();
                boolean b = FileService.renameFile(newFileName, selectedItem.getValue(), RunApplication.FolderPath.toString());
                if (b){
                    selectedItem.setValue(newFileName);
                    Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
                    selectedTab.setText(newFileName);
                }
            });
            renameStage = stage;
            RenameViewController renameViewController = fxmlLoader.getController();
            renameViewController.init(stage);
            RunApplication.prepareStage(renameHeight, renameWidth, scene, "Переименование элемента", stage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void loadDataOnFormOnClick(TreeItem<String> selectedItem){
        Tab tab = tabPane.getSelectionModel().getSelectedItem();
        tab.setContent(null);
        TabManager tabManager = new TabManager();
        BorderPane borderPane = tabManager.createTab(tab);
        String filename = selectedItem.getValue();
        tab.setText(filename);
        ObservableList<Node> childrens = borderPane.getChildren();
        for (Node children: childrens){
            if (children instanceof TextField){
                ((TextField) children).setText(filename);
            }
            if (children instanceof StackPane stackPane){
                VirtualizedScrollPane<CodeArea> virtualizedScrollPane = (VirtualizedScrollPane<CodeArea>) stackPane.getChildren().getFirst();
                CodeArea codeArea = virtualizedScrollPane.getContent();
                final String text = FileService.readFile(selectedItem);
                codeArea.replaceText(0,0, text);
                codeArea.textProperty().addListener((observableValue, s, t1) -> FileService.writeFile(selectedItem, codeArea));
            }
        }
        borderPane.setStyle("-fx-background-color: #282a36");
        tab.setContent(borderPane);
    }

    //Creates a tab and gives focus to it
    @FXML
    private Tab addTab() {
        Tab tab = new Tab("Новая вкладка");
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
        TreeItem<String> treeItem = new TreeItem<>(name);
        parent.getChildren().add(treeItem);
        sortTreeView();
    }

    @FXML
    private void createFolderInTree(){
        createFolder(treeView.getRoot());
    }

    public void createFolder(TreeItem<String> treeItem){
        File Folder = FileService.createFolderMarkdown(treeItem);
        if (Folder == null) {
            return;
        }
        EmptyExpandedTreeItem folder = new EmptyExpandedTreeItem(Folder.getName(), false);
        treeItem.getChildren().add(folder);
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
        FavoriteNotesDAO.getFavoriteNotes().forEach(item -> pathList.add(Paths.get(item.getPathNote())));
        loadItemsInTree(pathList);
        filesManagerSelected = false;
    }

    @FXML
    private void showSearch(){

    }

    private void loadItemsInTree(List<Path> pathList){
        pathList = pathList.stream().map(path -> RunApplication.FolderPath.relativize(path)).toList();
        pathList.forEach(item -> {
            String[] path = item.toString().split("\\\\");
            ObservableList<TreeItem<String>> rootList = treeView.getRoot().getChildren();
            //check tree contains file
            TreeItem<String> containedItem = null;
            EmptyExpandedTreeItem addedItem;
            for (TreeItem<String> i: rootList){
                if (path[0].equals(i.getValue()))
                    containedItem = i;
            }
            //if tree contains file
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