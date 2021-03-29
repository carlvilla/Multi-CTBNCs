package com.cig.mctbnc;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;
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
import com.cig.mctbnc.learning.structure.StructureLearningAlgorithm;
import com.cig.mctbnc.learning.structure.StructureLearningAlgorithmFactory;
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

	// ---------------------------- Synthetic dataset ----------------------------
	// Paths to datasets
	static List<List<String>> datasetsAll = List.of(
			List.of("Experiments/Datasets/synthetic/Experiment1/D1", "Experiments/Datasets/synthetic/Experiment1/D2",
					"Experiments/Datasets/synthetic/Experiment1/D3", "Experiments/Datasets/synthetic/Experiment1/D4",
					"Experiments/Datasets/synthetic/Experiment1/D5"),

			List.of("Experiments/Datasets/synthetic/Experiment2/D1", "Experiments/Datasets/synthetic/Experiment2/D2",
					"Experiments/Datasets/synthetic/Experiment2/D3", "Experiments/Datasets/synthetic/Experiment2/D4",
					"Experiments/Datasets/synthetic/Experiment2/D5"),

			List.of("Experiments/Datasets/synthetic/Experiment3/D1", "Experiments/Datasets/synthetic/Experiment3/D2",
					"Experiments/Datasets/synthetic/Experiment3/D3", "Experiments/Datasets/synthetic/Experiment3/D4",
					"Experiments/Datasets/synthetic/Experiment3/D5"),

			List.of("Experiments/Datasets/synthetic/Experiment4/D1", "Experiments/Datasets/synthetic/Experiment4/D2",
					"Experiments/Datasets/synthetic/Experiment4/D3", "Experiments/Datasets/synthetic/Experiment4/D4",
					"Experiments/Datasets/synthetic/Experiment4/D5"),

			List.of("Experiments/Datasets/synthetic/Experiment5/D1", "Experiments/Datasets/synthetic/Experiment5/D2",
					"Experiments/Datasets/synthetic/Experiment5/D3", "Experiments/Datasets/synthetic/Experiment5/D4",
					"Experiments/Datasets/synthetic/Experiment5/D5"),

			List.of("Experiments/Datasets/synthetic/Experiment6/D1", "Experiments/Datasets/synthetic/Experiment6/D2",
					"Experiments/Datasets/synthetic/Experiment6/D3", "Experiments/Datasets/synthetic/Experiment6/D4",
					"Experiments/Datasets/synthetic/Experiment6/D5"),

			List.of("Experiments/Datasets/synthetic/Experiment7/D1", "Experiments/Datasets/synthetic/Experiment7/D2",
					"Experiments/Datasets/synthetic/Experiment7/D3", "Experiments/Datasets/synthetic/Experiment7/D4",
					"Experiments/Datasets/synthetic/Experiment7/D5"),

			List.of("Experiments/Datasets/synthetic/Experiment8/D1", "Experiments/Datasets/synthetic/Experiment8/D2",
					"Experiments/Datasets/synthetic/Experiment8/D3", "Experiments/Datasets/synthetic/Experiment8/D4",
					"Experiments/Datasets/synthetic/Experiment8/D5"),

			List.of("Experiments/Datasets/synthetic/Experiment9/D1", "Experiments/Datasets/synthetic/Experiment9/D2",
					"Experiments/Datasets/synthetic/Experiment9/D3", "Experiments/Datasets/synthetic/Experiment9/D4",
					"Experiments/Datasets/synthetic/Experiment9/D5"),

			List.of("Experiments/Datasets/synthetic/Experiment10/D1", "Experiments/Datasets/synthetic/Experiment10/D2",
					"Experiments/Datasets/synthetic/Experiment10/D3", "Experiments/Datasets/synthetic/Experiment10/D4",
					"Experiments/Datasets/synthetic/Experiment10/D5"));

	static String nameTimeVariable = "t";
	static List<String> nameClassVariables = List.of("C1", "C2", "C3", "C4", "C5");
	static List<String> nameFeatureVariables = List.of("X1", "X2", "X3", "X4", "X5");
	// ---------------------------- Synthetic dataset ----------------------------

	// ---------------------------- Energy dataset ----------------------------
//	// Paths to datasets
//	static List<List<String>> datasetsAll = List.of(List.of("Experiments/Datasets/energy"));
//	// Time, class and feature variables
//	static String nameTimeVariable = "timestamp";
//	static List<String> nameClassVariables = List.of("M1", "M5", "M2", "M6", "M3", "M4");
//	static List<String> nameFeatureVariables = List.of("IA", "IB", "IC", "VA", "VB", "VC", "SA", "SB", "SC", "PA", "PB",
//			"PC", "QA", "QB", "QC");
	// ---------------------------- Energy dataset ----------------------------

	// ---------------------------- Experiment settings ----------------------------
	// Models to compare: "MCTBNC", "DAG-maxK MCTBNC", "Empty-digraph MCTBNC",
	// "Empty-maxK MCTBNC", "MCTNBC", "CTBNCs", "maxK CTBNCs"
	static List<String> models = List.of("maxK CTBNCs", "DAG-maxK MCTBNC");
	// Maximum number of feature parents (if maxK is used)
	static String maxK = "1";

	// Class subgraph
	// Parameter learning: "Bayesian estimation", "Maximum likelihood estimation"
	static String nameBnPLA = "Bayesian estimation";
	// Structure learning: "Hill climbing", "Random-restart hill climbing"
	static String nameBnSLA = "Hill climbing";
	// Hyperparameters (if Bayesian estimation is used)
	static double nx = 1;

	// Bridge and feature subgraphs
	static String nameCtbnPLA = "Bayesian estimation";
	static String nameCtbnSLA = "Hill climbing";
	// Hyperparameters (if Bayesian estimation is used)
	static double mxy = 1;
	static double tx = 0.001;

	// Initial structure: "Empty", "Naive Bayes"
	static String initialStructure = "Empty";

	// Score function: "Log-likelihood", "Bayesian Dirichlet equivalent",
	// "Conditional log-likelihood"
	static List<String> scoreFunctions = List.of("Log-likelihood");
	// Penalization (except for "Bayesian Dirichlet equivalent"): "BIC", "AIC", "No"
	static String penalizationFunction = "BIC";

	// Evaluation method: "Cross-validation", "Hold-out validation"
	static String selectedValidationMethod = "Cross-validation";
	static int folds = 5; // For "Cross-validation"
	static int trainingSize = 0; // For "Hold-out validation"
	static boolean shuffleSequences = true;
	// ---------------------------- Experiment settings ----------------------------

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
		// Iterate over experiments
		for (List<String> datasets : datasetsAll) {
			// Define output to store the results of the experiments
			metricsWriter = new ExcelExperimentsWriter(scoreFunctions, datasets, models, nameClassVariables,
					nameFeatureVariables, bnPLA, ctbnPLA, penalizationFunction, initialStructure);
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
						datasetReader.setVariables(nameTimeVariable, nameClassVariables, nameFeatureVariables);
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
		StructureLearningAlgorithm bnSLA = StructureLearningAlgorithmFactory.getAlgorithmBN(nameBnSLA, scoreFunction,
				penalizationFunction);
		StructureLearningAlgorithm ctbnSLA = StructureLearningAlgorithmFactory.getAlgorithmCTBN(nameCtbnSLA,
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
}
