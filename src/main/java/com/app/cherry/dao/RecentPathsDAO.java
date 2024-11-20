package com.app.cherry.dao;

import com.app.cherry.entity.RecentPaths;
import com.app.cherry.util.HibernateUtil;
import org.hibernate.Session;

import java.util.List;

public class RecentPathsDAO {
    public static List<String> getPaths() {
        Session session = HibernateUtil.sessionFactory.openSession();
        List<String> recentPathsList = session.createQuery("SELECT path FROM RecentPaths",
                String.class).getResultList();
        session.close();
        return recentPathsList;
    }

    public static void addPath(String path) {
        HibernateUtil.sessionFactory.inTransaction(session -> {
            List<RecentPaths> pathsList = session.createQuery("from RecentPaths", RecentPaths.class).getResultList();
            if (checkContainsPaths(pathsList, path) == -1 || pathsList.isEmpty()){
                RecentPaths recentPaths = new RecentPaths();
                List<Integer> listId = pathsList.stream().map(RecentPaths::getId).toList();
                int id = HibernateUtil.getId(listId);
                recentPaths.setId(id);
                recentPaths.setPath(path);
                session.persist(recentPaths);
            }
        });
    }

    public static void removePath(String path) {
        HibernateUtil.sessionFactory.inTransaction(session -> {
            List<RecentPaths> pathsList = session.createQuery("from RecentPaths", RecentPaths.class).getResultList();
            int index = checkContainsPaths(pathsList, path);
            if (index != -1) {
                session.remove(pathsList.get(index));
            }
        });
    }

    private static int checkContainsPaths(List<RecentPaths> listId, String path) {
        int index = -1;
        for (int i = 0; i < listId.size(); i++) {
            if (listId.get(i).getPath().equals(path)) {
                index = i;
                break;
            }
        }
        return index;
    }
}