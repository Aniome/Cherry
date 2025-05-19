package com.app.cherry.controls;

import com.app.cherry.RunApplication;
import com.app.cherry.util.icons.IconConfigurer;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.jetbrains.annotations.NotNull;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FolderOfFiles {
    private static Tab tab;
    private static TreeItem<String> selectedItem;
    private static String relativePath;
    private static TreeView<String> treeView;

    public static void buildFolderTab(Tab tab, TreeItem<String> selectedItem, String relativePath,
                                      TreeView<String> treeView) {
        //relativePath example: root\example.md
        FolderOfFiles.tab = tab;
        FolderOfFiles.selectedItem = selectedItem;
        FolderOfFiles.relativePath = relativePath;
        FolderOfFiles.treeView = treeView;

        tab.setGraphic(TabBuilder.createCircleUnsavedChanges());
        FlowPane containerOfFiles = buildContainerOfFiles(relativePath);
        ScrollPane scrollPane = new ScrollPane(containerOfFiles);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        BorderPane buildTabContent =
                new TabBuilder().buildTabContent(tab, selectedItem, scrollPane, false, treeView);
        tab.setContent(buildTabContent);
    }

    private static FlowPane buildContainerOfFiles(String path) {
        String folderPath = RunApplication.folderPath + File.separator + path;
        File[] files = new File(folderPath).listFiles();

        if (files == null) return null;
        List<File> filesList = Arrays.asList(files);
        List<VBox> itemsFolderList = new ArrayList<>();

        String iconsFolderPath = folderPath + File.separator + "[icons]";
        File iconsFolder = new File(iconsFolderPath);
        boolean iconsIsExist = filesList.contains(iconsFolder);

        //creating containers for elements
        Arrays.stream(files).forEach(folderItem -> {
            String folderItemName = folderItem.getName();
            double scale = 5.5;
            //double scale = 3;
            boolean isFile;

            FontIcon fileIcon = IconConfigurer.getFileIcon();
            //when folderItem file
            if (!folderItem.isDirectory()) {
                folderItemName = folderItemName.substring(0, folderItemName.length() - 3);
                fileIcon.setScaleX(scale);
                fileIcon.setScaleY(scale);
                isFile = true;
            } else {
                isFile = false;
            }

            Label labelFileName = new Label(folderItemName) {{
                //setMaxHeight(50);
                setMaxWidth(100);
                //setWrapText(true);
                setAlignment(Pos.CENTER);
                setTextOverrun(OverrunStyle.ELLIPSIS);
            }};

            final Border emptyBorder = new Border(new BorderStroke(Color.TRANSPARENT, BorderStrokeStyle.SOLID,
                    new CornerRadii(5), BorderWidths.DEFAULT));
            final Background emptyBackground = new Background(new BackgroundFill(Color.TRANSPARENT,
                    new CornerRadii(5), Insets.EMPTY));

            VBox vBoxContentItem = buildContentItem(labelFileName, emptyBorder, emptyBackground);

            setIcons(vBoxContentItem, iconsIsExist, folderItemName, iconsFolder, isFile, fileIcon);

            //VBox.setMargin(labelFileName, new Insets(20, 0, 0, 0));

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
            String finalFolderItemName = folderItemName;
            vBoxContentItem.setOnMouseClicked(event -> {
                if (isFile)
                    return;
                TreeItem<String> newSelectedItem = selectedItem;
                ObservableList<TreeItem<String>> selectedItemChildrens = selectedItem.getChildren();
                for (TreeItem<String> item : selectedItemChildrens) {
                    String itemValue = item.getValue();
                    if (itemValue.equals(finalFolderItemName)) {
                        newSelectedItem = item;
                    }
                }
                buildFolderTab(tab, newSelectedItem, relativePath + File.separator + finalFolderItemName,
                        treeView);
            });

            itemsFolderList.add(vBoxContentItem);
        });

        //creating container for a folder
        //top right bottom left
        return new FlowPane() {{
            //top right bottom left
            //setPadding(new Insets(10));
            getChildren().addAll(itemsFolderList);
        }};
    }

    @NotNull
    private static VBox buildContentItem(Label labelFileName, Border emptyBorder, Background emptyBackground) {
        final double heightContentItem = 239;
        final double widthContentItem = 170;
        return new VBox(labelFileName) {{
            prefWidth(widthContentItem);
            prefHeight(heightContentItem);
            setMinHeight(heightContentItem);
            setMinWidth(widthContentItem);
            setMaxHeight(heightContentItem);
            setMaxWidth(widthContentItem);
            setAlignment(Pos.CENTER);
            setBorder(emptyBorder);
            setBackground(emptyBackground);
            //top right bottom left
            //setPadding(new Insets(20, 20, 0, 20));
        }};
    }

    private static void setIcons(VBox vBoxContentItem, boolean iconsIsExist, String folderItemName, File iconsFolder,
                                 boolean isFile, FontIcon fileIcon) {
        ObservableList<Node> listVboxContent = vBoxContentItem.getChildren();
        if (iconsIsExist) {
            File[] filesOfIcons = iconsFolder.listFiles((dir1, name) ->
                    name.split("\\.")[0].equals(folderItemName));

            if (filesOfIcons != null && filesOfIcons.length > 0) {
                final int iconSizeHeight = 167;
                final int iconSizeWidth = 167;
                try {
                    ImageView imageView = new ImageView("file:/" + filesOfIcons[0]);
                    imageView.setFitHeight(iconSizeHeight);
                    imageView.setFitWidth(iconSizeWidth);
                    listVboxContent.addFirst(imageView);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        boolean iconsIsSet = listVboxContent.size() == 2;
        if (iconsIsSet) return;
        if (isFile) {
            listVboxContent.addFirst(fileIcon);
        } else {
            listVboxContent.addFirst(IconConfigurer.getFolderIcon(70));
        }
    }
}

/*
Video attributes:
tags
description
duration
date
 */
