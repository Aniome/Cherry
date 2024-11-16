package com.app.cherry.util.structures;

public class UniqueElementCodeArea {
    boolean isMarked;
    String text;
    int lineNumber;

    public UniqueElementCodeArea(boolean isMarked, String text, int lineNumber) {
        this.isMarked = isMarked;
        this.text = text;
        this.lineNumber = lineNumber;
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

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }
}
