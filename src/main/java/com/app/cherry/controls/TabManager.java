package com.app.cherry.controls;

import com.app.cherry.RunApplication;
import com.app.cherry.controls.codearea.MarkdownArea;
import com.app.cherry.util.io.FileService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import org.jetbrains.annotations.NotNull;
import org.kordamp.ikonli.javafx.FontIcon;

public class TabManager {
    private String oldTextFieldValue;

    public static void selectTab(Tab tab, TabPane tabPane) {
        int count = tabPane.getTabs().size() - 1;
        SingleSelectionModel<Tab> selectionModel = tabPane.getSelectionModel();
        tabPane.getTabs().add(count, tab);
        selectionModel.select(tab);
    }

    public static BorderPane createEmptyTab() {
        BorderPane borderPane = new BorderPane();
        Label emptyTab = new Label(RunApplication.resourceBundle.getString("LabelEmptyTab"));
        emptyTab.setFont(new Font(29));
        borderPane.setCenter(emptyTab);
        borderPane.setStyle("-fx-background-color: #282a36");
        return borderPane;
    }

    //adding tab when create new file
    public static void addTab(String fileName, TabPane tabPane, TreeItem<String> selectedItem) {
        Tab tab = new Tab(fileName);
        tab.setGraphic(TabManager.createCircleUnsavedChanges());
        TabManager tabManager = new TabManager();
        tab.setContent(tabManager.createTab(tab, selectedItem));
        TabManager.selectTab(tab, tabPane);
    }

    //Creates a form and fills it with content
    @NotNull
    public BorderPane createTab(Tab tab, TreeItem<String> selectedItem) {
        StackPane markdownArea = MarkdownArea.createMarkdownArea(selectedItem);

        String style = "-fx-border-width: 0; -fx-border-style: none;";

        TextField noteName = new TextField(tab.getText()) {{
            setFont(new Font(16));
            setAlignment(Pos.CENTER);
        }};

        noteName.focusedProperty().addListener((arg0,
                                                 oldPropertyValue, newPropertyValue) -> {
            //newPropertyValue - on focus
            //oldPropertyValue - lose focus
            String noteNameText = noteName.getText();
            //when clicked again on text field
            if (newPropertyValue) {
                oldTextFieldValue = noteNameText;
            }
            if (oldPropertyValue) {
                if (noteNameText.isEmpty()) {
                    noteName.setText(oldTextFieldValue);
                } else {
                    //when lose focus and renaming note
                    String pathTreeItem = FileService.getPath(selectedItem);
                    int lastIndexOfSeparator = pathTreeItem.lastIndexOf(RunApplication.separator);
                    pathTreeItem = pathTreeItem.substring(0, lastIndexOfSeparator);
                    //renameFile - newName, oldFile, path
                    boolean isSuccessRename = FileService.renameFile(noteNameText, selectedItem.getValue(), pathTreeItem);
                    if (isSuccessRename) {
                        selectedItem.setValue(noteNameText);
                        tab.setText(noteNameText);
                    }
                }
            }
        });
        noteName.setStyle(style);

        HBox hBoxTitleBar = getHBoxTitleBar(selectedItem, style, noteName);
        HBox.setHgrow(noteName, Priority.ALWAYS);

        //center top right bottom left
        return new BorderPane(markdownArea, hBoxTitleBar, null, null, null);
    }

    @NotNull
    private static HBox getHBoxTitleBar(TreeItem<String> selectedItem, String style, TextField noteName) {
        FontIcon saveIcon = new FontIcon("bx-save");
        saveIcon.setScaleX(1.5);
        saveIcon.setScaleY(1.5);
        Button saveButton = new Button("", saveIcon);
        saveButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        saveButton.setStyle(style);
        saveButton.setOnMouseClicked(event -> MarkdownArea.saveText(selectedItem));

        HBox hBoxTitleBar = new HBox(noteName, saveButton);
        hBoxTitleBar.setSpacing(10);
        hBoxTitleBar.setPadding(new Insets(5));
        return hBoxTitleBar;
    }

    public static Circle createCircleUnsavedChanges() {
        Circle circleUnsavedChanges = new Circle(5, Color.web("#bcbaba"));
        circleUnsavedChanges.setStroke(Color.BLACK);
        circleUnsavedChanges.setOpacity(0);
        return circleUnsavedChanges;
    }
}
