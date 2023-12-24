package com.app.cherry;

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
        File file = new File(path + ".md");
        try {
            if (file.exists()) {
                int i = 1;
                while (file.exists()){
                    file = new File(path + i + ".md");
                    i++;
                }
            }
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
