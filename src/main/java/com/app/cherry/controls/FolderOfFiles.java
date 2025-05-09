package com.app.cherry.controls;

import com.app.cherry.RunApplication;
import com.app.cherry.util.icons.IconConfigurer;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FolderOfFiles {

    public static void buildFolderTab(Tab tab, TreeItem<String> selectedItem, String relativePath, TreeView<String> treeView) {
        //relativePath example: root\example.md
        tab.setGraphic(TabBuilder.createCircleUnsavedChanges());
        FlowPane containerOfFiles = buildContainerOfFiles(relativePath);
        BorderPane buildTabContent =
                new TabBuilder().buildTabContent(tab, selectedItem, containerOfFiles, false, treeView);
        tab.setContent(buildTabContent);
    }

    private static FlowPane buildContainerOfFiles(String path) {
        File folder = new File(RunApplication.folderPath + File.separator + path);
        File[] files = folder.listFiles();

        List<VBox> itemsFolderList = new ArrayList<>();
        if (files == null) return null;

        //creating containers for elements
        Arrays.stream(files).forEach(folderItem -> {
            String folderItemName = folderItem.getName();
            //double scale = 4.5;
            double scale = 3;
            boolean isFile = false;

            FontIcon fileIcon = IconConfigurer.getFileIcon();
            //when folderItem file
            if (!folderItem.isDirectory()) {
                folderItemName = folderItemName.substring(0, folderItemName.length() - 3);
                fileIcon.setScaleX(scale);
                fileIcon.setScaleY(scale);
                isFile = true;
            }

            Label labelFileName = new Label(folderItemName) {{
                setMaxHeight(50);
                setMaxWidth(100);
                setWrapText(true);
                setTextOverrun(OverrunStyle.ELLIPSIS);
            }};

            final Border emptyBorder = new Border(new BorderStroke(Color.TRANSPARENT, BorderStrokeStyle.SOLID,
                    new CornerRadii(5), BorderWidths.DEFAULT));
            final Background emptyBackground = new Background(new BackgroundFill(Color.TRANSPARENT,
                    new CornerRadii(5), Insets.EMPTY));

            VBox vBoxContentItem = new VBox(labelFileName) {{
                prefWidth(105);
                prefHeight(145);
                setAlignment(Pos.CENTER);
                setBorder(emptyBorder);
                setBackground(emptyBackground);
                //top right bottom left
                setPadding(new Insets(20, 20, 0, 20));
            }};

            ObservableList<Node> listVboxContent = vBoxContentItem.getChildren();
            if (isFile) {
                listVboxContent.addFirst(fileIcon);
            } else {
                listVboxContent.addFirst(IconConfigurer.getFolderIcon(50));
            }

            VBox.setMargin(labelFileName, new Insets(20, 0, 0, 0));

            vBoxContentItem.setOnMouseEntered(event -> {
                vBoxContentItem.setBackground(new Background(new BackgroundFill(Color.GRAY,
                        new CornerRadii(5), Insets.EMPTY)));
                vBoxContentItem.setBorder(new Border(new BorderStroke(Color.WHITE, BorderStrokeStyle.SOLID,
                        new CornerRadii(5), BorderWidths.DEFAULT)));
            });
            vBoxContentItem.setOnMouseExited(event -> {
                vBoxContentItem.setBackground(emptyBackground);
                vBoxContentItem.setBorder(emptyBorder);
            });

            itemsFolderList.add(vBoxContentItem);
        });

        //creating container for a folder
        //top right bottom left
        return new FlowPane() {{
            setVgap(25);
            setHgap(25);
            //top right bottom left
            setPadding(new Insets(15, 0, 0, 15));
            getChildren().addAll(itemsFolderList);
        }};
    }
}
