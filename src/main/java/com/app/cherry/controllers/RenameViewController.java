package com.app.cherry.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RenameViewController {

    @FXML
    private Button OkButton;

    @FXML
    private TextField txtField;

    private Stage stage;

    public void init(Stage stage){
        this.stage = stage;
    }

    @FXML
    private void Confirm(){
        String txtFieldText = txtField.getText();
        if (txtFieldText.isEmpty())
            return;
        MainController.newFileName = txtFieldText;
        stage.close();
    }

    @FXML
    private void Cancel(){
        stage.close();
    }
}
