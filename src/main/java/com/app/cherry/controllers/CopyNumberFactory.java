package com.app.cherry.controllers;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import java.util.function.IntFunction;

public class CopyNumberFactory implements IntFunction<Node> {
    @Override
    public Node apply(int lineNumber) {
        Button button = new Button("Copy");
        button.setAlignment(Pos.CENTER);
        button.setScaleX(0.5);
        button.setScaleY(0.5);
        return button;
    }
}
