package com.app.cherry.util.configuration;

import atlantafx.base.theme.CupertinoLight;
import atlantafx.base.theme.Dracula;
import com.app.cherry.RunApplication;
import com.app.cherry.util.Alerts;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Application;
import javafx.stage.Stage;
import lombok.Getter;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

public class ApplyConfiguration {
    @Getter
    private static double dividerPosition;

    public static void build(Stage mainStage) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            SettingsData settingsData = objectMapper.readValue(
                    RunApplication.class.getResource("settings.json"), SettingsData.class);

            if (settingsData.language.equals("en")){
                RunApplication.resourceBundle = ResourceBundle.getBundle("local/text", Locale.ENGLISH);
            } else {
                RunApplication.resourceBundle = ResourceBundle.getBundle("local/text",
                        Locale.of("ru"));
            }

            if (settingsData.theme.equals("dark")){
                Application.setUserAgentStylesheet(new Dracula().getUserAgentStylesheet());
            } else {
                Application.setUserAgentStylesheet(new CupertinoLight().getUserAgentStylesheet());
            }

            mainStage.setHeight(settingsData.height);
            mainStage.setWidth(settingsData.width);

            mainStage.setMaximized(settingsData.maximized);

            dividerPosition = settingsData.dividerPosition;

        } catch (IOException e) {
            Alerts.createAndShowWarning("");
            System.out.println(e.getMessage());
        }
    }
}
