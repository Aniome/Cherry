package com.app.cherry.ModalPane;

import atlantafx.base.controls.ModalPane;
import atlantafx.base.layout.ModalBox;
import com.app.cherry.RunApplication;
import com.app.cherry.controls.listViewItems.ListCellSettingsModal;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.Objects;

public class SettingsModal {
    public void build(ModalPane modalPane, SplitPane splitPane) {
        Button closeBtn = new Button("Close");
        closeBtn.setOnAction(evt -> modalPane.hide(true));
        VBox settingsVbox = new VBox(closeBtn);

        HBox tabsVbox = createTabsVbox();

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

    private HBox createTabsVbox(){
        ListView<String> listView = new ListView<>();
        listView.getItems().addAll("main", "other");
        listView.setPadding(new Insets(10, 10, 10, 10));
        listView.setStyle("-fx-border-radius: 20;");
        listView.setCellFactory(item ->{
            ListCellSettingsModal listCellSettingsModal = new ListCellSettingsModal();
            listCellSettingsModal.setOnMouseClicked(event -> {
                String listViewItem = listCellSettingsModal.getItem();
                if (listViewItem == null)
                    return;
                if (listViewItem.equals("main")) {
                    System.out.println("test");
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
}
