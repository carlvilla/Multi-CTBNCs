package main.java.com.cig.mctbnc.view;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import main.java.com.cig.mctbnc.models.Dataset;
import main.java.com.cig.mctbnc.models.MCTBNC;
import main.java.com.cig.mctbnc.models.Sequence;

public class CommandLine {
	
	public CommandLine(String datasetFolder) {
		
		File folder = new File(datasetFolder);
		File[] files = folder.listFiles();
		List<Sequence> sequences = new ArrayList<Sequence>();
		String[] nameClassVariables = {"Exercise", "ExerciseMode"};
		
		int count = 0; //TEST
		System.out.println("Reading sequences...");
		for(File file:files) {
			List<String[]> data = readCSV(file.getAbsolutePath());
			String[] nameVariables = data.get(0); // Get names of variables
			data.remove(0); // Drop names of variables 
			sequences.add(new Sequence(nameVariables, nameClassVariables, data));
			
		
			// TEST
			if(count == 3) {
				break;
			}
			
			count++;
		
		}
		
		System.out.println("Preparing training and testing datasets...");
		Dataset trainingDataset = new Dataset(sequences);
		System.out.println(Arrays.toString(trainingDataset.getStatesVariable("S1")));
		
		// Obtain test sequences
		
		// Train model
		MCTBNC mctbnc = new MCTBNC(trainingDataset);
		mctbnc.display();		
		
	}
	
	/**
	 * Reads a CSV file
	 * @param pathFile
	 * @return
	 */
	public List<String[]> readCSV(String pathFile) {
		List<String[]> dataCSV = new ArrayList<String[]>();
		String row;
		try {
			BufferedReader csvReader = new BufferedReader(new FileReader(pathFile));
			while ((row = csvReader.readLine()) != null) {
			    String[] dataRow = row.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
			    dataCSV.add(dataRow);
			}
			csvReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return dataCSV;
	}

}
