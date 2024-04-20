package com.app.cherry;

import com.app.cherry.entity.Settings;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class HibernateUtil {
    private SessionFactory sessionFactory;

    protected void setUp() {
        // A SessionFactory is set up once for an application!
        StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .configure() // configures settings from hibernate.cfg.xml
                .build();
        try {
            sessionFactory = new MetadataSources( registry ).buildMetadata().buildSessionFactory();
        }
        catch (Exception e) {
            // The registry would be destroyed by the SessionFactory, but we had trouble building the SessionFactory
            // so destroy it manually.
            System.out.println(e.getMessage());
            StandardServiceRegistryBuilder.destroy( registry );
        }
    }

    protected void tearDown()  {
        if ( sessionFactory != null ) {
            sessionFactory.close();
        }
    }

    public Integer getHeight(){
        Session session = sessionFactory.openSession();
        Settings settings = session.get(Settings.class, 1);
        session.close();
        return settings.getHeight();
    }

    public void setHeight(Integer height){
        sessionFactory.inTransaction(session -> {
            Settings settings = session.get(Settings.class, 1);
            settings.setHeight(height);
            session.persist(settings);
        });
    }

    public Integer getWidth(){
        Session session = sessionFactory.openSession();
        Settings settings = session.get(Settings.class, 1);
        session.close();
        return settings.getWidth();
    }

    public Path getPath(){
        Session session = sessionFactory.openSession();
        Settings settings = session.get(Settings.class, 1);
        session.close();
        Path FolderPath = Paths.get(settings.getLastPath());
        Boolean condition = Files.exists(FolderPath) && Files.isExecutable(FolderPath) && Files.isDirectory(FolderPath);
        if (condition){
            return FolderPath;
        } else {
            return null;
        }
    }

    public void setPath(String path){
        sessionFactory.inTransaction(session -> {
            session.persist(new Settings(path));
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
