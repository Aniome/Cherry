package com.app.cherry.ModalPane;

import atlantafx.base.controls.ModalPane;
import atlantafx.base.controls.Spacer;
import atlantafx.base.layout.ModalBox;
import atlantafx.base.theme.CupertinoLight;
import atlantafx.base.theme.Dracula;
import atlantafx.base.util.IntegerStringConverter;
import com.app.cherry.RunApplication;
import com.app.cherry.controls.codearea.MarkdownArea;
import com.app.cherry.controls.listViewItems.ListCellItemsSettingsModal;
import com.app.cherry.util.configuration.ApplyConfiguration;
import com.app.cherry.util.configuration.SavingConfiguration;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;

import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

public class SettingsModal {
    private static ObservableList<Tab> tabs;

    public void build(ModalPane modalPane, SplitPane splitPane, ObservableList<Tab> tabs) {
        VBox settingsVbox = new VBox();
        createMainSettings(settingsVbox);
        SettingsModal.tabs = tabs;
        HBox tabsVbox = createTabsVbox(settingsVbox);

        SplitPane modalSplitPane = new SplitPane(tabsVbox, settingsVbox);
        modalSplitPane.setDividerPositions(0.2);

        VBox content = new VBox(modalSplitPane);
        content.setTranslateY(50);

        ModalBox modalBox = new ModalBox(modalPane);
        modalBox.maxHeightProperty().bind(splitPane.heightProperty().subtract(200));
        modalBox.maxWidthProperty().bind(splitPane.widthProperty().subtract(200));
        content.minHeightProperty().bind(modalBox.heightProperty().subtract(50));
        content.minWidthProperty().bind(modalBox.widthProperty());
        content.maxHeightProperty().bind(modalBox.heightProperty().subtract(50));
        modalSplitPane.minHeightProperty().bind(content.heightProperty());
        modalSplitPane.minWidthProperty().bind(content.widthProperty());
        modalBox.addContent(content);
        String style = "-fx-background-color: -color-bg-default;" +
                "-fx-background-radius: 20;";
        modalBox.setStyle(style);

        modalPane.show(modalBox);
    }

    private HBox createTabsVbox(VBox settingsVbox) {
        ListView<String> listView = new ListView<>();
        ResourceBundle resourceBundle = RunApplication.resourceBundle;
        String tabGeneral = resourceBundle.getString("SettingsTabGeneral");
        listView.getItems().addAll(tabGeneral);
        listView.setPadding(new Insets(10, 10, 10, 10));
        listView.setStyle("-fx-border-radius: 20;");
        listView.setCellFactory(item ->{
            ListCellItemsSettingsModal listCellSettingsModal = new ListCellItemsSettingsModal();
            listCellSettingsModal.setOnMouseClicked(event -> {
                String listViewItem = listCellSettingsModal.getItem();
                if (listViewItem == null)
                    return;
                if (listViewItem.equals(tabGeneral)) {
                    createMainSettings(settingsVbox);
                }
            });
            return listCellSettingsModal;
        });

        HBox tabsVbox = new HBox(listView);
        tabsVbox.getStylesheets().add(Objects.requireNonNull
                (RunApplication.class.getResource("css/settingsModalListView.css")).toExternalForm());
        listView.maxHeightProperty().bind(tabsVbox.heightProperty());

        return tabsVbox;
    }

    private void createMainSettings(VBox settingsVbox) {
        settingsVbox.getChildren().clear();

        settingsVbox.setSpacing(10);
        settingsVbox.setPadding(new Insets(10));
        String settingsVboxStyle = "-fx-background-radius: 10; -fx-border-radius: 20;";
        ApplyConfiguration.applyThemeOnSettingsPage(settingsVbox, settingsVboxStyle);

        ResourceBundle resourceBundle = RunApplication.resourceBundle;
        String languageEng = resourceBundle.getString("LanguageEng");
        String languageRus = resourceBundle.getString("LanguageRus");

        //Language settings
        HBox languageSettings = changeLanguage(resourceBundle, languageEng, languageRus);

        //Theme settings
        HBox themeSettings = changeTheme(resourceBundle, settingsVbox, settingsVboxStyle);

        //Font size settings
        HBox fontSizeSettings = changeFontSize(resourceBundle);

        settingsVbox.getChildren().addAll(languageSettings, themeSettings, fontSizeSettings);
    }

