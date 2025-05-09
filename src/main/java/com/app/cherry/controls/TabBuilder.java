package com.app.cherry.controls;

import atlantafx.base.theme.Styles;
import com.app.cherry.RunApplication;
import com.app.cherry.controls.codearea.MarkdownArea;
import com.app.cherry.util.configuration.ApplyConfiguration;
import com.app.cherry.util.icons.IconConfigurer;
import com.app.cherry.util.io.FileService;
import javafx.animation.PauseTransition;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.util.Duration;
import org.fxmisc.richtext.CodeArea;
import org.jetbrains.annotations.NotNull;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.ArrayList;
import java.util.List;

public class TabBuilder {
    private String oldTextFieldString;
    private CodeArea codeArea;

    //adding tab when create a new file
    public void buildTab(String fileName, TabPane tabPane, TreeItem<String> selectedItem, TreeView<String> treeView) {
        Tab tab = new Tab(fileName);
        tab.setGraphic(TabBuilder.createCircleUnsavedChanges());
        StackPane markdownArea = MarkdownArea.createMarkdownArea(selectedItem, this, tab);
        tab.setContent(buildTabContent(tab, selectedItem, markdownArea, true, treeView));
        TabBuilder.selectTab(tab, tabPane);
    }

    //Creates a form and fills it with content
    @NotNull
    public BorderPane buildTabContent(Tab tab, TreeItem<String> selectedItem, Node centerContent,
                                      boolean createTitleBar, TreeView<String> treeView) {
        HBox crumbs = buildCrumbs(getPathToTheTreeViewNote(selectedItem), treeView);
        String borderColor = ApplyConfiguration.getBorderColor();
        HBox hBoxTitleBar = buildHBoxTitleBar(selectedItem, tab, crumbs);

        VBox vBoxTopContainer;
        if (createTitleBar)
            vBoxTopContainer = new VBox(crumbs, hBoxTitleBar);
        else
            vBoxTopContainer = new VBox(crumbs);

        vBoxTopContainer.setPadding(new Insets(5));

        //top right bottom left
        vBoxTopContainer.setStyle("-fx-border-color: transparent " + borderColor + borderColor + " transparent;");

        //center top right bottom left
        return new BorderPane(centerContent, vBoxTopContainer, null, null, null);
    }

    public static void buildEmptyTab(Tab tab) {
        tab.setGraphic(TabBuilder.createCircleUnsavedChanges());
        Label emptyTab = new Label(RunApplication.getResourceBundle().getString("LabelEmptyTab"));
        emptyTab.setFont(new Font(29));
        //center top right bottom left
        BorderPane tabContent = new BorderPane(emptyTab);
        tabContent.setBackground(new Background(new BackgroundFill(ApplyConfiguration.getColorBackground(),
                CornerRadii.EMPTY, Insets.EMPTY)));
        tab.setContent(tabContent);
    }

    private HBox buildCrumbs(List<TreeItem<String>> pathToTheNote, TreeView<String> treeView) {
        HBox crumbs = new HBox();
        crumbs.setAlignment(Pos.CENTER);

        ObservableList<Node> crumbsChildren = crumbs.getChildren();
        int length = pathToTheNote.size();
        for (int i = 0; i < length; i++) {
            Button button;
            TreeItem<String> treeItem = pathToTheNote.get(i);
            String treeItemValue = treeItem.getValue();
            if (i + 1 != length) {
                button = new Button(treeItemValue, IconConfigurer.getFolderIcon(16));
                setStyleAndAddToCrumbs(button, crumbsChildren, treeItem, treeView);
                crumbsChildren.add(new FontIcon("mdal-chevron_right"));
            } else {
                button = new Button(treeItemValue, IconConfigurer.getFileIcon());
                setStyleAndAddToCrumbs(button, crumbsChildren, treeItem, treeView);
            }
        }
        return crumbs;
    }

