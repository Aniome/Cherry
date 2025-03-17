package com.app.cherry.util;

import com.app.cherry.RunApplication;
import com.app.cherry.entity.FavoriteNotes;
import com.app.cherry.entity.RecentPaths;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.slf4j.Logger;

import java.util.List;

public class HibernateUtil {
    private static SessionFactory sessionFactory;

    public static void setUp(String absolutePath) {
        // A SessionFactory is set up once for an application!
        String dbPath = String.format("jdbc:sqlite:%s/Databases.db", absolutePath);
        final StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .applySetting("hibernate.connection.url", dbPath).build();
        try {
            sessionFactory = new MetadataSources(registry)
                            .addAnnotatedClasses(FavoriteNotes.class, RecentPaths.class)
                            .buildMetadata()
                            .buildSessionFactory();
        }
        catch (Exception e) {
            // The registry would be destroyed by the SessionFactory, but we
            // had trouble building the SessionFactory so destroy it manually.
            Logger logger = RunApplication.buildLogger(HibernateUtil.class);
            logger.error("Error initializing with Hibernate:", e);
            Alerts.createAndShowError(String.valueOf(e));
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }

    public static void tearDown()  {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static Integer getId(List<Integer> listId) {
        int i;
        for (i = 0; i < Integer.MAX_VALUE; i++) {
            try {
                if (!listId.contains(i)) {
                    break;
                }
            } catch (IndexOutOfBoundsException e) {
                return --i;
            }
        }
        return i;
    }
}
