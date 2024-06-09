package com.app.cherry.controllers;

import com.app.cherry.Markdown;
import com.app.cherry.RunApplication;
import com.app.cherry.controls.EmptyExpandedTreeItem;
import com.app.cherry.controls.TreeCellFactory;
import com.app.cherry.util.Alerts;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
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

    private String oldTextFieldValue;
    private TreeItem<String> root;
    final double renameWidth = 600;
    final double renameHeight = 250;
    Stage mainStage;
    Stage renameStage;
    public static String newFileName;
    TreeItem<String> selectedItem;
    Tab selectedTab;
    TreeCellFactory treeCellFactory;
    public ContextMenu contextMenu;

    @FXML
    private void CloseWindow(MouseEvent event) {
        Platform.exit();
    }

    public void init(Stage mainStage){
        this.mainStage = mainStage;
        treeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        root = new TreeItem<>("");
        treeView.setRoot(root);
        treeView.setShowRoot(false);
        loadFilesInTreeview();
        sortTreeView();
        createContextMenu();

        treeCellFactory = new TreeCellFactory(treeView, this);

        splitPane.setDividerPositions(0.12);
        splitPane.widthProperty().addListener((observableValue, number, t1) -> splitPane.setDividerPositions(0.12));

        createScalable();
    }

    private void loadFilesInTreeview(){
        List<Path> pathList = Markdown.getListFiles();
        pathList = pathList.stream().map(path -> RunApplication.FolderPath.relativize(path)).toList();
        pathList.forEach(item -> {
            String[] path = item.toString().split("\\\\");
            ObservableList<TreeItem<String>> rootList = root.getChildren();
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
                TreeItem<String> treeItem = root;
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
                ((Button)button).setPrefHeight(newHeight);
        });
        gridPane.widthProperty().addListener((observableValue, number, t1) -> {
            double newWidth = (t1.doubleValue() / 10) * 2;
            for (Node button: children)
                ((Button)button).setPrefWidth(newWidth);
        });
    }

    private void createContextMenu(){
        contextMenu = new ContextMenu();
        MenuItem menuItem1 = new MenuItem("Новая заметка");
        menuItem1.setOnAction(actionEvent -> {
            selectedItem = treeView.getSelectionModel().getSelectedItem();
            TreeItem<String> parent = selectedItem.getParent();
            createFile(parent);
        });
        MenuItem menuItem2 = new MenuItem("Новая папка");
        menuItem2.setOnAction(actionEvent -> {
            //createFolder();
        });
        MenuItem menuItem3 = new MenuItem("Переименовать");
        menuItem3.setOnAction(actionEvent -> {
            selectedItem = treeView.getSelectionModel().getSelectedItem();
            selectedTab = tabPane.getSelectionModel().getSelectedItem();
            if (renameStage == null)
                openModalWindow();
            else
                renameStage.show();
        });
        MenuItem menuItem4 = new MenuItem("Добавить в закладки");
        MenuItem menuItem5 = new MenuItem("Удалить");
        menuItem5.setOnAction(actionEvent -> {
            selectedItem = treeView.getSelectionModel().getSelectedItem();
            boolean isDelete = Markdown.deleteFile(selectedItem);
            if (isDelete){
                TreeItem<String> parentSelectedItem = selectedItem.getParent();
                parentSelectedItem.getChildren().remove(selectedItem);
                selectedTab = tabPane.getSelectionModel().getSelectedItem();
                selectedTab.setContent(createEmptyTab());
                selectedTab.setText("Новая вкладка");
            } else {
                Alerts.CreateAndShowWarning("Не удалось удалить");
            }
        });
        contextMenu.getItems().addAll(menuItem1, menuItem2, menuItem3, menuItem4, menuItem5);
        treeView.setContextMenu(contextMenu);
    }

    private void openModalWindow(){
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
                boolean b = Markdown.renameFile(newFileName, selectedItem.getValue(), RunApplication.FolderPath.toString());
                if (b){
                    selectedItem.setValue(newFileName);
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
        BorderPane borderPane = createTab(tab);
        String filename = selectedItem.getValue();
        tab.setText(filename);
        ObservableList<Node> childrens = borderPane.getChildren();
        for (Node children: childrens){
            if (children instanceof TextField){
                ((TextField) children).setText(filename);
            }
            if (children instanceof TextArea textArea){
                textArea.setText(Markdown.readFile(selectedItem));
                textArea.textProperty().addListener((observableValue, s, t1) -> Markdown.writeFile(selectedItem, textArea));
            }
        }
        tab.setContent(borderPane);
    }

    //Creates a tab and gives focus to it
    @FXML
    private Tab addTab() {
        Tab tab = new Tab("Новая вкладка");
        tab.setContent(createEmptyTab());
        selectTab(tab);
        return tab;
    }

    private void addTab(String fileName){
        Tab tab = new Tab(fileName);
        tab.setContent(createTab(tab));
        selectTab(tab);
    }

    private void selectTab(Tab tab){
        int count = tabPane.getTabs().size() - 1;
        SingleSelectionModel<Tab> selectionModel = tabPane.getSelectionModel();
        tabPane.getTabs().add(count, tab);
        selectionModel.select(tab);
    }

    //Event on the note creation button
    @FXML
    private void createNote(){
        createFile(root);
    }

    private void createFile(TreeItem<String> parent) {
        File newNote = Markdown.createFileMarkdown(parent);
        if (newNote == null){
            return;
        }
        String name = newNote.getName().replace(".md", "");
        addTab(name);
        TreeItem<String> treeItem = new TreeItem<>(name);
        parent.getChildren().add(treeItem);
        sortTreeView();
    }

    @FXML
    private void createFolder(){
        File Folder = Markdown.createFolderMarkdown();
        if (Folder == null) {
            return;
        }
        EmptyExpandedTreeItem folder = new EmptyExpandedTreeItem(Folder.getName(), false);
        root.getChildren().add(folder);
        sortTreeView();
    }

    private void sortTreeView(){
        SortedList<TreeItem<String>> content = root.getChildren().sorted(Comparator.comparing(TreeItem::getValue));
        root.getChildren().setAll(content);
    }

    //Creates a form and fills it with content
    @NotNull
    private BorderPane createTab(Tab tab){
        BorderPane borderPane = new BorderPane();
        TextField textField = new TextField(tab.getText()){{
            setFont(new Font(16));
            setAlignment(Pos.CENTER);
        }};
        textField.getStylesheets().add(Objects.requireNonNull(RunApplication.class.getResource("css/text_field.css")).toExternalForm());
        borderPane.setTop(textField);

        textField.focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
            //newPropertyValue - on focus
            //oldPropertyValue - out focus
            if (newPropertyValue) {
                oldTextFieldValue = textField.getText();
            }
            if (oldPropertyValue && textField.getText().isEmpty()) {
                textField.setText(oldTextFieldValue);
            } else {
                tab.setText(textField.getText());
            }
        });

        TextArea textArea = new TextArea(){{
            setFont(new Font(16));
        }};
        textArea.getStylesheets().add(Objects.requireNonNull(RunApplication.class.getResource("css/text_area.css")).toExternalForm());
        borderPane.setCenter(textArea);

        return borderPane;
    }

    private BorderPane createEmptyTab(){
        BorderPane borderPane = new BorderPane();
        Label label = new Label("Ни один файл не открыт");
        label.setFont(new Font(29));

        borderPane.setCenter(label);
        return borderPane;
    }
}