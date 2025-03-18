package com.app.cherry.dao;

import com.app.cherry.RunApplication;
import com.app.cherry.entity.FavoriteNotes;
import com.app.cherry.util.Alerts;
import com.app.cherry.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.exception.GenericJDBCException;

import java.util.List;

public class FavoriteNotesDAO {

    public static void setPathNote(String pathNote) {
        try {
            HibernateUtil.getSessionFactory().inTransaction(session -> {
                List<FavoriteNotes> favoriteNotesList
                        = session.createQuery("from FavoriteNotes", FavoriteNotes.class).getResultList();
                if (containsPathNote(pathNote, favoriteNotesList)) {
                    Alerts.createAndShowWarning(RunApplication.getResourceBundle()
                            .getString("FavoriteNotesContains"));
                    return;
                }
                FavoriteNotes favoriteNotes = new FavoriteNotes();
                List<Integer> listId = favoriteNotesList.stream().map(FavoriteNotes::getId).toList();
                int id = HibernateUtil.getId(listId);
                favoriteNotes.setId(id);
                favoriteNotes.setPathNote(pathNote);
                session.persist(favoriteNotes);
            });
        } catch (Exception e) {
            Alerts.createAndShowError(String.valueOf(e));
        }
    }

    private static boolean containsPathNote(String pathNote, List<FavoriteNotes> favoriteNotesList) {
        for(FavoriteNotes favoriteNotes : favoriteNotesList) {
            if(favoriteNotes.getPathNote().equals(pathNote)) {
                return true;
            }
        }
        return false;
    }

    public static List<FavoriteNotes> getFavoriteNotes() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            List<FavoriteNotes> favoriteNotes
                    = session.createQuery("from FavoriteNotes", FavoriteNotes.class).getResultList();
            session.close();
            for (FavoriteNotes item : favoriteNotes) {
                if (!item.getPathNote().contains(RunApplication.folderPath.toString())) {
                    return List.of();
                }
            }
            return favoriteNotes;
        } catch (GenericJDBCException e) {
            createTable(session);
            return null;
        }
    }

    private static void createTable(Session session) {
        FavoriteNotes favoriteNotes = new FavoriteNotes(0, "");
        session.persist(favoriteNotes);
    }

    public static void deleteFavoriteNote(int id) {
        try {
            HibernateUtil.getSessionFactory().inTransaction(session -> {
                FavoriteNotes deletingNote = session.find(FavoriteNotes.class, id);
                if (deletingNote != null) {
                    session.remove(deletingNote);
                }
            });
        } catch (Exception e) {
            Alerts.createAndShowError(String.valueOf(e));
        }
    }
}
