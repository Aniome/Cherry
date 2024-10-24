package com.app.cherry.controllers;

import com.app.cherry.RunApplication;
import com.app.cherry.controls.listViewItems.ListCellItem;
import com.app.cherry.dao.RecentPathsDAO;
import com.app.cherry.util.Alerts;
import com.app.cherry.util.FileService;
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

    private Stage InitialStage;

    public void setInitialStage(Stage initialStage) {
        InitialStage = initialStage;
    }

    @FXML
    private void TemplateStorage(){
        ResourceBundle resourceBundle = RunApplication.resourceBundle;
        //change buttons
        ChangeControls(UpHBox, new String[]{resourceBundle.getString("InitNameStorage")});
        ChangeControls(DownHBox, new String[]{resourceBundle.getString("InitLocation"),
                resourceBundle.getString("InitBrowse")});

        textField = new TextField(){{
            setFont(new Font(18));
        }};
        UpHBox.getChildren().add(textField);

        //Change visible buttons
        ChangeVisibleButtons(true);
    }

    private void ChangeControls(HBox Hbox, String[] strings){
        Iterator<Node> iterator = CreateIterator(Hbox);
        IterationControls(iterator, strings);
    }

    private void IterationControls(Iterator<Node> iterator, String[] str){
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

    private void ChangeVisibleButtons(boolean value){
        Back.setVisible(value);
        Create.setVisible(value);
    }

    private Iterator<Node> CreateIterator(HBox Hbox){
        return Hbox.getChildren().iterator();
    }

    @FXML
    private void OpenStorage(){
        DirectoryChooser directoryChooser = new DirectoryChooser();
        Optional<File> selectedDirectory = Optional.ofNullable(directoryChooser.showDialog(InitialStage));
        selectedDirectory.ifPresent(file -> {
            RunApplication.FolderPath = Paths.get(file.toURI());
            ResourceBundle resourceBundle = RunApplication.resourceBundle;
            if (OpenButton.getText().equals(resourceBundle.getString("InitBrowse"))) {
                DownLabel.setFont(new Font(12));
                DownLabel.setText(resourceBundle.getString("InitStoragePath") + " "
                        + RunApplication.FolderPath.toString());
            }else {
                showMainStage();
            }
        });
    }

    @FXML
    private void BackToMainMenu(){
        ResourceBundle resourceBundle = RunApplication.resourceBundle;
        ChangeControls(UpHBox, new String[]{resourceBundle.getString("NewStorage")});
        ChangeControls(DownHBox, new String[]{resourceBundle.getString("OpenStorage"),
                resourceBundle.getString("OpenButton")});

        UpHBox.getChildren().add(new Button(resourceBundle.getString("CreateButton")){{
            setFont(new Font(18));
            setOnMouseClicked(mouseEvent -> TemplateStorage());
        }});

        ChangeVisibleButtons(false);
        DownLabel.setText("");
    }

    @FXML
    private void CreateStorage(){
        ResourceBundle resourceBundle = RunApplication.resourceBundle;
        if (textField.getText().isEmpty()){
            Alerts.createAndShowWarning(resourceBundle.getString("InitLabelNameStorage"));
            return;
        }
        if (RunApplication.FolderPath == null){
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
            ListCellItem treeCell = new ListCellItem(listView, this);
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
