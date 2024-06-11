package com.app.cherry.controllers;

import com.app.cherry.Markdown;
import com.app.cherry.util.Alerts;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class CreateContextMenu {

    public static ContextMenu folderContextMenu;
    public static ContextMenu noteContextMenu;

    public static void createContextMenu(TreeView<String> treeView, MainController mainController, Stage renameStage, TabPane tabPane){
        MenuItem newNoteMenuItem = new MenuItem("Новая заметка");
        newNoteMenuItem.setOnAction(actionEvent -> {
            TreeItem<String> selectedItem = treeView.getSelectionModel().getSelectedItem();
            TreeItem<String> parent = selectedItem.getParent();
            mainController.createFile(parent);
        });
        MenuItem newFolderMenuItem = new MenuItem("Новая папка");
        newFolderMenuItem.setOnAction(actionEvent -> {
            //createFolder();
        });


        folderContextMenu = new ContextMenu(newNoteMenuItem, newFolderMenuItem,
                getRenameMenuItem(renameStage, mainController), getFavoriteMenuItem(), getDeleteMenuItem(treeView, tabPane, mainController));
        noteContextMenu = new ContextMenu(getRenameMenuItem(renameStage, mainController), getFavoriteMenuItem(),
                getDeleteMenuItem(treeView, tabPane, mainController));
    }


    private static MenuItem getRenameMenuItem(Stage renameStage, MainController mainController) {
        MenuItem renameMenuItem = new MenuItem("Переименовать");
        renameMenuItem.setOnAction(actionEvent -> {
            if (renameStage == null)
                mainController.openModalWindow();
            else
                renameStage.show();
        });
        return renameMenuItem;
    }

    private static MenuItem getFavoriteMenuItem() {
        return new MenuItem("Добавить в закладки");
    }

    private static MenuItem getDeleteMenuItem(TreeView<String> treeView, TabPane tabPane, MainController mainController) {
        MenuItem deleteMenuItem = new MenuItem("Удалить");
        deleteMenuItem.setOnAction(actionEvent -> {
            TreeItem<String> selectedItem = treeView.getSelectionModel().getSelectedItem();
            boolean isDelete = Markdown.deleteFile(selectedItem);
            if (isDelete){
                TreeItem<String> parentSelectedItem = selectedItem.getParent();
                parentSelectedItem.getChildren().remove(selectedItem);
                Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
                selectedTab.setContent(mainController.createEmptyTab());
                selectedTab.setText("Новая вкладка");
            } else {
                Alerts.CreateAndShowWarning("Не удалось удалить");
            }
        });
        return deleteMenuItem;
    }
}
