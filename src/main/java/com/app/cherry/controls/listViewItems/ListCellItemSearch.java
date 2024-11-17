package com.app.cherry.controls.listViewItems;

import com.app.cherry.util.structures.SearchListViewItem;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;

public class ListCellItemSearch extends ListCell<SearchListViewItem> {
    @Override
    public void updateItem(SearchListViewItem value, boolean empty) {
        super.updateItem(value, empty);

        if (empty) {
            setGraphic(null);
            return;
        }

        Label label = new Label(value.toString());
        setGraphic(label);
    }
}
