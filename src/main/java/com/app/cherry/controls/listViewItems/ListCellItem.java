package com.app.cherry.controls.listViewItems;

import atlantafx.base.controls.Spacer;
import atlantafx.base.theme.Styles;
import com.app.cherry.RunApplication;
import com.app.cherry.controllers.InitController;
import com.app.cherry.dao.RecentPathsDAO;
import com.app.cherry.util.Alerts;
import com.app.cherry.util.io.FileService;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import org.kordamp.ikonli.javafx.FontIcon;

import java.nio.file.Paths;
import java.util.ResourceBundle;

public class ListCellItem extends ListCell<String> {

    private final Label titleLabel;
    private final Label folderLabel;
    private final HBox root;

    public ListCellItem(ListView<String> listView, InitController initController) {

        titleLabel = new Label();

        folderLabel = new Label();
        folderLabel.setFont(new Font(18));

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

        VBox vBox = new VBox(folderLabel, titleLabel);

        root = new HBox(vBox,
                new Spacer(),
                closeBtn
        );
        root.setAlignment(Pos.CENTER_LEFT);

        root.setOnMouseClicked(mouseEvent -> {
            String path = titleLabel.getText();
            if (FileService.checkExists(path)){
                RunApplication.FolderPath = Paths.get(path);
                initController.showMainStage();
            } else {
                ResourceBundle resourceBundle = RunApplication.resourceBundle;
                Alerts.createAndShowWarning(resourceBundle.getString("InitFolderNotFound"));
            }
        });
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
