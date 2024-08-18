package com.app.cherry.controls;

import atlantafx.base.controls.Spacer;
import atlantafx.base.theme.Styles;
import com.app.cherry.RunApplication;
import com.app.cherry.controllers.InitController;
import com.app.cherry.dao.RecentPathsDAO;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.nio.file.Paths;

public class ListViewItem extends ListCell<String> {

    private final HBox root;
    private final Label titleLabel;

    public ListViewItem(ListView<String> listView, InitController initController) {
        titleLabel = new Label();
        titleLabel.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getClickCount() >= 2){
                RunApplication.FolderPath = Paths.get(titleLabel.getText());
                initController.ShowMainStage();
            }
        });

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

        root = new HBox(titleLabel,
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

        titleLabel.setText(value);
        setGraphic(root);
    }
}
