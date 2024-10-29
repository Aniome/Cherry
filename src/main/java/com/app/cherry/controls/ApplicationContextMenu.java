package com.app.cherry.controls;

import com.app.cherry.RunApplication;
import com.app.cherry.controllers.MainController;
import com.app.cherry.dao.FavoriteNotesDAO;
import com.app.cherry.util.Alerts;
import com.app.cherry.util.io.FileService;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.ResourceBundle;

public class ApplicationContextMenu {

    public static ContextMenu folderContextMenu;
    public static ContextMenu noteContextMenu;

    public static void createContextMenu(TreeView<String> treeView, MainController mainController,
                                         Stage renameStage, TabPane tabPane){
        ResourceBundle resourceBundle = RunApplication.resourceBundle;
        MenuItem newNoteMenuItem = new MenuItem(resourceBundle.getString("ContextMenuNewNote"));
        newNoteMenuItem.setOnAction(actionEvent -> {
            TreeItem<String> selectedItem = treeView.getSelectionModel().getSelectedItem();
            mainController.createFile(selectedItem);
        });
        MenuItem newFolderMenuItem = new MenuItem(resourceBundle.getString("ContextMenuNewFolder"));
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
        MenuItem renameMenuItem = new MenuItem(RunApplication.resourceBundle.getString("ContextMenuRename"));
        renameMenuItem.setOnAction(actionEvent -> {
            if (renameStage == null)
                mainController.openRenameWindow();
            else
                renameStage.show();
        });
        return renameMenuItem;
    }

    private static MenuItem getFavoriteMenuItem(TreeView<String> treeView) {
        MenuItem favoriteMenuItem = new MenuItem(RunApplication.resourceBundle.getString("ContextMenuFavorite"));
        favoriteMenuItem.setOnAction(actionEvent -> {
            TreeItem<String> selectedItem = treeView.getSelectionModel().getSelectedItem();
            FavoriteNotesDAO.setPathNote(FileService.getPath(selectedItem));
        });
        return favoriteMenuItem;
    }

    private static MenuItem getDeleteMenuItem(TreeView<String> treeView, TabPane tabPane) {
        ResourceBundle resourceBundle = RunApplication.resourceBundle;
        MenuItem deleteMenuItem = new MenuItem(resourceBundle.getString("ContextMenuDelete"));
        deleteMenuItem.setOnAction(actionEvent -> {
            TreeItem<String> selectedItem = treeView.getSelectionModel().getSelectedItem();
            boolean isDelete = FileService.deleteFile(selectedItem);
            if (isDelete){
                TreeItem<String> parentSelectedItem = selectedItem.getParent();
                parentSelectedItem.getChildren().remove(selectedItem);
                Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
                selectedTab.setContent(TabManager.createEmptyTab());
                selectedTab.setText(resourceBundle.getString("EmptyTab"));
            } else {
                Alerts.createAndShowWarning(resourceBundle.getString("ContextMenuDeleteFailed"));
            }
        });
        return deleteMenuItem;
    }
}
