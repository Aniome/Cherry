package com.app.cherry;

import com.app.cherry.util.Alerts;

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Markdown {
    public static String ReadFile(String FileName){
        String result = "";
        try {
            File file = new File(RunApplication.FolderPath.toString() + "\\" + FileName + ".md");
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                result += line;
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static File[] getFiles(){
        String path = RunApplication.FolderPath.toString() + "\\";
        File file = new File(path);
        return file.listFiles();
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

        boolean renamed = oldFile.renameTo(newFile);

        if(renamed) {
            return true;
        } else {
            return false;
        }
    }
}
