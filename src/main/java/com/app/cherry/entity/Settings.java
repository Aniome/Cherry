package com.app.cherry.entity;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
public class Settings {
    @GeneratedValue
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;
    @Basic
    @Column(name = "LastPath", length = -1)
    private String lastPath;

    @Column(name = "height")
    private Double height;

    @Column(name = "width")
    private Double width;

    @Column(name = "isMaximized")
    private Integer isMaximized;


    @Column(name = "DividerPosition")
    private Double dividerPosition;

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

    public Integer getIsMaximized() {
        return isMaximized;
    }

    public void setIsMaximized(Integer isMaximized) {
        this.isMaximized = isMaximized;
    }

    public Double getDividerPosition() {
        return dividerPosition;
    }

    public void setDividerPosition(Double dividerPosition) {
        this.dividerPosition = dividerPosition;
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
