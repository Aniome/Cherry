package com.app.cherry.controls;

import atlantafx.base.controls.Breadcrumbs;
import atlantafx.base.controls.Breadcrumbs.BreadCrumbItem;
import atlantafx.base.theme.Styles;
import com.app.cherry.RunApplication;
import com.app.cherry.controls.codearea.MarkdownArea;
import com.app.cherry.util.configuration.ApplyConfiguration;
import com.app.cherry.util.icons.IconConfigurer;
import com.app.cherry.util.io.FileService;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
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

public class TabBuilder {
    private String oldTextFieldString;
    private CodeArea codeArea;
    private List<Button> breadCrumbsButtons;

    //adding tab when create new file
    public void buildTab(String fileName, TabPane tabPane, TreeItem<String> selectedItem) {
        Tab tab = new Tab(fileName);
        tab.setGraphic(TabBuilder.createCircleUnsavedChanges());
        StackPane markdownArea = MarkdownArea.createMarkdownArea(selectedItem, this, tab);
        tab.setContent(buildTabContent(tab, selectedItem, markdownArea, true));
        TabBuilder.selectTab(tab, tabPane);
    }

    public void buildFolderTab(Tab tab, TreeItem<String> selectedItem, String path)  {
        tab.setGraphic(TabBuilder.createCircleUnsavedChanges());
        FlowPane containerOfFiles = buildContainerOfFiles(path);
        tab.setContent(buildTabContent(tab, selectedItem, containerOfFiles, false));
    }

    //Creates a form and fills it with content
    @NotNull
    public BorderPane buildTabContent(Tab tab, TreeItem<String> selectedItem, Node centerContent,
                                      boolean createTitleBar) {
        BreadCrumbItem<String> root = Breadcrumbs.buildTreeModel(getPathToTheTreeViewNote(selectedItem));
        Breadcrumbs<String> crumbs = buildBreadcrumbs(root);

        String borderColor = ApplyConfiguration.getBorderColor();

        VBox vBoxCrumbs = new VBox(crumbs);
        vBoxCrumbs.setAlignment(Pos.CENTER);
        vBoxCrumbs.setPadding(new Insets(-10));

        HBox hBoxTitleBar = buildHBoxTitleBar(selectedItem, tab, crumbs);

        VBox vBoxTopContainer;

        if (createTitleBar)
            vBoxTopContainer = new VBox(vBoxCrumbs, hBoxTitleBar);
        else
            vBoxTopContainer = new VBox(vBoxCrumbs);

        vBoxTopContainer.setPadding(new Insets(5));

        //top right bottom left
        vBoxTopContainer.setStyle("-fx-border-color: transparent " + borderColor + borderColor + " transparent;");

        //center top right bottom left
        BorderPane borderPanePage = new BorderPane(centerContent, vBoxTopContainer, null, null, null);
        setSelectedCrumbListener(crumbs, hBoxTitleBar, borderPanePage, tab);

        return borderPanePage;
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

    private void setSelectedCrumbListener(Breadcrumbs<String> crumbs, HBox hBoxTitleBar, BorderPane borderPanePage,
                                          Tab tab) {
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
            String tabNoteName = listPath.getLast();
            tab.setText(tabNoteName);
            String pathTreeItem = String.join(RunApplication.getSeparator(), listPath);

            FlowPane folderContent = buildContainerOfFiles(pathTreeItem);
            borderPanePage.setCenter(folderContent);
        });
    }

    private FlowPane buildContainerOfFiles(String path) {
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

        //creating container for folder
        //top right bottom left
        return new FlowPane() {{
            setVgap(25);
            setHgap(25);
            //top right bottom left
            setPadding(new Insets(15, 0, 0, 15));
            getChildren().addAll(itemsFolderList);
        }};
    }

    @NotNull
    private Breadcrumbs<String> buildBreadcrumbs(BreadCrumbItem<String> root) {
        Breadcrumbs<String> crumbs = new Breadcrumbs<>(root);
        breadCrumbsButtons = new ArrayList<>();
        crumbs.setCrumbFactory(crumb -> {
            Button button;
            if (crumb.isLeaf()) {
                button = new Button(crumb.getValue(), IconConfigurer.getFileIcon());
            } else {
                button = new Button(crumb.getValue(), IconConfigurer.getFolderIcon(16));
            }
            button.toBack();
            button.getStyleClass().add(Styles.FLAT);
            button.setFocusTraversable(false);
            breadCrumbsButtons.add(button);
            return button;
        });
        crumbs.setDividerFactory(stringBreadCrumbItem -> {
            if (stringBreadCrumbItem == null) return null;
            return !stringBreadCrumbItem.isLast() ? new Label("", new FontIcon(Material2AL.CHEVRON_RIGHT)) : null;
        });
        return crumbs;
    }

    @NotNull
    private HBox buildHBoxTitleBar(TreeItem<String> selectedItem, Tab tab, Breadcrumbs<String> crumbs) {
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
                oldTextFieldString = noteNameText;
            }
            if (oldPropertyValue) {
                if (noteNameText.isEmpty()) {
                    noteName.setText(oldTextFieldString);
                } else {
                    //when lose focus and renaming note
                    //if text with no changes, return
                    if (noteNameText.equals(oldTextFieldString)) return;

                    //getting path to the file
                    String pathTreeItem = FileService.getPath(selectedItem);
                    int lastIndexOfSeparator = pathTreeItem.lastIndexOf(RunApplication.getSeparator());
                    pathTreeItem = pathTreeItem.substring(0, lastIndexOfSeparator);

                    //renameFile - newName, oldFile, path
                    boolean isSuccessRename = FileService.renameFile(noteNameText, selectedItem.getValue(),
                            pathTreeItem);

                    if (isSuccessRename) {
                        selectedItem.setValue(noteNameText);
                        tab.setText(noteNameText);

                        BreadCrumbItem<String> selectedCrumb = crumbs.getSelectedCrumb();
                        for (int i = breadCrumbsButtons.size() - 1; i > 0; i--) {
                            Button button = breadCrumbsButtons.get(i);
                            if (selectedCrumb.getValue().equals(button.getText())) {
                                button.setText(noteNameText);
                                break;
                            }
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

    private static String[] getPathToTheTreeViewNote(TreeItem<String> selectedItem) {
        TreeItem<String> currentTreeItem = selectedItem;
        List<String> breadCrumbItems = new ArrayList<>();
        while (currentTreeItem.getParent() != null) {
            if (currentTreeItem.getValue().isEmpty())
                break;
            breadCrumbItems.add(currentTreeItem.getValue());
            currentTreeItem = currentTreeItem.getParent();
        }
        breadCrumbItems = breadCrumbItems.reversed();
        return breadCrumbItems.toArray(new String[0]);
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
