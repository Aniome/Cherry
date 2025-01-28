package com.app.cherry.util.structures;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class UniqueElementCodeAreaTest {
    @Test
    public void testingClass() {
        String text = "Testing note";
        boolean isMarked = false;
        int lineNumber = 0;
        UniqueElementCodeArea uniqueElementCodeArea = new UniqueElementCodeArea(isMarked, text, lineNumber);
        Assertions.assertEquals(isMarked, uniqueElementCodeArea.isMarked());
        Assertions.assertEquals(lineNumber, uniqueElementCodeArea.getLineNumber());
        Assertions.assertEquals(text, uniqueElementCodeArea.getText());
    }
}
