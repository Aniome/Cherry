package com.app.cherry.controls.codearea;

import atlantafx.base.theme.Styles;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.Paragraph;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Collection;
import java.util.function.IntFunction;

public class CopyPasteFactory implements IntFunction<Node> {
    private CodeArea codeArea;

    public void setCodeArea(CodeArea codeArea) {
        this.codeArea = codeArea;
    }

    @Override
    public Node apply(int lineNumber) {
        Button copyButton = new Button(null, new FontIcon("mdal-content_copy"));
        setStyleButton(copyButton);
        copyButton.setOnMouseClicked(mouseEvent -> {
            Paragraph<Collection<String>, String, Collection<String>> paragraph = codeArea.getParagraph(lineNumber);
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(paragraph.getText());
            clipboard.setContent(content);
        });
        copyButton.setTranslateX(-5);

        Button pasteButton = new Button(null, new FontIcon("mdal-content_paste"));
        setStyleButton(pasteButton);
        pasteButton.setOnMouseClicked(mouseEvent -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            String pasteText = clipboard.getString();
            codeArea.insertText(lineNumber, 0, pasteText);
        });

        return new HBox(pasteButton, copyButton);
    }

    private void setStyleButton(Button button) {
        button.getStyleClass().addAll(Styles.BUTTON_CIRCLE, Styles.ACCENT);
        button.setAlignment(Pos.CENTER);
        button.setScaleX(0.5);
        button.setScaleY(0.5);
    }

}
