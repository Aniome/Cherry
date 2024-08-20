package com.app.cherry.controls.codearea;

import atlantafx.base.theme.Styles;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.Paragraph;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Collection;
import java.util.function.IntFunction;

public class CopyNumberFactory implements IntFunction<Node> {
    private CodeArea codeArea;

    @Override
    public Node apply(int lineNumber) {
        Button button = new Button(null, new FontIcon("mdal-content_copy"));
        button.getStyleClass().addAll(Styles.BUTTON_CIRCLE, Styles.ACCENT);
        button.setAlignment(Pos.CENTER);
        button.setScaleX(0.5);
        button.setScaleY(0.5);
        button.setOnMouseClicked(mouseEvent -> {
            Paragraph<Collection<String>, String, Collection<String>> paragraph = codeArea.getParagraph(lineNumber);
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(paragraph.getText());
            clipboard.setContent(content);
        });
        return button;
    }

    public void setCodeArea(CodeArea codeArea) {
        this.codeArea = codeArea;
    }
}
