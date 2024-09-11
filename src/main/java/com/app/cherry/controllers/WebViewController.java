package com.app.cherry.controllers;

import javafx.fxml.FXML;
import javafx.scene.web.WebView;

public class WebViewController {
    @FXML
    private WebView engine;

    public void init(String link){
        engine.getEngine().load(link);
    }
}
