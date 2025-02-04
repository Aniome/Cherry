package com.app.cherry.controls;

import atlantafx.base.controls.Breadcrumbs;
import atlantafx.base.controls.Breadcrumbs.BreadCrumbItem;
import atlantafx.base.theme.Styles;
import com.app.cherry.RunApplication;
import com.app.cherry.controls.codearea.MarkdownArea;
import com.app.cherry.util.io.FileService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import org.jetbrains.annotations.NotNull;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

        TextField noteName = new TextField(tab.getText()) {{
            setFont(new Font(16));
            setAlignment(Pos.CENTER);
            setStyle("-fx-border-width: 0; -fx-border-style: none;");
        }};

        //when note rename
        noteName.focusedProperty().addListener((arg0,
                                                 oldPropertyValue, newPropertyValue) -> {
            //newPropertyValue - on focus
            //oldPropertyValue - lost focus
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

        HBox hBoxTitleBar = getHBoxTitleBar(selectedItem, noteName);
        HBox.setHgrow(noteName, Priority.ALWAYS);

        TreeItem<String> currentTreeItem = selectedItem;
        List<String> breadCrumbItems = new ArrayList<>();
        while (currentTreeItem.getParent() != null) {
            if (currentTreeItem.getValue().isEmpty())
                break;
            breadCrumbItems.add(currentTreeItem.getValue());
            currentTreeItem = currentTreeItem.getParent();
        }

        String[] items = breadCrumbItems.toArray(new String[0]);
        BreadCrumbItem<String> root = Breadcrumbs.buildTreeModel(items);

        Breadcrumbs<String> crumbs = buildStringBreadcrumbs(root);

        VBox vBoxTopContainer = new VBox(crumbs, hBoxTitleBar);
        HBox.setHgrow(vBoxTopContainer, Priority.ALWAYS);

        //center top right bottom left
        BorderPane page = new BorderPane(markdownArea, vBoxTopContainer, null, null, null);

        crumbs.selectedCrumbProperty().addListener((obs,
                                                    oldVal, newVal) -> {
            hBoxTitleBar.getChildren().clear();
            StringBuilder pathTreeItem = new StringBuilder();
            BreadCrumbItem<String> currentBreadCrumb = newVal;
            while (currentBreadCrumb.getParent() != null) {
                pathTreeItem.append(RunApplication.separator).append(currentBreadCrumb.getValue());
                currentBreadCrumb = (BreadCrumbItem<String>) currentBreadCrumb.getParent();
            }

            File folder = new File(RunApplication.folderPath + File.separator + pathTreeItem);
            File[] files = folder.listFiles();

            List<Button> buttons = new ArrayList<>();
            if (files != null) {
                Arrays.stream(files).forEach(file -> {
                    String fileName = file.getName();
                    if (fileName.endsWith(".md"))
                        fileName = fileName.substring(0, fileName.length() - 3);
                    Button button = new Button(fileName);
                    button.setFont(new Font(16));
                    FontIcon fontIcon;
                    if (file.isDirectory()) {
                        fontIcon = new FontIcon("mdal-folder_open");
                    } else {
                        fontIcon = new FontIcon("mdal-insert_drive_file");
                    }
                    button.setGraphic(fontIcon);
                    buttons.add(button);
                });
            }

            FlowPane folderContent = new FlowPane() {{
                setVgap(5);
                setHgap(5);
                setPadding(new Insets(10));
            }};

            for (Button button : buttons) {
                folderContent.getChildren().add(button);
            }

            page.setCenter(folderContent);
        });

        return page;
    }

    @NotNull
    private static Breadcrumbs<String> buildStringBreadcrumbs(BreadCrumbItem<String> root) {
        Breadcrumbs<String> crumbs = new Breadcrumbs<>(root);
        crumbs.setCrumbFactory(crumb -> {
            String fontIcon;
            if (crumb.isLeaf()) {
                fontIcon = "mdal-insert_drive_file";
            } else {
                fontIcon = "mdal-folder_open";
            }
            Button btn = new Button(crumb.getValue(), new FontIcon(fontIcon));
            btn.getStyleClass().add(Styles.FLAT);
            btn.setFocusTraversable(false);
            return btn;
        });
        return crumbs;
    }

    @NotNull
    private static HBox getHBoxTitleBar(TreeItem<String> selectedItem, TextField noteName) {
        FontIcon saveIcon = new FontIcon("bx-save") {{
            setScaleX(1.5);
            setScaleY(1.5);
        }};

        Button saveButton = new Button("", saveIcon) {{
            setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            setStyle("-fx-border-width: 0; -fx-border-style: none;");
        }};
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
