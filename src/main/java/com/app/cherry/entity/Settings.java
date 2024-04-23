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
    private Double height;

    @Column(name = "width")
    private Double width;

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

    public Double getHeight() {
        return height;
    }

    public void setHeight(Double height) {
        this.height = height;
    }

    public Double getWidth() {
        return width;
    }

    public void setWidth(Double width) {
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
