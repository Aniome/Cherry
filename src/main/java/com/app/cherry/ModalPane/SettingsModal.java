package com.app.cherry.ModalPane;

import atlantafx.base.controls.ModalPane;
import atlantafx.base.controls.Spacer;
import atlantafx.base.layout.ModalBox;
import atlantafx.base.theme.CupertinoLight;
import atlantafx.base.theme.Dracula;
import com.app.cherry.RunApplication;
import com.app.cherry.controls.listViewItems.ListCellSettingsModal;
import com.app.cherry.util.configuration.ApplyConfiguration;
import com.app.cherry.util.configuration.SavingConfiguration;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

public class SettingsModal {
    public void build(ModalPane modalPane, SplitPane splitPane) {
        VBox settingsVbox = new VBox();
        createMainSettings(settingsVbox);
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
            ListCellSettingsModal listCellSettingsModal = new ListCellSettingsModal();
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
        settingsVbox.setPadding(new Insets(10, 10, 10, 10));
        String settingsVboxStyle = "-fx-background-radius: 10; -fx-border-radius: 20;";
        ApplyConfiguration.applyThemeOnSettingsPage(settingsVbox, settingsVboxStyle);

        ResourceBundle resourceBundle = RunApplication.resourceBundle;
        String languageEng = resourceBundle.getString("LanguageEng");
        String languageRus = resourceBundle.getString("LanguageRus");

        //Language settings
        ChoiceBox<String> languageChoiceBox = new ChoiceBox<>();
        languageChoiceBox.getItems().addAll(languageEng, languageRus);
        Locale locale = resourceBundle.getLocale();
        SingleSelectionModel<String> languageChoiceBoxSelectionModel = languageChoiceBox.getSelectionModel();
        if (locale.equals(Locale.ENGLISH)){
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

        //Theme settings
        Label themLabel = new Label(resourceBundle.getString("SettingsTheme"));
        languageLabel.setFont(new Font(18));
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
        themeChoiceBoxSelectionModel.selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.equals(dracula)) {
                Application.setUserAgentStylesheet(new Dracula().getUserAgentStylesheet());
                ApplyConfiguration.theme = dracula;
                ApplyConfiguration.applyThemeOnMarkdownArea();
                ApplyConfiguration.applyThemeOnSettingsPage(settingsVbox, settingsVboxStyle);
            } else {
                Application.setUserAgentStylesheet(new CupertinoLight().getUserAgentStylesheet());
                ApplyConfiguration.theme = cupertinoLight;
                ApplyConfiguration.applyThemeOnMarkdownArea();
                ApplyConfiguration.applyThemeOnSettingsPage(settingsVbox, settingsVboxStyle);
            }
            ApplyConfiguration.applyThemeOnMainPage();
        });



        settingsVbox.getChildren().addAll(languageSettings, themeSettings);
    }
}
