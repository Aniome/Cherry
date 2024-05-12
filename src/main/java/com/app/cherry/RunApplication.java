package com.app.cherry;

import com.app.cherry.controllers.InitController;
import com.app.cherry.controllers.MainController;
import javafx.application.Application;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import java.io.IOException;
import java.nio.file.Path;



public class RunApplication extends Application {
    public static Path FolderPath;
    public static final String title = "Cherry";
    private static final double InitialHeight = 400;
    private static final double InitialWidth = 600;
    public static final double MainHeight = 480;
    public static final double MainWidth = 640;

    @Override
    public void start(Stage stage) throws IOException {
        HibernateUtil hibernateUtil = new HibernateUtil();
        hibernateUtil.setUp();
        FolderPath = hibernateUtil.getPath();
        Double height = hibernateUtil.getHeight();
        Double width = hibernateUtil.getWidth();


        FXMLLoader fxmlLoader = new FXMLLoader(RunApplication.class.getResource("fxmls/main-view.fxml"));
        //Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        //width height
        Scene scene = new Scene(fxmlLoader.load(), MainWidth, MainHeight);
        SetIcon(stage);
        if (FolderPath == null){
            fxmlLoader = new FXMLLoader(RunApplication.class.getResource("fxmls/init-view.fxml"));
            Scene secondScene = new Scene(fxmlLoader.load(), InitialWidth, InitialHeight);
            Stage InitialStage = new Stage();
            SetIcon(InitialStage);
            InitController initController = fxmlLoader.getController();
            initController.setInitialStage(InitialStage);
            initController.setMainStage(stage);
            PrepareStage(InitialHeight,InitialWidth,secondScene,"", InitialStage);
            InitialStage.setResizable(false);
        }
        else {
            MainController mainController = fxmlLoader.getController();
            mainController.init(stage);
            stage.setHeight(height);
            stage.setWidth(width);
            PrepareStage(MainHeight, MainWidth, scene, title, stage);
            stage.setMaximized(hibernateUtil.isMaximized());
            stage.setOnHiding((event) -> {
                boolean isMaximized = stage.isMaximized();
                if (!isMaximized) {
                    hibernateUtil.setHeight(stage.getHeight());
                    hibernateUtil.setWidth(stage.getWidth());
                }
                hibernateUtil.setIsMaximized(isMaximized);
                hibernateUtil.tearDown();
            });
        }
    }

    public static void SetIcon(Stage stage){
        stage.getIcons().add(new Image(String.valueOf(RunApplication.class.getResource("Image/cherry_icon.png"))));
    }

    public static void PrepareStage(double height, double width, Scene scene, String title, Stage stage){
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