package com.app.cherry.controls;

import atlantafx.base.controls.Breadcrumbs;
import atlantafx.base.controls.Breadcrumbs.BreadCrumbItem;
import atlantafx.base.theme.Styles;
import com.app.cherry.RunApplication;
import com.app.cherry.controls.codearea.MarkdownArea;
import com.app.cherry.util.configuration.ApplyConfiguration;
import com.app.cherry.util.icons.Icons;
import com.app.cherry.util.io.FileService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import org.fxmisc.richtext.CodeArea;
import org.jetbrains.annotations.NotNull;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TabManager {
    private String oldTextFieldValue;
    private CodeArea codeArea;

    public static void selectTab(Tab tab, TabPane tabPane) {
        int count = tabPane.getTabs().size() - 1;
        SingleSelectionModel<Tab> selectionModel = tabPane.getSelectionModel();
        tabPane.getTabs().add(count, tab);
        selectionModel.select(tab);
    }

    public static BorderPane createEmptyTab() {
        Label emptyTab = new Label(RunApplication.resourceBundle.getString("LabelEmptyTab"));
        emptyTab.setFont(new Font(29));
        //center top right bottom left
        return new BorderPane(emptyTab) {{
            setBackground(new Background(new BackgroundFill(ApplyConfiguration.getColorBackground(),
                    CornerRadii.EMPTY, Insets.EMPTY)));
        }};
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
        StackPane markdownArea = MarkdownArea.createMarkdownArea(selectedItem, this);
        HBox hBoxTitleBar = buildHBoxTitleBar(selectedItem, tab);

        //creating path to the note
        TreeItem<String> currentTreeItem = selectedItem;
        List<String> breadCrumbItems = new ArrayList<>();
        while (currentTreeItem.getParent() != null) {
            if (currentTreeItem.getValue().isEmpty())
                break;
            breadCrumbItems.add(currentTreeItem.getValue());
            currentTreeItem = currentTreeItem.getParent();
        }
        breadCrumbItems = breadCrumbItems.reversed();

        BreadCrumbItem<String> root = Breadcrumbs.buildTreeModel(breadCrumbItems.toArray(new String[0]));

        Breadcrumbs<String> crumbs = buildStringBreadcrumbs(root);
        String borderColor = ApplyConfiguration.getBorderColor();

        VBox vBoxCrumbs = new VBox(crumbs);
        vBoxCrumbs.setAlignment(Pos.CENTER);
        vBoxCrumbs.setPadding(new Insets(-10));

        VBox vBoxTopContainer = new VBox(vBoxCrumbs, hBoxTitleBar);
        vBoxTopContainer.setPadding(new Insets(5));

        //top right bottom left
        vBoxTopContainer.setStyle("-fx-border-color: transparent " + borderColor + borderColor + " transparent;");

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
            List<String> listPath = new ArrayList<>();
            BreadCrumbItem<String> currentBreadCrumb = newVal;
            while (currentBreadCrumb != null) {
                listPath.add(currentBreadCrumb.getValue());
                currentBreadCrumb = (BreadCrumbItem<String>) currentBreadCrumb.getParent();
            }
            listPath = listPath.reversed();
            String pathTreeItem = String.join(RunApplication.separator, listPath);

            File folder = new File(RunApplication.folderPath + File.separator + pathTreeItem);
            File[] files = folder.listFiles();

            List<VBox> itemsFolderList = new ArrayList<>();
            if (files == null) return;

            //creating containers for elements
            Arrays.stream(files).forEach(folderItem -> {
                FontIcon fontIcon = new FontIcon(Icons.FOLDER_ICON.getIconName());
                String fileName = folderItem.getName();
                if (!folderItem.isDirectory()) {
                    fileName = fileName.substring(0, fileName.length() - 3);
                    fontIcon = new FontIcon(Icons.FILE_ICON.getIconName());
                }

                double scale = 4.5;
                fontIcon.setScaleX(scale);
                fontIcon.setScaleY(scale);

                Label labelFileName = new Label(fileName);
                labelFileName.setMaxHeight(50);
                labelFileName.setMaxWidth(100);
                labelFileName.setWrapText(true);
                labelFileName.setTextOverrun(OverrunStyle.ELLIPSIS);

                final Border emptyBorder = new Border(new BorderStroke(Color.TRANSPARENT, BorderStrokeStyle.SOLID,
                        new CornerRadii(5), BorderWidths.DEFAULT));
                final Background emptyBackground = new Background(new BackgroundFill(Color.TRANSPARENT,
                        new CornerRadii(5), Insets.EMPTY));

                VBox vBoxContentItem = new VBox(fontIcon, labelFileName) {{
                    prefWidth(105);
                    prefHeight(145);
                    setAlignment(Pos.CENTER);
                    setBorder(emptyBorder);
                    setBackground(emptyBackground);
                    //top right bottom left
                    setPadding(new Insets(20, 20, 0, 20));
                }};

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

            //creating container for folder
            FlowPane folderContent = new FlowPane() {{
                setVgap(25);
                setHgap(25);
                //top right bottom left
                setPadding(new Insets(15, 0, 0, 15));
                getChildren().addAll(itemsFolderList);
            }};

            borderPanePage.setCenter(folderContent);
        });
    }

    @NotNull
    private Breadcrumbs<String> buildStringBreadcrumbs(BreadCrumbItem<String> root) {
        Breadcrumbs<String> crumbs = new Breadcrumbs<>(root);
        crumbs.setCrumbFactory(crumb -> {
            String fontIcon;
            if (crumb.isLeaf()) {
                fontIcon = Icons.FILE_ICON.getIconName();
            } else {
                fontIcon = Icons.FOLDER_ICON.getIconName();
            }
            Button button = new Button(crumb.getValue(), new FontIcon(fontIcon));
            button.toBack();
            button.getStyleClass().add(Styles.FLAT);
            button.setFocusTraversable(false);
            return button;
        });
        crumbs.setDividerFactory(stringBreadCrumbItem -> {
            if (stringBreadCrumbItem == null) return null;
            return !stringBreadCrumbItem.isLast() ? new Label("", new FontIcon(Material2AL.CHEVRON_RIGHT)) : null;
        });
        return crumbs;
    }

    @NotNull
    private HBox buildHBoxTitleBar(TreeItem<String> selectedItem, Tab tab) {
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
                    boolean isSuccessRename = FileService.renameFile(noteNameText, selectedItem.getValue(),
                            pathTreeItem);
                    if (isSuccessRename) {
                        selectedItem.setValue(noteNameText);
                        tab.setText(noteNameText);
                    }
                }
            }
        });

        FontIcon saveIcon = new FontIcon("bx-save") {{
            setScaleX(1.5);
            setScaleY(1.5);
        }};

        Button saveButton = new Button("", saveIcon) {{
            setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        }};
        saveButton.setOnMouseClicked(event -> MarkdownArea.saveText(codeArea, selectedItem));

        HBox.setHgrow(noteName, Priority.ALWAYS);

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

    public CodeArea getCodeArea() {
        return codeArea;
    }

    public void setCodeArea(CodeArea codeArea) {
        this.codeArea = codeArea;
    }
}