    private static HBox changeLanguage(ResourceBundle resourceBundle, String languageEng,
                                                    String languageRus) {
        ChoiceBox<String> languageChoiceBox = new ChoiceBox<>();
        languageChoiceBox.getItems().addAll(languageEng, languageRus);
        Locale locale = resourceBundle.getLocale();
        SingleSelectionModel<String> languageChoiceBoxSelectionModel = languageChoiceBox.getSelectionModel();
        if (locale.equals(Locale.ENGLISH)) {
            languageChoiceBoxSelectionModel.select(languageEng);
        } else {
            languageChoiceBoxSelectionModel.select(languageRus);
        }
        Label languageLabel = new Label(resourceBundle.getString("SettingsLanguage"));
        languageLabel.setFont(new Font(18));
        HBox languageSettings = new HBox(languageLabel, new Spacer(), languageChoiceBox);

        languageChoiceBoxSelectionModel.selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.equals(languageRus)) {
                RunApplication.resourceBundle = ResourceBundle.getBundle("local/text",
                        Locale.of("ru"));
                SavingConfiguration.language = "ru";
            } else {
                RunApplication.resourceBundle = ResourceBundle.getBundle("local/text", Locale.ENGLISH);
                SavingConfiguration.language = "en";
            }
        });
        return languageSettings;
    }

    private static HBox changeTheme(ResourceBundle resourceBundle, VBox settingsVbox, String settingsVboxStyle) {
        Label themLabel = new Label(resourceBundle.getString("SettingsTheme"));
        ChoiceBox<String> themeChoiceBox = new ChoiceBox<>();
        String dracula = "Dark";
        String cupertinoLight = "Light";
        themeChoiceBox.getItems().addAll(cupertinoLight, dracula);
        HBox themeSettings = new HBox(themLabel, new Spacer(), themeChoiceBox);
        SingleSelectionModel<String> themeChoiceBoxSelectionModel = themeChoiceBox.getSelectionModel();
        if (Application.getUserAgentStylesheet().contains("dracula")){
            themeChoiceBoxSelectionModel.select(dracula);
        } else {
            themeChoiceBoxSelectionModel.select(cupertinoLight);
        }
        themeChoiceBoxSelectionModel.selectedItemProperty().addListener((
                observable, oldValue, newValue) -> {
            if (newValue.equals(dracula)) {
                Application.setUserAgentStylesheet(new Dracula().getUserAgentStylesheet());
                ApplyConfiguration.theme = dracula;
            } else {
                Application.setUserAgentStylesheet(new CupertinoLight().getUserAgentStylesheet());
                ApplyConfiguration.theme = cupertinoLight;
            }
            updateTheme(settingsVbox, settingsVboxStyle);
        });
        return themeSettings;
    }

    private static void updateTheme(VBox settingsVbox, String settingsVboxStyle) {
        ApplyConfiguration.updateThemeOnMarkdownArea();
        ApplyConfiguration.applyThemeOnSettingsPage(settingsVbox, settingsVboxStyle);
        ApplyConfiguration.applyThemeOnMainPageLeftPanel();
        ApplyConfiguration.updateThemeOnStackPaneBackgroundLineNumber(tabs);
        ApplyConfiguration.updateThemeOnTopContainer(tabs);
    }

    private static HBox changeFontSize(ResourceBundle resourceBundle) {
        Label changeFontLabel = new Label(resourceBundle.getString("SettingsLabelFontSize"));
        Spinner<Integer> fontSizeSpinner = new Spinner<>(1, 50, MarkdownArea.fontSize);
        IntegerStringConverter.createFor(fontSizeSpinner);
        fontSizeSpinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
        fontSizeSpinner.setEditable(true);

        fontSizeSpinner.valueProperty().addListener(
                (observable, oldValue, newValue) -> {
            for (Tab tab: tabs) {
                Node tabContent = tab.getContent();
                if (tabContent instanceof AnchorPane) return;

                BorderPane borderPaneContent = (BorderPane) tabContent;
                StackPane stackPaneContent = (StackPane) borderPaneContent.getCenter();
                var virtualizedScrollPane = (VirtualizedScrollPane<?>) stackPaneContent.getChildren().getFirst();
                CodeArea codeArea = (CodeArea) virtualizedScrollPane.getContent();

                MarkdownArea.fontSize = newValue;
                codeArea.setStyle("-fx-font-size: "+ newValue +"px;");
            }
        });

        return new HBox(changeFontLabel, new Spacer(), fontSizeSpinner);
    }
}
