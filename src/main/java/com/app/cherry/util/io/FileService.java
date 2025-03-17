package com.app.cherry.util.io;

import com.app.cherry.RunApplication;
import com.app.cherry.util.Alerts;
import com.sun.jna.platform.FileUtils;
import javafx.scene.control.TreeItem;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

public class FileService {
    //List for FileVisitor
    public static List<Path> pathList;

    public static String readFile(TreeItem<String> treeItem) {
        Path path = Paths.get(getPath(treeItem));
        return readFile(path, new StringBuilder());
    }

    public static String readFile(Path path) {
        return readFile(path, new StringBuilder());
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

    public static void writeFile(TreeItem<String> treeItem, String content) {
        try (RandomAccessFile file = new RandomAccessFile(getPath(treeItem), "rw");
             FileChannel channel = file.getChannel()) {
            ByteBuffer buffer = ByteBuffer.wrap(content.getBytes());
            channel.write(buffer);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static boolean deleteFile(TreeItem<String> treeItem) {
        String filePath = getPath(treeItem);
        FileUtils fileUtils = FileUtils.getInstance();
        if (fileUtils.hasTrash()) {
            try {
                fileUtils.moveToTrash(new File(filePath));
            } catch (IOException ioe) {
                System.out.println(ioe.getMessage());
                return false;
            }
        } else {
            System.out.println("No Trash available");
        }
        return true;
    }

    public static List<Path> getListFiles() {
        pathList = new LinkedList<>();
        Path path = RunApplication.folderPath;
        try {
            Files.walkFileTree(path, new mdFileVisitor());
        } catch (IOException e) {
            Alerts.createAndShowError(e.getMessage());
        }
        return pathList;
    }

    public static File createFileMarkdown(TreeItem<String> parent) {
        String path = getPath(parent);
        try {
            File file = checkExists(path, ".md");
            if (file.createNewFile()) {
                return file;
            } else {
                return null;
            }
        } catch (IOException e) {
            Alerts.createAndShowError(e.getMessage());
            return null;
        }
    }

    public static File createFolderMarkdown(TreeItem<String> treeItem) {
        String path = getPath(treeItem);
        File folder = checkExists(path, "");
        if (folder.mkdir()) {
            return folder;
        } else {
            return null;
        }
    }

    public static File checkExists(String path, String extension) {
        ResourceBundle resourceBundle = RunApplication.resourceBundle;
        String untitled = RunApplication.getSeparator() + resourceBundle.getString("FileNameUntitled");
        File file = new File(path + untitled + extension);
        if (file.exists()) {
            int i = 1;
            while (file.exists()) {
                file = new File(path + untitled + i + extension);
                i++;
            }
        }
        return file;
    }

    public static boolean checkExists(String path) {
        return new File(path).exists();
    }

    public static boolean renameFile(String newName, String oldName, String path) {
        File oldFile = new File(path + RunApplication.getSeparator() + oldName + ".md");
        File newFile = new File(path + RunApplication.getSeparator() + newName + ".md");
        return oldFile.renameTo(newFile);
    }

    public static String getPath(TreeItem<String> treeItem) {
        StringBuilder pathName = new StringBuilder(RunApplication.folderPath.toString() + RunApplication.getSeparator());
        List<String> listPath = new LinkedList<>();
        TreeItem<String> loadingItem = treeItem;
        while (treeItem.getParent() != null) {
            listPath.add(treeItem.getValue());
            treeItem = treeItem.getParent();
        }
        listPath = listPath.reversed();
        listPath.forEach(item -> pathName.append(item).append(RunApplication.getSeparator()));
        pathName.deleteCharAt(pathName.length() - 1);
        if (loadingItem.isLeaf()) {
            pathName.append(".md");
        }
        return pathName.toString();
    }
}
