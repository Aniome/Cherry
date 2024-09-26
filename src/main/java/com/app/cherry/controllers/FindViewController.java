package com.app.cherry.controllers;

import atlantafx.base.theme.Tweaks;
import com.app.cherry.util.Unique;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Accordion;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TitledPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.Paragraph;
import org.reactfx.collection.LiveList;

import java.util.*;

public class FindViewController {
    @FXML
    Accordion accordion;
    private CodeArea codeArea;

    public void init(CodeArea codeArea) {
        this.codeArea = codeArea;
    }

    @FXML
    private void findDuplicates() {
        Thread thread = new Thread(() -> {
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
            }
            Platform.runLater( () -> {
                for (String uniqueText : uniqueMap.keySet()) {
                    String replacedString = uniqueMap.get(uniqueText).toString().replaceAll("[|]", "");
                    String text = "Строка " + uniqueText + " повторяется на следующих строках: ";
                    Label label = new Label(text + replacedString);
                    TitledPane titledPane = new TitledPane("Найден дубликат строки", label);
                    titledPane.getStyleClass().add(Tweaks.ALT_ICON);
                    accordion.getPanes().add(titledPane);
                }

            });
        });
        thread.start();
    }
}
