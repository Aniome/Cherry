package com.app.cherry.controls.codearea;

import com.app.cherry.RunApplication;
import com.app.cherry.util.configuration.ApplyConfiguration;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
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

import java.util.Collection;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MarkdownArea {
    private static final String LINK_PATTERN = "(http|https)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]+";
    private static final String WORDS_PATTERN = ".*";
    private static CodeArea codeArea;
    public static int fontSize;

    private static final Pattern PATTERN = Pattern.compile(
            "(?<LINK>" + LINK_PATTERN + ")"
            + "|(?<WORDS>" + WORDS_PATTERN + ")"
    );

    public static StackPane createMarkdownArea() {
        CodeArea codeArea = new CodeArea();
        codeArea.setStyle("-fx-font-size: "+ fontSize +"px;");
        MarkdownArea.codeArea = codeArea;
        IntFunction<Node> numberFactory = LineNumberFactory.get(codeArea);
        IntFunction<Node> graphicFactory = createGraphicFactory(numberFactory, codeArea);

        codeArea.setParagraphGraphicFactory(graphicFactory);

        codeArea.setOnMouseClicked(event -> {
            if (event.isControlDown()) {
                int offset = codeArea.getCurrentParagraph();
                Paragraph<Collection<String>, String, Collection<String>> paragraph = codeArea.getParagraph(offset);
                StyleSpans<Collection<String>> styleSpans = paragraph.getStyleSpans();
                int ind = styleSpans.getSpanCount();
                StyleSpan<Collection<String>> styleSpan = styleSpans.getStyleSpan(ind - 1);
                String clickedText = paragraph.getText();

                if (styleSpan.getStyle().contains("link")) {
                    RunApplication.showBrowserWindow(clickedText);
                }
            }
        });

        codeArea.setOnKeyPressed(event -> {
            KeyCombination keyCombination = new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN);
            if (keyCombination.match(event)) {
                RunApplication.showFindWindow(codeArea);
            }
        });

        codeArea.getVisibleParagraphs().addModificationObserver
                (new VisibleParagraphStyler<>(codeArea, MarkdownArea::computeHighlighting));

        // auto-indent: insert previous line's indents on enter
        final Pattern whiteSpace = Pattern.compile( "^\\s+" );

        codeArea.addEventHandler(KeyEvent.KEY_PRESSED, KE -> {
            if ( KE.getCode() == KeyCode.ENTER ) {
                int caretPosition = codeArea.getCaretPosition();
                int currentParagraph = codeArea.getCurrentParagraph();
                Matcher m0 = whiteSpace.matcher(codeArea.getParagraph(currentParagraph - 1).getSegments()
                        .getFirst());
                if (m0.find())
                    Platform.runLater( () -> codeArea.insertText(caretPosition, m0.group()) );
            }
        });

        return new StackPane(new VirtualizedScrollPane<>(codeArea));
    }

    public static void applyStylesPage(int pageLength){
        for (int i = 0; i < pageLength; i++) {
            String textCodeArea = codeArea.getText(i, 0, i, codeArea.getParagraphLength(i));
            int startPos = codeArea.getAbsolutePosition(i, 0);
            codeArea.setStyleSpans(startPos, MarkdownArea.computeHighlighting(textCodeArea));
        }
    }

    public static void applyStyles(int from, int to){
        Thread thread = new Thread(() -> {
            //add styling for the text
            Platform.runLater(() -> {
                for (int i = from; i < to; i++) {
                    String textCodeArea = codeArea.getText(i, 0, i, codeArea.getParagraphLength(i));
                    int startPos = codeArea.getAbsolutePosition(i, 0);
                    codeArea.setStyleSpans(startPos, MarkdownArea.computeHighlighting(textCodeArea));
                }
            });
        });
        thread.start();
    }

    private static @NotNull IntFunction<Node> createGraphicFactory(IntFunction<Node> numberFactory, CodeArea codeArea) {
        CopyNumberFactory copyNumberFactory = new CopyNumberFactory();
        copyNumberFactory.setCodeArea(codeArea);
        return line -> {
            HBox hbox = new HBox(numberFactory.apply(line), copyNumberFactory.apply(line));
            hbox.setSpacing(1);
            hbox.setAlignment(Pos.CENTER);

            Rectangle rectangle = new Rectangle();
            ApplyConfiguration.applyThemeOnRectangleBackgroundLineNumber(rectangle);
            if (line == 0) {
                rectangle.widthProperty().bind(hbox.widthProperty().subtract(2));
                rectangle.heightProperty().bind(codeArea.heightProperty());
            } else {
                rectangle.widthProperty().bind(hbox.widthProperty().subtract(2));
                rectangle.heightProperty().bind(hbox.heightProperty());
            }
            StackPane.setAlignment(rectangle, Pos.TOP_LEFT);
            StackPane stackPane = new StackPane(rectangle, hbox);
            stackPane.toFront();
            ApplyConfiguration.listStackPaneLineNumber.add(stackPane);
            return stackPane;
        };
    }

    public static StyleSpans<Collection<String>> computeHighlighting(String text) {
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

    private static class VisibleParagraphStyler<PS, SEG, S> implements
            Consumer<ListModification<? extends Paragraph<PS, SEG, S>>> {
        private final GenericStyledArea<PS, SEG, S> area;
        private final Function<String,StyleSpans<S>> computeStyles;
        public int index = 0;

        public VisibleParagraphStyler( GenericStyledArea<PS, SEG, S> area,
                                       Function<String,StyleSpans<S>> computeStyles ) {
            this.computeStyles = computeStyles;
            this.area = area;
        }

        @Override
        public void accept(ListModification<? extends Paragraph<PS, SEG, S>> lm) {
            if (lm.getAddedSize() > 0) {
                Platform.runLater( () -> {
                    int paragraphSize = area.getParagraphs().size();
                    if (index < paragraphSize) {
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

