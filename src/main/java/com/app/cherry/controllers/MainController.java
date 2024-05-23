package com.app.cherry.controllers;

import com.app.cherry.controls.EditableTreeCell;
import com.app.cherry.Markdown;
import com.app.cherry.RunApplication;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
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
    TreeCell<String> cell;
    final double renameWidth = 600;
    final double renameHeight = 250;
    Stage mainStage;
    Stage renameStage;
    public static String newFileName;
    ContextMenu contextMenu;
    TreeItem<String> selectedItem;
    Tab selectedTab;

    @FXML
    private void CloseWindow(MouseEvent event) {
        Platform.exit();
    }

    public void init(Stage mainStage){
        this.mainStage = mainStage;
        root = new TreeItem<>("");
        treeView.setRoot(root);
        treeView.setShowRoot(false);
        //Loading list files in treeview
        List<Path> pathList = Markdown.getListFiles();
        pathList = pathList.stream().map(path -> RunApplication.FolderPath.relativize(path)).toList();
        pathList.forEach(path -> {
            String[] paths = path.toString().split("\\\\");
            TreeItem<String> treeItem = root;
            TreeItem<String> newTreeItem;
            for (String str: paths){
                if (str.contains(".md"))
                    str = str.replace(".md", "");
                newTreeItem = new TreeItem<>(str);
                treeItem.getChildren().add(newTreeItem);
                treeItem = newTreeItem;
            }
        });
        CreateContextMenu();
        treeView.setCellFactory(tree -> {
            TreeCell<String> _cell = new EditableTreeCell();

            _cell.setOnMouseEntered( mouseEvent -> {
                TreeItem<String> treeItem = _cell.getTreeItem();
                if (treeItem == null)
                    return;
                if (ShowingContextMenu())
                    return;
                treeView.getSelectionModel().select(treeItem);
            });
            _cell.setOnMouseExited(mouseEvent -> {
                if (ShowingContextMenu())
                    return;
                treeView.setContextMenu(null);
                treeView.getSelectionModel().clearSelection();
            });
            _cell.setOnMouseClicked(event -> {
                TreeItem<String> selectedItem = _cell.getTreeItem();
                if (selectedItem == null || !selectedItem.isLeaf())
                    return;
                MouseButton mouseButton = event.getButton();
                if (mouseButton.equals(MouseButton.PRIMARY)){
                    LoadDataOnFormOnClick(selectedItem);
                }
                if (mouseButton.equals(MouseButton.SECONDARY)){
                    treeView.setContextMenu(contextMenu);
                }
            });
            cell = _cell;
            return _cell;
        });

        splitPane.setDividerPositions(0.12);
        splitPane.widthProperty().addListener((observableValue, number, t1) -> {
            splitPane.setDividerPositions(0.12);
        });

        createScalable();
    }

    private void createScalable(){
        var children = gridPane.getChildren();
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

    private void CreateContextMenu(){
        ContextMenu contextMenu = new ContextMenu();
        MenuItem menuItem1 = new MenuItem("Переименовать");
        menuItem1.setOnAction(actionEvent -> {
            selectedItem = treeView.getSelectionModel().getSelectedItem();
            selectedTab = tabPane.getSelectionModel().getSelectedItem();
            if (renameStage == null)
                OpenModalWindow();
            else
                renameStage.show();
        });
        MenuItem menuItem2 = new MenuItem("Добавить в закладки");
        MenuItem menuItem3 = new MenuItem("Удалить");
        menuItem3.setOnAction(actionEvent -> {
            selectedItem = treeView.getSelectionModel().getSelectedItem();
            boolean isDelete = Markdown.deleteFile(selectedItem);
            if (isDelete){
                root.getChildren().remove(selectedItem);
                selectedTab = tabPane.getSelectionModel().getSelectedItem();
                selectedTab.setContent(CreateEmptyTab());
            }
        });
        contextMenu.getItems().addAll(menuItem1, menuItem2, menuItem3);
        treeView.setContextMenu(contextMenu);
        this.contextMenu = contextMenu;
    }

    private void LoadDataOnFormOnClick(TreeItem<String> selectedItem){
        Tab tab = tabPane.getSelectionModel().getSelectedItem();
        tab.setContent(null);
        BorderPane borderPane = CreateTab(tab);
        String filename = selectedItem.getValue();
        tab.setText(filename);
        ObservableList<Node> childrens = borderPane.getChildren();
        for (Node children: childrens){
            if (children instanceof TextField){
                ((TextField) children).setText(filename);
            }
            if (children instanceof TextArea textArea){
                textArea.setText(Markdown.readFile(selectedItem));
                textArea.textProperty().addListener((observableValue, s, t1) -> {
                    Markdown.writeFile(selectedItem, textArea);
                });
            }
        }
        tab.setContent(borderPane);
    }

    private void OpenModalWindow(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(RunApplication.class.getResource("fxmls/rename-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), renameWidth, renameHeight);
            Stage stage = new Stage();
            RunApplication.SetIcon(stage);
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
            RunApplication.PrepareStage(renameHeight, renameWidth, scene, "Переименование элемента", stage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean ShowingContextMenu(){
        ContextMenu contMenu = treeView.getContextMenu();
        return contMenu != null && contMenu.isShowing();
    }

    //Creates a tab and gives focus to it
    @FXML
    private Tab AddTab() {
        Tab tab = new Tab("Новая вкладка");
        tab.setContent(CreateEmptyTab());
        SelectTab(tab);
        return tab;
    }

    private void AddTab(String fileName){
        Tab tab = new Tab(fileName);
        tab.setContent(CreateTab(tab));
        SelectTab(tab);
    }

    private void SelectTab(Tab tab){
        int count = tabPane.getTabs().size() - 1;
        SingleSelectionModel<Tab> selectionModel = tabPane.getSelectionModel();
        tabPane.getTabs().add(count, tab);
        selectionModel.select(tab);
    }

    //Event on the note creation button
    @FXML
    private void CreateNote(){
        File NewNote = Markdown.createFileMarkdown();
        if (NewNote == null){
            return;
        }
        String name = NewNote.getName().replace(".md", "");
        AddTab(name);
        TreeItem<String> treeItem = new TreeItem<>(name);
        root.getChildren().add(treeItem);
        SortTreeView();
    }

    @FXML
    private void CreateFolder(){
        File Folder = Markdown.createFolderMarkdown();
        if (Folder == null) {
            return;
        }
        TreeItem<String> folder = new TreeItem<>(Folder.getName());
        folder.getChildren().add(null);
        root.getChildren().add(folder);
        SortTreeView();
    }

    private void SortTreeView(){
        SortedList<TreeItem<String>> content = root.getChildren().sorted(Comparator.comparing(TreeItem::getValue));
        root.getChildren().setAll(content);
    }

    //Creates a form and fills it with content
    @NotNull
    private BorderPane CreateTab(Tab tab){
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

    private BorderPane CreateEmptyTab(){
        BorderPane borderPane = new BorderPane();
        Label label = new Label("Ни один файл не открыт");
        label.setFont(new Font(29));

        borderPane.setCenter(label);
        return borderPane;
    }
}