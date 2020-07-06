package main.java.com.cig.mctbnc.view;


public class Main {
	
	/**
	 * Application entry point. 
	 * @param args application command line arguments
	 */
	public static void main(String[] args) {
		
		String datasetFolder = "src/main/resources/rehabilitation";
		
		try {
			new CommandLine(datasetFolder);
		} catch (Exception e) {
			System.err.println("Error: " + e);
			System.err.println("StackTrace:\n");
			e.printStackTrace(System.err);
			System.exit(1);
		}
		
		
	}

}
