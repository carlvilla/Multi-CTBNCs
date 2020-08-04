package com.cig.mctbnc.view;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cig.mctbnc.data.reader.DatasetReader;
import com.cig.mctbnc.data.reader.SeparateCSVReader;
import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.learning.parameters.BNParameterMLE;
import com.cig.mctbnc.learning.parameters.CTBNParameterMLE;
import com.cig.mctbnc.learning.parameters.ParameterLearningAlgorithm;
import com.cig.mctbnc.learning.structure.HillClimbing;
import com.cig.mctbnc.learning.structure.StructureLearningAlgorithm;
import com.cig.mctbnc.models.MCTBNC;
import com.cig.mctbnc.nodes.CIMNode;
import com.cig.mctbnc.nodes.CPTNode;

public class CommandLine {

	static Logger logger = LogManager.getLogger(CommandLine.class);

	public CommandLine(String datasetFolder) throws Exception {

		File folder = new File(datasetFolder);
		File[] files = folder.listFiles();
		List<String> nameClassVariables = List.of("ExerciseMode", "Exercise", "S1");
		String[] excludeVariables = {"S5", "S6", "S7", "S8", "S9", "S10", "S11", "S12", "S13", "S14",
				"S15", "S16", "S17", "S18", "S19", "S20", "S21", "S22", "S23", "S24", "S25", "S26", "S27", "S28",
				"S29" };
		String nameTimeVariable = "t";

		logger.info("Reading sequences from {}", datasetFolder);
		logger.info("Preparing training and testing datasets");

		// Generate datasets
		// For now it will be used 70% sequences for training and 30% for testing
		// Define training dataset
		File[] trainingFiles = Arrays.copyOfRange(files, 0, (int) (files.length * 0.7));
		DatasetReader dgTraining = new SeparateCSVReader(trainingFiles, nameTimeVariable, nameClassVariables,
				excludeVariables);
		Dataset trainingDataset = dgTraining.readDataset();

		// Define testing dataset
		File[] testingFiles = Arrays.copyOfRange(files, (int) (files.length * 0.7), (int) (files.length));
		DatasetReader dgTesting = new SeparateCSVReader(testingFiles, nameTimeVariable, nameClassVariables,
				excludeVariables);
		Dataset testingDataset = dgTesting.readDataset();

		logger.info("Time variable: {}", trainingDataset.getNameTimeVariable());
		logger.info("Features: {}", trainingDataset.getNameFeatures());
		logger.info("Class variables: {}", (Arrays.toString(nameClassVariables.toArray())));

		// Define learning algorithms for the class subgraph
		ParameterLearningAlgorithm bnParameterLearningAlgorithm = new BNParameterMLE();
		StructureLearningAlgorithm bnStructureLearningAlgorithm = new HillClimbing();

		// Define learning algorithms for the feature and class subgraph
		ParameterLearningAlgorithm ctbnParameterLearningAlgorithm = new CTBNParameterMLE();
		StructureLearningAlgorithm ctbnStructureLearningAlgorithm = new HillClimbing();

		// Define multi-dimensional continuous time Bayesian network model
		MCTBNC<CPTNode, CIMNode> mctbnc = new MCTBNC<CPTNode, CIMNode>(trainingDataset, ctbnParameterLearningAlgorithm,
				ctbnStructureLearningAlgorithm, bnParameterLearningAlgorithm, bnStructureLearningAlgorithm);

		// Initial structure
		// Define initial structure - IT MAY NOT BE WORKING CORRECTLY DUE TO THE NODE
		// INDEXERS OF THE PGMs
		/*
		 * int numNodes = trainingDataset.getNumVariables(); boolean[][]
		 * initialStructure = new boolean[numNodes][numNodes];
		 * initialStructure[trainingDataset.getIndexVariable("Exercise")][
		 * trainingDataset .getIndexVariable("ExerciseMode")] = true;
		 * initialStructure[trainingDataset.getIndexVariable("S1")][trainingDataset
		 * .getIndexVariable("ExerciseMode")] = true;
		 * initialStructure[trainingDataset.getIndexVariable("S2")][trainingDataset
		 * .getIndexVariable("ExerciseMode")] = true;
		 * mctbnc.setStructure(initialStructure);
		 */

		// Train model
		mctbnc.learn();
		mctbnc.display();

	}

}
