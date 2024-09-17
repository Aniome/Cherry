package com.app.cherry.controls.codearea;

import org.fxmisc.richtext.StyleClassedTextArea;

public class MarkdownArea extends StyleClassedTextArea {
    public MarkdownArea(String text) {
        insertText(0, text);

    }
}
