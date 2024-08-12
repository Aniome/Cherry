package com.app.cherry.util;

import com.app.cherry.RunApplication;
import javafx.scene.control.TreeItem;
import org.fxmisc.richtext.CodeArea;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

public class FileService {
    //List for FileVisitor
    public static List<Path> pathList;
    public static String readFile(TreeItem<String> treeItem){
        StringBuilder result = new StringBuilder();
        Path path = Paths.get(getPath(treeItem));
        try (FileReader fr = new FileReader(path.toFile())) {
            int i;
            while ((i = fr.read()) != -1) {
                char ch = (char) i;
                result.append(ch);
            }
        } catch (IOException e) {
            Alerts.CreateAndShowError(e.getMessage());
        }
        return result.toString();
    }

    public static void writeFile(TreeItem<String> treeItem, CodeArea codeArea){
        try (FileWriter fileWriter = new FileWriter(getPath(treeItem))) {
            fileWriter.write(codeArea.getText());
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
        String path = getPath(parent);
        try {
            File file = checkExists(path, ".md");
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

    public static File createFolderMarkdown(TreeItem<String> treeItem){
        String path = getPath(treeItem);
        File folder = checkExists(path, "");
        if (folder.mkdir()){
            return folder;
        } else {
            return null;
        }
    }

    public static File checkExists(String path, String extension) {
        File file = new File(path + "\\Без названия" + extension);
        if (file.exists()) {
            int i = 1;
            while (file.exists()){
                file = new File(path + "\\Без названия" + i + extension);
                i++;
            }
        }
        return file;
    }

    public static boolean renameFile(String NewName, String OldName, String path){
        File oldFile = new File(path + "/" + OldName + ".md");
        File newFile = new File(path + "/" + NewName + ".md");

        return oldFile.renameTo(newFile);
    }

    public static String getPath(TreeItem<String> treeItem){
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
