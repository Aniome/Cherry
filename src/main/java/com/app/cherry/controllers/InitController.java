package com.app.cherry.controllers;

import com.app.cherry.RunApplication;
import com.app.cherry.controls.listViewItems.ListCellItemInitPage;
import com.app.cherry.dao.RecentPathsDAO;
import com.app.cherry.util.Alerts;
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
    private HBox UpHBox;

    @FXML
    private HBox DownHBox;

    @FXML
    private Button Back;

    @FXML
    private Button Create;

    @FXML
    private Label DownLabel;

    @FXML
    private Button OpenButton;

    @FXML
    private ListView<String> listView;

    @FXML
    private SplitPane splitPane;

    private TextField textField;

    public Stage InitialStage;

    @FXML
    private void templateStorage(){
        ResourceBundle resourceBundle = RunApplication.resourceBundle;
        //change buttons
        changeControls(UpHBox, new String[]{resourceBundle.getString("InitNameStorage")});
        changeControls(DownHBox, new String[]{resourceBundle.getString("InitLocation"),
                resourceBundle.getString("InitBrowse")});

        textField = new TextField(){{
            setFont(new Font(18));
        }};
        UpHBox.getChildren().add(textField);

        //Change visible buttons
        changeVisibleButtons(true);
    }

    private void changeControls(HBox Hbox, String[] strings){
        Iterator<Node> iterator = createIterator(Hbox);
        iterationControls(iterator, strings);
    }

    private void iterationControls(Iterator<Node> iterator, String[] str){
        while (iterator.hasNext()){
            Node node = iterator.next();
            if (node instanceof Label){
                ((Label) node).setText(str[0]);
            }
            if (node instanceof Button){
                if (str.length < 2) {
                    iterator.remove();
                }else{
                    ((Button) node).setText(str[1]);
                }
            }
            if (node instanceof TextField){
                iterator.remove();
            }
        }
    }

    private void changeVisibleButtons(boolean value){
        Back.setVisible(value);
        Create.setVisible(value);
    }

    private Iterator<Node> createIterator(HBox Hbox){
        return Hbox.getChildren().iterator();
    }

    @FXML
    private void openStorage(){
        DirectoryChooser directoryChooser = new DirectoryChooser();
        Optional<File> selectedDirectory = Optional.ofNullable(directoryChooser.showDialog(InitialStage));
        selectedDirectory.ifPresent(file -> {
            RunApplication.folderPath = Paths.get(file.toURI());
            ResourceBundle resourceBundle = RunApplication.resourceBundle;
            if (OpenButton.getText().equals(resourceBundle.getString("InitBrowse"))) {
                DownLabel.setFont(new Font(12));
                DownLabel.setText(resourceBundle.getString("InitStoragePath") + " "
                        + RunApplication.folderPath.toString());
            }else {
                showMainStage();
            }
        });
    }

    @FXML
    private void backToMainMenu(){
        ResourceBundle resourceBundle = RunApplication.resourceBundle;
        changeControls(UpHBox, new String[]{resourceBundle.getString("NewStorage")});
        changeControls(DownHBox, new String[]{resourceBundle.getString("OpenStorage"),
                resourceBundle.getString("OpenButton")});

        UpHBox.getChildren().add(new Button(resourceBundle.getString("CreateButton")){{
            setFont(new Font(18));
            setOnMouseClicked(mouseEvent -> templateStorage());
        }});

        changeVisibleButtons(false);
        DownLabel.setText("");
    }

    @FXML
    private void createStorage(){
        ResourceBundle resourceBundle = RunApplication.resourceBundle;
        if (textField.getText().isEmpty()){
            Alerts.createAndShowWarning(resourceBundle.getString("InitLabelNameStorage"));
            return;
        }
        if (RunApplication.folderPath == null){
            Alerts.createAndShowWarning(resourceBundle.getString("InitLabelPathStorage"));
            return;
        }
        showMainStage();
    }

    public void showMainStage(){
        InitialStage.close();
        RunApplication.showMainWindow();
    }

    public void loadPaths(){
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
