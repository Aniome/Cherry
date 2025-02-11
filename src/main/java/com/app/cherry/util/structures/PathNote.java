package com.app.cherry.util.structures;

public class PathNote {
    String[] pathNote;
    int selectedIndex;

    public PathNote() {}

    public PathNote(String[] pathNote, int selectedIndex) {
        this.pathNote = pathNote;
        this.selectedIndex = selectedIndex;
    }

    public String[] getPathNote() {
        return pathNote;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }
}
