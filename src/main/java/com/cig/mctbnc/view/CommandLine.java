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
import com.cig.mctbnc.learning.BNLearningAlgorithms;
import com.cig.mctbnc.learning.CTBNLearningAlgorithms;
import com.cig.mctbnc.learning.parameters.bn.BNMaximumLikelihoodEstimation;
import com.cig.mctbnc.learning.parameters.bn.BNParameterLearningAlgorithm;
import com.cig.mctbnc.learning.parameters.ctbn.CTBNBayesianEstimation;
import com.cig.mctbnc.learning.parameters.ctbn.CTBNMaximumLikelihoodEstimation;
import com.cig.mctbnc.learning.parameters.ctbn.CTBNParameterLearningAlgorithm;
import com.cig.mctbnc.learning.structure.BNStructureLearningAlgorithm;
import com.cig.mctbnc.learning.structure.CTBNStructureLearningAlgorithm;
import com.cig.mctbnc.learning.structure.StructureLearningAlgorithm;
import com.cig.mctbnc.learning.structure.optimization.hillclimbing.BNHillClimbing;
import com.cig.mctbnc.learning.structure.optimization.hillclimbing.CTBNHillClimbing;
import com.cig.mctbnc.models.MCTBNC;
import com.cig.mctbnc.nodes.CIMNode;
import com.cig.mctbnc.nodes.CPTNode;
import com.cig.mctbnc.performance.HoldOut;

public class CommandLine {

	static Logger logger = LogManager.getLogger(CommandLine.class);

	Map<String, BNParameterLearningAlgorithm> parameterLearningBN = new HashMap<String, BNParameterLearningAlgorithm>() {
		{
			put("MLE", new BNMaximumLikelihoodEstimation());
		}
	};

	Map<String, CTBNParameterLearningAlgorithm> parameterLearningCTBN = new HashMap<String, CTBNParameterLearningAlgorithm>() {
		{
			put("MLE", new CTBNMaximumLikelihoodEstimation()); // Maximum likelihood estimation
			put("BE", new CTBNBayesianEstimation()); // Bayesian estimation
		}
	};

	Map<String, BNStructureLearningAlgorithm> structureLearningBN = new HashMap<String, BNStructureLearningAlgorithm>() {
		{
			put("HillClimbing", new BNHillClimbing());
		}
	};

	Map<String, CTBNStructureLearningAlgorithm> structureLearningCTBN = new HashMap<String, CTBNStructureLearningAlgorithm>() {
		{
			put("HillClimbing", new CTBNHillClimbing());
		}
	};

	public CommandLine(String datasetFolder) throws Exception {
		// Specify the folder path from which the data is extracted
		String folder = datasetFolder;
		logger.info("Reading sequences from {}", datasetFolder);

		// Rehabilitation dataset
		List<String> nameClassVariables = List.of("ExerciseMode");
		List<String> excludeVariables = List.of("S5", "S6", "S7", "S8", "S9", "S10", "S11", "S12", "S13", "S14", "S15",
				"S16", "S17", "S18", "S19", "S20", "S21", "S22", "S23", "S24", "S25", "S26", "S27", "S28", "S29");
		String nameTimeVariable = "t";

		// Artificial dataset
//		List<String> nameClassVariables = List.of("C1", "C2");
//		List<String> excludeVariables = List.of();
//		String nameTimeVariable = "Time";

		// Define if the model will be validated or only learned
		boolean modelValidation = true;

		// ---------------- Definition datasets reader ----------------
		// Define dataset reader
		// DatasetReader datasetReader = new SeparateCSVReader(folder, nameTimeVariable,
		// nameClassVariables,
		// excludeVariables);
		DatasetReader datasetReader = new SeparateCSVReader(folder);

		// -------------------------- LEARNING ALGORITHMS --------------------------
		// Define learning algorithms for the class subgraph
		BNParameterLearningAlgorithm bnParameterLearningAlgorithm = parameterLearningBN.get("MLE");
		BNStructureLearningAlgorithm bnStructureLearningAlgorithm = structureLearningBN.get("HillClimbing");
		BNLearningAlgorithms bnLearningAlgs = new BNLearningAlgorithms(bnParameterLearningAlgorithm,
				bnStructureLearningAlgorithm);

		// Define learning algorithms for the feature and class subgraph
		CTBNParameterLearningAlgorithm ctbnParameterLearningAlgorithm = new CTBNBayesianEstimation(1, 1, 0.0005); // parameterLearningCTBN.get("MLE");
		CTBNStructureLearningAlgorithm ctbnStructureLearningAlgorithm = structureLearningCTBN.get("HillClimbing");
		CTBNLearningAlgorithms ctbnLearningAlgs = new CTBNLearningAlgorithms(ctbnParameterLearningAlgorithm,
				ctbnStructureLearningAlgorithm);

		// -------------------------- LEARNING ALGORITHMS --------------------------

		// Define the type of multi-dimensional continuous time Bayesian network
		// classifier to use
		String classMCTBNC = "KMCTBNC";
		Map<String, String> args = null;
		MCTBNC<CPTNode, CIMNode> mctbnc = ClassifierFactory.<CPTNode, CIMNode>getMCTBNC(classMCTBNC, bnLearningAlgs,
				ctbnLearningAlgs, args, CPTNode.class, CIMNode.class);

		// Determine the penalization function (for complexity of the BN and CTBN
		// structure)
		String penalizationFunction = "No";
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
			logger.info("Validating model");
			// Hold-out validation
			double trainingSize = 0.7;
			boolean shuffleSequences = true;
			HoldOut testingMethod = new HoldOut(datasetReader, trainingSize, shuffleSequences);

			// Cross-validation
//			int folds = 4;
//			boolean shuffleSequences = true;
//			CrossValidation testingMethod = new CrossValidation(datasetReader, folds, shuffleSequences);

			// Evaluate the performance of the model
			testingMethod.evaluate(mctbnc);
		} else {
			logger.info("Training model with all available data");
			// Obtain whole dataset
			// Dataset dataset = datasetReader.readDataset();
			// Learn model
			// mctbnc.learn(dataset);
			// Display model
			// mctbnc.display();
		}
	}

}
