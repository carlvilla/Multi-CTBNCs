package com.cig.mctbnc.view;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cig.mctbnc.classification.Metrics;
import com.cig.mctbnc.data.reader.DatasetReader;
import com.cig.mctbnc.data.reader.SeparateCSVReader;
import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.learning.parameters.ParameterLearningAlgorithm;
import com.cig.mctbnc.learning.parameters.bn.BNParameterMLE;
import com.cig.mctbnc.learning.parameters.ctbn.CTBNMaximumLikelihoodEstimation;
import com.cig.mctbnc.learning.structure.HillClimbingBN;
import com.cig.mctbnc.learning.structure.HillClimbingCTBN;
import com.cig.mctbnc.learning.structure.StructureLearningAlgorithm;
import com.cig.mctbnc.learning.structure.constraints.MCTBNC.GeneralMCTBNC;
import com.cig.mctbnc.learning.structure.constraints.MCTBNC.StructureConstraintsMCTBNC;
import com.cig.mctbnc.models.MCTBNC;
import com.cig.mctbnc.nodes.CIMNode;
import com.cig.mctbnc.nodes.CPTNode;

public class CommandLine {

	static Logger logger = LogManager.getLogger(CommandLine.class);

	public CommandLine(String datasetFolder) throws Exception {

		File folder = new File(datasetFolder);
		File[] files = folder.listFiles();
		List<String> nameClassVariables = List.of("ExerciseMode");
		String[] excludeVariables = { "S5", "S6", "S7", "S8", "S9", "S10", "S11", "S12", "S13", "S14", "S15", "S16",
				"S17", "S18", "S19", "S20", "S21", "S22", "S23", "S24", "S25", "S26", "S27", "S28", "S29" };
		String nameTimeVariable = "t";

		logger.info("Reading sequences from {}", datasetFolder);
		logger.info("Number of sequences {}", files.length);
		logger.info("Preparing training and testing datasets (Hold-out)");

		// Random permutation of the set of sequences
		// logger.info("Sequences are randomly permuted");
		// Collections.shuffle(Arrays.asList(files));

		// Generate datasets
		// For now it will be used 70% sequences for training and 30% for testing
		// Define training dataset
		File[] trainingFiles = Arrays.copyOfRange(files, 0, (int) (files.length * 0.8));
		DatasetReader drTraining = new SeparateCSVReader(trainingFiles, nameTimeVariable, nameClassVariables,
				excludeVariables);
		Dataset trainingDataset = drTraining.readDataset();
		logger.info("Sequences for training {}", trainingDataset.getNumDataPoints());

		// Define testing dataset
		File[] testingFiles = Arrays.copyOfRange(files, (int) (files.length * 0.8), (int) (files.length));
		DatasetReader drTesting = new SeparateCSVReader(testingFiles, nameTimeVariable, nameClassVariables,
				excludeVariables);
		Dataset testingDataset = drTesting.readDataset();
		logger.info("Sequences for testing {}", testingDataset.getNumDataPoints());

		logger.info("Time variable: {}", trainingDataset.getNameTimeVariable());
		logger.info("Features: {}", trainingDataset.getNameFeatures());
		logger.info("Class variables: {}", (Arrays.toString(nameClassVariables.toArray())));

		// Define learning algorithms for the class subgraph
		ParameterLearningAlgorithm bnParameterLearningAlgorithm = new BNParameterMLE();
		StructureLearningAlgorithm bnStructureLearningAlgorithm = new HillClimbingBN();

		// Define learning algorithms for the feature and class subgraph
		ParameterLearningAlgorithm ctbnParameterLearningAlgorithm = new CTBNMaximumLikelihoodEstimation();
		// new CTBNBayesianEstimation(1, 1, 0.05);
		StructureLearningAlgorithm ctbnStructureLearningAlgorithm = new HillClimbingCTBN();
		// Define type of MCTBNC that will be learned (structure constraints)
		StructureConstraintsMCTBNC structureConstraintsMCTBNC = new GeneralMCTBNC(); // new MCTNBC();
		// Determine if the structure complexity (of the BN and CTBN) should be penalized
		String penalizationFunction = "None";
		structureConstraintsMCTBNC.setPenalizationFunction(penalizationFunction);
		logger.info("Using penalization function {}", penalizationFunction);

		// Define multi-dimensional continuous time Bayesian network model
		MCTBNC<CPTNode, CIMNode> mctbnc = new MCTBNC<CPTNode, CIMNode>(trainingDataset, ctbnParameterLearningAlgorithm,
				ctbnStructureLearningAlgorithm, bnParameterLearningAlgorithm, bnStructureLearningAlgorithm,
				CPTNode.class, CIMNode.class, structureConstraintsMCTBNC);

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

		// Perform predictions with MCTBNC
		// String[][] predictions = mctbnc.predict(testingDataset);
		String[][] predictions = mctbnc.predict(testingDataset);

		logger.info("1/0 subset accuracy: {}",
				Metrics.subsetAccuracy(predictions, testingDataset.getValuesClassVariables()));

	}

}
