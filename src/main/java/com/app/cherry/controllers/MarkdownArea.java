package com.app.cherry.controllers;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.GenericStyledArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.Paragraph;
import org.fxmisc.richtext.model.StyleSpan;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.reactfx.collection.ListModification;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MarkdownArea {
    private static final String LINK_PATTERN = "\\b(" + "https://\\S+" + ")\\b";

    private static final Pattern PATTERN = Pattern.compile("(?<LINK>" + LINK_PATTERN + ")");

    public static StackPane createMarkdownArea() {
        CodeArea codeArea = new CodeArea();
        IntFunction<Node> numberFactory = LineNumberFactory.get(codeArea);
        CopyNumberFactory copyNumberFactory = new CopyNumberFactory();
        IntFunction<Node> graphicFactory = line -> {
            HBox hbox = new HBox(numberFactory.apply(line), copyNumberFactory.apply(line));
            hbox.setSpacing(1);
            hbox.setAlignment(Pos.CENTER);

            BorderPane borderPane1 = new BorderPane();
            borderPane1.setCenter(numberFactory.apply(line));
            borderPane1.setRight(copyNumberFactory.apply(line));
            if (line == 0){
                Rectangle rectangle = new Rectangle();
                rectangle.setFill(Color.GRAY);
                rectangle.widthProperty().bind(hbox.widthProperty().subtract(2));
                rectangle.heightProperty().bind(codeArea.heightProperty());
                StackPane.setAlignment(rectangle, Pos.TOP_LEFT);
                return new StackPane(rectangle, hbox);
            }
            return new StackPane(hbox);
        };

        codeArea.setOnMouseClicked(event -> {        codeArea.setParagraphGraphicFactory(graphicFactory);
            int offset = codeArea.getCurrentParagraph();
            Paragraph<Collection<String>, String, Collection<String>> paragraph = codeArea.getParagraph(offset);
            StyleSpans<Collection<String>> t = paragraph.getStyleSpans();
            int ind = t.getSpanCount();
            StyleSpan<Collection<String>> styleSpan = t.getStyleSpan(ind-1);
            String clickedText = paragraph.getText();


            if (styleSpan.getStyle().contains("link")) {
                System.out.println(clickedText);
            }
        });

        codeArea.setParagraphGraphicFactory(graphicFactory);

        codeArea.getVisibleParagraphs().addModificationObserver
                (new VisibleParagraphStyler<>(codeArea, MarkdownArea::computeHighlighting));

        // auto-indent: insert previous line's indents on enter
        final Pattern whiteSpace = Pattern.compile( "^\\s+" );

        codeArea.addEventHandler( KeyEvent.KEY_PRESSED, KE -> {
            if ( KE.getCode() == KeyCode.ENTER ) {
                int caretPosition = codeArea.getCaretPosition();
                int currentParagraph = codeArea.getCurrentParagraph();
                Matcher m0 = whiteSpace.matcher(codeArea.getParagraph( currentParagraph-1 ).getSegments().getFirst());
                if ( m0.find() )
                    Platform.runLater( () -> codeArea.insertText( caretPosition, m0.group() ) );
            }
        });

        return new StackPane(new VirtualizedScrollPane<>(codeArea));
    }

    private static StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        while(matcher.find()) {
            String styleClass =
                    matcher.group("KEYWORD") != null ? "keyword" :
                            matcher.group("PAREN") != null ? "paren" :
                                    matcher.group("BRACE") != null ? "brace" :
                                            matcher.group("BRACKET") != null ? "bracket" :
                                                    matcher.group("SEMICOLON") != null ? "semicolon" :
                                                            matcher.group("STRING") != null ? "string" :
                                                                    matcher.group("COMMENT") != null ? "comment" :
                                                                            matcher.group("LINK") != null ? "link" :
                                                                                    null; /* never happens */ assert styleClass != null;
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }

    private static class VisibleParagraphStyler<PS, SEG, S> implements Consumer<ListModification<? extends Paragraph<PS, SEG, S>>> {
        private final GenericStyledArea<PS, SEG, S> area;
        private final Function<String,StyleSpans<S>> computeStyles;
        private int prevParagraph, prevTextLength;

        public VisibleParagraphStyler( GenericStyledArea<PS, SEG, S> area, Function<String,StyleSpans<S>> computeStyles ) {
            this.computeStyles = computeStyles;
            this.area = area;
        }

        @Override
        public void accept( ListModification<? extends Paragraph<PS, SEG, S>> lm ) {
            if ( lm.getAddedSize() > 0 )
                Platform.runLater( () -> {
                    int paragraph = Math.min( area.firstVisibleParToAllParIndex() + lm.getFrom(),
                            area.getParagraphs().size() - 1);
                    String text = area.getText(paragraph, 0, paragraph, area.getParagraphLength(paragraph));

                    if ( paragraph != prevParagraph || text.length() != prevTextLength ) {
                        if ( paragraph < area.getParagraphs().size()-1 ) {
                            int startPos = area.getAbsolutePosition( paragraph, 0 );
                            area.setStyleSpans( startPos, computeStyles.apply( text ) );
                        }
                        prevTextLength = text.length();
                        prevParagraph = paragraph;
                    }
                });
        }
    }
}

