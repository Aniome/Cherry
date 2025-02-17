package com.app.cherry.controls.TreeViewItems;

import javafx.scene.Node;
import javafx.scene.control.TreeItem;

public class TreeItemCustom extends TreeItem<String> {
    public boolean childrenLoaded;

    public TreeItemCustom(String s, boolean childrenLoaded, Node icon) {
        super(s, icon);
        //mdal-folder, mdal-insert_drive_file
        //true is leaf, false is empty expanded
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
