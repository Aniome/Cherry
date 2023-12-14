package com.app.cherry;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
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

    @FXML
    private void CloseWindow(MouseEvent event) {
        Platform.exit();
    }

    public void init(){
        //listview.getStylesheets().add(Objects.requireNonNull(MainController.class.getResource("css/listview.css")).toExternalForm());
        treeView.setRoot(new TreeItem<>(""));
        treeView.setShowRoot(false);
    }

    //Creates a tab and gives focus to it
    @FXML
    private Tab AddTab() {
        CreateFileMarkdown();
        Tab tab = new Tab(Unknown);
        tab.setContent(CreateTab(tab));
        int ind = Tab_Pane.getTabs().size() - 1;
        SingleSelectionModel<Tab> selectionModel = Tab_Pane.getSelectionModel();
        Tab_Pane.getTabs().add(ind, tab);
        selectionModel.select(tab);
        return tab;
    }

    @FXML
    private void CreateNote(){
        Tab tab = AddTab();
        TreeItem<String> root = treeView.getRoot();
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
        treeView.getRoot().getChildren().add(treeItem);
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

    private boolean CreateFileMarkdown(){
        File file = new File(RunApplication.FolderPath.toString());
        try {
            if (file.createNewFile()){
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}