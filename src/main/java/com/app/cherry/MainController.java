package com.app.cherry;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.util.Callback;
import org.jetbrains.annotations.NotNull;

import java.io.File;
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

    @FXML
    private void CloseWindow(MouseEvent event) {
        Platform.exit();
    }

    public void init(){
        //listview.getStylesheets().add(Objects.requireNonNull(MainController.class.getResource("css/listview.css")).toExternalForm());
        root = new TreeItem<>("");
        treeView.setRoot(root);
        treeView.setShowRoot(false);
        //Loading list files in treeview
        Arrays.stream(Markdown.getFiles()).forEach(file -> {
            TreeItem treeItem = new TreeItem<>(file.getName().replace(".md",""));
            root.getChildren().add(treeItem);
        });
        treeView.setCellFactory(tree -> {
            TreeCell<String> cell = new TreeCell<>() {
                @Override
                public void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);
                    } else {
                        setText(item);
                    }
                }
            };
            cell.setOnMouseEntered( mouseEvent -> {
                TreeItem<String> treeItem = cell.getTreeItem();
                if (treeItem == null)
                    return;
                treeView.getSelectionModel().select(treeItem);
            });
            cell.setOnMouseExited(mouseEvent -> {
                treeView.getSelectionModel().clearSelection();
            });
            cell.setOnMouseClicked(event -> {
                if (cell.isEmpty()) {
                    return;
                }
                TreeItem<String> selectedItem = cell.getTreeItem();
                MouseButton mouseButton = event.getButton();
                //Load data in form on click
                if (mouseButton.equals(MouseButton.PRIMARY)){
                    Tab SelectedTab = Tab_Pane.getSelectionModel().getSelectedItem();
                    SelectedTab.setContent(null);
                    BorderPane borderPane = CreateTab(SelectedTab);
                    String filename = selectedItem.getValue();
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
                }
                if (mouseButton.equals(MouseButton.SECONDARY) && cell.getTreeItem() != null){
                    ContextMenu contextMenu = new ContextMenu();
                    MenuItem menuItem1 = new MenuItem("Переименовать");
                    //menuItem1.setOnAction(actionEvent -> new EditableTreeCell().Edit());
                    MenuItem menuItem2 = new MenuItem("Переместить файл в");
                    MenuItem menuItem3 = new MenuItem("Добавить в закладки");
                    MenuItem menuItem4 = new MenuItem("Удалить");
                    contextMenu.getItems().addAll(menuItem1, menuItem2, menuItem3, menuItem4);
                    treeView.setContextMenu(contextMenu);
                }

            });
            return cell ;
        });

        splitpane.widthProperty().addListener((observableValue, number, t1) -> {
            splitpane.setDividerPositions(0.16353677621283255);
        });
    }

    //Creates a tab and gives focus to it
    @FXML
    private Tab AddTab() {
        Tab tab = new Tab("Новая вкладка");
        tab.setContent(CreateEmptyTab(tab));
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
        AddTab(NewNote.getName());
        TreeItem<String> treeItem = new TreeItem<>(NewNote.getName());
        root.getChildren().add(treeItem);
        SortedList<TreeItem<String>> content = root.getChildren().sorted(Comparator.comparing(TreeItem::getValue));
        root.getChildren().setAll(content);
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
        TextField textField = new TextField(tab.getText()){{
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

    private BorderPane CreateEmptyTab(Tab tab){
        BorderPane borderPane = new BorderPane();
        Label label = new Label("Ни один файл не открыт");
        label.setFont(new Font(29));

        borderPane.setCenter(label);
        return borderPane;
    }
}