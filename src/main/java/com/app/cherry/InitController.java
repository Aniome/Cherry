package com.app.cherry;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
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

    private TextField textField;

    private Stage InitialStage;

    private Stage MainStage;

    public void setMainStage(Stage mainStage) {
        MainStage = mainStage;
    }

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

    private void ShowMainStage(){
        try {
            InitialStage.close();
            FXMLLoader fxmlLoader = new FXMLLoader(RunApplication.class.getResource("main-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), RunApplication.MainWidth, RunApplication.MainHeight);
            RunApplication.PrepareStage(RunApplication.MainHeight, RunApplication.MainWidth, scene, RunApplication.title, MainStage);
        } catch (IOException e) {
            Alerts.CreateAndShowError(e.getMessage());
        }
    }
}
