package com.app.cherry.util.icons;

import com.app.cherry.RunApplication;

public enum Icons {
    FILE_ICON("mdal-insert_drive_file"),
    FOLDER_ICON(String.valueOf(RunApplication.class.getResource("icons/folder_icon48.png"))),
    TITLE_ICON(String.valueOf(RunApplication.class.getResource("icons/cherry_icon.png")));

    private final String icon;

    Icons(String icon) {
        this.icon = icon;
    }

    public String getIcon() {
        return icon;
    }
}
