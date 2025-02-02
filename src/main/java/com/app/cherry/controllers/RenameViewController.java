package com.app.cherry.controllers;

import com.app.cherry.RunApplication;
import com.app.cherry.util.Alerts;
import com.app.cherry.util.io.FileService;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class RenameViewController {
    @FXML
    private TextField txtField;

    private Stage stage;
    private TreeItem<String> selectedTreeItem;
    private Tab selectedTab;

    public void init(Stage stage, TreeItem<String> selectedTreeItem, Tab selectedTab) {
        this.stage = stage;
        this.selectedTreeItem = selectedTreeItem;
        this.selectedTab = selectedTab;
    }

    @FXML
    private void confirm(){
        String newFileName = txtField.getText();
        if (newFileName.isEmpty()) {
            Alerts.createAndShowWarning(RunApplication.resourceBundle.getString("RenameEmptyTextField"));
            return;
        }

        String pathTreeItem = FileService.getPath(selectedTreeItem);
        int lastIndexOfSeparator = pathTreeItem.lastIndexOf(RunApplication.separator);
        pathTreeItem = pathTreeItem.substring(0, lastIndexOfSeparator);
        //renameFile - newName, oldFile, path
        boolean successfulRename = FileService.renameFile(newFileName, selectedTreeItem.getValue(), pathTreeItem);
        if (successfulRename) {
            selectedTreeItem.setValue(newFileName);

            BorderPane borderPaneContent = (BorderPane) selectedTab.getContent();
            HBox titleHbox = (HBox) borderPaneContent.getTop();
            if (titleHbox == null) {
                //if tab is empty, exit
                return;
            }
            selectedTab.setText(newFileName);
            TextField noteName = (TextField) titleHbox.getChildren().getFirst();
            noteName.setText(newFileName);
        }

        stage.close();
    }

    @FXML
    private void cancel(){
        stage.close();
    }
}
