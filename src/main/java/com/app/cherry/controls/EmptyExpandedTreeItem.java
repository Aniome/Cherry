package com.app.cherry.controls;

import javafx.scene.control.TreeItem;

public class EmptyExpandedTreeItem extends TreeItem<String> {
    public boolean childrenLoaded;

    public EmptyExpandedTreeItem(String s, boolean childrenLoaded) {
        super(s);
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
