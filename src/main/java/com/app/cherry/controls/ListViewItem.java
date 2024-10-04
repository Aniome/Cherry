package com.app.cherry.controls;

import atlantafx.base.controls.Spacer;
import atlantafx.base.theme.Styles;
import com.app.cherry.RunApplication;
import com.app.cherry.controllers.InitController;
import com.app.cherry.dao.RecentPathsDAO;
import com.app.cherry.util.Alerts;
import com.app.cherry.util.FileService;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.nio.file.Paths;

public class ListViewItem extends ListCell<String> {

    //private final ScrollPane scrollPane;
    private final Label titleLabel;
    private final Label folderLabel;
    private final HBox root;
    private final ListView<String> listView;

    public ListViewItem(ListView<String> listView, InitController initController) {
        this.listView = listView;

        titleLabel = new Label();
        titleLabel.setOnMouseClicked(mouseEvent -> {
            String path = titleLabel.getText();
            if (FileService.checkExists(path)){
                RunApplication.FolderPath = Paths.get(path);
                initController.ShowMainStage();
            } else {
                Alerts.CreateAndShowWarning("Папка не найдена");
            }
        });

        folderLabel = new Label();

        FontIcon fontIcon = new FontIcon("mdal-close");
        fontIcon.setScaleX(1.3);
        fontIcon.setScaleY(1.3);

        Button closeBtn = new Button(null, fontIcon);
        closeBtn.setScaleX(0.7);
        closeBtn.setScaleY(0.7);
        closeBtn.getStyleClass().addAll(Styles.BUTTON_CIRCLE, Styles.BUTTON_OUTLINED, Styles.DANGER);
        closeBtn.setOnMouseClicked(mouseEvent -> {
            String deletingPath = titleLabel.getText();
            listView.getItems().remove(deletingPath);
            RecentPathsDAO.removePath(deletingPath);
        });

//        BorderPane borderPane = new BorderPane();
//        borderPane.setTop(folderLabel);
//        borderPane.setCenter(titleLabel);
//        borderPane.setRight(closeBtn);
//        scrollPane = new ScrollPane(borderPane);

        VBox vBox = new VBox(folderLabel, titleLabel);

        root = new HBox(vBox,
                new Spacer(),
                closeBtn
        );
        root.setAlignment(Pos.CENTER_LEFT);
    }

    @Override
    public void updateItem(String value, boolean empty) {
        super.updateItem(value, empty);

        if (empty) {
            setGraphic(null);
            return;
        }


        int lastSlash = value.lastIndexOf("\\") + 1;
        String folderName = value.substring(lastSlash);
        folderLabel.setText(folderName);
        titleLabel.setText(value);
        setGraphic(root);
    }
}
