package es.upm.fi.cig.multictbnc;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.upm.fi.cig.multictbnc.classification.ClassifierFactory;
import es.upm.fi.cig.multictbnc.data.reader.DatasetReader;
import es.upm.fi.cig.multictbnc.data.reader.MultipleCSVReader;
import es.upm.fi.cig.multictbnc.exceptions.UnreadDatasetException;
import es.upm.fi.cig.multictbnc.learning.BNLearningAlgorithms;
import es.upm.fi.cig.multictbnc.learning.CTBNLearningAlgorithms;
import es.upm.fi.cig.multictbnc.learning.parameters.bn.BNParameterLearningAlgorithm;
import es.upm.fi.cig.multictbnc.learning.parameters.bn.BNParameterLearningAlgorithmFactory;
import es.upm.fi.cig.multictbnc.learning.parameters.ctbn.CTBNParameterLearningAlgorithm;
import es.upm.fi.cig.multictbnc.learning.parameters.ctbn.CTBNParameterLearningAlgorithmFactory;
import es.upm.fi.cig.multictbnc.learning.structure.StructureLearningAlgorithm;
import es.upm.fi.cig.multictbnc.learning.structure.StructureLearningAlgorithmFactory;
import es.upm.fi.cig.multictbnc.models.MultiCTBNC;
import es.upm.fi.cig.multictbnc.nodes.CIMNode;
import es.upm.fi.cig.multictbnc.nodes.CPTNode;
import es.upm.fi.cig.multictbnc.performance.CrossValidationBinaryRelevance;
import es.upm.fi.cig.multictbnc.performance.ValidationMethod;
import es.upm.fi.cig.multictbnc.performance.ValidationMethodFactory;
import es.upm.fi.cig.multictbnc.writers.performance.ExcelExperimentsWriter;
import es.upm.fi.cig.multictbnc.writers.performance.MetricsWriter;

