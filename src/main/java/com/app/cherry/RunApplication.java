package com.app.cherry;

import com.app.cherry.controllers.InitController;
import com.app.cherry.controllers.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import java.io.IOException;
import java.nio.file.Path;

import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.JDOMException;
import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;

public class RunApplication extends Application {
    public static Path FolderPath;
    public static final String title = "Cherry";
    private static final double InitialHeight = 400;
    private static final double InitialWidth = 600;
    public static final double MainHeight = 720;
    public static final double MainWidth = 1280;
    private String AbsolutePath;
    @Override
    public void start(Stage stage) throws IOException {
        PrepareHibernate();
        HibernateUtil hibernateUtil = new HibernateUtil();
        hibernateUtil.setUp();
        FolderPath = hibernateUtil.getPath();
        Integer height = hibernateUtil.getHeight();
        Integer width = hibernateUtil.getWidth();


        FXMLLoader fxmlLoader = new FXMLLoader(RunApplication.class.getResource("fxmls/main-view.fxml"));
        //Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        // width height
        Scene scene = new Scene(fxmlLoader.load(), MainWidth, MainHeight);
        //stage.setMaximized(true);
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
            mainController.init(stage);
            stage.setHeight(height);
            stage.setWidth(width);
            PrepareStage(MainHeight, MainWidth, scene, title, stage);
            stage.heightProperty().addListener((observableValue, number, t1) -> {

            });
            stage.widthProperty().addListener((observableValue, number, t1) -> {
                System.out.println("observableValue = " + observableValue);
                System.out.println("number = " + number);
                System.out.println("t1 = " + t1);
            });
            stage.setOnHiding((event) -> {
                Integer i = (int) stage.getHeight();
                hibernateUtil.setHeight(i);
                hibernateUtil.tearDown();
            });
        }
    }

    private void PrepareHibernate(){
        File path = new File("");
        AbsolutePath = path.getAbsolutePath().replace("\\", "/");
        ChangeXML(AbsolutePath + "/src/main/resources/hibernate.cfg.xml");
        ChangeXML(AbsolutePath + "/target/classes/hibernate.cfg.xml");
    }

    private void ChangeXML(String filePath){
        try {
            // loading XML
            SAXBuilder builder = new SAXBuilder();
            Document document = builder.build(filePath);
            Element rootElement = document.getRootElement();
            Element sessionfactory = rootElement.getChild("session-factory");

            List<Element> properties = sessionfactory.getChildren();
            Element connection = null;
            for (Element element: properties){
                if (element.getAttributeValue("name").equals("connection.url")){
                    connection = element;
                    break;
                }
            }

            // add a tag to the document
            String value = connection.getValue();
            if (value.isEmpty()){
                connection.addContent("jdbc:sqlite:" + AbsolutePath + "/src/main/resources/com/app/cherry/Databases.db");
            } else if (!value.contains(AbsolutePath)) {
                connection.removeContent();
                connection.addContent("jdbc:sqlite:" + AbsolutePath + "/src/main/resources/com/app/cherry/Databases.db");
            } else {
                return;
            }

            /*Element path = new Element("property");
            path.setAttribute("name","connection.url");
            sessionfactory.addContent(0, path);*/

            // write changes in XML file
            XMLOutputter xmlOutput = new XMLOutputter(Format.getPrettyFormat());
            FileWriter fileWriter = new FileWriter(filePath);
            xmlOutput.output(document, fileWriter);
            fileWriter.close();

        } catch (JDOMException | IOException e) {
            System.out.println(e.getMessage());
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