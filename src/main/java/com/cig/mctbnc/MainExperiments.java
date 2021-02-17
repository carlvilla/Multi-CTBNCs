package com.cig.mctbnc;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cig.mctbnc.classification.ClassifierFactory;
import com.cig.mctbnc.data.reader.DatasetReader;
import com.cig.mctbnc.data.reader.MultipleCSVReader;
import com.cig.mctbnc.exceptions.UnreadDatasetException;
import com.cig.mctbnc.learning.BNLearningAlgorithms;
import com.cig.mctbnc.learning.CTBNLearningAlgorithms;
import com.cig.mctbnc.learning.parameters.bn.BNParameterLearningAlgorithm;
import com.cig.mctbnc.learning.parameters.bn.BNParameterLearningAlgorithmFactory;
import com.cig.mctbnc.learning.parameters.ctbn.CTBNParameterLearningAlgorithm;
import com.cig.mctbnc.learning.parameters.ctbn.CTBNParameterLearningAlgorithmFactory;
import com.cig.mctbnc.learning.structure.BNStructureLearningAlgorihtmFactory;
import com.cig.mctbnc.learning.structure.BNStructureLearningAlgorithm;
import com.cig.mctbnc.learning.structure.CTBNStructureLearningAlgorihtmFactory;
import com.cig.mctbnc.learning.structure.CTBNStructureLearningAlgorithm;
import com.cig.mctbnc.models.MCTBNC;
import com.cig.mctbnc.nodes.CIMNode;
import com.cig.mctbnc.nodes.CPTNode;
import com.cig.mctbnc.performance.CrossValidationSeveralModels;
import com.cig.mctbnc.performance.ValidationMethod;
import com.cig.mctbnc.performance.ValidationMethodFactory;
import com.cig.mctbnc.performance.writers.ExcelExperimentsWriter;
import com.cig.mctbnc.performance.writers.MetricsWriter;

