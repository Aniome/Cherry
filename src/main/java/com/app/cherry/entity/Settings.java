package com.app.cherry.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
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
