package com.app.cherry.util.structures;

import java.nio.file.Path;

public class SearchListViewItem {
    public String searchText;
    public Path path;

    public SearchListViewItem(String searchText, Path path) {
        this.searchText = searchText;
        this.path = path;
    }
}
