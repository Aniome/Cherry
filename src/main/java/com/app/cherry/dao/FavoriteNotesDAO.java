package com.app.cherry.dao;

import com.app.cherry.entity.FavoriteNotes;
import com.app.cherry.util.Alerts;
import com.app.cherry.util.HibernateUtil;
import org.hibernate.Session;
import java.util.List;

public class FavoriteNotesDAO {

    public static void setPathNote(String pathNote){
        HibernateUtil.sessionFactory.inTransaction(session -> {
            List<FavoriteNotes> favoriteNotesList = session.createQuery("from FavoriteNotes", FavoriteNotes.class).getResultList();
            if (containsPathNote(pathNote, favoriteNotesList)){
                Alerts.CreateAndShowWarning("Элемент уже в избранном");
                return;
            }
            FavoriteNotes favoriteNotes = new FavoriteNotes();
            favoriteNotes.setId(findId());
            favoriteNotes.setPathNote(pathNote);
            session.persist(favoriteNotes);
        });
    }

    private static boolean containsPathNote(String pathNote, List<FavoriteNotes> favoriteNotesList){
        for(FavoriteNotes favoriteNotes : favoriteNotesList){
            if(favoriteNotes.getPathNote().equals(pathNote)){
                return true;
            }
        }
        return false;
    }

    private static Integer findId(){
        Session session = HibernateUtil.sessionFactory.openSession();
        FavoriteNotes favoriteNotes;
        int i;
        for (i = 0; i < Integer.MAX_VALUE; i++) {
            favoriteNotes = session.get(FavoriteNotes.class, i);
            if (favoriteNotes == null){
                break;
            }
        }
        session.close();
        return i;
    }

    public static List<FavoriteNotes> getFavoriteNotes(){
        Session session = HibernateUtil.sessionFactory.openSession();
        List<FavoriteNotes> favoriteNotes = session.createQuery("from FavoriteNotes", FavoriteNotes.class).getResultList();
        session.close();
        return favoriteNotes;
    }
}
