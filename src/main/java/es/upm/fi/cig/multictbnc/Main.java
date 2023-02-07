package es.upm.fi.cig.multictbnc;

import es.upm.fi.cig.multictbnc.gui.Controller;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;

/**
 * JavaFX application to interact with the CTBNLab software.
 *
 * @author Carlos Villa Blanco
 */
public class Main extends Application {
    private static final Logger logger = LogManager.getLogger(Main.class);

    /**
     * Application entry point.
     *
     * @param args application command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        String startMsg = "  ______________  _  ____        __ \n" +
                " / ___/_  __/ _ )/ |/ / /  ___ _/ / \n" +
                "/ /__  / / / _  /    / /__/ _ `/ _ \\\n" +
                "\\___/ /_/ /____/_/|_/____/\\_,_/_.__/";
        System.out.println(startMsg);
        System.setProperty("org.graphstream.ui", "javafx");
        // Extract main scene from FXML file
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Main.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);

        // Change look and feel
        JMetro jMetro = new JMetro(Style.LIGHT);
        jMetro.setScene(scene);

        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.setTitle("CTBNLab");
        URL imageURL = getClass().getResource("/imgs/icon.png");
        Image icon = new Image(imageURL.toExternalForm());
        primaryStage.getIcons().add(icon);
        primaryStage.show();
        primaryStage.setMinWidth(primaryStage.getWidth());
        primaryStage.setMinHeight(primaryStage.getHeight());
        // Obtain controller of the scene
        Controller controller = loader.getController();
        // Pass the stage to the controller. This is necessary to show dialogs.
        controller.setStage(primaryStage);
    }

    @Override
    public void stop() {
        logger.info("Closing application");
    }

}