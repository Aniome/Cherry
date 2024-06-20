package com.app.cherry.entity;

import jakarta.persistence.*;

@Entity
public class FavoriteNotes {
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;
    @Column(name = "pathNote")
    private String pathNote;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPathNote() {
        return pathNote;
    }

    public void setPathNote(String pathNote) {
        this.pathNote = pathNote;
    }
}
