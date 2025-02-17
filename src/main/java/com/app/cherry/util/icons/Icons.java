package com.app.cherry.util.icons;

import com.app.cherry.RunApplication;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.kordamp.ikonli.javafx.FontIcon;

public enum Icons {
//FOLDER_ICON("mdal-folder"),
//    FOLDER_ICON("icons/folder_icon48.png"),
//    FILE_ICON("mdal-insert_drive_file"),
//    TITLE_ICON("icons/cherry_icon.png");
    FILE_ICON(new FontIcon("mdal-insert_drive_file")),
    FOLDER_ICON(new ImageView(String.valueOf(RunApplication.class.getResource("icons/folder_icon48.png")))),
    TITLE_ICON(new Image(String.valueOf(RunApplication.class.getResource("icons/cherry_icon.png"))));

    private final Object icon;

    Icons(Object icon) {
        this.icon = icon;
    }

    public Object getIcon() {
        return icon;
    }
}
