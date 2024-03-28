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
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

public class MainController{

    @FXML
    private TreeView<String> treeView;

    @FXML
    private TabPane Tab_Pane;
    @FXML
    private SplitPane splitpane;

    private String OldTextFieldValue;
    private TreeItem<String> root;
    TreeCell<String> cell;
    final double RenameWidth = 600;
    final double RenameHeight = 250;
    Stage MainStage;
    Stage RenameStage;
    public static String NewFileName;
    ContextMenu contextMenu;
    TreeItem<String> SelectedItem;
    Tab SelectedTab;

    @FXML
    private void CloseWindow(MouseEvent event) {
        Platform.exit();
    }

    public void init(Stage mainStage){
        this.MainStage = mainStage;
        root = new TreeItem<>("");
        treeView.setRoot(root);
        treeView.setShowRoot(false);
        //Loading list files in treeview
        Arrays.stream(Markdown.getFiles()).forEach(file -> {
            TreeItem treeItem = new TreeItem<>(file.getName().replace(".md",""));
            root.getChildren().add(treeItem);
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
                if (selectedItem == null)
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

        splitpane.widthProperty().addListener((observableValue, number, t1) -> {
            splitpane.setDividerPositions(0.16353677621283255);
        });
    }

    private void CreateContextMenu(){
        ContextMenu contextMenu = new ContextMenu();
        MenuItem menuItem1 = new MenuItem("Переименовать");
        menuItem1.setOnAction(actionEvent -> {
            SelectedItem = treeView.getSelectionModel().getSelectedItem();
            SelectedTab = Tab_Pane.getSelectionModel().getSelectedItem();
            if (RenameStage == null)
                OpenModalWindow();
            else
                RenameStage.show();
        });
        MenuItem menuItem2 = new MenuItem("Переместить файл в");
        MenuItem menuItem3 = new MenuItem("Добавить в закладки");
        MenuItem menuItem4 = new MenuItem("Удалить");
        contextMenu.getItems().addAll(menuItem1, menuItem2, menuItem3, menuItem4);
        treeView.setContextMenu(contextMenu);
        this.contextMenu = contextMenu;
    }

    private void LoadDataOnFormOnClick(TreeItem<String> selectedItem){
        Tab tab = Tab_Pane.getSelectionModel().getSelectedItem();
        tab.setContent(null);
        BorderPane borderPane = CreateTab(tab);
        String filename = selectedItem.getValue();
        tab.setText(filename);
        ObservableList<Node> childrens = borderPane.getChildren();
        for (Node children: childrens){
            if (children instanceof TextField){
                ((TextField) children).setText(filename);
            }
            if (children instanceof TextArea){
                ((TextArea) children).setText(Markdown.ReadFile(filename));
            }
        }
        tab.setContent(borderPane);
    }

    private void OpenModalWindow(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(RunApplication.class.getResource("fxmls/rename-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), RenameWidth, RenameHeight);
            Stage stage = new Stage();
            RunApplication.SetIcon(stage);
            stage.setResizable(false);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(MainStage);
            stage.setOnHiding((event) -> {
                if (NewFileName == null) {
                    return;
                }
                boolean b = Markdown.RenameFile(NewFileName, SelectedItem.getValue(), RunApplication.FolderPath.toString());
                if (b){
                    SelectedItem.setValue(NewFileName);
                    SelectedTab.setText(NewFileName);
                }
            });
            RenameStage = stage;
            RenameViewController renameViewController = fxmlLoader.getController();
            renameViewController.init(stage);
            RunApplication.PrepareStage(RenameHeight, RenameWidth, scene, "Переименование элемента", stage);
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

    private Tab AddTab(String fileName){
        Tab tab = new Tab(fileName);
        tab.setContent(CreateTab(tab));
        SelectTab(tab);
        return tab;
    }

    private void SelectTab(Tab tab){
        int count = Tab_Pane.getTabs().size() - 1;
        SingleSelectionModel<Tab> selectionModel = Tab_Pane.getSelectionModel();
        Tab_Pane.getTabs().add(count, tab);
        selectionModel.select(tab);
    }

    //Event on the note creation button
    @FXML
    private void CreateNote(){
        File NewNote = Markdown.CreateFileMarkdown();
        if (NewNote == null){
            return;
        }
        String name = NewNote.getName().replace(".md", "");
        AddTab(name);
        TreeItem<String> treeItem = new TreeItem<>(name);
        root.getChildren().add(treeItem);
        SortTreeView();
    }

    private boolean CheckTree(String str){
        var tree = treeView.getRoot().getChildren();
        for (TreeItem<String> s: tree){
            if (s.getValue().equals(str)){
                return true;
            }
        }
        return false;
    }

    @FXML
    private void CreateFolder(){
        File Folder = Markdown.CreateFolderMarkdown();
        if (Folder == null) {
            return;
        }
        TreeItem<String> treeItem = new TreeItem<>(Folder.getName());
        treeItem.getChildren().add(new TreeItem<>());
        root.getChildren().add(treeItem);
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
                OldTextFieldValue = textField.getText();
            }
            if (oldPropertyValue && textField.getText().isEmpty()) {
                textField.setText(OldTextFieldValue);
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