package com.app.cherry.util.structures;

import java.nio.file.Path;

public class PathNote {
    Path[] pathNote;

    public Path[] getPathNote() {
        return pathNote;
    }

    public void setPathNote(Path pathNote) {
        this.pathNote = new Path[]{pathNote};
    }
}
