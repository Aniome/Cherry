package com.app.cherry;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class RunApplication extends Application {
    public static Path FolderPath;
    public static final String title = "Cherry";
    private static final double InitialHeight = 400;
    private static final double InitialWidth = 600;
    public static final double MainHeight = 720;
    public static final double MainWidth = 1280;

    @Override
    public void start(Stage stage) throws IOException {
        //PrepareHibernate();
        HibernateUtil hibernateUtil = new HibernateUtil();
        hibernateUtil.setUp();
        FolderPath = hibernateUtil.getPath();
        hibernateUtil.tearDown();

        FXMLLoader fxmlLoader = new FXMLLoader(RunApplication.class.getResource("fxmls/main-view.fxml"));
        //Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        // ширина высота
        Scene scene = new Scene(fxmlLoader.load(), MainWidth, MainHeight);
        //stage.setMaximized(true);
        FolderPath = Paths.get("D\\Iam");
        //stage.getIcons().add(new Image(String.valueOf(RunApplication.class.getResource("Image/cherry_icon.png"))));
        SetIcon(stage);
        if (FolderPath == null){
            fxmlLoader = new FXMLLoader(RunApplication.class.getResource("fxmls/init-view.fxml"));
            Scene secondScene = new Scene(fxmlLoader.load(), InitialWidth, InitialHeight);
            Stage InitialStage = new Stage();
            //InitialStage.getIcons().add(new Image(String.valueOf(RunApplication.class.getResource("Image/cherry_icon.png"))));
            SetIcon(InitialStage);
            InitController initController = fxmlLoader.getController();
            initController.setInitialStage(InitialStage);
            initController.setMainStage(stage);
            PrepareStage(InitialHeight,InitialWidth,secondScene,"", InitialStage);
            InitialStage.setResizable(false);
        }
        else {
            MainController mainController = fxmlLoader.getController();
            mainController.init();
            PrepareStage(MainHeight, MainWidth, scene, title, stage);
        }
    }

    static void SetIcon(Stage stage){
        stage.getIcons().add(new Image(String.valueOf(RunApplication.class.getResource("Image/cherry_icon.png"))));
    }

    static void PrepareStage(double height, double width, Scene scene, String title, Stage stage){
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