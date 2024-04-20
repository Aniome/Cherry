package com.app.cherry.entity;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
public class Settings {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;
    @Basic
    @Column(name = "LastPath", nullable = true, length = -1)
    private String lastPath;

    @Column(name = "height")
    private Integer height;

    @Column(name = "width")
    private Integer width;

    public Settings(String path) {
        this.lastPath = path;
    }

    public Settings() {

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLastPath() {
        return lastPath;
    }

    public void setLastPath(String lastPath) {
        this.lastPath = lastPath;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Settings settings = (Settings) o;
        return Objects.equals(id, settings.id) && Objects.equals(lastPath, settings.lastPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, lastPath);
    }
}
