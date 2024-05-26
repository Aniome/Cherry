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
import javafx.scene.input.*;
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

    private static final DataFormat JAVA_FORMAT = DataFormat.PLAIN_TEXT;
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
    ContextMenu contextMenu;
    TreeItem<String> selectedItem;
    Tab selectedTab;
    /////////////
    TreeItem<String> draggedItem;

    @FXML
    private void CloseWindow(MouseEvent event) {
        Platform.exit();
    }

    public void init(Stage mainStage){
        this.mainStage = mainStage;
        root = new TreeItem<>("");
        treeView.setRoot(root);
        treeView.setShowRoot(false);
        loadFilesInTreeview();
        createContextMenu();
        treeView.setCellFactory(tree -> {
            TreeCell<String> treeCell = new EditableTreeCell();

            treeCell.setOnMouseEntered( mouseEvent -> {
                TreeItem<String> treeItem = treeCell.getTreeItem();
                if (treeItem == null)
                    return;
                if (showingContextMenu())
                    return;
                treeView.getSelectionModel().select(treeItem);
            });
            treeCell.setOnMouseExited(mouseEvent -> {
                if (showingContextMenu())
                    return;
                if (mouseEvent.isPrimaryButtonDown())
                    return;
                treeView.setContextMenu(null);
                treeView.getSelectionModel().clearSelection();
            });
            treeCell.setOnMouseClicked(event -> {
                TreeItem<String> selectedItem = treeCell.getTreeItem();
                if (selectedItem == null || !selectedItem.isLeaf())
                    return;
                MouseButton mouseButton = event.getButton();
                if (mouseButton.equals(MouseButton.PRIMARY)){
                    loadDataOnFormOnClick(selectedItem);
                }
                if (mouseButton.equals(MouseButton.SECONDARY)){
                    treeView.setContextMenu(contextMenu);
                }
            });
            //cell.setOnMousePressed(mouseEvent -> {});
            //cell.setOnMouseReleased(mouseEvent -> {});

            treeCell.setOnDragDetected((MouseEvent event) -> dragDetected(event, treeCell));
            treeCell.setOnDragOver((DragEvent event) -> dragOver(event, treeCell));
            treeCell.setOnDragDropped((DragEvent event) -> drop(event, treeCell, treeView));
            //treeCell.setOnDragDone((DragEvent event) -> clearDropLocation());

            return treeCell;
        });

        splitPane.setDividerPositions(0.12);
        splitPane.widthProperty().addListener((observableValue, number, t1) -> splitPane.setDividerPositions(0.12));

        createScalable();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    private void dragDetected(MouseEvent event, TreeCell<String> treeCell) {
        draggedItem = treeCell.getTreeItem();

        Dragboard db = treeCell.startDragAndDrop(TransferMode.MOVE);

        ClipboardContent content = new ClipboardContent();
        content.put(JAVA_FORMAT, draggedItem.getValue());
        db.setContent(content);
        db.setDragView(treeCell.snapshot(null, null));
        event.consume();
    }

    private void dragOver(DragEvent event, TreeCell<String> treeCell) {
        if (!event.getDragboard().hasContent(JAVA_FORMAT))
            return;
        TreeItem<String> thisItem = treeCell.getTreeItem();

        // can't drop on itself
        if (draggedItem == null || thisItem == null || thisItem == draggedItem)
            return;

        event.acceptTransferModes(TransferMode.MOVE);
    }

    private void drop(DragEvent event, TreeCell<String> treeCell, TreeView<String> treeView) {
        Dragboard db = event.getDragboard();
        boolean success = false;
        if (!db.hasContent(JAVA_FORMAT))
            return;

        TreeItem<String> thisItem = treeCell.getTreeItem();
        TreeItem<String> droppedItemParent = draggedItem.getParent();

        // remove from previous location
        droppedItemParent.getChildren().remove(draggedItem);

        // dropping on parent node makes it the first child
        if (Objects.equals(droppedItemParent, thisItem)) {
            thisItem.getChildren().add(0, draggedItem);
        }
        else {
            // add to new location
            int indexInParent = thisItem.getParent().getChildren().indexOf(thisItem);
            thisItem.getParent().getChildren().add(indexInParent + 1, draggedItem);
        }
        thisItem.getParent().getChildren().add(draggedItem);
        treeView.getSelectionModel().select(draggedItem);
        event.setDropCompleted(success);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    private void loadFilesInTreeview(){
        List<Path> pathList = Markdown.getListFiles();
        pathList = pathList.stream().map(path -> RunApplication.FolderPath.relativize(path)).toList();
        pathList.forEach(item -> {
            String[] path = item.toString().split("\\\\");
            int lastIndex = path.length - 1;
            path[lastIndex] = path[lastIndex].replace(".md", "");
            ObservableList<TreeItem<String>> rootList = root.getChildren();
            TreeItem<String> containedItem = null;
            for (TreeItem<String> i: rootList){
                if (path[0].equals(i.getValue()))
                    containedItem = i;
            }
            if (containedItem != null){
                TreeItem<String> treeItem = containedItem;
                boolean isContained = false;
                for (int i = 1; i < path.length; i++){
                    ObservableList<TreeItem<String>> treeList = treeItem.getChildren();
                    for (TreeItem<String> treeListItem: treeList){
                        if (path[i].equals(treeListItem.getValue())){
                            treeItem = treeListItem;
                            isContained = true;
                            break;
                        }
                    }
                    if (isContained){
                        continue;
                    }
                    treeList.add(new TreeItem<>(path[i]));
                    break;
                }
            } else if (path.length > 1) {
                //creating tree hierarchy
                TreeItem<String> treeItem = root;
                TreeItem<String> newTreeItem;
                for (String str: path){
                    newTreeItem = new TreeItem<>(str);
                    treeItem.getChildren().add(newTreeItem);
                    treeItem = newTreeItem;
                }
            } else {
                rootList.add(new TreeItem<>(path[0]));
            }
        });
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

    private void createContextMenu(){
        ContextMenu contextMenu = new ContextMenu();
        MenuItem menuItem1 = new MenuItem("Переименовать");
        menuItem1.setOnAction(actionEvent -> {
            selectedItem = treeView.getSelectionModel().getSelectedItem();
            selectedTab = tabPane.getSelectionModel().getSelectedItem();
            if (renameStage == null)
                openModalWindow();
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
                selectedTab.setContent(createEmptyTab());
                selectedTab.setText("Новая вкладка");
            }
        });
        contextMenu.getItems().addAll(menuItem1, menuItem2, menuItem3);
        treeView.setContextMenu(contextMenu);
        this.contextMenu = contextMenu;
    }

    private void loadDataOnFormOnClick(TreeItem<String> selectedItem){
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

    private boolean showingContextMenu(){
        ContextMenu contMenu = treeView.getContextMenu();
        return contMenu != null && contMenu.isShowing();
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
        File NewNote = Markdown.createFileMarkdown();
        if (NewNote == null){
            return;
        }
        String name = NewNote.getName().replace(".md", "");
        addTab(name);
        TreeItem<String> treeItem = new TreeItem<>(name);
        root.getChildren().add(treeItem);
        sortTreeView();
    }

    @FXML
    private void createFolder(){
        File Folder = Markdown.createFolderMarkdown();
        if (Folder == null) {
            return;
        }
        TreeItem<String> folder = new TreeItem<>(Folder.getName());
        folder.getChildren().add(null);
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