    private void setStyleAndAddToCrumbs(Button button, ObservableList<Node> crumbsChildren,
                                        TreeItem<String> treeItem, TreeView<String> treeView) {
        crumbsChildren.add(button);
        button.getStyleClass().add(Styles.FLAT);
        button.setFocusTraversable(false);
        button.setOnMouseClicked(event -> {
            MultipleSelectionModel<TreeItem<String>> selectionModel = treeView.getSelectionModel();
            selectionModel.select(treeItem);

            PauseTransition pause = new PauseTransition(Duration.seconds(3));
            pause.setOnFinished(e -> selectionModel.clearSelection());
            pause.play();
        });
    }

    @NotNull
    private HBox buildHBoxTitleBar(TreeItem<String> selectedItem, Tab tab, HBox crumbs) {
        TextField noteName = new TextField(tab.getText()) {{
            setFont(new Font(16));
            setAlignment(Pos.CENTER);
        }};
        //Breadcrumbs<String> crumbs
        //when note rename
        noteName.focusedProperty().addListener((arg0,
                                                oldPropertyValue, newPropertyValue) -> {
            //newPropertyValue - on focus
            //oldPropertyValue - lost focus
            String noteNameText = noteName.getText();
            //when clicked again on text field
            if (newPropertyValue) {
                oldTextFieldString = noteNameText;
            }
            if (oldPropertyValue) {
                if (noteNameText.isEmpty()) {
                    noteName.setText(oldTextFieldString);
                } else {
                    //when lose focus and renaming note
                    //if text with no changes, return
                    if (noteNameText.equals(oldTextFieldString)) return;

                    //getting a path to the file
                    String pathTreeItem = FileService.getPath(selectedItem);
                    int lastIndexOfSeparator = pathTreeItem.lastIndexOf(RunApplication.getSeparator());
                    pathTreeItem = pathTreeItem.substring(0, lastIndexOfSeparator);

                    //renameFile - newName, oldFile, path
                    boolean isSuccessRename = FileService.renameFile(noteNameText, selectedItem.getValue(),
                            pathTreeItem);

                    if (isSuccessRename) {
                        selectedItem.setValue(noteNameText);
                        tab.setText(noteNameText);

                        Node lastButton = crumbs.getChildren().getLast();
                        if (lastButton instanceof Button button) {
                            button.setText(noteNameText);
                        }
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
        saveButton.setOnMouseClicked(event -> MarkdownArea.saveText(codeArea, selectedItem, tab));

        HBox.setHgrow(noteName, Priority.ALWAYS);

        return new HBox(noteName, saveButton) {{
            setSpacing(10);
            setPadding(new Insets(5));
        }};
    }

    private static List<TreeItem<String>> getPathToTheTreeViewNote(TreeItem<String> selectedItem) {
        TreeItem<String> currentTreeItem = selectedItem;
        List<TreeItem<String>> breadCrumbItems = new ArrayList<>();
        while (currentTreeItem.getParent() != null) {
            if (currentTreeItem.getValue().isEmpty())
                break;
            breadCrumbItems.add(currentTreeItem);
            currentTreeItem = currentTreeItem.getParent();
        }
        breadCrumbItems = breadCrumbItems.reversed();
        return breadCrumbItems;
    }

    public static Circle createCircleUnsavedChanges() {
        return new Circle(5, Color.web("#bcbaba")) {{
            setStroke(Color.BLACK);
            setOpacity(0);
        }};
    }

    public static void selectTab(Tab tab, TabPane tabPane) {
        //index tab, before tab '+'
        int indexTab = tabPane.getTabs().size() - 1;
        tabPane.getTabs().add(indexTab, tab);
        tabPane.getSelectionModel().select(tab);
    }

    ///////////////////////////////////////////////////////////////
    //getters and setters
    public CodeArea getCodeArea() {
        return codeArea;
    }

    public void setCodeArea(CodeArea codeArea) {
        this.codeArea = codeArea;
    }
}
