package com.app.cherry.controls.TreeViewItems;

import javafx.scene.control.TreeItem;
import org.kordamp.ikonli.javafx.FontIcon;

public class TreeItemCustom extends TreeItem<String> {
    public boolean childrenLoaded;

    public TreeItemCustom(String s, boolean childrenLoaded, String iconName) {
        super(s, new FontIcon(iconName));
        //mdal-folder_open, mdal-insert_drive_file
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
