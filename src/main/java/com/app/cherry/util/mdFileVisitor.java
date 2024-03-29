package com.app.cherry.util;

import javafx.scene.control.TreeItem;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class mdFileVisitor extends SimpleFileVisitor<Path>{
    TreeItem<String> treeItem;

    public mdFileVisitor(TreeItem<String> treeItem) {
        this.treeItem = treeItem;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        return null;
    }
}
