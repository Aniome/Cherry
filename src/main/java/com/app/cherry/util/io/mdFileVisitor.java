package com.app.cherry.util.io;

import com.app.cherry.RunApplication;

import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class mdFileVisitor extends SimpleFileVisitor<Path> {
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs){
        FileService.pathList.add(file);
        return FileVisitResult.CONTINUE;
    }
    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs){
        if (RunApplication.folderPath.equals(dir)){
            return FileVisitResult.CONTINUE;
        }
        FileService.pathList.add(dir);
        return FileVisitResult.CONTINUE;
    }
}
