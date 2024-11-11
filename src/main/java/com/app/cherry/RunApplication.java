package com.app.cherry;

import com.app.cherry.controllers.*;
import com.app.cherry.util.HibernateUtil;
import com.app.cherry.util.configuration.ApplyConfiguration;
import com.app.cherry.util.configuration.SavingConfiguration;
import com.app.cherry.util.io.FileService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.fxmisc.richtext.CodeArea;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.ResourceBundle;


public class RunApplication extends Application {
    private static Stage mainStage;
    public static Path folderPath;
    public static final String title = "Cherry";
    private static final double InitialHeight = 600;
    private static final double InitialWidth = 800;
    public static final double MainHeight = 480;
    public static final double MainWidth = 640;
    public static final double renameWidth = 600;
    public static final double renameHeight = 250;
    public static ResourceBundle resourceBundle;
    public static String separator;
    public static String appPath;

    @Override
    public void start(Stage stage) {
        HibernateUtil.setUp();
        ApplyConfiguration.build(stage);

        //Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        //width height
        mainStage = stage;
        if (folderPath == null){
            showInitialWindow();
        } else {
            if (folderPath.toString().contains("/")){
                separator = "/";
            } else {
                separator = "\\\\";
            }
            showMainWindow();
        }
    }

    public static void showMainWindow() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(
                    RunApplication.class.getResource("fxmls/main-view.fxml"), resourceBundle);
            Scene scene = new Scene(fxmlLoader.load(), MainWidth, MainHeight);
            scene.getStylesheets().add(Objects.requireNonNull
                    (RunApplication.class.getResource("css/keywords.css")).toExternalForm());
            ApplyConfiguration.setMainScene(scene);
            MainController mainController = fxmlLoader.getController();
            mainController.init(mainStage);
            setIcon(mainStage);
            prepareStage(MainHeight, MainWidth, scene, title, mainStage);
            mainController.afterShowing();
            SavingConfiguration.observableMainStage(mainStage, mainController);
        } catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

    public static void showInitialWindow() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(RunApplication.class.getResource("fxmls/init-view.fxml"),
                    resourceBundle);
            Scene secondScene = new Scene(fxmlLoader.load(), InitialWidth, InitialHeight);
            Stage initialStage = new Stage();
            setIcon(initialStage);
            SavingConfiguration.observableInitStage(initialStage);
            InitController initController = fxmlLoader.getController();
            initController.InitialStage = initialStage;
            prepareStage(InitialHeight, InitialWidth, secondScene,"", initialStage);
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

    public static void showRenameWindow(TreeView<String> treeView, TabPane tabPane) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(RunApplication.class.getResource("fxmls/rename-view.fxml"),
                    resourceBundle);
            Scene scene = new Scene(fxmlLoader.load(), renameWidth, renameHeight);
            Stage stage = new Stage();
            RunApplication.setIcon(stage);
            stage.setResizable(false);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(mainStage);
            SavingConfiguration.renameStage = stage;
            stage.setOnHiding((event) -> {
                String newFileName = MainController.newFileName;
                if (newFileName == null) {
                    return;
                }
                TreeItem<String> selectedItem = treeView.getSelectionModel().getSelectedItem();
                boolean b = FileService.renameFile(newFileName, selectedItem.getValue(),
                        RunApplication.folderPath.toString());
                if (b){
                    selectedItem.setValue(newFileName);
                    Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
                    selectedTab.setText(newFileName);
                }
            });
            RenameViewController renameViewController = fxmlLoader.getController();
            renameViewController.init(stage);
            String renameWindowTitle = RunApplication.resourceBundle.getString("RenameWindowTitle");
            RunApplication.prepareStage(renameHeight, renameWidth, scene, renameWindowTitle, stage);
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
            FXMLLoader fxmlLoader = new FXMLLoader(RunApplication.class.getResource("fxmls/help-view.fxml"));
            double helpViewWidth = 800, helpViewHeight = 600;
            Scene scene = new Scene(fxmlLoader.load(), helpViewWidth, helpViewHeight);
            Stage stage = new Stage();
            RunApplication.setIcon(stage);
            stage.setResizable(false);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(mainStage);
            RunApplication.prepareStage(renameHeight, renameWidth, scene, "", stage);
            HelpViewController helpViewController = fxmlLoader.getController();
            helpViewController.init();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void setIcon(Stage stage) {
        stage.getIcons().add(new Image(String.valueOf(RunApplication.class.getResource("Image/cherry_icon.png"))));
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