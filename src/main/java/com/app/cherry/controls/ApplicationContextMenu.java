package com.app.cherry.controls;

import com.app.cherry.RunApplication;
import com.app.cherry.controllers.MainController;
import com.app.cherry.dao.FavoriteNotesDAO;
import com.app.cherry.util.Alerts;
import com.app.cherry.util.io.FileService;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.fxmisc.richtext.CodeArea;

import java.nio.file.Path;
import java.util.List;
import java.util.ResourceBundle;

public class ApplicationContextMenu {

    public static ContextMenu folderContextMenu;
    public static ContextMenu noteContextMenu;

    public static void buildTreeViewContextMenu(TreeView<String> treeView, MainController mainController,
                                                Stage renameStage, TabPane tabPane) {
        ResourceBundle resourceBundle = RunApplication.getResourceBundle();

        //menu item for creating new note
        MenuItem newNoteMenuItem = new MenuItem(resourceBundle.getString("ContextMenuNewNote"));
        newNoteMenuItem.setOnAction(actionEvent -> {
            TreeItem<String> selectedItem = treeView.getSelectionModel().getSelectedItem();
            mainController.createNote(selectedItem);
        });

        //menu item for creating new folder
        MenuItem newFolderMenuItem = new MenuItem(resourceBundle.getString("ContextMenuNewFolder"));
        newFolderMenuItem.setOnAction(actionEvent -> {
            TreeItem<String> selectedItem = treeView.getSelectionModel().getSelectedItem();
            mainController.createFolder(selectedItem);
        });

        //context menu for folder
        folderContextMenu = new ContextMenu(newNoteMenuItem, newFolderMenuItem,
                getRenameMenuItem(renameStage, mainController), getFavoriteMenuItem(treeView),
                getDeleteMenuItem(treeView, tabPane),
                getFolderOfFilesMenuItem(tabPane.getSelectionModel().getSelectedItem(), treeView));
        //context menu for note item
        noteContextMenu = new ContextMenu(getRenameMenuItem(renameStage, mainController),
                getFavoriteMenuItem(treeView), getDeleteMenuItem(treeView, tabPane));
    }

    private static MenuItem getFolderOfFilesMenuItem(Tab tab, TreeView<String> treeView) {
        MenuItem folderOfFiles = new MenuItem("Show view of files");
        folderOfFiles.setOnAction(actionEvent -> {
            MultipleSelectionModel<TreeItem<String>> selectionModel = treeView.getSelectionModel();
            Path absolutePath = Path.of(FileService.getPath(selectionModel.getSelectedItem()));
            String relativePath = RunApplication.folderPath.relativize(absolutePath).toString();
            FolderOfFiles.buildFolderTab(tab, selectionModel.getSelectedItem(), relativePath, treeView);
        });
        return folderOfFiles;
    }

    private static MenuItem getRenameMenuItem(Stage renameStage, MainController mainController) {
        MenuItem renameMenuItem = new MenuItem(RunApplication.getResourceBundle().getString("ContextMenuRename"));
        renameMenuItem.setOnAction(actionEvent -> {
            if (renameStage == null)
                mainController.openRenameWindow();
            else
                renameStage.show();
        });
        return renameMenuItem;
    }

    private static MenuItem getFavoriteMenuItem(TreeView<String> treeView) {
        MenuItem favoriteMenuItem = new MenuItem(RunApplication.getResourceBundle()
                .getString("ContextMenuFavorite"));
        favoriteMenuItem.setOnAction(actionEvent -> {
            TreeItem<String> selectedItem = treeView.getSelectionModel().getSelectedItem();
            FavoriteNotesDAO.setPathNote(FileService.getPath(selectedItem));
        });
        return favoriteMenuItem;
    }

    private static MenuItem getDeleteMenuItem(TreeView<String> treeView, TabPane tabPane) {
        ResourceBundle resourceBundle = RunApplication.getResourceBundle();
        MenuItem deleteMenuItem = new MenuItem(resourceBundle.getString("ContextMenuDelete"));
        deleteMenuItem.setOnAction(actionEvent -> {
            TreeItem<String> selectedItem = treeView.getSelectionModel().getSelectedItem();
            boolean isDelete = FileService.deleteFile(selectedItem);
            if (isDelete) {
                TreeItem<String> parentSelectedItem = selectedItem.getParent();
                parentSelectedItem.getChildren().remove(selectedItem);

                Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
                selectedTab.setText(resourceBundle.getString("EmptyTab"));
                TabBuilder.buildEmptyTab(selectedTab);
            } else {
                Alerts.createAndShowWarning(resourceBundle.getString("ContextMenuDeleteFailed"));
            }
        });
        return deleteMenuItem;
    }

    public static ContextMenu buildCodeAreaContextMenu(CodeArea codeArea) {
        MenuItem cutMenuItem = new MenuItem(RunApplication.getResourceBundle()
                .getString("ContextMenuCodeAreaCut"));
        cutMenuItem.setOnAction(actionEvent -> codeArea.cut());
        MenuItem copyMenuItem = new MenuItem(RunApplication.getResourceBundle()
                .getString("ContextMenuCodeAreaCopy"));
        copyMenuItem.setOnAction(actionEvent -> codeArea.copy());
        MenuItem pasteMenuItem = new MenuItem(RunApplication.getResourceBundle()
                .getString("ContextMenuCodeAreaPaste"));
        pasteMenuItem.setOnAction(actionEvent -> codeArea.paste());
        MenuItem deleteMenuItem = new MenuItem(
                RunApplication.getResourceBundle().getString("ContextMenuCodeAreaDelete"));
        deleteMenuItem.setOnAction(actionEvent -> codeArea.deleteText(codeArea.getSelection()));

        List.of(cutMenuItem, copyMenuItem, pasteMenuItem, deleteMenuItem)
                .forEach(menuItem -> menuItem.setStyle("-fx-font-size: 16px;"));
        MenuItem[] menuItems = {cutMenuItem, copyMenuItem, deleteMenuItem};
        for (MenuItem menuItem : menuItems) {
            menuItem.setDisable(true);
        }

        codeArea.selectedTextProperty().addListener(
                (observable, oldValue, newValue) -> {
                    boolean state = newValue.isEmpty();
                    for (MenuItem menuItem : menuItems) {
                        menuItem.setDisable(state);
                    }
                }
        );
        return new ContextMenu(cutMenuItem, copyMenuItem, pasteMenuItem, deleteMenuItem);
    }
}
