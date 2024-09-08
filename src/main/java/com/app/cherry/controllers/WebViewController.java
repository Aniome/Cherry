package com.app.cherry.controllers;

import com.app.cherry.RunApplication;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.io.IOException;

public class WebViewController {
    @FXML
    private WebView engine;

    public void init(){
        engine.getEngine().load("https://www.google.com/");
    }
}
