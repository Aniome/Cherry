package com.app.cherry.util;

public class Unique {
    boolean isMarked;
    String text;
    int lineNumber;

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public Unique(boolean isMarked, String text) {
        this.isMarked = isMarked;
        this.text = text;
    }

    public boolean isMarked() {
        return isMarked;
    }

    public void setMarked(boolean marked) {
        isMarked = marked;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
