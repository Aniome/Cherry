package com.app.cherry.dao;

import com.app.cherry.entity.Settings;
import com.app.cherry.util.HibernateUtil;
import org.hibernate.Session;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SettingsDAO {

    public static Double getHeight(){
        Session session = HibernateUtil.sessionFactory.openSession();
        Settings settings = session.get(Settings.class, 1);
        Double height = settings.getHeight();
        session.close();
        return height;
    }

    public static void setHeight(Double height){
        HibernateUtil.sessionFactory.inTransaction(session -> {
            Settings settings = session.get(Settings.class, 1);
            settings.setHeight(height);
            session.persist(settings);
        });
    }

    public static Double getWidth(){
        Session session = HibernateUtil.sessionFactory.openSession();
        Settings settings = session.get(Settings.class, 1);
        Double width = settings.getWidth();
        session.close();
        return width;
    }

    public static void setWidth(Double width){
        HibernateUtil.sessionFactory.inTransaction(session -> {
            Settings settings = session.get(Settings.class, 1);
            settings.setWidth(width);
            session.persist(settings);
        });
    }

    public static Path getPath(){
        Session session = HibernateUtil.sessionFactory.openSession();
        Settings settings = session.get(Settings.class, 1);
        session.close();
        Path FolderPath = Paths.get(settings.getLastPath());
        boolean condition = Files.exists(FolderPath) && Files.isExecutable(FolderPath) && Files.isDirectory(FolderPath);
        if (condition){
            return FolderPath;
        } else {
            return null;
        }
    }

    public static void setPath(String path){
        HibernateUtil.sessionFactory.inTransaction(session -> {
            Settings settings = session.get(Settings.class, 1);
            settings.setLastPath(path);
            session.persist(settings);
        });
    }

    public static Double getDividerPosition(){
        Session session = HibernateUtil.sessionFactory.openSession();
        Settings settings = session.get(Settings.class, 1);
        session.close();
        return settings.getDividerPosition();
    }

    public static void setDividerPosition(Double dividerPosition){
        HibernateUtil.sessionFactory.inTransaction(session -> {
           Settings settings = session.get(Settings.class, 1);
           settings.setDividerPosition(dividerPosition);
           session.persist(settings);
        });
    }

    public static Boolean isMaximized(){
        Session session = HibernateUtil.sessionFactory.openSession();
        Settings settings = session.get(Settings.class, 1);
        Integer isMaximized = settings.getIsMaximized();
        session.close();
        return isMaximized != null && isMaximized == 1;
    }

    public static void setIsMaximized(Boolean isMaximized){
        HibernateUtil.sessionFactory.inTransaction(session -> {
            Settings settings = session.get(Settings.class, 1);
            settings.setIsMaximized(isMaximized ? 1 : 0);
            session.persist(settings);
        });
    }
}
