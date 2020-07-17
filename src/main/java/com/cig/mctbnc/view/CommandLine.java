package com.cig.mctbnc.view;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.learning.parameters.BNParameterMLE;
import com.cig.mctbnc.learning.parameters.CTBNParameterMLE;
import com.cig.mctbnc.learning.parameters.ParameterLearningAlgorithm;
import com.cig.mctbnc.learning.structure.HillClimbing;
import com.cig.mctbnc.learning.structure.StructureLearningAlgorithm;
import com.cig.mctbnc.models.MCTBNC;
import com.cig.mctbnc.nodes.DiscreteNode;

public class CommandLine {

	static Logger logger = LogManager.getLogger(CommandLine.class);

	public CommandLine(String datasetFolder) throws Exception {

		File folder = new File(datasetFolder);
		File[] files = folder.listFiles();
		String[] nameClassVariables = { "Exercise", "ExerciseMode", "S1", "S2", "S3" };
		String nameTimeVariable = "t";

		logger.info("Reading sequences from {}", datasetFolder);
		logger.info("Preparing training and testing datasets");

		// For now it will be used 70% sequences for training and 30% for testing
		// Define training dataset
		Dataset trainingDataset = new Dataset(nameTimeVariable, nameClassVariables);
		File[] trainingFiles = Arrays.copyOfRange(files, 0, (int) (files.length * 0.05));
		for (File file : trainingFiles) {
			List<String[]> dataSequence = readCSV(file.getAbsolutePath());
			trainingDataset.addSequence(dataSequence);
		}

		// Define testing dataset
		Dataset testingDataset = new Dataset(nameTimeVariable, nameClassVariables);
		File[] testingFiles = Arrays.copyOfRange(files, (int) (files.length * 0.05), (int) (files.length * 0.06));
		for (File file : testingFiles) {
			List<String[]> dataSequence = readCSV(file.getAbsolutePath());
			testingDataset.addSequence(dataSequence);
		}

		logger.info("Time variable: {}", trainingDataset.getNameTimeVariable());
		logger.info("Features: {}", Arrays.toString(trainingDataset.getNameFeatures()));
		logger.info("Class variables: {}", Arrays.toString(nameClassVariables));

		// Define initial structure
		int numNodes = trainingDataset.getNumVariables();
		boolean[][] initialStructure = new boolean[numNodes][numNodes];
		initialStructure[trainingDataset.getIndexVariable("Exercise")][trainingDataset
				.getIndexVariable("ExerciseMode")] = true;
		initialStructure[trainingDataset.getIndexVariable("S1")][trainingDataset
				.getIndexVariable("ExerciseMode")] = true;
		initialStructure[trainingDataset.getIndexVariable("S2")][trainingDataset
				.getIndexVariable("ExerciseMode")] = true;

		// Define learning algorithms for the class subgraph
		ParameterLearningAlgorithm bnParameterLearningAlgorithm = new BNParameterMLE();
		StructureLearningAlgorithm bnStructureLearningAlgorithm = new HillClimbing();

		// Define learning algorithms for the feature and class subgraph
		ParameterLearningAlgorithm ctbnParameterLearningAlgorithm = new CTBNParameterMLE();
		StructureLearningAlgorithm ctbnStructureLearningAlgorithm = new HillClimbing();

		// Define multi-dimensional continuous time Bayesian network model
		MCTBNC<DiscreteNode> mctbnc = new MCTBNC<DiscreteNode>(trainingDataset, ctbnParameterLearningAlgorithm,
				ctbnStructureLearningAlgorithm, bnParameterLearningAlgorithm, bnStructureLearningAlgorithm);
		// Initial structure
		mctbnc.setStructure(initialStructure);

		// mctbnc.learnStructure();

		// Train model
		mctbnc.learn();
		mctbnc.display();

	}

	/**
	 * Reads a CSV file. It returns the 
	 * 
	 * @param pathFile path to the CSV file
	 * @return list with the rows (arrays) of the CSV 
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
