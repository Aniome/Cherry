package com.app.cherry.controls;

import com.app.cherry.controls.codearea.MixedArea;
import com.app.cherry.util.Unique;
import javafx.application.Platform;
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
import org.fxmisc.richtext.model.Paragraph;
import org.jetbrains.annotations.NotNull;
import org.reactfx.collection.LiveList;

import java.util.*;

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
        button.setOnAction(e -> {
            ObservableList<Node> childrens = borderPane.getChildren();
            for (Node children: childrens){
                if (children instanceof StackPane stackPane){
                    Thread thread = new Thread(() -> {
                        @SuppressWarnings("unchecked")
                        VirtualizedScrollPane<CodeArea> virtualizedScrollPane = (VirtualizedScrollPane<CodeArea>) stackPane.getChildren().getFirst();
                        CodeArea codeArea = virtualizedScrollPane.getContent();
                        LiveList<Paragraph<Collection<String>, String, Collection<String>>> listParagraphs = codeArea.getParagraphs();
                        LinkedList<Unique> uniqueLinkedList = new LinkedList<>();
                        for (int i = 0; i < listParagraphs.size(); i++) {
                            uniqueLinkedList.add(new Unique(false, listParagraphs.get(i).getText(), i));
                        }
                        HashMap<String, Set<Integer>> uniqueMap = new HashMap<>();
                        for (int i = 0; i < uniqueLinkedList.size(); i++) {
                            Unique uniqueI = uniqueLinkedList.get(i);
                            if (uniqueI.isMarked()) {
                                continue;
                            }
                            String uniqueTextI = uniqueI.getText();
                            for (int j = 0; j < uniqueLinkedList.size(); j++) {
                                if (i == j) {
                                    continue;
                                }
                                Unique uniqueJ = uniqueLinkedList.get(j);
                                String uniqueTextJ = uniqueJ.getText();
                                if (uniqueTextI.equals(uniqueTextJ) && !uniqueTextJ.isEmpty()) {
                                    if (uniqueMap.containsKey(uniqueTextI)) {
                                        uniqueMap.get(uniqueTextI).add(uniqueJ.getLineNumber());
                                        uniqueJ.setMarked(true);
                                    } else {
                                        LinkedHashSet<Integer> uniqueSet = new LinkedHashSet<>();
                                        uniqueSet.add(uniqueI.getLineNumber());
                                        uniqueSet.add(uniqueJ.getLineNumber());
                                        uniqueMap.put(uniqueTextI, uniqueSet);
                                        uniqueJ.setMarked(true);
                                    }
                                }
                            }
                            System.out.println(i);
                        }
                        System.out.println(uniqueMap);
                    });
                    thread.start();
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

        MixedArea mixedArea = new MixedArea();
        borderPane.setCenter(mixedArea.createMarkdownArea());

        return borderPane;
    }
}
