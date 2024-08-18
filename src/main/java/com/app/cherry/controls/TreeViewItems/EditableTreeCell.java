package com.app.cherry.controls.TreeViewItems;

import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TreeCell;

public class EditableTreeCell extends TreeCell<String> {
    @Override
    public void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            setText(getItem());
            setGraphic(getTreeItem().getGraphic());
        }
    }

    @Override
    public void commitEdit(String newValue) {
        super.commitEdit(newValue);
        setContentDisplay(ContentDisplay.TEXT_ONLY);
    }
}
