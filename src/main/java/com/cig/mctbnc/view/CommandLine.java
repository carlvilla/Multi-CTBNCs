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
import com.cig.mctbnc.learning.parameters.BNParameterLearning;
import com.cig.mctbnc.learning.parameters.BNParameterMLE;
import com.cig.mctbnc.learning.parameters.CTBNParameterLearning;
import com.cig.mctbnc.learning.parameters.CTBNParameterMLE;
import com.cig.mctbnc.learning.structure.BNStructureHillClimbing;
import com.cig.mctbnc.learning.structure.BNStructureLearning;
import com.cig.mctbnc.learning.structure.CTBNCStructureLearning;
import com.cig.mctbnc.learning.structure.CTBNCStructureMLE;
import com.cig.mctbnc.models.MCTBNC;
import com.cig.mctbnc.nodes.DiscreteNode;

public class CommandLine {

	static Logger logger = LogManager.getLogger(CommandLine.class);

	public CommandLine(String datasetFolder) throws Exception {

		File folder = new File(datasetFolder);
		File[] files = folder.listFiles();
		String[] nameClassVariables = { "Exercise", "ExerciseMode", "S1" };
		String nameTimeVariable = "t";

		logger.info("Reading sequences from {}", datasetFolder);
		logger.info("Preparing training and testing datasets");

		// For now it will be used 70% sequences for training and 30% for testing
		// Define training dataset
		Dataset trainingDataset = new Dataset(nameTimeVariable, nameClassVariables);
		File[] trainingFiles = Arrays.copyOfRange(files, 0, (int) (files.length * 0.7));
		for (File file : trainingFiles) {
			List<String[]> dataSequence = readCSV(file.getAbsolutePath());
			trainingDataset.addSequence(dataSequence);
		}

		// Define testing dataset
		Dataset testingDataset = new Dataset(nameTimeVariable, nameClassVariables);
		File[] testingFiles = Arrays.copyOfRange(files, (int) (files.length * 0.7), files.length);
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
		initialStructure[0][30] = true;
		initialStructure[31][30] = true;

		// Define learning algorithms for the class subgraph
		BNParameterLearning bnParameterLearningAlgorithm = new BNParameterMLE();
		BNStructureLearning bnStructureLearningAlgorithm = new BNStructureHillClimbing();

		// Define learning algorithms for the feature and class subgraph
		CTBNParameterLearning ctbnParameterLearningAlgorithm = new CTBNParameterMLE();
		CTBNCStructureLearning ctbnStructureLearningAlgorithm = new CTBNCStructureMLE();

		// Define multi-dimensional continuous time Bayesian network model
		MCTBNC<DiscreteNode> mctbnc = new MCTBNC<DiscreteNode>(trainingDataset, ctbnParameterLearningAlgorithm,
				ctbnStructureLearningAlgorithm, bnParameterLearningAlgorithm, bnStructureLearningAlgorithm);
		// Initial structure
		// mctbnc.setStructure(initialStructure);

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
