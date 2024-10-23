package com.app.cherry.controls.listViewItems;

import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.text.Font;

public class ListCellSettingsModal extends ListCell<String> {
    @Override
    public void updateItem(String value, boolean empty) {
        super.updateItem(value, empty);

        if (empty) {
            setGraphic(null);
            return;
        }

        Font font = new Font("Arial", 14);
        Label label = new Label(value);
        label.setFont(font);
        setGraphic(label);
    }
}
