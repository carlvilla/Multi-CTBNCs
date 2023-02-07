package es.upm.fi.cig.multictbnc.experiments;

import es.upm.fi.cig.multictbnc.classification.ClassifierFactory;
import es.upm.fi.cig.multictbnc.data.reader.DatasetReader;
import es.upm.fi.cig.multictbnc.data.reader.MultipleCSVReader;
import es.upm.fi.cig.multictbnc.exceptions.ErroneousValueException;
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
import es.upm.fi.cig.multictbnc.performance.CrossValidationBinaryRelevanceMethod;
import es.upm.fi.cig.multictbnc.performance.ValidationMethod;
import es.upm.fi.cig.multictbnc.performance.ValidationMethodFactory;
import es.upm.fi.cig.multictbnc.writers.performance.ExcelExperimentsWriter;
import es.upm.fi.cig.multictbnc.writers.performance.MetricsWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Class used to automate the execution of experiments from the article Villa-Blanco et al. (2021).
 *
 * @author Carlos Villa Blanco
 */
public class MainExperimentsMultiCTBNC {
	private static final Logger logger = LogManager.getLogger(MainExperimentsMultiCTBNC.class);
	// ---------------------------- Experiment settings ----------------------------
	// Class subgraph
	// Parameter learning: "Bayesian estimation", "Maximum likelihood estimation"
	static String NAMEBNPLA = "Bayesian estimation";
	// Structure learning: "Hill climbing", "Random-restart hill climbing"
	static String NAMEBNSLA = "Hill climbing";
	// Hyperparameters (if Bayesian estimation is used)
	static double NX = 1;
	// Bridge and feature subgraphs
	static String NAMECTBNPLA = "Bayesian estimation";
	static String NAMECTBNSLA = "Hill climbing";
	// Hyperparameters (if Bayesian estimation is used)
	static double MXY = 1;
	static double TX = 0.001;
	// Initial structure: "Empty", "Naive Bayes"
	static String INITIALSTRUCTURE = "Empty";
	// Maximum number of feature parents (if maxK is used)
	static String MAXK = "1";
	// Evaluation method
	static int FOLDS = 5; // For cross-validation
	static boolean ESTIMATEPROBABILITIES = true;
	static boolean SHUFFLESEQUENCES = true;
	static MetricsWriter metricsWriter;

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
		// Retrieve models to compare
		String model1 = args[1];
		String model2 = args[2];
		List<String> models = List.of(model1, model2);
		// Retrieve score function: "Log-likelihood", "Bayesian Dirichlet equivalent",
		// "Conditional log-likelihood"
		String scoreFunction = args[3];
		// Penalisation (except for "Bayesian Dirichlet equivalent"): "BIC", "AIC", "No"
		String penalisationFunction = args[4];
		// Define parameter learning algorithms
		BNParameterLearningAlgorithm bnPLA = BNParameterLearningAlgorithmFactory.getAlgorithm(NAMEBNPLA, NX);
		CTBNParameterLearningAlgorithm ctbnPLA = CTBNParameterLearningAlgorithmFactory.getAlgorithm(NAMECTBNPLA, MXY,
				TX);
		// Hyperparameters that could be necessary for the generation of the models
		Map<String, String> hyperparameters = new WeakHashMap<>();
		hyperparameters.put("maxK", MAXK);
		// Iterate over experiments
		for (List<String> datasets : datasetsAll) {
			// Define output to store the results of the experiments
			String nameExperiment = getNameExperiment(datasets);
			String filename = nameExperiment + "_" + models.get(0).replaceAll("\\s+", "_") + "_" +
					models.get(1).replaceAll("\\s+", "_") + "_" + scoreFunction.replaceAll("\\s+", "_");
			metricsWriter = new ExcelExperimentsWriter(List.of(scoreFunction), datasets, models, nameClassVariables,
					nameFeatureVariables, bnPLA, ctbnPLA, penalisationFunction, INITIALSTRUCTURE, seeds, filename);
			// Iterate over different permutations of the same dataset
			for (long seed : seeds) {
				System.out.printf("------------------------------ Score function: %s ------------------------------\n",
						scoreFunction);
				Map<String, String> paramSLA = Map.of("scoreFunction", scoreFunction, "penalisationFunction",
						penalisationFunction);
				// Iterate over the datasets that are evaluated
				for (String pathDataset : datasets) {
					if (seeds.size() > 1)
						System.out.printf("############################# DATASET: %s (Shuffling seed: %d) " +
								"#############################\n", pathDataset, seed);
					else
						System.out.printf("############################# DATASET: %s #############################\n",
								pathDataset);
					try {
						DatasetReader datasetReader = new MultipleCSVReader(pathDataset);
						// Set the variables that will be used
						if (nameFeatureVariables != null)
							datasetReader.setVariables(nameTimeVariable, nameClassVariables, nameFeatureVariables);
						else
							datasetReader.setTimeAndClassVariables(nameTimeVariable, nameClassVariables);
						for (String selectedModel : models) {
							System.out.printf(
									"***************************** MODEL: %s " + "*****************************\n",
									selectedModel);
							performExperiment(datasetReader, bnPLA, ctbnPLA, selectedModel, paramSLA, hyperparameters,
									seed);
						}
					} catch (Exception e) {
						logger.error(e.getMessage());
					}
				}
			}
			metricsWriter.close();
		}
	}

	private static List<String> getClassVariables(String selectedExperiment) {
		switch (selectedExperiment) {
			case ("synthetic"):
				return List.of("C1", "C2", "C3", "C4", "C5");
			case ("energy"):
				return List.of("M1", "M2", "M3", "M4", "M5", "M6");
			case ("britishHousehold"):
				return List.of("Smoker", "Employment status", "Sex", "Employed or self-employed in most recent job",
						"Prefers to move house", "Dental check-up", "Responsible adult for child",
						"Lives with spouse or partner", "Limb problems");
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
				return List.of("IA", "IB", "IC", "VA", "VB", "VC", "SA", "SB", "SC", "PA", "PB", "PC", "QA", "QB",
						"QC");
			case ("britishHousehold"):
				return null;
			default:
				System.err.println("Selected experiment was not found");
				return null;
		}
	}

	private static String getNameExperiment(List<String> datasets) {
		if (datasets.size() > 1)
			return Paths.get(datasets.get(0)).getParent().getFileName().toString();
		return Paths.get(datasets.get(0)).getFileName().toString();
	}

	private static List<List<String>> getPathDatasets(String selectedExperiment) {
		switch (selectedExperiment) {
			case ("synthetic"):
				return List.of(List.of("datasets/VillaBlancoEtAl2021/synthetic/Experiment1/dataset0",
								"datasets/VillaBlancoEtAl2021/synthetic/Experiment1/dataset1",
								"datasets/VillaBlancoEtAl2021/synthetic/Experiment1/dataset2",
								"datasets/VillaBlancoEtAl2021/synthetic/Experiment1/dataset3",
								"datasets/VillaBlancoEtAl2021/synthetic/Experiment1/dataset4"),
						List.of("datasets/VillaBlancoEtAl2021/synthetic/Experiment2/dataset0",
								"datasets/VillaBlancoEtAl2021/synthetic/Experiment2/dataset1",
								"datasets/VillaBlancoEtAl2021/synthetic/Experiment2/dataset2",
								"datasets/VillaBlancoEtAl2021/synthetic/Experiment2/dataset3",
								"datasets/VillaBlancoEtAl2021/synthetic/Experiment2/dataset4"),
						List.of("datasets/VillaBlancoEtAl2021/synthetic/Experiment3/dataset0",
								"datasets/VillaBlancoEtAl2021/synthetic/Experiment3/dataset1",
								"datasets/VillaBlancoEtAl2021/synthetic/Experiment3/dataset2",
								"datasets/VillaBlancoEtAl2021/synthetic/Experiment3/dataset3",
								"datasets/VillaBlancoEtAl2021/synthetic/Experiment3/dataset4"),
						List.of("datasets/VillaBlancoEtAl2021/synthetic/Experiment4/dataset0",
								"datasets/VillaBlancoEtAl2021/synthetic/Experiment4/dataset1",
								"datasets/VillaBlancoEtAl2021/synthetic/Experiment4/dataset2",
								"datasets/VillaBlancoEtAl2021/synthetic/Experiment4/dataset3",
								"datasets/VillaBlancoEtAl2021/synthetic/Experiment4/dataset4"),
						List.of("datasets/VillaBlancoEtAl2021/synthetic/Experiment5/dataset0",
								"datasets/VillaBlancoEtAl2021/synthetic/Experiment5/dataset1",
								"datasets/VillaBlancoEtAl2021/synthetic/Experiment5/dataset2",
								"datasets/VillaBlancoEtAl2021/synthetic/Experiment5/dataset3",
								"datasets/VillaBlancoEtAl2021/synthetic/Experiment5/dataset4"),
						List.of("datasets/VillaBlancoEtAl2021/synthetic/Experiment6/dataset0",
								"datasets/VillaBlancoEtAl2021/synthetic/Experiment6/dataset1",
								"datasets/VillaBlancoEtAl2021/synthetic/Experiment6/dataset2",
								"datasets/VillaBlancoEtAl2021/synthetic/Experiment6/dataset3",
								"datasets/VillaBlancoEtAl2021/synthetic/Experiment6/dataset4"),
						List.of("datasets/VillaBlancoEtAl2021/synthetic/Experiment7/dataset0",
								"datasets/VillaBlancoEtAl2021/synthetic/Experiment7/dataset1",
								"datasets/VillaBlancoEtAl2021/synthetic/Experiment7/dataset2",
								"datasets/VillaBlancoEtAl2021/synthetic/Experiment7/dataset3",
								"datasets/VillaBlancoEtAl2021/synthetic/Experiment7/dataset4"),
						List.of("datasets/VillaBlancoEtAl2021/synthetic/Experiment8/dataset0",
								"datasets/VillaBlancoEtAl2021/synthetic/Experiment8/dataset1",
								"datasets/VillaBlancoEtAl2021/synthetic/Experiment8/dataset2",
								"datasets/VillaBlancoEtAl2021/synthetic/Experiment8/dataset3",
								"datasets/VillaBlancoEtAl2021/synthetic/Experiment8/dataset4"),
						List.of("datasets/VillaBlancoEtAl2021/synthetic/Experiment9/dataset0",
								"datasets/VillaBlancoEtAl2021/synthetic/Experiment9/dataset1",
								"datasets/VillaBlancoEtAl2021/synthetic/Experiment9/dataset2",
								"datasets/VillaBlancoEtAl2021/synthetic/Experiment9/dataset3",
								"datasets/VillaBlancoEtAl2021/synthetic/Experiment9/dataset4"),
						List.of("datasets/VillaBlancoEtAl2021/synthetic/Experiment10/dataset0",
								"datasets/VillaBlancoEtAl2021/synthetic/Experiment10/dataset1",
								"datasets/VillaBlancoEtAl2021/synthetic/Experiment10/dataset2",
								"datasets/VillaBlancoEtAl2021/synthetic/Experiment10/dataset3",
								"datasets/VillaBlancoEtAl2021/synthetic/Experiment10/dataset4"));
			case ("energy"):
				return List.of(List.of("datasets/VillaBlancoEtAl2021/energy"));
			default:
				System.err.println("Selected experiment was not found");
				return null;
		}
	}

	private static List<Long> getSeeds(String selectedExperiment) {
		switch (selectedExperiment) {
			case ("synthetic"):
			case ("britishHousehold"):
				return List.of(10L);
			case ("energy"):
				return List.of(203901165L, 210776381L, 219721216L, 168929L, 71283273L, 154241767L, 61801568L,
						118950040L, 62100514L, 13014671L, 40044639L, 197151791L, 25959076L, 135446808L, 165931238L);
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
			case ("britishHousehold"):
				return "timestamp";
			default:
				System.err.println("Selected experiment was not found");
				return null;
		}
	}

	private static void performExperiment(DatasetReader datasetReader, BNParameterLearningAlgorithm bnPLA,
										  CTBNParameterLearningAlgorithm ctbnPLA, String selectedModel,
										  Map<String, String> paramSLA, Map<String, String> hyperparameters, long seed)
			throws UnreadDatasetException, ErroneousValueException {
		// Define structure learning algorithms
		StructureLearningAlgorithm bnSLA = StructureLearningAlgorithmFactory.getAlgorithmBN(NAMEBNSLA, paramSLA);
		StructureLearningAlgorithm ctbnSLA = StructureLearningAlgorithmFactory.getAlgorithmCTBN(NAMECTBNSLA, paramSLA);
		BNLearningAlgorithms bnLearningAlgs = new BNLearningAlgorithms(bnPLA, bnSLA);
		CTBNLearningAlgorithms ctbnLearningAlgs = new CTBNLearningAlgorithms(ctbnPLA, ctbnSLA);
		// Generate selected model and validation method
		MultiCTBNC<CPTNode, CIMNode> model;
		ValidationMethod validationMethod;
		// Cross-validation may be performed using a binary relevance or a unique model
		if (selectedModel.equals("CTBNCs")) {
			model = ClassifierFactory.getMultiCTBNC("Multi-CTBNC", bnLearningAlgs, ctbnLearningAlgs, hyperparameters,
					CPTNode.class, CIMNode.class);
			validationMethod = new CrossValidationBinaryRelevanceMethod(datasetReader, FOLDS, ESTIMATEPROBABILITIES,
					SHUFFLESEQUENCES, seed);
		} else if (selectedModel.equals("maxK CTBNCs")) {
			model = ClassifierFactory.getMultiCTBNC("DAG-maxK Multi-CTBNC", bnLearningAlgs, ctbnLearningAlgs,
					hyperparameters, CPTNode.class, CIMNode.class);
			validationMethod = new CrossValidationBinaryRelevanceMethod(datasetReader, FOLDS, ESTIMATEPROBABILITIES,
					SHUFFLESEQUENCES, seed);
		} else {
			model = ClassifierFactory.getMultiCTBNC(selectedModel, bnLearningAlgs, ctbnLearningAlgs, hyperparameters,
					CPTNode.class, CIMNode.class);
			validationMethod = ValidationMethodFactory.getValidationMethod("Cross-validation", datasetReader, null, 0,
					FOLDS, ESTIMATEPROBABILITIES, SHUFFLESEQUENCES, seed);
		}
		// Set output to show results
		validationMethod.setWriter(metricsWriter);
		// Define initial structure
		model.setIntialStructure(INITIALSTRUCTURE);
		// Evaluate the performance of the model
		validationMethod.evaluate(model);
	}

}