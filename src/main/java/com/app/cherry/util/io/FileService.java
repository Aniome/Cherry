package com.app.cherry.util.io;

import com.app.cherry.RunApplication;
import com.app.cherry.util.Alerts;
import javafx.scene.control.TreeItem;
import org.fxmisc.richtext.CodeArea;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

public class FileService {
    //List for FileVisitor
    public static List<Path> pathList;
    public static String readFile(TreeItem<String> treeItem){
        StringBuilder result = new StringBuilder();
        Path path = Paths.get(getPath(treeItem));
        return readFile(path, result);
    }

    public static String readFile(Path path){
        StringBuilder result = new StringBuilder();
        return readFile(path, result);
    }

    @NotNull
    private static String readFile(Path path, StringBuilder result) {
        try (FileReader fr = new FileReader(path.toFile())) {
            int i;
            while ((i = fr.read()) != -1) {
                char ch = (char) i;
                result.append(ch);
            }
        } catch (IOException e) {
            Alerts.createAndShowError(e.getMessage());
        }
        return result.toString();
    }

    public static void writeFile(TreeItem<String> treeItem, CodeArea codeArea){
        try (FileWriter fileWriter = new FileWriter(getPath(treeItem))) {
            fileWriter.write(codeArea.getText());
        } catch (IOException e) {
            Alerts.createAndShowWarning(e.getMessage());
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
        Path path = RunApplication.folderPath;
        try {
            Files.walkFileTree(path, new mdFileVisitor());
        } catch (IOException e) {
            Alerts.createAndShowError(e.getMessage());
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
            Alerts.createAndShowError(e.getMessage());
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
        ResourceBundle resourceBundle = RunApplication.resourceBundle;
        String untitled = RunApplication.separator + resourceBundle.getString("FileNameUntitled");
        File file = new File(path + untitled + extension);
        if (file.exists()) {
            int i = 1;
            while (file.exists()){
                file = new File(path + untitled + i + extension);
                i++;
            }
        }
        return file;
    }

    public static boolean checkExists(String path) {
        return new File(path).exists();
    }

    public static boolean renameFile(String newName, String oldName, String path){
        File oldFile = new File(path + "/" + oldName + ".md");
        File newFile = new File(path + "/" + newName + ".md");

        return oldFile.renameTo(newFile);
    }

    public static String getPath(TreeItem<String> treeItem) {
        StringBuilder pathName = new StringBuilder(RunApplication.folderPath.toString() + RunApplication.separator);
        List<String> list = new LinkedList<>();
        TreeItem<String> loadingItem = treeItem;
        while (treeItem.getParent() != null){
            list.add(treeItem.getValue());
            treeItem = treeItem.getParent();
        }
        list = list.reversed();
        list.forEach(item -> pathName.append(item).append("\\"));
        pathName.deleteCharAt(pathName.length() - 1);
        if (loadingItem.isLeaf()){
            pathName.append(".md");
        }
        return pathName.toString();
    }
}
