package com.cig.mctbnc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cig.mctbnc.gui.controllers.Controller;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * JavaFX application to interact with the MCTBNCs software.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class Main extends Application {
	static Logger logger = LogManager.getLogger(Main.class);

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
		logger.debug("Initializing MCTBNCs application");
		System.setProperty("org.graphstream.ui", "javafx");
		// Extract main scene from fxml file
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Main.fxml"));
		Parent root = (Parent) loader.load();
		Scene scene = new Scene(root);
		primaryStage.setScene(scene);
		primaryStage.setResizable(false);
		primaryStage.show();
		// Obtain controller of the scene
		Controller controller = (Controller) loader.getController();
		// Pass the stage to the controller. This is necessary to show dialogs.
		controller.setStage(primaryStage);
	}

	@Override
	public void stop() {
		logger.info("Closing application");
	}

}
