package com.app.cherry;

import com.app.cherry.util.Alerts;
import com.app.cherry.util.mdFileVisitor;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class Markdown {
    //Lis for FileVisitor
    public static List<Path> pathList;
    public static String ReadFile(TreeItem<String> treeItem){
        StringBuilder result = new StringBuilder();
        StringBuilder pathname = new StringBuilder(RunApplication.FolderPath.toString() + "\\");
        List<String> list = new LinkedList<>();
        while (treeItem.getParent() != null){
            list.add(treeItem.getValue());
            treeItem = treeItem.getParent();
        }
        list = list.reversed();
        list.forEach(item -> pathname.append(item).append("\\"));
        pathname.deleteCharAt(pathname.length() - 1);
        try {
            File file = new File(pathname + ".md");
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                result.append(line);
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            Alerts.CreateAndShowError(e.getMessage());
        }
        return result.toString();
    }

    public static int WriteFile(String filename, TextArea textArea){
        int result;
        String path = RunApplication.FolderPath.toString() + "\\" + filename;
        try (RandomAccessFile file = new RandomAccessFile(path, "rw");
            FileChannel channel = file.getChannel()) {
            String text = textArea.getText();
            ByteBuffer buffer = ByteBuffer.wrap(text.getBytes());
            result = channel.write(buffer);
        } catch (IOException e) {
            Alerts.CreateAndShowWarning(e.getMessage());
            return 0;
        }
        return result;
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

    public static File CreateFileMarkdown(){
        String path = RunApplication.FolderPath.toString() + "\\Без названия";
        try {
            File file = CheckExists(new File(path), ".md");
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

    public static File CreateFolderMarkdown(){
        File folder = new File(RunApplication.FolderPath.toString() + "\\Без названия");
        folder = CheckExists(folder, "");
        if (folder.mkdir()){
            return folder;
        } else {
            return null;
        }
    }

    public static File CheckExists(File f, String extension) {
        if (f.exists()) {
            int i = 1;
            while (f.exists()){
                f = new File(RunApplication.FolderPath.toString() + "\\Без названия" + i + extension);
                i++;
            }
        }
        return f;
    }

    public static boolean RenameFile(String NewName, String OldName, String path){
        File oldFile = new File(path + "/" + OldName + ".md");
        File newFile = new File(path + "/" + NewName + ".md");

        return oldFile.renameTo(newFile);
    }
}