/**
 * Class used to automate the execution of experiments.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class MainExperiments {

	static List<String> datasets = List.of("C:\\Users\\Carlos\\Desktop\\datasets\\synthetic\\Experiment2\\D1",
			"C:\\Users\\Carlos\\Desktop\\datasets\\synthetic\\Experiment2\\D2",
			"C:\\Users\\Carlos\\Desktop\\datasets\\synthetic\\Experiment2\\D3",
			"C:\\Users\\Carlos\\Desktop\\datasets\\synthetic\\Experiment2\\D4",
			"C:\\Users\\Carlos\\Desktop\\datasets\\synthetic\\Experiment2\\D5");

	// ---------------------------- Synthetic dataset ----------------------------
	static String nameTimeVariable = "t";
	static List<String> nameClassVariables = List.of("C1", "C2", "C3", "C4", "C5");
	static List<String> nameAllFeatureVariables = List.of("X1", "X2", "X3", "X4", "X5");
	static List<String> nameSelectedFeatureVariables = List.of("X1", "X2", "X3", "X4", "X5");
	// ---------------------------- Synthetic dataset ----------------------------

	static String optionFeaturesUsed = ""; // random

	static List<String> models = List.of("CTBNCs", "DAG-maxK MCTBNC"); // "Empty-maxK MCTBNC",

	// Get names learning algorithms
	static String nameBnPLA = "Bayesian estimation";
	static String nameBnSLA = "Hill climbing";
	// Get hyperparameters
	static double nx = 1;

	// Get names learning algorithms
	static String nameCtbnPLA = "Bayesian estimation";
	static String nameCtbnSLA = "Hill climbing";
	// Get hyperparameters
	static double mxy = 1;
	static double tx = 0.001;

	// Get score function
	static List<String> scoreFunctions = List.of("Log-likelihood", "Bayesian score"); // "Conditional log-likelihood",
	// Define penalization function (if any)
	static String penalizationFunction = "BIC"; // "AIC", "No", "BIC"

	static String maxK = "9";

	static String initialStructure = "Empty"; // Empty, Naive Bayes

	static String selectedValidationMethod = "Cross-validation";
	static int folds = 5;
	static boolean shuffleSequences = true;

	static int trainingSize = 0;

	static MetricsWriter metricsWriter;

	static Logger logger = LogManager.getLogger(MainExperiments.class);

	/**
	 * Class use to perform experiments.
	 * 
	 * @param args
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args) {
		// Define parameter learning algorithms
		BNParameterLearningAlgorithm bnPLA = BNParameterLearningAlgorithmFactory.getAlgorithm(nameBnPLA, nx);
		CTBNParameterLearningAlgorithm ctbnPLA = CTBNParameterLearningAlgorithmFactory.getAlgorithm(nameCtbnPLA, mxy,
				tx);
		// Parameters that could be necessary for the generation of the models
		Map<String, String> parameters = new WeakHashMap<String, String>();
		parameters.put("maxK", maxK);
		// Test over different sets of features
		List<List<String>> setsFeatures = setFeatures(optionFeaturesUsed, nameAllFeatureVariables);
		// Iterate over each set of features that is evaluated
		for (List<String> nameSelectedFeatureVariables : setsFeatures) {
			// Define output to store the results of the experiments
			metricsWriter = new ExcelExperimentsWriter(scoreFunctions, datasets, models, nameSelectedFeatureVariables,
					nameClassVariables, bnPLA, ctbnPLA, penalizationFunction, initialStructure);
			// Iterate over the score functions that are used
			for (String scoreFunction : scoreFunctions) {
				System.out.printf("------------------------------ Score function: %s ------------------------------\n",
						scoreFunction);
				// Iterate over the datasets that are evaluated
				for (String pathDataset : datasets) {
					System.out.printf("############################# DATASET: %s #############################\n",
							pathDataset);
					try {
						DatasetReader datasetReader = new MultipleCSVReader(pathDataset);
						// Set the variables that will be used
						datasetReader.setVariables(nameTimeVariable, nameClassVariables, nameSelectedFeatureVariables);
						for (String selectedModel : models) {
							System.out.printf("***************************** MODEL: %s *****************************\n",
									selectedModel);
							performExperiment(datasetReader, bnPLA, ctbnPLA, selectedModel, scoreFunction, parameters);
						}
					} catch (FileNotFoundException e) {
						logger.error(e.getMessage());
					} catch (UnreadDatasetException e) {
						logger.error(e.getMessage());
					}
				}
			}
			metricsWriter.close();
		}
	}

	private static void performExperiment(DatasetReader datasetReader, BNParameterLearningAlgorithm bnPLA,
			CTBNParameterLearningAlgorithm ctbnPLA, String selectedModel, String scoreFunction,
			Map<String, String> parameters) throws UnreadDatasetException {
		// Define structure learning algorithms
		BNStructureLearningAlgorithm bnSLA = BNStructureLearningAlgorihtmFactory.getAlgorithm(nameBnSLA, scoreFunction,
				penalizationFunction);
		CTBNStructureLearningAlgorithm ctbnSLA = CTBNStructureLearningAlgorihtmFactory.getAlgorithm(nameCtbnSLA,
				scoreFunction, penalizationFunction);
		BNLearningAlgorithms bnLearningAlgs = new BNLearningAlgorithms(bnPLA, bnSLA);
		CTBNLearningAlgorithms ctbnLearningAlgs = new CTBNLearningAlgorithms(ctbnPLA, ctbnSLA);
		// Generate selected model and validation method
		MCTBNC<CPTNode, CIMNode> model;
		ValidationMethod validationMethod;
		if (selectedModel.equals("CTBNCs")) {
			model = ClassifierFactory.<CPTNode, CIMNode>getMCTBNC("MCTBNC", bnLearningAlgs, ctbnLearningAlgs,
					parameters, CPTNode.class, CIMNode.class);
			validationMethod = new CrossValidationSeveralModels(datasetReader, folds, shuffleSequences);
		} else if (selectedModel.equals("maxK CTBNCs")) {
			model = ClassifierFactory.<CPTNode, CIMNode>getMCTBNC("DAG-maxK MCTBNC", bnLearningAlgs, ctbnLearningAlgs,
					parameters, CPTNode.class, CIMNode.class);
			validationMethod = new CrossValidationSeveralModels(datasetReader, folds, shuffleSequences);
		} else {
			model = ClassifierFactory.<CPTNode, CIMNode>getMCTBNC(selectedModel, bnLearningAlgs, ctbnLearningAlgs,
					parameters, CPTNode.class, CIMNode.class);
			validationMethod = ValidationMethodFactory.getValidationMethod(selectedValidationMethod, datasetReader,
					shuffleSequences, trainingSize, folds);
		}
		// Set output to show results
		validationMethod.setWriter(metricsWriter);
		// Define initial structure
		model.setIntialStructure(initialStructure);
		// Evaluate the performance of the model
		validationMethod.evaluate(model);
	}

	/**
	 * Return a list with the different combination of features that are tested.
	 * 
	 * @param optionFeaturesUsed
	 * @param nameAllFeatureVariables
	 * 
	 * @return list of features
	 */
	public static List<List<String>> setFeatures(String optionFeaturesUsed, List<String> nameAllFeatureVariables) {
		List<List<String>> setFeatures = new ArrayList<List<String>>();
		// Random set of features
		if (optionFeaturesUsed.equals("random")) {
			int trials = 5000;
			for (int z = 0; z < trials; z++) {
				Random rn = new Random();
				int numFeatures = rn.nextInt(3 - 2 + 1) + 2;
				List<Integer> indexes = new ArrayList<Integer>();
				for (int i = 0; i < numFeatures; i++) {
					int idx = rn.nextInt(nameAllFeatureVariables.size());
					while (indexes.contains(idx)) {
						idx = rn.nextInt(nameAllFeatureVariables.size());
					}
					indexes.add(idx);
				}
				List<String> nameFeatureVariables = new ArrayList<String>();
				for (int idx : indexes)
					nameFeatureVariables.add(nameAllFeatureVariables.get(idx));

				setFeatures.add(nameFeatureVariables);
			}
		} else
			return List.of(nameSelectedFeatureVariables);
		return setFeatures;
	}
}
