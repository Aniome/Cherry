package com.app.cherry.util.icons;

import javafx.scene.image.ImageView;
import org.kordamp.ikonli.javafx.FontIcon;

public class IconConfigurer {
    public static ImageView getFolderIcon(double iconSize) {
        //double iconSize = 16;
        ImageView imageView = new ImageView(Icons.FOLDER_ICON.getIcon());
        imageView.setFitHeight(iconSize);
        imageView.setFitWidth(iconSize);
        return imageView;
    }

    public static FontIcon getFileIcon() {
        return new FontIcon(Icons.FILE_ICON.getIcon());
    }
}
