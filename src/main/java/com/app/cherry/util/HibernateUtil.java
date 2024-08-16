package com.app.cherry.util;

import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import java.io.File;

public class HibernateUtil {
    public static SessionFactory sessionFactory;

    public static void setUp() {
        // A SessionFactory is set up once for an application!
        File path = new File("");
        String absolutePath = path.getAbsolutePath().replace("\\", "/");
        String dbPath = String.format("jdbc:sqlite:%s/Databases.db", absolutePath);
        StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .configure() // configures settings from hibernate.cfg.xml
                //.applySetting("hibernate.connection.url", dbPath)
                .build();
        try {
            sessionFactory = new MetadataSources( registry ).buildMetadata().buildSessionFactory();
        }
        catch (Exception e) {
            // The registry would be destroyed by the SessionFactory, but we had trouble building the SessionFactory
            // so destroy it manually.
            Alerts.CreateAndShowWarning(e.getMessage());
            StandardServiceRegistryBuilder.destroy( registry );
        }
    }

    public static void tearDown()  {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }

	/*
    public void testBasicUsage() {
        // now lets pull events from the database and list them
        sessionFactory.inTransaction(session -> {
            session.createSelectionQuery("from Event", Event.class).getResultList()
                    .forEach(event -> out.println("Event (" + event.getDate() + ") : " + event.getTitle()));
        });
	}
	*/
}
