package com.app.cherry;

import com.app.cherry.controllers.*;
import com.app.cherry.util.HibernateUtil;
import com.app.cherry.util.configuration.ApplyConfiguration;
import com.app.cherry.util.configuration.SavingConfiguration;
import com.app.cherry.util.icons.Icons;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.fxmisc.richtext.CodeArea;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.ResourceBundle;


public class RunApplication extends Application {
    private static final double INITIAL_HEIGHT = 600;
    private static final double INITIAL_WIDTH = 800;
    public static final double MAIN_HEIGHT = 480;
    public static final double MAIN_WIDTH = 640;
    public static final double RENAME_WIDTH = 600;
    public static final double RENAME_HEIGHT = 250;
    public static final String TITLE = "Cherry";
    public static ResourceBundle resourceBundle;
    public static String separator;
    public static String appPath;
    private static Stage mainStage;
    public static Path folderPath;

    @Override
    public void start(Stage stage) {
        HibernateUtil.setUp();
        setSeparator();
        ApplyConfiguration.loadAndApplySettings(stage);
        //Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        //width height
        mainStage = stage;
        if (folderPath == null) {
            showInitialWindow();
        } else {
            showMainWindow();
        }
    }

    public static void setSeparator() {
        String path = new File("").getAbsolutePath();
        if (path.contains("/")) {
            separator = "/";
        } else {
            separator = "\\";
        }
    }

    public static void showMainWindow() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(
                    RunApplication.class.getResource("fxmls/main-view.fxml"), resourceBundle);
            Scene scene = new Scene(fxmlLoader.load(), MAIN_WIDTH, MAIN_HEIGHT);
            scene.getStylesheets().add(Objects.requireNonNull
                    (RunApplication.class.getResource("css/keywords.css")).toExternalForm());
            ApplyConfiguration.setMainSceneAndSetTheme(scene);
            ApplyConfiguration.changeThemeCssOnMainScene();
            MainController mainController = fxmlLoader.getController();
            setIcon(mainStage);
            prepareStage(MAIN_HEIGHT, MAIN_WIDTH, scene, TITLE, mainStage);
            mainController.setDividerPositionAfterShowing();
            SavingConfiguration.observableMainStage(mainStage, mainController);
        } catch (IOException e){
            e.printStackTrace();
            //System.out.println(e.getMessage());
        }
    }

    public static void showInitialWindow() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(RunApplication.class.getResource("fxmls/init-view.fxml"),
                    resourceBundle);
            Scene secondScene = new Scene(fxmlLoader.load(), INITIAL_WIDTH, INITIAL_HEIGHT);
            Stage initialStage = new Stage();
            setIcon(initialStage);
            SavingConfiguration.observableInitStage(initialStage);
            InitController initController = fxmlLoader.getController();
            initController.initialStage = initialStage;
            prepareStage(INITIAL_HEIGHT, INITIAL_WIDTH, secondScene,"", initialStage);
            initialStage.setResizable(false);
            initController.loadPaths();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void showBrowserWindow(String clickedText) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(RunApplication.class.getResource("fxmls/web-view.fxml"),
                    resourceBundle);
            double webViewWidth = 800, webViewHeight = 600;
            Scene secondScene = new Scene(fxmlLoader.load(), webViewWidth, webViewHeight);
            Stage webViewStage = new Stage();
            setIcon(webViewStage);
            webViewStage.initModality(Modality.WINDOW_MODAL);
            webViewStage.initOwner(mainStage);
            SavingConfiguration.browserStage = webViewStage;
            WebViewController webViewController = fxmlLoader.getController();
            webViewController.init(clickedText);
            prepareStage(webViewHeight, webViewWidth, secondScene,"Browser", webViewStage);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void showRenameWindow(TreeItem<String> selectedTreeItem, Tab selectedTab) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(RunApplication.class.getResource("fxmls/rename-view.fxml"),
                    resourceBundle);
            Scene scene = new Scene(fxmlLoader.load(), RENAME_WIDTH, RENAME_HEIGHT);
            Stage renameStage = new Stage();
            RunApplication.setIcon(renameStage);
            renameStage.setResizable(false);
            renameStage.initModality(Modality.WINDOW_MODAL);
            renameStage.initOwner(mainStage);
            SavingConfiguration.renameStage = renameStage;
            RenameViewController renameViewController = fxmlLoader.getController();
            renameViewController.init(renameStage, selectedTreeItem, selectedTab);
            String renameWindowTitle = RunApplication.resourceBundle.getString("RenameWindowTitle");
            RunApplication.prepareStage(RENAME_HEIGHT, RENAME_WIDTH, scene, renameWindowTitle, renameStage);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void showFindWindow(CodeArea codeArea) {
        try {
            ResourceBundle resourceBundle = RunApplication.resourceBundle;
            FXMLLoader fxmlLoader = new FXMLLoader(RunApplication.class.getResource("fxmls/find-view.fxml"),
                    resourceBundle);
            double findViewWidth = 600, findViewHeight = 400;
            Scene secondScene = new Scene(fxmlLoader.load(), findViewWidth, findViewHeight);
            Stage findViewStage = new Stage();
            findViewStage.setTitle(resourceBundle.getString("FindViewTitle"));
            SavingConfiguration.findStage = findViewStage;
            RunApplication.setIcon(findViewStage);
            FindViewController findViewController = fxmlLoader.getController();
            findViewController.init(codeArea);
            RunApplication.prepareStage(findViewHeight, findViewWidth, secondScene,"", findViewStage);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void showHelpStage() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(RunApplication.class.getResource("fxmls/help-view.fxml"),
                    resourceBundle);
            double helpViewWidth = 800, helpViewHeight = 600;
            Scene scene = new Scene(fxmlLoader.load(), helpViewWidth, helpViewHeight);
            Stage stage = new Stage();
            RunApplication.setIcon(stage);
            stage.setResizable(false);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(mainStage);
            RunApplication.prepareStage(RENAME_HEIGHT, RENAME_WIDTH, scene, "", stage);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void setIcon(Stage stage) {
        stage.getIcons().add(new Image(Icons.TITLE_ICON.getIcon()));
    }

    public static void prepareStage(double height, double width, Scene scene, String title, Stage stage) {
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