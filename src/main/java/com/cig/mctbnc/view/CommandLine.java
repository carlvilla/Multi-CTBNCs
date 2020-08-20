package com.cig.mctbnc.view;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cig.mctbnc.classification.ClassifierFactory;
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
import com.cig.mctbnc.nodes.CIMNode;
import com.cig.mctbnc.nodes.CPTNode;
import com.cig.mctbnc.performance.CrossValidation;

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

	public CommandLine(String datasetFolder) throws Exception {
		// Specify the folder path from which the data is extracted
		String folder = datasetFolder;
		logger.info("Reading sequences from {}", datasetFolder);

		// Rehabilitation dataset
		List<String> nameClassVariables = List.of("ExerciseMode");
		List<String> excludeVariables = List.of("S6", "S7", "S8", "S9", "S10", "S11", "S12", "S13", "S14", "S15", "S16",
				"S17", "S18", "S19", "S20", "S21", "S22", "S23", "S24", "S25", "S26", "S27", "S28", "S29");
		String nameTimeVariable = "t";

		// Artificial dataset
//		List<String> nameClassVariables = List.of("C1", "C2");
//		List<String> excludeVariables = List.of();
//		String nameTimeVariable = "Time";

		// Define if the model will be validated or only learned
		boolean modelValidation = true;

		// ---------------- Definition datasets reader ----------------
		// Define dataset reader
		DatasetReader datasetReader = new SeparateCSVReader(folder, nameTimeVariable, nameClassVariables,
				excludeVariables);

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
		String classMCTBNC = "MCTBNC";
		MCTBNC<CPTNode, CIMNode> mctbnc = ClassifierFactory.<CPTNode, CIMNode>getMCTBNC(classMCTBNC,
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

		if (modelValidation) {
			// Hold-out validation
			// double trainingSize = 0.7;
			// boolean shuffleSequences = true;
			// HoldOut testingMethod = new HoldOut(datasetReader, trainingSize,
			// shuffleSequences);

			// Cross-validation
			int folds = 5;
			boolean shuffleSequences = true;
			CrossValidation testingMethod = new CrossValidation(datasetReader, folds, shuffleSequences);

			// Evaluate the performance of the model
			testingMethod.evaluate(mctbnc);
		} else {
			// Obtain whole dataset
			Dataset dataset = datasetReader.readDataset();
			// Learn model
			mctbnc.learn(dataset);
			// Display model
			mctbnc.display();
		}
	}

}
