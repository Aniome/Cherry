package com.app.cherry.controls;

import com.app.cherry.controls.codearea.MixedArea;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.jetbrains.annotations.NotNull;

public class TabManager {
    private String oldTextFieldValue;

    public static void selectTab(Tab tab, TabPane tabPane){
        int count = tabPane.getTabs().size() - 1;
        SingleSelectionModel<Tab> selectionModel = tabPane.getSelectionModel();
        tabPane.getTabs().add(count, tab);
        selectionModel.select(tab);
    }

    public static BorderPane createEmptyTab(){
        BorderPane borderPane = new BorderPane();
        Label label = new Label("Ни один файл не открыт");
        label.setFont(new Font(29));
        borderPane.setCenter(label);
        borderPane.setStyle("-fx-background-color: #282a36");
        return borderPane;
    }

    public static void addTab(String fileName, TabPane tabPane){
        Tab tab = new Tab(fileName);
        TabManager tabManager = new TabManager();
        tab.setContent(tabManager.createTab(tab));
        TabManager.selectTab(tab, tabPane);
    }

    //Creates a form and fills it with content
    @NotNull
    public BorderPane createTab(Tab tab){
        BorderPane borderPane = new BorderPane();

        TextField textField = new TextField(tab.getText()){{
            setFont(new Font(16));
            setAlignment(Pos.CENTER);
        }};
        Button button = new Button("Найти повторяющиеся строки");
        button.setOnAction(event -> {
            ObservableList<Node> childrens = borderPane.getChildren();
            for (Node children: childrens){
                if (children instanceof StackPane stackPane){
                    @SuppressWarnings("unchecked")
                    VirtualizedScrollPane<CodeArea> virtualizedScrollPane = (VirtualizedScrollPane<CodeArea>) stackPane.getChildren().getFirst();
                    CodeArea codeArea = virtualizedScrollPane.getContent();
                }
            }
        });
        ToolBar toolBar = new ToolBar(button);
        VBox vBox = new VBox(textField, toolBar);
        borderPane.setTop(vBox);

        textField.focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
            //newPropertyValue - on focus
            //oldPropertyValue - out focus
            if (newPropertyValue) {
                oldTextFieldValue = textField.getText();
            }
            if (oldPropertyValue && textField.getText().isEmpty()) {
                textField.setText(oldTextFieldValue);
            } else {
                tab.setText(textField.getText());
            }
        });

        borderPane.setCenter(MixedArea.createMarkdownArea());

        return borderPane;
    }
}
