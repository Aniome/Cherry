package com.app.cherry.controls.TreeViewItems;

import javafx.scene.control.TreeItem;
import org.kordamp.ikonli.javafx.FontIcon;

public class EmptyExpandedTreeItem extends TreeItem<String> {
    public boolean childrenLoaded;

    public EmptyExpandedTreeItem(String s, boolean childrenLoaded, String iconName) {
        super(s, new FontIcon(iconName));
        //mdal-folder
        //true is leaf, false is expanded
        //the flag does not need to be changed
        this.childrenLoaded = childrenLoaded;
    }

    @Override
    public boolean isLeaf() {
        if (childrenLoaded) {
            return getChildren().isEmpty() ;
        }
        return false ;
    }
}
