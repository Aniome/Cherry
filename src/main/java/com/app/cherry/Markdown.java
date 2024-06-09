package com.app.cherry;

import com.app.cherry.util.Alerts;
import com.app.cherry.util.DeletingFileVisitor;
import com.app.cherry.util.mdFileVisitor;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

public class Markdown {
    //List for FileVisitor
    public static List<Path> pathList;
    public static String readFile(TreeItem<String> treeItem){
        StringBuilder result = new StringBuilder();
        Path path = Paths.get(getPath(treeItem));
        try {
            Files.readAllLines(path).forEach(str -> result.append(str).append("\n"));
        } catch (IOException e) {
            Alerts.CreateAndShowError(e.getMessage());
        }
        return result.toString();
    }

    public static void writeFile(TreeItem<String> treeItem, TextArea textArea){
        try (RandomAccessFile file = new RandomAccessFile(getPath(treeItem), "rw");
            FileChannel channel = file.getChannel()) {
            String text = textArea.getText();
            ByteBuffer buffer = ByteBuffer.wrap(text.getBytes());
            channel.write(buffer);
        } catch (IOException e) {
            Alerts.CreateAndShowWarning(e.getMessage());
        }
    }

    public static boolean deleteFile(TreeItem<String> treeItem){
        String filepath = getPath(treeItem);
        File file = new File(filepath);
        Path path = Paths.get(filepath);
        try {
            if (file.exists()){
                Files.walkFileTree(path, new DeletingFileVisitor());
                return true;
            }
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    public static List<Path> getListFiles(){
        pathList = new LinkedList<>();
        Path path = RunApplication.FolderPath;
        try {
            Files.walkFileTree(path, new mdFileVisitor());
        } catch (IOException e) {
            Alerts.CreateAndShowError(e.getMessage());
        }
        return pathList;
    }

    public static File createFileMarkdown(TreeItem<String> parent){
        String path = getPath(parent) + "\\Без названия";
        try {
            File file = checkExists(new File(path), ".md");
            if (file.createNewFile()){
                return file;
            } else {
                return null;
            }
        } catch (IOException e) {
            Alerts.CreateAndShowError(e.getMessage());
            return null;
        }
    }

    public static File createFolderMarkdown(){
        File folder = new File(RunApplication.FolderPath.toString() + "\\Без названия");
        folder = checkExists(folder, "");
        if (folder.mkdir()){
            return folder;
        } else {
            return null;
        }
    }

    public static File checkExists(File f, String extension) {
        if (f.exists()) {
            int i = 1;
            while (f.exists()){
                f = new File(RunApplication.FolderPath.toString() + "\\Без названия" + i + extension);
                i++;
            }
        }
        return f;
    }

    public static boolean renameFile(String NewName, String OldName, String path){
        File oldFile = new File(path + "/" + OldName + ".md");
        File newFile = new File(path + "/" + NewName + ".md");

        return oldFile.renameTo(newFile);
    }

    private static String getPath(TreeItem<String> treeItem){
        StringBuilder pathname = new StringBuilder(RunApplication.FolderPath.toString() + "\\");
        List<String> list = new LinkedList<>();
        TreeItem<String> loadingItem = treeItem;
        while (treeItem.getParent() != null){
            list.add(treeItem.getValue());
            treeItem = treeItem.getParent();
        }
        list = list.reversed();
        list.forEach(item -> pathname.append(item).append("\\"));
        pathname.deleteCharAt(pathname.length() - 1);
        if (loadingItem.isLeaf()){
            pathname.append(".md");
        }
        return pathname.toString();
    }
}
