package com.app.cherry.entity;

import jakarta.persistence.*;

@Entity
public class RecentPaths {
    @Id
    @Column(name = "id")
    private Integer id;
    @Basic
    @Column(name = "path")
    private String path;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
