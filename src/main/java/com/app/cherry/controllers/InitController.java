package com.app.cherry.controllers;

import com.app.cherry.RunApplication;
import com.app.cherry.controls.listViewItems.ListCellItemInitPage;
import com.app.cherry.dao.RecentPathsDAO;
import com.app.cherry.util.Alerts;
import com.app.cherry.util.configuration.SavingConfiguration;
import com.app.cherry.util.io.FileService;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class InitController {
    @FXML
    private HBox upHBox;
    @FXML
    private HBox downHBox;
    @FXML
    private Button back;
    @FXML
    private Button create;
    @FXML
    private Label downLabel;
    @FXML
    private Button openButton;
    @FXML
    private ListView<String> listView;
    @FXML
    private SplitPane splitPane;

    private TextField textField;

    public Stage initialStage;

    @FXML
    private void templateStorage() {
        ResourceBundle resourceBundle = RunApplication.getResourceBundle();
        //change buttons
        changeControls(upHBox, new String[]{resourceBundle.getString("InitNameStorage")});
        changeControls(downHBox, new String[]{resourceBundle.getString("InitLocation"),
                resourceBundle.getString("InitBrowse")});

        textField = new TextField() {{
            setFont(new Font(18));
        }};
        upHBox.getChildren().add(textField);

        //Change visible buttons
        changeVisibleButtons(true);
    }

    private void changeControls(HBox Hbox, String[] strings) {
        Iterator<Node> iterator = createIterator(Hbox);
        iterationControls(iterator, strings);
    }

    private void iterationControls(Iterator<Node> iterator, String[] str) {
        while (iterator.hasNext()) {
            Node node = iterator.next();
            if (node instanceof Label) {
                ((Label) node).setText(str[0]);
            }
            if (node instanceof Button) {
                if (str.length < 2) {
                    iterator.remove();
                }else{
                    ((Button) node).setText(str[1]);
                }
            }
            if (node instanceof TextField) {
                iterator.remove();
            }
        }
    }

    private void changeVisibleButtons(boolean value){
        back.setVisible(value);
        create.setVisible(value);
    }

    private Iterator<Node> createIterator(HBox Hbox){
        return Hbox.getChildren().iterator();
    }

    @FXML
    private void openStorage() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        Optional<File> selectedDirectory = Optional.ofNullable(directoryChooser.showDialog(initialStage));
        selectedDirectory.ifPresent(file -> {
            RunApplication.folderPath = Paths.get(file.toURI());
            RunApplication.buildSeparatorAndAppPath();
            ResourceBundle resourceBundle = RunApplication.getResourceBundle();
            if (openButton.getText().equals(resourceBundle.getString("InitBrowse"))) {
                downLabel.setFont(new Font(12));
                downLabel.setText(resourceBundle.getString("InitStoragePath") + " "
                        + RunApplication.folderPath.toString());
            }else {
                showMainStage();
            }
        });
    }

    @FXML
    private void backToMainMenu() {
        ResourceBundle resourceBundle = RunApplication.getResourceBundle();
        changeControls(upHBox, new String[]{resourceBundle.getString("NewStorage")});
        changeControls(downHBox, new String[]{resourceBundle.getString("OpenStorage"),
                resourceBundle.getString("OpenButton")});

        upHBox.getChildren().add(new Button(resourceBundle.getString("CreateButton")){{
            setFont(new Font(18));
            setOnMouseClicked(mouseEvent -> templateStorage());
        }});

        changeVisibleButtons(false);
        downLabel.setText("");
    }

    @FXML
    private void createStorage() {
        ResourceBundle resourceBundle = RunApplication.getResourceBundle();
        String folderName = textField.getText();
        if (folderName.isEmpty()){
            Alerts.createAndShowWarning(resourceBundle.getString("InitLabelNameStorage"));
            return;
        }
        if (RunApplication.folderPath == null){
            Alerts.createAndShowWarning(resourceBundle.getString("InitLabelPathStorage"));
            return;
        }
        RunApplication.buildSeparatorAndAppPath();
        String path = RunApplication.folderPath.toString() + RunApplication.getSeparator() + folderName;
        RunApplication.folderPath = Paths.get(path);
        File folder = new File(path);
        if (folder.mkdir()) {
            showMainStage();
        } else {
            Alerts.createAndShowWarning(resourceBundle.getString("InitFailedCreateFolder"));
        }
    }

    public void showMainStage() {
        SavingConfiguration.preparationMainStage = true;
        initialStage.close();
        RunApplication.showMainWindow();
    }

    public void loadPaths() {
        List<String> listRecentPaths = RecentPathsDAO.getPaths();
        Iterator<String> iterator = listRecentPaths.iterator();
        while (iterator.hasNext()){
            String path = iterator.next();
            boolean isExist = FileService.checkExists(path);
            if (!isExist){
                RecentPathsDAO.removePath(path);
                iterator.remove();
            }
        }

        listView.setCellFactory(lvItem -> {
            ListCellItemInitPage treeCell = new ListCellItemInitPage(listView, this);
            MultipleSelectionModel<String> selectionModel = listView.getSelectionModel();
            treeCell.setOnMouseEntered( mouseEvent -> {
                String treeCellItem = treeCell.getItem();
                if (treeCellItem == null)
                    return;
                selectionModel.select(treeCellItem);
            });
            treeCell.setOnMouseExited(mouseEvent -> selectionModel.clearSelection());

            return treeCell;
        });
        listView.getItems().addAll(listRecentPaths);
        splitPane.setDividerPositions(0.35);
    }
}
