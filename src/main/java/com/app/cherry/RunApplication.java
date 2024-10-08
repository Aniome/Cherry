package com.app.cherry;

import atlantafx.base.theme.Dracula;
import com.app.cherry.controllers.InitController;
import com.app.cherry.controllers.MainController;
import com.app.cherry.controllers.WebViewController;
import com.app.cherry.dao.RecentPathsDAO;
import com.app.cherry.dao.SettingsDAO;
import com.app.cherry.util.HibernateUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import org.scenicview.ScenicView;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;


public class RunApplication extends Application {
    public static Path FolderPath;
    public static final String title = "Cherry";
    private static final double InitialHeight = 600;
    private static final double InitialWidth = 800;
    public static final double MainHeight = 480;
    public static final double MainWidth = 640;
    private static Stage mainStage;
    private static Double height;
    private static Double width;
    public static ResourceBundle resourceBundle;

    @Override
    public void start(Stage stage) {
        Application.setUserAgentStylesheet(new Dracula().getUserAgentStylesheet());

        resourceBundle = ResourceBundle.getBundle("local/text", Locale.ENGLISH);

        HibernateUtil.setUp();
        FolderPath = SettingsDAO.getPath();
        height = SettingsDAO.getHeight();
        width = SettingsDAO.getWidth();

        //Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        //width height
        mainStage = stage;
        if (FolderPath == null){
            showInitialWindow();
        } else {
            showMainWindow();
        }
    }

    public static void showMainWindow() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(RunApplication.class.getResource("fxmls/main-view.fxml"), resourceBundle);
            Scene scene = new Scene(fxmlLoader.load(), MainWidth, MainHeight);
            scene.getStylesheets().add(Objects.requireNonNull(RunApplication.class.getResource("css/keywords.css")).toExternalForm());
            MainController mainController = fxmlLoader.getController();
            mainController.init(mainStage);
            setIcon(mainStage);
            mainStage.setHeight(height);
            mainStage.setWidth(width);
            prepareStage(MainHeight, MainWidth, scene, title, mainStage);
            mainStage.setMaximized(SettingsDAO.isMaximized());
            mainController.afterShowing();
            mainStage.setOnHiding((event) -> {
                RecentPathsDAO.addPath(FolderPath.toString());
                boolean isMaximized = mainStage.isMaximized();
                if (!isMaximized) {
                    SettingsDAO.setHeight(mainStage.getHeight());
                    SettingsDAO.setWidth(mainStage.getWidth());
                }
                SettingsDAO.setIsMaximized(isMaximized);
                SettingsDAO.setDividerPosition(mainController.splitPane.getDividerPositions()[0]);
                SettingsDAO.setPath(FolderPath.toString());

                HibernateUtil.tearDown();
            });
        } catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

    public static void showInitialWindow() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(RunApplication.class.getResource("fxmls/init-view.fxml"), resourceBundle);
            Scene secondScene = new Scene(fxmlLoader.load(), InitialWidth, InitialHeight);
            Stage InitialStage = new Stage();
            setIcon(InitialStage);
            InitController initController = fxmlLoader.getController();
            initController.setInitialStage(InitialStage);
            prepareStage(InitialHeight, InitialWidth, secondScene,"", InitialStage);
            InitialStage.setResizable(false);
            initController.loadPaths();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void showBrowser(String clickedText){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(RunApplication.class.getResource("fxmls/web-view.fxml"),
                    resourceBundle);
            double webViewWidth = 800, webViewHeight = 600;
            Scene secondScene = new Scene(fxmlLoader.load(), webViewWidth, webViewHeight);
            Stage webViewStage = new Stage();
            setIcon(webViewStage);
            WebViewController webViewController = fxmlLoader.getController();
            webViewController.init(clickedText);
            prepareStage(webViewHeight, webViewWidth, secondScene,"Browser", webViewStage);
        } catch (IOException e) {
            System.out.println(e.getMessage());
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
        //ScenicView.show(scene);
    }

    public static void main(String[] args) {
        launch();
    }
}