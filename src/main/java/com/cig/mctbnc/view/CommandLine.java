package com.cig.mctbnc.view;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cig.mctbnc.classification.ClassifierFactory;
import com.cig.mctbnc.classification.Prediction;
import com.cig.mctbnc.data.reader.DatasetReader;
import com.cig.mctbnc.data.reader.SeparateCSVReader;
import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.learning.parameters.ParameterLearningAlgorithm;
import com.cig.mctbnc.learning.parameters.bn.BNParameterMLE;
import com.cig.mctbnc.learning.parameters.ctbn.CTBNBayesianEstimation;
import com.cig.mctbnc.learning.parameters.ctbn.CTBNMaximumLikelihoodEstimation;
import com.cig.mctbnc.learning.structure.HillClimbingBN;
import com.cig.mctbnc.learning.structure.HillClimbingCTBN;
import com.cig.mctbnc.learning.structure.StructureLearningAlgorithm;
import com.cig.mctbnc.models.MCTBNC;
import com.cig.mctbnc.models.MCTNBC;
import com.cig.mctbnc.nodes.CIMNode;
import com.cig.mctbnc.nodes.CPTNode;
import com.cig.mctbnc.performance.Metrics;

public class CommandLine {

	static Logger logger = LogManager.getLogger(CommandLine.class);

	Map<String, ParameterLearningAlgorithm> parameterLearningBN = new HashMap<String, ParameterLearningAlgorithm>() {
		{
			put("MLE", new BNParameterMLE());
		}
	};

	Map<String, ParameterLearningAlgorithm> parameterLearningCTBN = new HashMap<String, ParameterLearningAlgorithm>() {
		{
			put("MLE", new CTBNMaximumLikelihoodEstimation()); // Maximum likelihood estimation
			put("BE", new CTBNBayesianEstimation()); // Bayesian estimation
		}
	};

	Map<String, StructureLearningAlgorithm> structureLearningBN = new HashMap<String, StructureLearningAlgorithm>() {
		{
			put("HillClimbing", new HillClimbingBN());
		}
	};

	Map<String, StructureLearningAlgorithm> structureLearningCTBN = new HashMap<String, StructureLearningAlgorithm>() {
		{
			put("HillClimbing", new HillClimbingCTBN());
		}
	};

	// There are different types of MCTBNCs depending on the restrictions on their
	// structures
	/*
	 * Map<String, Classifier> classifiers = new HashMap<String, Classifier>() { {
	 * put("MCTBNC", new MCTBNC()); // Multidimensional continuous time Bayesian
	 * network classifier put("MCTNBC", new MCTNBC()); // Multidimensioal continuous
	 * time Naive Bayes classifier } };
	 */

	public CommandLine(String datasetFolder) throws Exception {

		// Specify the folder path from which the data is extracted
		String folder = datasetFolder;
		logger.info("Reading sequences from {}", datasetFolder);

		// Rehabilitation dataset
		List<String> nameClassVariables = List.of("ExerciseMode");
		List<String> excludeVariables = List.of("S7", "S8", "S9", "S10", "S11", "S12", "S13", "S14", "S15", "S16",
				"S17", "S18", "S19", "S20", "S21", "S22", "S23", "S24", "S25", "S26", "S27", "S28", "S29");
		String nameTimeVariable = "t";

		// Artificial dataset
		// List<String> nameClassVariables = List.of("C1", "C2");
		// String[] excludeVariables = {};
		// String nameTimeVariable = "Time";

		// ---------------- Definition datasets for hold-out testing ----------------
		// Define dataset reader
		DatasetReader datasetReader = new SeparateCSVReader(folder, nameTimeVariable, nameClassVariables,
				excludeVariables);
		// Generate training and testing datasets
		double trainingSize = 0.8;
		datasetReader.generateTrainAndTest(trainingSize, false);
		// Obtain training dataset
		Dataset trainingDataset = datasetReader.getTraining();
		// Obtain testing dataset
		Dataset testingDataset = datasetReader.getTesting();

		// ---------------- Definition datasets for hold-out testing ----------------

		logger.info("Sequences for training {}", trainingDataset.getNumDataPoints());
		logger.info("Sequences for testing {}", testingDataset.getNumDataPoints());
		logger.info("Time variable: {}", trainingDataset.getNameTimeVariable());
		logger.info("Features: {}", trainingDataset.getNameFeatures());
		logger.info("Class variables: {}", (Arrays.toString(nameClassVariables.toArray())));

		// -------------------------- LEARNING ALGORITHMS --------------------------
		// Define learning algorithms for the class subgraph
		ParameterLearningAlgorithm bnParameterLearningAlgorithm = parameterLearningBN.get("MLE");
		StructureLearningAlgorithm bnStructureLearningAlgorithm = structureLearningBN.get("HillClimbing");

		// Define learning algorithms for the feature and class subgraph
		ParameterLearningAlgorithm ctbnParameterLearningAlgorithm = parameterLearningCTBN.get("MLE");
		StructureLearningAlgorithm ctbnStructureLearningAlgorithm = structureLearningCTBN.get("HillClimbing");
		// -------------------------- LEARNING ALGORITHMS --------------------------

		// Define the type of multi-dimensional continuous time Bayesian network
		// classifier to use
		MCTBNC<CPTNode, CIMNode> mctbnc = ClassifierFactory.<CPTNode, CIMNode>getMCTBNC("MCTBNC", trainingDataset,
				ctbnParameterLearningAlgorithm, ctbnStructureLearningAlgorithm, bnParameterLearningAlgorithm,
				bnStructureLearningAlgorithm, CPTNode.class, CIMNode.class);

		// Determine the penalization function (for complexity of the BN and CTBN
		// structure)
		String penalizationFunction = "BIC";
		mctbnc.setPenalizationFunction(penalizationFunction);

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
		Prediction[] predictions = mctbnc.predict(testingDataset);
		// Evaluate the performance of the model
		Metrics.evaluate(predictions, testingDataset);

	}

}
