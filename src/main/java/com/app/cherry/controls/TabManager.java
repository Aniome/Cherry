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
import javafx.scene.text.Font;
import org.fxmisc.richtext.CodeArea;
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
        Label label = new Label(RunApplication.resourceBundle.getString("LabelEmptyTab"));
        label.setFont(new Font(29));
        borderPane.setCenter(label);
        borderPane.setStyle("-fx-background-color: #282a36");
        return borderPane;
    }

    public static void addTab(String fileName, TabPane tabPane, TreeItem<String> selectedItem) {
        Tab tab = new Tab(fileName);
        TabManager tabManager = new TabManager();
        tab.setContent(tabManager.createTab(tab, selectedItem));
        TabManager.selectTab(tab, tabPane);
    }

    //Creates a form and fills it with content
    @NotNull
    public BorderPane createTab(Tab tab, TreeItem<String> selectedItem) {
        StackPane markdownArea = MarkdownArea.createMarkdownArea();

        String style = "-fx-border-width: 0; -fx-border-style: none;";

        TextField noteName = new TextField(tab.getText()){{
            setFont(new Font(16));
            setAlignment(Pos.CENTER);
        }};

        noteName.focusedProperty().addListener((arg0,
                                                 oldPropertyValue, newPropertyValue) -> {
            //newPropertyValue - on focus
            //oldPropertyValue - out focus
            String noteNameText = noteName.getText();
            if (newPropertyValue) {
                oldTextFieldValue = noteNameText;
            }
            if (oldPropertyValue && noteNameText.isEmpty()) {
                noteName.setText(oldTextFieldValue);
            } else {
                tab.setText(noteNameText);

                boolean conditions = FileService.renameFile(noteNameText, selectedItem.getValue(),
                        RunApplication.folderPath.toString());
                if (conditions) {
                    selectedItem.setValue(noteNameText);
                    tab.setText(noteNameText);
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

        CodeArea codeArea = MarkdownArea.getCodeArea();
        saveButton.setOnMouseClicked(event -> FileService.writeFile(selectedItem, codeArea.getText()));

        HBox hBoxTitleBar = new HBox(noteName, saveButton);
        hBoxTitleBar.setSpacing(10);
        hBoxTitleBar.setPadding(new Insets(5));
        return hBoxTitleBar;
    }
}
