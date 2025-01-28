package com.app.cherry.util.structures;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

public class SearchListViewItemTest {
    @Test
    void testToString() {
        String nameNote = "Hello";
        SearchListViewItem searchListViewItem = new SearchListViewItem(nameNote, Path.of("D:\\test.md"));
        String toString = searchListViewItem.toString();
        Assertions.assertEquals(nameNote, toString);
    }
}
