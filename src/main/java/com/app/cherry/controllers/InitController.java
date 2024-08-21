package com.app.cherry.controllers;

import atlantafx.base.theme.Styles;
import com.app.cherry.controls.ListViewItem;
import com.app.cherry.dao.RecentPathsDAO;
import com.app.cherry.util.Alerts;
import com.app.cherry.RunApplication;
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
import java.util.*;

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

    private TextField textField;

    private Stage InitialStage;

    public void setInitialStage(Stage initialStage) {
        InitialStage = initialStage;
    }

    @FXML
    private void TemplateStorage(){
        //change buttons
        ChangeControls(UpHBox, new String[]{"Имя хранилища"});
        ChangeControls(DownHBox, new String[]{"Расположение", "Просмотр"});

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
            if (OpenButton.getText().equals("Просмотр")){
                DownLabel.setFont(new Font(12));
                DownLabel.setText("Хранилище будет расположено по пути: " + RunApplication.FolderPath.toString());
            }else {
                ShowMainStage();
            }
        });
    }

    @FXML
    private void BackToMainMenu(){
        ChangeControls(UpHBox, new String[]{"Создать новое хранилище"});
        ChangeControls(DownHBox, new String[]{"Открыть хранилище", "Открыть"});

        UpHBox.getChildren().add(new Button("Создать"){{
            setFont(new Font(18));
            setOnMouseClicked(mouseEvent -> TemplateStorage());
        }});

        ChangeVisibleButtons(false);
        DownLabel.setText("");
    }

    @FXML
    private void CreateStorage(){
        if (textField.getText().isEmpty()){
            Alerts.CreateAndShowWarning("Укажите имя хранилища");
            return;
        }
        if (RunApplication.FolderPath == null){
            Alerts.CreateAndShowWarning("Укажите путь до хранилища");
            return;
        }
        ShowMainStage();
    }

    public void ShowMainStage(){
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
        listView.setCellFactory(lvItem -> new ListViewItem(listView, this));
        Styles.toggleStyleClass(listView, Styles.BORDERED);
        listView.getItems().addAll(listRecentPaths);
    }
}
