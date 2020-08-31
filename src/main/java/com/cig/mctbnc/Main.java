package com.cig.mctbnc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cig.mctbnc.gui.controllers.Controller;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

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
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Main.fxml"));
		Parent root = (Parent)loader.load();
		Scene scene = new Scene(root);
		primaryStage.setScene(scene);
		primaryStage.show();

		Controller controller = (Controller) loader.getController();
		controller.setStage(primaryStage);

//		String datasetFolder = "src/main/resources/datasets/rehabilitation/Exercise1";
//
//		try {
//			logger.debug("Initializing MCTBNCs from Main");
//			new CommandLine(datasetFolder);
//		} catch (Exception e) {
//			System.err.println("Error: " + e);
//			System.err.println("StackTrace:\n");
//			e.printStackTrace(System.err);
//			System.exit(1);
//		}

	}
	
	@Override
	public void stop() {
		logger.info("Closing application");
	}

}
