package com.app.cherry.util.configuration;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SettingsData {
    String language;
    String theme;
    double height;
    double width;
    boolean maximized;
    double dividerPosition;
}
