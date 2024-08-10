package com.app.cherry;

import atlantafx.base.theme.Dracula;
import com.app.cherry.controllers.InitController;
import com.app.cherry.controllers.MainController;
import com.app.cherry.dao.SettingsDAO;
import com.app.cherry.util.HibernateUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;


public class RunApplication extends Application {
    public static Path FolderPath;
    public static final String title = "Cherry";
    private static final double InitialHeight = 400;
    private static final double InitialWidth = 600;
    public static final double MainHeight = 480;
    public static final double MainWidth = 640;

    @Override
    public void start(Stage stage) throws IOException {
        Application.setUserAgentStylesheet(new Dracula().getUserAgentStylesheet());

        HibernateUtil.setUp();
        FolderPath = SettingsDAO.getPath();
        Double height = SettingsDAO.getHeight();
        Double width = SettingsDAO.getWidth();

        FXMLLoader fxmlLoader = new FXMLLoader(RunApplication.class.getResource("fxmls/main-view.fxml"));
        //Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        //width height
        Scene scene = new Scene(fxmlLoader.load(), MainWidth, MainHeight);
        scene.getStylesheets().add(Objects.requireNonNull(RunApplication.class.getResource("css/java-keywords.css")).toExternalForm());
        setIcon(stage);
        if (FolderPath == null){
            fxmlLoader = new FXMLLoader(RunApplication.class.getResource("fxmls/init-view.fxml"));
            Scene secondScene = new Scene(fxmlLoader.load(), InitialWidth, InitialHeight);
            Stage InitialStage = new Stage();
            setIcon(InitialStage);
            InitController initController = fxmlLoader.getController();
            initController.setInitialStage(InitialStage);
            initController.setMainStage(stage);
            prepareStage(InitialHeight, InitialWidth, secondScene,"", InitialStage);
            InitialStage.setResizable(false);
        } else {
            MainController mainController = fxmlLoader.getController();
            mainController.init(stage, this);
            stage.setHeight(height);
            stage.setWidth(width);
            prepareStage(MainHeight, MainWidth, scene, title, stage);
            stage.setMaximized(HibernateUtil.isMaximized());
            stage.setOnHiding((event) -> {
                boolean isMaximized = stage.isMaximized();
                if (!isMaximized) {
                    SettingsDAO.setHeight(stage.getHeight());
                    SettingsDAO.setWidth(stage.getWidth());
                }
                HibernateUtil.setIsMaximized(isMaximized);
                HibernateUtil.tearDown();
            });
        }
    }

    public static void setIcon(Stage stage){
        stage.getIcons().add(new Image(String.valueOf(RunApplication.class.getResource("Image/cherry_icon.png"))));
    }

    public static void prepareStage(double height, double width, Scene scene, String title, Stage stage){
        stage.setTitle(title);
        stage.setScene(scene);
        stage.setMinWidth(width);
        stage.setMinHeight(height);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}