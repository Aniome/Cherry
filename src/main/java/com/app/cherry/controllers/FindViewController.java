package com.app.cherry.controllers;

import atlantafx.base.theme.Styles;
import atlantafx.base.theme.Tweaks;
import com.app.cherry.RunApplication;
import com.app.cherry.util.Alerts;
import com.app.cherry.util.structures.UniqueElementCodeArea;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.Paragraph;
import org.reactfx.collection.LiveList;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class FindViewController {
    @FXML
    Accordion accordion;
    @FXML
    ProgressBar progressBar;
    @FXML
    Label label;
    @FXML
    StackPane stackPane;

    private CodeArea codeArea;

    public void init(CodeArea codeArea) {
        this.codeArea = codeArea;
        progressBar.getStyleClass().add(Styles.LARGE);
    }

    @FXML
    private void findDuplicates() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                LiveList<Paragraph<Collection<String>, String, Collection<String>>> listParagraphs =
                        codeArea.getParagraphs();
                LinkedList<UniqueElementCodeArea> uniqueLinkedList = new LinkedList<>();
                for (int i = 0; i < listParagraphs.size(); i++) {
                    uniqueLinkedList.add(new UniqueElementCodeArea(false, listParagraphs.get(i).getText(), i));
                }
                Platform.runLater(() -> stackPane.setVisible(true));

                int length = uniqueLinkedList.size();
                HashMap<String, Set<Integer>> uniqueMap = new HashMap<>();
                for (int i = 0; i < length; i++) {
                    UniqueElementCodeArea uniqueI = uniqueLinkedList.get(i);
                    if (uniqueI.isMarked()) {
                        continue;
                    }
                    String uniqueTextI = uniqueI.getText();
                    for (int j = 0; j < uniqueLinkedList.size(); j++) {
                        if (i == j) {
                            continue;
                        }
                        UniqueElementCodeArea uniqueJ = uniqueLinkedList.get(j);
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
                    double progressPercent = ((double) (i + 1) / (double) length) * 100;

                    BigDecimal bd = new BigDecimal(progressPercent);
                    bd = bd.setScale(2, RoundingMode.HALF_UP);
                    double percentOut = bd.doubleValue();

                    updateProgress(percentOut, 100);
                    updateMessage(percentOut + "%");
                }

                Platform.runLater(() -> {
                    ResourceBundle resourceBundle = RunApplication.resourceBundle;
                    if (uniqueMap.isEmpty()) {
                        Alerts.createAndShowWarning(resourceBundle.getString("DuplicatesNotFound"));
                    }
                    for (String uniqueText : uniqueMap.keySet()) {
                        String replacedString = uniqueMap.get(uniqueText).toString().replaceAll("[\\[\\]]",
                                "");
                        String findingString = resourceBundle.getString("FindingStringP1") + " " + uniqueText
                                + " " + resourceBundle.getString("FindingStringP2") + " ";
                        TextFlow textFlow = new TextFlow(new Text(findingString + replacedString));
                        textFlow.setTextAlignment(TextAlignment.JUSTIFY);
                        textFlow.setMinHeight(70);
                        ScrollPane scrollPane = new ScrollPane(textFlow);
                        scrollPane.setFitToWidth(true);

                        TitledPane titledPane = new TitledPane(
                                resourceBundle.getString("DuplicateStringFound"), scrollPane);
                        titledPane.animatedProperty().bind(new SimpleBooleanProperty(true));
                        titledPane.getStyleClass().add(Tweaks.ALT_ICON);
                        accordion.getPanes().add(titledPane);
                    }
                });
                return null;
            }
        };

        // reset properties, so we can start a new task
        task.setOnSucceeded(evt2 -> {
            progressBar.progressProperty().unbind();
            label.textProperty().unbind();

            progressBar.setProgress(0);
            label.setText(null);

            stackPane.setVisible(false);
        });

        progressBar.progressProperty().bind(task.progressProperty());
        label.textProperty().bind(task.messageProperty());

        new Thread(task).start();
    }
}
