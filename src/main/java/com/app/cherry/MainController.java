package com.app.cherry;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class MainController{

    @FXML
    private TreeView<String> treeView;

    @FXML
    private TabPane Tab_Pane;

    private final String Unknown = "Без названия";
    private String OldTextFieldValue;
    private TreeItem<String> root;

    @FXML
    private void CloseWindow(MouseEvent event) {
        Platform.exit();
    }

    public void init(){
        //listview.getStylesheets().add(Objects.requireNonNull(MainController.class.getResource("css/listview.css")).toExternalForm());
        root = new TreeItem<>("");
        treeView.setRoot(root);
        treeView.setShowRoot(false);
        String[] files = Markdown.getFiles();
        for (String file :files){
            root.getChildren().add(new TreeItem(file));
        }
        //Load data on click
        treeView.getSelectionModel().selectedItemProperty().addListener((observableValue, stringTreeItem, t1) -> {
            Tab SelectedTab = Tab_Pane.getSelectionModel().getSelectedItem();
            SelectedTab.setContent(null);
            BorderPane borderPane = CreateTab(SelectedTab);
            String filename = t1.getValue();
            SelectedTab.setText(filename);
            ObservableList<Node> childrens = borderPane.getChildren();
            for (Node children: childrens){
                if (children instanceof TextField){
                    ((TextField) children).setText(filename);
                }
                if (children instanceof TextArea){
                    ((TextArea) children).setText(Markdown.ReadFile(filename));
                }
            }
            SelectedTab.setContent(borderPane);
        });
    }

    //Creates a tab and gives focus to it
    @FXML
    private Tab AddTab() {
        Tab tab = new Tab(Unknown);
        tab.setContent(CreateTab(tab));
        int ind = Tab_Pane.getTabs().size() - 1;
        SingleSelectionModel<Tab> selectionModel = Tab_Pane.getSelectionModel();
        Tab_Pane.getTabs().add(ind, tab);
        selectionModel.select(tab);
        return tab;
    }

    //Event on the note creation button
    @FXML
    private void CreateNote(){
        if (Markdown.CreateFileMarkdown() == null){
            return;
        }
        Tab tab = AddTab();
        //Creates treeitem and does the binding
        TreeItem<String> treeItem = new TreeItem<>(Unknown);
        treeItem.valueProperty().bind(tab.textProperty());
        //treeItem.setValue();
        root.getChildren().add(treeItem);
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
        TreeItem<String> treeItem = new TreeItem<>("Test");
        treeItem.getChildren().add(new TreeItem<>());
        root.getChildren().add(treeItem);
    }

    //Creates a form and fills it with content
    @NotNull
    private BorderPane CreateTab(Tab tab){
        BorderPane borderPane = new BorderPane();
        TextField textField = new TextField(Unknown){{
            setFont(new Font(16));
            setAlignment(Pos.CENTER);
        }};
        textField.getStylesheets().add(Objects.requireNonNull(MainController.class.getResource("css/text_field.css")).toExternalForm());
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
        textArea.getStylesheets().add(Objects.requireNonNull(MainController.class.getResource("css/text_area.css")).toExternalForm());
        borderPane.setCenter(textArea);

        return borderPane;
    }
}