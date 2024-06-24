package com.app.cherry.controllers;

import com.app.cherry.controls.TabManager;
import com.app.cherry.dao.FavoriteNotesDAO;
import com.app.cherry.util.FileService;
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
            mainController.createFile(selectedItem);
        });
        MenuItem newFolderMenuItem = new MenuItem("Новая папка");
        newFolderMenuItem.setOnAction(actionEvent -> {
            TreeItem<String> selectedItem = treeView.getSelectionModel().getSelectedItem();
            mainController.createFolder(selectedItem);
        });


        folderContextMenu = new ContextMenu(newNoteMenuItem, newFolderMenuItem,
                getRenameMenuItem(renameStage, mainController), getFavoriteMenuItem(treeView),
                getDeleteMenuItem(treeView, tabPane));
        noteContextMenu = new ContextMenu(getRenameMenuItem(renameStage, mainController),
                getFavoriteMenuItem(treeView), getDeleteMenuItem(treeView, tabPane));
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

    private static MenuItem getFavoriteMenuItem(TreeView<String> treeView) {
        MenuItem favoriteMenuItem = new MenuItem("Добавить в закладки");
        favoriteMenuItem.setOnAction(actionEvent -> {
            TreeItem<String> selectedItem = treeView.getSelectionModel().getSelectedItem();
            FavoriteNotesDAO.setPathNote(FileService.getPath(selectedItem));
        });
        return favoriteMenuItem;
    }

    private static MenuItem getDeleteMenuItem(TreeView<String> treeView, TabPane tabPane) {
        MenuItem deleteMenuItem = new MenuItem("Удалить");
        deleteMenuItem.setOnAction(actionEvent -> {
            TreeItem<String> selectedItem = treeView.getSelectionModel().getSelectedItem();
            boolean isDelete = FileService.deleteFile(selectedItem);
            if (isDelete){
                TreeItem<String> parentSelectedItem = selectedItem.getParent();
                parentSelectedItem.getChildren().remove(selectedItem);
                Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
                selectedTab.setContent(TabManager.createEmptyTab());
                selectedTab.setText("Новая вкладка");
            } else {
                Alerts.CreateAndShowWarning("Не удалось удалить");
            }
        });
        return deleteMenuItem;
    }
}
