package com.app.cherry.controls;

import javafx.scene.control.TreeItem;

public class EmptyExpandedTreeItem extends TreeItem<String> {
    public boolean childrenLoaded;

    public EmptyExpandedTreeItem(String s, boolean childrenLoaded) {
        super(s);
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
