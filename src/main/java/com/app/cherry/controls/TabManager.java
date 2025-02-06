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
    private final String fileIcon = "mdal-insert_drive_file";
    private final String folderIcon = "mdal-folder_open";

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

        HBox hBoxTitleBar = buildHBoxTitleBar(selectedItem, noteName);
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
        ToggleButton toggleButtonCrumbs = new ToggleButton();
        toggleButtonCrumbs.selectedProperty().addListener((arg0, oldValue,
                                                           newValue) -> toggleButtonCrumbs.setSelected(false));
        toggleButtonCrumbs.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        toggleButtonCrumbs.setPadding(new Insets(-5));

        toggleButtonCrumbs.setGraphic(crumbs);

        VBox vBoxCrumbs = new VBox(toggleButtonCrumbs);
        vBoxCrumbs.setPadding(new Insets(5));

        VBox vBoxTopContainer = new VBox(vBoxCrumbs, hBoxTitleBar);
        HBox.setHgrow(vBoxTopContainer, Priority.ALWAYS);
        vBoxTopContainer.setBackground(new Background(new BackgroundFill(Color.web("#181920"), CornerRadii.EMPTY, Insets.EMPTY)));

        //center top right bottom left
        BorderPane borderPanePage = new BorderPane(markdownArea, vBoxTopContainer, null, null, null);
        setSelectedCrumbListener(crumbs, hBoxTitleBar, borderPanePage);

        return borderPanePage;
    }

    private void setSelectedCrumbListener(Breadcrumbs<String> crumbs, HBox hBoxTitleBar, BorderPane borderPanePage) {
        crumbs.selectedCrumbProperty().addListener((obs,
                                                    oldVal, newVal) -> {
            hBoxTitleBar.getChildren().clear();
            //get full path to the directory
            StringBuilder pathTreeItem = new StringBuilder();
            BreadCrumbItem<String> currentBreadCrumb = newVal;
            while (currentBreadCrumb.getParent() != null) {
                pathTreeItem.append(RunApplication.separator).append(currentBreadCrumb.getValue());
                currentBreadCrumb = (BreadCrumbItem<String>) currentBreadCrumb.getParent();
            }

            File folder = new File(RunApplication.folderPath + File.separator + pathTreeItem);
            File[] files = folder.listFiles();

            List<VBox> itemsFolderList = new ArrayList<>();
            if (files != null) {
                Arrays.stream(files).forEach(folderItem -> {
                    String fileName = folderItem.getName();
                    FontIcon fontIcon = new FontIcon(folderIcon);

                    if (!folderItem.isDirectory()) {
//                        fileName = fileName.substring(0, fileName.length() - 3);
//                        fontIcon = new FontIcon(fileIcon);
                        return;
                    }

                    double scale = 4.5;
                    fontIcon.setScaleX(scale);
                    fontIcon.setScaleY(scale);

                    Label labelFileName = new Label(fileName);
                    labelFileName.setWrapText(true);

                    VBox vBoxContentButtonItem = new VBox(fontIcon, labelFileName);
                    vBoxContentButtonItem.prefWidth(105);
                    vBoxContentButtonItem.prefHeight(145);
                    vBoxContentButtonItem.setAlignment(Pos.CENTER);

                    vBoxContentButtonItem.setStyle("-fx-border-radius: 5; -fx-background-radius: 5");
                    //top right bottom left
                    VBox.setMargin(labelFileName, new Insets(20, 5, -20, 5));

                    vBoxContentButtonItem.setOnMouseEntered(event -> {
                        vBoxContentButtonItem.setBackground(new Background(new BackgroundFill
                                (Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY)));
                        vBoxContentButtonItem.setBorder(Border.stroke(Color.WHITE));
                    });
                    vBoxContentButtonItem.setOnMouseExited(event -> {
                        vBoxContentButtonItem.setBackground(null);
                        vBoxContentButtonItem.setBorder(null);
                    });

                    itemsFolderList.add(vBoxContentButtonItem);
                });
            }

            //creating container for folder
            FlowPane folderContent = new FlowPane() {{
                setVgap(25);
                setHgap(25);
                setPadding(new Insets(15, 0, 0, 15));
            }};
            //folderContent.setBackground(new Background(new BackgroundFill(Color.AQUA, CornerRadii.EMPTY, Insets.EMPTY)));
            folderContent.getChildren().addAll(itemsFolderList);

            borderPanePage.setCenter(folderContent);
        });

    }

    @NotNull
    private Breadcrumbs<String> buildStringBreadcrumbs(BreadCrumbItem<String> root) {
        Breadcrumbs<String> crumbs = new Breadcrumbs<>(root);
        crumbs.setCrumbFactory(crumb -> {
            String fontIcon;
            if (crumb.isLeaf()) {
                fontIcon = fileIcon;
            } else {
                fontIcon = folderIcon;
            }
            Button button = new Button(crumb.getValue(), new FontIcon(fontIcon));
            button.getStyleClass().add(Styles.FLAT);
            button.setFocusTraversable(false);
            return button;
        });
        return crumbs;
    }

    @NotNull
    private static HBox buildHBoxTitleBar(TreeItem<String> selectedItem, TextField noteName) {
        FontIcon saveIcon = new FontIcon("bx-save") {{
            setScaleX(1.5);
            setScaleY(1.5);
        }};

        Button saveButton = new Button("", saveIcon) {{
            setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        }};
        saveButton.setOnMouseClicked(event -> MarkdownArea.saveText(selectedItem));

        return new HBox(noteName, saveButton) {{
            setSpacing(10);
            setPadding(new Insets(5));
        }};
    }

    public static Circle createCircleUnsavedChanges() {
        Circle circleUnsavedChanges = new Circle(5, Color.web("#bcbaba"));
        circleUnsavedChanges.setStroke(Color.BLACK);
        circleUnsavedChanges.setOpacity(0);
        return circleUnsavedChanges;
    }
}
