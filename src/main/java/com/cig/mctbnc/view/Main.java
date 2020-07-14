package com.cig.mctbnc.view;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {

	static Logger logger = LogManager.getLogger(Main.class);

	/**
	 * Application entry point.
	 * 
	 * @param args
	 *            application command line arguments
	 */
	public static void main(String[] args) {

		String datasetFolder = "src/main/java/resources/datasets/rehabilitation";

		try {
			logger.debug("Initializing MCTBNCs from Main");
			new CommandLine(datasetFolder);
		} catch (Exception e) {
			System.err.println("Error: " + e);
			System.err.println("StackTrace:\n");
			e.printStackTrace(System.err);
			System.exit(1);
		}

	}

}
