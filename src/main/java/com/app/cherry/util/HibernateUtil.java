package com.app.cherry.util;

import com.app.cherry.entity.Settings;
import org.hibernate.Session;
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

    public static Boolean isMaximized(){
        Session session = sessionFactory.openSession();
        Settings settings = session.get(Settings.class, 1);
        Integer isMaximized = settings.getIsMaximized();
        session.close();
        return isMaximized != null && isMaximized == 1;
    }

    public static void setIsMaximized(Boolean isMaximized){
        sessionFactory.inTransaction(session -> {
            Settings settings = session.get(Settings.class, 1);
            settings.setIsMaximized(isMaximized ? 1 : 0);
            session.persist(settings);
        });
    }

	/*
    public void testBasicUsage() {
        // create a couple of events...
        sessionFactory.inTransaction(session -> {
            session.persist(new Event("Our very first event!", now()));
            session.persist(new Event("A follow up event", now()));
        });
        // now lets pull events from the database and list them
        sessionFactory.inTransaction(session -> {
            session.createSelectionQuery("from Event", Event.class).getResultList()
                    .forEach(event -> out.println("Event (" + event.getDate() + ") : " + event.getTitle()));
        });
	}

	@SuppressWarnings("unchecked")
	public void testBasicUsage() {
		// create a couple of events...
		Session session = sessionFactory.openSession();
		session.beginTransaction();
		session.save(new Settings("123"));
		session.save( new Event( "A follow up event", new Date() ) );
		session.getTransaction().commit();
		session.close();

		// now lets pull events from the database and list them
		session = sessionFactory.openSession();
        session.beginTransaction();
        List result = session.createQuery( "from Event" ).list();
		for ( Event event : (List<Event>) result ) {
			System.out.println( "Event (" + event.getDate() + ") : " + event.getTitle() );
		}
        session.getTransaction().commit();
        session.close();
	}*/
}
