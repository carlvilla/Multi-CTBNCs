package main.java.com.cig.mctbnc.view;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import main.java.com.cig.mctbnc.learning.parameters.BNParameterLearning;
import main.java.com.cig.mctbnc.learning.parameters.BNParameterMLE;
import main.java.com.cig.mctbnc.learning.parameters.CTBNParameterLearning;
import main.java.com.cig.mctbnc.learning.parameters.CTBNParameterMLE;
import main.java.com.cig.mctbnc.learning.structure.BNStructureHillClimbing;
import main.java.com.cig.mctbnc.learning.structure.BNStructureLearning;
import main.java.com.cig.mctbnc.learning.structure.CTBNStructureLearning;
import main.java.com.cig.mctbnc.learning.structure.CTBNStructureMLE;
import main.java.com.cig.mctbnc.learning.structure.MCTBNCStructureHillClimbing;
import main.java.com.cig.mctbnc.learning.structure.MCTBNCStructureLearning;
import main.java.com.cig.mctbnc.models.Dataset;
import main.java.com.cig.mctbnc.models.MCTBNC;
import main.java.com.cig.mctbnc.models.Sequence;

public class CommandLine {

	public CommandLine(String datasetFolder) throws Exception {

		File folder = new File(datasetFolder);
		File[] files = folder.listFiles();
		List<Sequence> sequences = new ArrayList<Sequence>();
		String[] nameClassVariables = { "Exercise", "ExerciseMode", "S1", "S2", "S3" };
		String nameTimeVariable = "t";

		Map<String, Integer> indexVariables = new HashMap<String, Integer>(); // Map variable name with its index

		int count = 0; // TEST
		System.out.println("Reading sequences...");
		for (File file : files) {
			try {
				List<String[]> data = readCSV(file.getAbsolutePath());
				String[] nameVariables = data.get(0); // Get names of variables

				// It is stored the feature names given by the first sequence. If other
				// sequences
				// give different features, a exception will be thrown.
				if (sequences.size() == 0) {
					// Define the index of each variable
					for (String nameVariable : nameVariables) {
						indexVariables.put(nameVariable, Arrays.asList(nameVariables).indexOf(nameVariable));
					}

				} else {
					for (String nameVariable : nameVariables) {
						if (!indexVariables.containsKey(nameVariable)) {
							throw new Exception("Sequences cannot have different variables");
						}
					}
				}

				data.remove(0); // Drop names of variables
				sequences.add(new Sequence(nameVariables, nameClassVariables, nameTimeVariable, data));

				// TEST
				if (count == 40) {
					break;
				}

				count++;

			} catch (Exception e) {
				System.err.println("Error: " + e);
			}

		}

		System.out.println(indexVariables);

		System.out.println("Preparing training and testing datasets...");
		// For now it will be used 70% sequences for training and 30% for testing
		int numSequences = sequences.size();
		int lastIndexTraining = (int) (numSequences * 0.7);
		int firstIndexTesting = lastIndexTraining + 1;

		// Obtain train and test sequences
		Dataset trainingDataset = new Dataset(indexVariables, sequences.subList(0, lastIndexTraining));
		Dataset testingDataset = new Dataset(indexVariables, sequences.subList(firstIndexTesting, numSequences));

		// Obtain predefined initial structure
		int numNodes = trainingDataset.getNumVariables();
		boolean[][] initialStructure = new boolean[numNodes][numNodes];
		initialStructure[0][30] = true;
		initialStructure[31][30] = true;

		// Define learning algorithms for the class subgraph
		BNParameterLearning bnParameterLearningAlgorithm = new BNParameterMLE();
		BNStructureLearning bnStructureLearningAlgorithm = new BNStructureHillClimbing();

		// Define learning algorithms for the feature and class subgraph
		CTBNParameterLearning ctbnParameterLearningAlgorithm = new CTBNParameterMLE();
		CTBNStructureLearning ctbnStructureLearningAlgorithm = new CTBNStructureMLE();

		// Define multi-dimensional continuous time Bayesian network model
		MCTBNC<String> mctbnc = new MCTBNC<String>(trainingDataset, ctbnParameterLearningAlgorithm,
				ctbnStructureLearningAlgorithm, bnParameterLearningAlgorithm, bnStructureLearningAlgorithm);
		// Initial structure
		//mctbnc.setStructure(initialStructure);

		// mctbnc.learnStructure();
		
		// Train model
		mctbnc.learn();
		mctbnc.display();

	}

	/**
	 * Reads a CSV file
	 * 
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
