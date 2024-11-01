package com.app.cherry.dao;

import com.app.cherry.RunApplication;
import com.app.cherry.entity.FavoriteNotes;
import com.app.cherry.util.Alerts;
import com.app.cherry.util.HibernateUtil;
import org.hibernate.Session;

import java.util.List;

public class FavoriteNotesDAO {

    public static void setPathNote(String pathNote){
        HibernateUtil.sessionFactory.inTransaction(session -> {
            List<FavoriteNotes> favoriteNotesList
                    = session.createQuery("from FavoriteNotes", FavoriteNotes.class).getResultList();
            if (containsPathNote(pathNote, favoriteNotesList)){
                Alerts.createAndShowWarning("Элемент уже в избранном");
                return;
            }
            FavoriteNotes favoriteNotes = new FavoriteNotes();
            List<Integer> listId = favoriteNotesList.stream().map(FavoriteNotes::getId).toList();
            int id = HibernateUtil.getId(listId);
            favoriteNotes.setId(id);
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

    public static List<FavoriteNotes> getFavoriteNotes(){
        Session session = HibernateUtil.sessionFactory.openSession();
        List<FavoriteNotes> favoriteNotes
                = session.createQuery("from FavoriteNotes", FavoriteNotes.class).getResultList();
        session.close();
        for (FavoriteNotes item : favoriteNotes){
            if (!item.getPathNote().contains(RunApplication.folderPath.toString())){
                return List.of();
            }
        }
        return favoriteNotes;
    }

    public static void deleteFavoriteNote(int id){
        HibernateUtil.sessionFactory.inTransaction(session -> {
            FavoriteNotes deletingNote = session.find(FavoriteNotes.class, id);
            if (deletingNote != null){
                session.remove(deletingNote);
            }
        });
    }
}
