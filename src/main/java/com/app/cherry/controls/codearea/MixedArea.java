package com.app.cherry.controls.codearea;

import com.app.cherry.RunApplication;
import com.app.cherry.controllers.WebViewController;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.GenericStyledArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.Paragraph;
import org.fxmisc.richtext.model.StyleSpan;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.jetbrains.annotations.NotNull;
import org.reactfx.collection.ListModification;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MixedArea {
    private static final String LINK_PATTERN = "\\b(" + "(http|https)://\\S+" + ")\\b";
    private static final String WORDS_PATTERN = ".*";

    private static final Pattern PATTERN = Pattern.compile(
            "(?<LINK>" + LINK_PATTERN + ")"
            + "|(?<WORDS>" + WORDS_PATTERN + ")"
    );

    public StackPane createMarkdownArea() {
        CodeArea codeArea = new CodeArea();
        IntFunction<Node> numberFactory = LineNumberFactory.get(codeArea);
        IntFunction<Node> graphicFactory = createGraphicFactory(numberFactory, codeArea);

        codeArea.setParagraphGraphicFactory(graphicFactory);

        codeArea.setOnMouseClicked(event -> {
            int offset = codeArea.getCurrentParagraph();
            Paragraph<Collection<String>, String, Collection<String>> paragraph = codeArea.getParagraph(offset);
            StyleSpans<Collection<String>> styleSpans = paragraph.getStyleSpans();
            int ind = styleSpans.getSpanCount();
            StyleSpan<Collection<String>> styleSpan = styleSpans.getStyleSpan(ind - 1);
            String clickedText = paragraph.getText();

            if (styleSpan.getStyle().contains("link")) {
                try {
                    FXMLLoader fxmlLoader = new FXMLLoader(RunApplication.class.getResource("fxmls/web-view.fxml"));
                    double webViewWidth = 800, webViewHeight = 600;
                    Scene secondScene = new Scene(fxmlLoader.load(), webViewWidth, webViewHeight);
                    Stage webViewStage = new Stage();
                    RunApplication.setIcon(webViewStage);
                    WebViewController webViewController = fxmlLoader.getController();
                    webViewController.init(clickedText);
                    RunApplication.prepareStage(webViewHeight, webViewWidth, secondScene,"", webViewStage);
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
        });

        codeArea.getVisibleParagraphs().addModificationObserver
                (new VisibleParagraphStyler<>(codeArea, this::computeHighlighting));

        // auto-indent: insert previous line's indents on enter
        final Pattern whiteSpace = Pattern.compile( "^\\s+" );

        codeArea.addEventHandler(KeyEvent.KEY_PRESSED, KE -> {
            if ( KE.getCode() == KeyCode.ENTER ) {
                int caretPosition = codeArea.getCaretPosition();
                int currentParagraph = codeArea.getCurrentParagraph();
                Matcher m0 = whiteSpace.matcher(codeArea.getParagraph(currentParagraph - 1).getSegments().getFirst());
                if (m0.find())
                    Platform.runLater( () -> codeArea.insertText(caretPosition, m0.group()) );
            }
        });

        return new StackPane(new VirtualizedScrollPane<>(codeArea));
    }

    private static @NotNull IntFunction<Node> createGraphicFactory(IntFunction<Node> numberFactory, CodeArea codeArea) {
        CopyNumberFactory copyNumberFactory = new CopyNumberFactory();
        copyNumberFactory.setCodeArea(codeArea);
        return line -> {
            HBox hbox = new HBox(numberFactory.apply(line), copyNumberFactory.apply(line));
            hbox.setSpacing(1);
            hbox.setAlignment(Pos.CENTER);

            if (line == 0){
                Rectangle rectangle = new Rectangle();
                rectangle.setFill(Color.web("#282c34"));
                rectangle.widthProperty().bind(hbox.widthProperty().subtract(2));
                rectangle.heightProperty().bind(codeArea.heightProperty());
                StackPane.setAlignment(rectangle, Pos.TOP_LEFT);
                return new StackPane(rectangle, hbox);
            }
            return new StackPane(hbox);
        };
    }

    private StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        while(matcher.find()) {
            String styleClass =
                    matcher.group("LINK") != null ? "link" :
                        matcher.group("WORDS") != null ? "word-code" :
                            null; /* never happens */ assert styleClass != null;
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }

    private class VisibleParagraphStyler<PS, SEG, S> implements Consumer<ListModification<? extends Paragraph<PS, SEG, S>>> {
        private final GenericStyledArea<PS, SEG, S> area;
        private final Function<String,StyleSpans<S>> computeStyles;
        private int index = 0;

        public VisibleParagraphStyler( GenericStyledArea<PS, SEG, S> area, Function<String,StyleSpans<S>> computeStyles ) {
            this.computeStyles = computeStyles;
            this.area = area;
        }

        @Override
        public void accept(ListModification<? extends Paragraph<PS, SEG, S>> lm) {
            if (lm.getAddedSize() > 0){
                Platform.runLater( () -> {
                    int paragraphSize = area.getParagraphs().size();
                    if (index < paragraphSize) {
                        String text = area.getText(index, 0, index, area.getParagraphLength(index));
                        int startPos = area.getAbsolutePosition(index, 0);
                        area.setStyleSpans(startPos, computeStyles.apply(text));
                        index++;
                    } else {
                        int currentParagraph = area.getCurrentParagraph();
                        String text = area.getText(currentParagraph, 0,
                                currentParagraph, area.getParagraphLength(currentParagraph));
                        int startPos = area.getAbsolutePosition(currentParagraph, 0);
                        area.setStyleSpans(startPos, computeStyles.apply(text));
                    }
                });
            }
        }
    }
}

