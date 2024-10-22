package com.app.cherry.ModalPane;

import atlantafx.base.controls.ModalPane;
import atlantafx.base.layout.ModalBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class SettingsModal {
    public void build(ModalPane modalPane, SplitPane splitPane) {
        Button closeBtn = new Button("Close");
        closeBtn.setOnAction(evt -> modalPane.hide(true));
        VBox settingsVbox = new VBox(closeBtn);

        VBox tabsVbox = createTabsVbox();

        SplitPane modalSplitPane = new SplitPane(tabsVbox, settingsVbox);
        modalSplitPane.setDividerPositions(0.2);

        VBox content = new VBox(modalSplitPane);
        content.setTranslateY(50);
        VBox.setVgrow(content, Priority.ALWAYS);
        content.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        ModalBox modalBox = new ModalBox(modalPane);
        modalBox.maxHeightProperty().bind(splitPane.heightProperty().subtract(200));
        modalBox.maxWidthProperty().bind(splitPane.widthProperty().subtract(200));
        content.minHeightProperty().bind(modalBox.heightProperty().subtract(50));
        content.minWidthProperty().bind(modalBox.widthProperty());
        modalSplitPane.minHeightProperty().bind(content.heightProperty());
        modalSplitPane.minWidthProperty().bind(content.widthProperty());
        modalBox.addContent(content);
        String style = "-fx-background-color: -color-bg-default;" +
                "-fx-background-radius: 20;";
        modalBox.setStyle(style);

        modalPane.show(modalBox);
    }

    private VBox createTabsVbox(){
        Font font = new Font("Arial", 18);
        ToggleButton mainSettingsBtn = new ToggleButton("Main Settings");
        mainSettingsBtn.setFont(font);
        ToggleButton otherSettingsBtn = new ToggleButton("Other Settings");
        otherSettingsBtn.setFont(font);
        mainSettingsBtn.setSelected(true);
        ToggleGroup toggleGroup = new ToggleGroup();
        toggleGroup.getToggles().addAll(mainSettingsBtn, otherSettingsBtn);

        VBox tabsVbox = new VBox(mainSettingsBtn, otherSettingsBtn);
        tabsVbox.setPadding(new Insets(10, 10, 10, 10));
        tabsVbox.setAlignment(Pos.TOP_LEFT);
        tabsVbox.setSpacing(10);
        BackgroundFill backgroundFill = new BackgroundFill(Color.web("#262626"), CornerRadii.EMPTY, Insets.EMPTY);
        tabsVbox.setBackground(new Background(backgroundFill));

        return tabsVbox;
    }
}
