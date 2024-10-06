package com.app.cherry.controllers;

import javafx.fxml.FXML;
import javafx.scene.web.WebView;

public class WebViewController {
    @FXML
    private WebView engine;

    public void init(String link){
        try {
            engine.getEngine().load(link);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
