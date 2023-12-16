package com.app.cherry;

import java.io.File;
import java.io.IOException;

public class Markdown {
    public static String ReadFile(){


        return "";
    }

    public static String[] getFiles(){
        String path = RunApplication.FolderPath.toString() + "\\";
        File file = new File(path);
        return file.list();
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
}
