package com.app.cherry.dao;

import com.app.cherry.entity.RecentPaths;
import com.app.cherry.util.HibernateUtil;
import org.hibernate.Session;

import java.util.List;

public class RecentPathsDAO {
    public static List<RecentPaths> getPaths(){
        Session session = HibernateUtil.sessionFactory.openSession();
        List<RecentPaths> recentPathsList = session.createQuery("from RecentPaths ", RecentPaths.class).getResultList();
        session.close();
        return recentPathsList;
    }

    public static void addPath(String path){
        HibernateUtil.sessionFactory.inTransaction(session -> {
            List<Integer> listId = session.createQuery("select id from RecentPaths", Integer.class).getResultList();
            RecentPaths recentPaths = new RecentPaths();
            recentPaths.setId(getId(listId));
            recentPaths.setPath(path);
            session.persist(recentPaths);
        });
    }

    public static Integer getId(List<Integer> listId){
        int i;
        for (i = 0; i < Integer.MAX_VALUE; i++) {
            if (!listId.contains(i)) {
                break;
            }
        }
        return i;
    }
}