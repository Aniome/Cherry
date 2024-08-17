package com.app.cherry.dao;

import com.app.cherry.entity.RecentPaths;
import com.app.cherry.util.HibernateUtil;
import org.hibernate.Session;

import java.util.List;

public class RecentPathsDAO {
    public static List<String> getPaths(){
        Session session = HibernateUtil.sessionFactory.openSession();
        List<String> recentPathsList = session.createQuery("select path from RecentPaths ", String.class).getResultList();
        session.close();
        return recentPathsList;
    }

    public static void addPath(String path){
        HibernateUtil.sessionFactory.inTransaction(session -> {
            List<RecentPaths> listId = session.createQuery("from RecentPaths", RecentPaths.class).getResultList();
            boolean notContains = false;
            for (RecentPaths p: listId){
                if (!p.getPath().equals(path)){
                    notContains = true;
                    break;
                }
            }
            if (notContains || listId.isEmpty()){
                RecentPaths recentPaths = new RecentPaths();
                recentPaths.setId(getId(listId));
                recentPaths.setPath(path);
                session.persist(recentPaths);
            }
        });
    }

    public static Integer getId(List<RecentPaths> listId){
        int i;
        for (i = 0; i < Integer.MAX_VALUE; i++) {
            try {
                if (listId.get(i).getId() == i) {
                    i++;
                    break;
                }
            } catch (IndexOutOfBoundsException e){
                return i;
            }
        }
        return i;
    }
}