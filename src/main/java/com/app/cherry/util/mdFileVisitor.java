package com.app.cherry.util;

import com.app.cherry.Markdown;
import com.app.cherry.RunApplication;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class mdFileVisitor extends SimpleFileVisitor<Path> {
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Markdown.pathList.add(file);
        return FileVisitResult.CONTINUE;
    }
    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        if (RunApplication.FolderPath.equals(dir)){
            return FileVisitResult.CONTINUE;
        }
        Markdown.pathList.add(dir);
        return FileVisitResult.CONTINUE;
    }
}
