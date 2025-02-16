package com.app.cherry.util.icons;

public enum Icons {
    FOLDER_ICON("mdal-folder"),
    FILE_ICON("mdal-insert_drive_file");

    private final String iconName;

    Icons(String iconName) {
        this.iconName = iconName;
    }

    public String getIconName() {
        return iconName;
    }
}