/**
 * Class used to automate the execution of experiments.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class MainExperiments {
	// ---------------------------- Experiment settings ----------------------------
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

	// Maximum number of feature parents (if maxK is used)
	static String maxK = "1";

	// Number of random restarts (if random-restart hill climbing is used)
	static int numRestarts = 5;

	// Evaluation method: "Cross-validation", "Hold-out validation"
	static int folds = 5; // For "Cross-validation"
	static boolean estimateProbabilities = true;
	static boolean shuffleSequences = true;
	// ---------------------------- Experiment settings ----------------------------
	static MetricsWriter metricsWriter;
	static Logger logger = LogManager.getLogger(MainExperiments.class);

	/**
	 * Application entry point.
	 * 
	 * @param args command-line arguments
	 */
	public static void main(String[] args) {
		// Retrieve experiment to perform
		String selectedExperiment = args[0];
		List<List<String>> datasetsAll = getPathDatasets(selectedExperiment);
		String nameTimeVariable = getTimeVariable(selectedExperiment);
		List<String> nameClassVariables = getClassVariables(selectedExperiment);
		List<String> nameFeatureVariables = getFeatureVariables(selectedExperiment);
		List<Long> seeds = getSeeds(selectedExperiment);
		// Retrieve models to compare: "Multi-CTBNC", "DAG-maxK Multi-CTBNC", "Empty-digraph
		// Multi-CTBNC", "Empty-maxK Multi-CTBNC", "Multi-CTNBC", "CTBNCs", "maxK CTBNCs"
		String model1 = args[1];
		String model2 = args[2];
		List<String> models = List.of(model1, model2);
		// Retrieve score function: "Log-likelihood", "Bayesian Dirichlet equivalent",
		// "Conditional log-likelihood"
		List<String> scoreFunctions = List.of(args[3]);
		// Penalization (except for "Bayesian Dirichlet equivalent"): "BIC", "AIC", "No"
		String penalizationFunction = args[4];
		// Define parameter learning algorithms
		BNParameterLearningAlgorithm bnPLA = BNParameterLearningAlgorithmFactory.getAlgorithm(nameBnPLA, nx);
		CTBNParameterLearningAlgorithm ctbnPLA = CTBNParameterLearningAlgorithmFactory.getAlgorithm(nameCtbnPLA, mxy,
				tx);
		// Hyperparameters that could be necessary for the generation of the models
		Map<String, String> hyperparameters = new WeakHashMap<String, String>();
		hyperparameters.put("maxK", maxK);
		// Iterate over experiments
		for (List<String> datasets : datasetsAll) {
			// Define output to store the results of the experiments
			metricsWriter = new ExcelExperimentsWriter(scoreFunctions, datasets, models, nameClassVariables,
					nameFeatureVariables, bnPLA, ctbnPLA, penalizationFunction, initialStructure, seeds);
			// Iterate over different permutations of the same dataset
			for (long seed : seeds) {
				// Iterate over the score functions that are used
				for (String scoreFunction : scoreFunctions) {
					System.out.printf(
							"------------------------------ Score function: %s ------------------------------\n",
							scoreFunction);
					// Iterate over the datasets that are evaluated
					for (String pathDataset : datasets) {
						if (seeds.size() > 1)
							System.out.printf(
									"############################# DATASET: %s (Shuffling seed: %d) #############################\n",
									pathDataset, seed);
						else
							System.out.printf(
									"############################# DATASET: %s #############################\n",
									pathDataset);
						try {
							DatasetReader datasetReader = new MultipleCSVReader(pathDataset);
							// Set the variables that will be used
							datasetReader.setVariables(nameTimeVariable, nameClassVariables, nameFeatureVariables);
							for (String selectedModel : models) {
								System.out.printf(
										"***************************** MODEL: %s *****************************\n",
										selectedModel);
								performExperiment(datasetReader, bnPLA, ctbnPLA, selectedModel, scoreFunction,
										penalizationFunction, hyperparameters, seed);
							}
						} catch (FileNotFoundException e) {
							logger.error(e.getMessage());
						} catch (UnreadDatasetException e) {
							logger.error(e.getMessage());
						}
					}
				}
			}
			metricsWriter.close();
		}
	}

	private static void performExperiment(DatasetReader datasetReader, BNParameterLearningAlgorithm bnPLA,
			CTBNParameterLearningAlgorithm ctbnPLA, String selectedModel, String scoreFunction,
			String penalizationFunction, Map<String, String> hyperparameters, long seed) throws UnreadDatasetException {
		// Define structure learning algorithms
		StructureLearningAlgorithm bnSLA = StructureLearningAlgorithmFactory.getAlgorithmBN(nameBnSLA, scoreFunction,
				penalizationFunction, numRestarts);
		StructureLearningAlgorithm ctbnSLA = StructureLearningAlgorithmFactory.getAlgorithmCTBN(nameCtbnSLA,
				scoreFunction, penalizationFunction, numRestarts);
		BNLearningAlgorithms bnLearningAlgs = new BNLearningAlgorithms(bnPLA, bnSLA);
		CTBNLearningAlgorithms ctbnLearningAlgs = new CTBNLearningAlgorithms(ctbnPLA, ctbnSLA);
		// Generate selected model and validation method
		MultiCTBNC<CPTNode, CIMNode> model;
		ValidationMethod validationMethod;
		// Cross validation may be performed using a binary relevance or a unique model
		if (selectedModel.equals("CTBNCs")) {
			model = ClassifierFactory.<CPTNode, CIMNode>getMultiCTBNC("Multi-CTBNC", bnLearningAlgs, ctbnLearningAlgs,
					hyperparameters, CPTNode.class, CIMNode.class);
			validationMethod = new CrossValidationBinaryRelevance(datasetReader, folds, estimateProbabilities,
					shuffleSequences, seed);
		} else if (selectedModel.equals("maxK CTBNCs")) {
			model = ClassifierFactory.<CPTNode, CIMNode>getMultiCTBNC("DAG-maxK Multi-CTBNC", bnLearningAlgs, ctbnLearningAlgs,
					hyperparameters, CPTNode.class, CIMNode.class);
			validationMethod = new CrossValidationBinaryRelevance(datasetReader, folds, estimateProbabilities,
					shuffleSequences, seed);
		} else {
			model = ClassifierFactory.<CPTNode, CIMNode>getMultiCTBNC(selectedModel, bnLearningAlgs, ctbnLearningAlgs,
					hyperparameters, CPTNode.class, CIMNode.class);
			validationMethod = ValidationMethodFactory.getValidationMethod("Cross-validation", datasetReader, 0, folds,
					estimateProbabilities, shuffleSequences, seed);
		}
		// Set output to show results
		validationMethod.setWriter(metricsWriter);
		// Define initial structure
		model.setIntialStructure(initialStructure);
		// Evaluate the performance of the model
		validationMethod.evaluate(model);
	}

	private static List<List<String>> getPathDatasets(String selectedExperiment) {
		switch (selectedExperiment) {
		case ("synthetic"):
			return List.of(
					List.of("datasets/synthetic/Experiment1/D1", "datasets/synthetic/Experiment1/D2",
							"datasets/synthetic/Experiment1/D3", "datasets/synthetic/Experiment1/D4",
							"datasets/synthetic/Experiment1/D5"),
					List.of("datasets/synthetic/Experiment2/D1", "datasets/synthetic/Experiment2/D2",
							"datasets/synthetic/Experiment2/D3", "datasets/synthetic/Experiment2/D4",
							"datasets/synthetic/Experiment2/D5"),
					List.of("datasets/synthetic/Experiment3/D1", "datasets/synthetic/Experiment3/D2",
							"datasets/synthetic/Experiment3/D3", "datasets/synthetic/Experiment3/D4",
							"datasets/synthetic/Experiment3/D5"),
					List.of("datasets/synthetic/Experiment4/D1", "datasets/synthetic/Experiment4/D2",
							"datasets/synthetic/Experiment4/D3", "datasets/synthetic/Experiment4/D4",
							"datasets/synthetic/Experiment4/D5"),
					List.of("datasets/synthetic/Experiment5/D1", "datasets/synthetic/Experiment5/D2",
							"datasets/synthetic/Experiment5/D3", "datasets/synthetic/Experiment5/D4",
							"datasets/synthetic/Experiment5/D5"),
					List.of("datasets/synthetic/Experiment6/D1", "datasets/synthetic/Experiment6/D2",
							"datasets/synthetic/Experiment6/D3", "datasets/synthetic/Experiment6/D4",
							"datasets/synthetic/Experiment6/D5"),
					List.of("datasets/synthetic/Experiment7/D1", "datasets/synthetic/Experiment7/D2",
							"datasets/synthetic/Experiment7/D3", "datasets/synthetic/Experiment7/D4",
							"datasets/synthetic/Experiment7/D5"),
					List.of("datasets/synthetic/Experiment8/D1", "datasets/synthetic/Experiment8/D2",
							"datasets/synthetic/Experiment8/D3", "datasets/synthetic/Experiment8/D4",
							"datasets/synthetic/Experiment8/D5"),
					List.of("datasets/synthetic/Experiment9/D1", "datasets/synthetic/Experiment9/D2",
							"datasets/synthetic/Experiment9/D3", "datasets/synthetic/Experiment9/D4",
							"datasets/synthetic/Experiment9/D5"),
					List.of("datasets/synthetic/Experiment10/D1", "datasets/synthetic/Experiment10/D2",
							"datasets/synthetic/Experiment10/D3", "datasets/synthetic/Experiment10/D4",
							"datasets/synthetic/Experiment10/D5"));
		case ("energy"):
			return List.of(List.of("datasets/energy"));
		default:
			System.err.println("Selected experiment was not found");
			return null;
		}
	}

	private static String getTimeVariable(String selectedExperiment) {
		switch (selectedExperiment) {
		case ("synthetic"):
			return "t";
		case ("energy"):
			return "timestamp";
		default:
			System.err.println("Selected experiment was not found");
			return null;
		}
	}

	private static List<String> getClassVariables(String selectedExperiment) {
		switch (selectedExperiment) {
		case ("synthetic"):
			return List.of("C1", "C2", "C3", "C4", "C5");
		case ("energy"):
			return List.of("M1", "M2", "M3", "M4", "M5", "M6");
		default:
			System.err.println("Selected experiment was not found");
			return null;
		}
	}

	private static List<String> getFeatureVariables(String selectedExperiment) {
		switch (selectedExperiment) {
		case ("synthetic"):
			return List.of("X1", "X2", "X3", "X4", "X5");
		case ("energy"):
			return List.of("IA", "IB", "IC", "VA", "VB", "VC", "SA", "SB", "SC", "PA", "PB", "PC", "QA", "QB", "QC");
		default:
			System.err.println("Selected experiment was not found");
			return null;
		}
	}

	private static List<Long> getSeeds(String selectedExperiment) {
		switch (selectedExperiment) {
		case ("synthetic"):
			return List.of(10L);
		case ("energy"):
			return List.of(203901165L, 210776381L, 219721216L, 168929L, 71283273L, 154241767L, 61801568L, 118950040L,
					62100514L, 13014671L, 40044639L, 197151791L, 25959076L, 135446808L, 165931238L);
		default:
			System.err.println("Selected experiment was not found");
			return null;
		}
	}

}
