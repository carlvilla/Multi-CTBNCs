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
import es.upm.fi.cig.multictbnc.performance.ValidationMethod;
import es.upm.fi.cig.multictbnc.performance.ValidationMethodFactory;
import es.upm.fi.cig.multictbnc.util.Util;
import es.upm.fi.cig.multictbnc.writers.performance.ExcelExperimentsWriter;
import es.upm.fi.cig.multictbnc.writers.performance.MetricsWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class is used to perform the experiments from the articles Villa-Blanco et al. (2022) and Villa-Blanco et al.
 * (2023).
 *
 * @author Carlos Villa Blanco
 */
public class MainExperimentsStructureLearning {

	// ---------------------------- Experiment settings ----------------------------
	static final String NAMEBNPLA = "Bayesian estimation";
	static final double NX = 1;
	static final String NAMECTBNPLA = "Bayesian estimation";
	static final double MXY = 1;
	static final double TX = 0.001;
	static final int FOLDS = 5; // Cross-validation
	static final boolean ESTIMATEPROBABILITIES = true;
	static final boolean SHUFFLESEQUENCES = true;
	static final List<Long> SEEDS = List.of(10L); //10L, 342L, 3523L, 463L, 699L, 676L, 29L, 113L, 698L, 921L
	// The constraint-based algorithm is executed under different significances
	static final String SIGNIFICANCEPC = "0.05";
	static final String SIGTIMETRANSITIONHYPOTHESIS = "0.00001";
	static final String SIGSTATETOSTATETRANSITIONHYPOTHESIS = "0.00001";
	private static final Logger logger = LogManager.getLogger(MainExperimentsStructureLearning.class);
	static String TABULISTSIZE = "5";

	/**
	 * Application entry point. The path to the datasets of the experiments has to be provided. Each folder should
	 * contain the datasets for a single experiment and they are assumed to contain the same variables.
	 *
	 * @param args command-line arguments
	 */
	public static void main(String[] args) {
		if (args == null || args.length == 0) {
			logger.error("No datasets were provided");
			return;
		}
		String pathFolderExperiments = args[0];
		String[] namesExperiments = extractExperimentFolders(pathFolderExperiments);
		for (String nameExperiment : namesExperiments) {
			String pathExperiment = Path.of(pathFolderExperiments, nameExperiment).toString();
			logger.info("Running experiments with datasets from folder: {}", pathExperiment);
			String[] pathDatasets = Util.extractPathExperimentDatasets(pathExperiment);
			if (pathDatasets == null || pathDatasets.length == 0) {
				logger.warn("No datasets were found in the path: {}", pathExperiment);
				return;
			}

			// The name of the time variable is assumed to be "t"
			String nameTimeVariable = "t";
			// Assuming all datasets of an experiment have the same number of variables
			List<String> nameVariables = extractVariables(pathDatasets[0]);
			List<String> nameClassVariables = extractNamesClassVariables(args, nameVariables);
			List<String> nameStructureLearningAlgorithms = extractNamesStructureLearningAlgorithms(args);

			// The rest of the variables are assumed to be feature variables (except the time variable)
			List<String> nameFeatureVariables = extractFeatureVariables(nameVariables, nameTimeVariable,
					nameClassVariables);

			// Define parameter learning algorithms
			BNParameterLearningAlgorithm bnPLA = BNParameterLearningAlgorithmFactory.getAlgorithm(NAMEBNPLA, NX);
			CTBNParameterLearningAlgorithm ctbnPLA = CTBNParameterLearningAlgorithmFactory.getAlgorithm(NAMECTBNPLA,
					MXY, TX);

			List<MultiCTBNC<CPTNode, CIMNode>> modelsToEvaluate = defineModelsToEvaluate(
					nameStructureLearningAlgorithms, bnPLA, ctbnPLA);

			MetricsWriter metricsWriter = new ExcelExperimentsWriter(Arrays.asList(pathDatasets),
					nameStructureLearningAlgorithms, nameClassVariables, nameFeatureVariables, bnPLA, ctbnPLA,
					"Empty (Score-based alg.)", Double.valueOf(SIGNIFICANCEPC),
					Double.valueOf(SIGTIMETRANSITIONHYPOTHESIS), Double.valueOf(SIGSTATETOSTATETRANSITIONHYPOTHESIS),
					SEEDS, nameExperiment);

			performExperiments(pathDatasets, nameTimeVariable, nameClassVariables, nameFeatureVariables,
					modelsToEvaluate, metricsWriter);
			metricsWriter.close();
		}
	}

	private static List<MultiCTBNC<CPTNode, CIMNode>> defineModelsToEvaluate(
			List<String> nameStructureLearningAlgorithms, BNParameterLearningAlgorithm bnPLA,
			CTBNParameterLearningAlgorithm ctbnPLA) {
		List<MultiCTBNC<CPTNode, CIMNode>> modelsToEvaluate = new ArrayList<MultiCTBNC<CPTNode, CIMNode>>();
		for (String nameStructureLearningAlgorithm : nameStructureLearningAlgorithms) {
			MultiCTBNC<CPTNode, CIMNode> model = null;
			switch (nameStructureLearningAlgorithm) {
				case "Hill climbing (BIC)":
					model = getMultiCTBNCLearnedWithHillClimbingBIC(bnPLA, ctbnPLA);
					break;
				case "Tabu search (BIC)":
					model = getMultiCTBNCLearnedWithTabuSearchBIC(bnPLA, ctbnPLA);
					break;
				case "Hill climbing (BDe)":
					model = getMultiCTBNCLearnedWithHillClimbingBDe(bnPLA, ctbnPLA);
					break;
				case "CTPC":
					model = getMultiCTBNCLearnedWithCTPC(bnPLA, ctbnPLA);
					break;
				case "MB-CTPC":
					model = getMultiCTBNCLearnedWithMBCTPC(bnPLA, ctbnPLA);
					break;
				case "Hybrid algorithm [size sep set = 0]":
					model = getMultiCTBNCLearnedWithHybridSep(bnPLA, ctbnPLA, "0");
					break;
				case "Hybrid algorithm [size sep set = 1]":
					model = getMultiCTBNCLearnedWithHybridSep(bnPLA, ctbnPLA, "1");
					break;
				case "Hybrid algorithm [size sep set = 2]":
					model = getMultiCTBNCLearnedWithHybridSep(bnPLA, ctbnPLA, "2");
					break;
			}
			modelsToEvaluate.add(model);
		}
		return modelsToEvaluate;
	}

	private static List<String> extractClassVariables(List<String> nameVariables) {
		// Assuming class variables have names starting with "C"
		return nameVariables.stream().filter(name -> name.charAt(0) == 'C').collect(Collectors.toList());
	}

	private static String[] extractExperimentFolders(String pathFolderExperiments) {
		File file = new File(pathFolderExperiments);
		String[] namesExperiments = file.list(new FilenameFilter() {
			@Override
			public boolean accept(File current, String name) {
				return new File(current, name).isDirectory();
			}
		});
		// Sort the experiments
		Arrays.sort(namesExperiments, new Comparator<String>() {
			@Override
			public int compare(String s1, String s2) {
				return Long.compare(Util.extractFirstLong(s1), Util.extractFirstLong(s2));
			}
		});
		return namesExperiments;
	}

	private static List<String> extractFeatureVariables(List<String> nameVariables, String nameTimeVariable,
														List<String> nameClassVariables) {
		// All variables, except the time and class variables, are assumed to be feature variables
		return nameVariables.stream().filter(
				name -> !name.equals(nameTimeVariable) && !nameClassVariables.contains(name)).collect(
				Collectors.toList());
	}

	private static List<String> extractNamesClassVariables(String[] args, List<String> nameVariables) {
		if (args.length == 3) {
			// The names of the class variables were provided
			return Arrays.stream(args[2].split(";")).collect(Collectors.toList());
		}
		// The name of the class variables is assumed to start by "C"
		return extractClassVariables(nameVariables);
	}

	private static List<String> extractNamesStructureLearningAlgorithms(String[] args) {
		List<String> nameStructureLearningAlgorithms;
		if (args.length > 1) {
			// The names of the algorithms were provided
			return Arrays.stream(args[1].split(";")).collect(Collectors.toList());
		}
		// Common algorithms are employed
		return List.of("Hill climbing (BIC)", "Tabu search (BIC)", "Hill climbing (BDe)", "CTPC", "MB-CTPC",
				"Hybrid algorithm [size sep set = 0]", "Hybrid algorithm [size sep set = 1]");
	}

	private static List<String> extractVariables(String pathDataset) {
		DatasetReader datasetReader;
		try {
			datasetReader = new MultipleCSVReader(pathDataset);
			return datasetReader.getNameVariables();
		} catch (FileNotFoundException | UnreadDatasetException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static MultiCTBNC<CPTNode, CIMNode> generateModel(BNParameterLearningAlgorithm bnPLA,
															  CTBNParameterLearningAlgorithm ctbnPLA,
															  String structureLearningAlgorithm,
															  Map<String, String> paramSLA) {
		StructureLearningAlgorithm bnSLA = StructureLearningAlgorithmFactory.getAlgorithmBN(structureLearningAlgorithm,
				paramSLA);
		StructureLearningAlgorithm ctbnSLA = StructureLearningAlgorithmFactory.getAlgorithmCTBN(
				structureLearningAlgorithm, paramSLA);
		BNLearningAlgorithms bnLearningAlgs = new BNLearningAlgorithms(bnPLA, bnSLA);
		CTBNLearningAlgorithms ctbnLearningAlgs = new CTBNLearningAlgorithms(ctbnPLA, ctbnSLA);
		return ClassifierFactory.getMultiCTBNC("Multi-CTBNC", bnLearningAlgs, ctbnLearningAlgs, null, CPTNode.class,
				CIMNode.class);
	}

	private static MultiCTBNC<CPTNode, CIMNode> getMultiCTBNCLearnedWithCTPC(BNParameterLearningAlgorithm bnPLA,
																			 CTBNParameterLearningAlgorithm ctbnPLA) {
		Map<String, String> paramConstraintBasedSLA = new HashMap<>();
		paramConstraintBasedSLA.put("significancePC", SIGNIFICANCEPC);
		paramConstraintBasedSLA.put("sigTimeTransitionHyp", SIGTIMETRANSITIONHYPOTHESIS);
		paramConstraintBasedSLA.put("sigStateToStateTransitionHyp", SIGSTATETOSTATETRANSITIONHYPOTHESIS);
		return generateModel(bnPLA, ctbnPLA, "CTPC", paramConstraintBasedSLA);
	}

	private static MultiCTBNC<CPTNode, CIMNode> getMultiCTBNCLearnedWithHillClimbingBDe(
			BNParameterLearningAlgorithm bnPLA, CTBNParameterLearningAlgorithm ctbnPLA) {
		Map<String, String> paramScoreBasedSLA = new HashMap<>();
		paramScoreBasedSLA.put("scoreFunction", "Bayesian Dirichlet equivalent");
		paramScoreBasedSLA.put("penalisationFunction", "No");
		return generateModel(bnPLA, ctbnPLA, "Hill climbing", paramScoreBasedSLA);
	}

	private static MultiCTBNC<CPTNode, CIMNode> getMultiCTBNCLearnedWithHillClimbingBIC(
			BNParameterLearningAlgorithm bnPLA, CTBNParameterLearningAlgorithm ctbnPLA) {
		Map<String, String> paramScoreBasedSLA = new HashMap<>();
		paramScoreBasedSLA.put("scoreFunction", "Log-likelihood");
		paramScoreBasedSLA.put("penalisationFunction", "BIC");
		return generateModel(bnPLA, ctbnPLA, "Hill climbing", paramScoreBasedSLA);
	}

	private static MultiCTBNC<CPTNode, CIMNode> getMultiCTBNCLearnedWithHybridSep(BNParameterLearningAlgorithm bnPLA,
																				  CTBNParameterLearningAlgorithm ctbnPLA,
																				  String maxSizeSepSet) {
		Map<String, String> paramHybridSLA = new HashMap<>();
		paramHybridSLA.put("scoreFunction", "Log-likelihood");
		paramHybridSLA.put("penalisationFunction", "BIC");
		paramHybridSLA.put("significancePC", SIGNIFICANCEPC);
		paramHybridSLA.put("sigTimeTransitionHyp", SIGTIMETRANSITIONHYPOTHESIS);
		paramHybridSLA.put("sigStateToStateTransitionHyp", SIGSTATETOSTATETRANSITIONHYPOTHESIS);
		paramHybridSLA.put("maxSizeSepSet", maxSizeSepSet);
		return generateModel(bnPLA, ctbnPLA, "Hybrid algorithm", paramHybridSLA);
	}

	private static MultiCTBNC<CPTNode, CIMNode> getMultiCTBNCLearnedWithMBCTPC(BNParameterLearningAlgorithm bnPLA,
																			   CTBNParameterLearningAlgorithm ctbnPLA) {
		Map<String, String> paramConstraintBasedSLA = new HashMap<>();
		paramConstraintBasedSLA.put("significancePC", SIGNIFICANCEPC);
		paramConstraintBasedSLA.put("sigTimeTransitionHyp", SIGTIMETRANSITIONHYPOTHESIS);
		paramConstraintBasedSLA.put("sigStateToStateTransitionHyp", SIGSTATETOSTATETRANSITIONHYPOTHESIS);
		return generateModel(bnPLA, ctbnPLA, "MB-CTPC", paramConstraintBasedSLA);
	}

	private static MultiCTBNC<CPTNode, CIMNode> getMultiCTBNCLearnedWithTabuSearchBIC(
			BNParameterLearningAlgorithm bnPLA, CTBNParameterLearningAlgorithm ctbnPLA) {
		Map<String, String> paramScoreBasedSLA = new HashMap<>();
		paramScoreBasedSLA.put("scoreFunction", "Log-likelihood");
		paramScoreBasedSLA.put("penalisationFunction", "BIC");
		paramScoreBasedSLA.put("tabuListSize", TABULISTSIZE);
		return generateModel(bnPLA, ctbnPLA, "Tabu search", paramScoreBasedSLA);
	}

	private static void performExperiments(String[] pathDatasets, String nameTimeVariable,
										   List<String> nameClassVariables, List<String> nameFeatureVariables,
										   List<MultiCTBNC<CPTNode, CIMNode>> modelsToEvaluate,
										   MetricsWriter metricsWriter) {
		for (String pathDataset : pathDatasets) {
			logger.info("Cross-validation on dataset: {}", pathDataset);
			for (Long seed : SEEDS) {
				logger.info("Seed used to shuffle the dataset: {}", seed);
				// Read dataset
				DatasetReader datasetReader;
				try {
					datasetReader = new MultipleCSVReader(pathDataset);
					datasetReader.setVariables(nameTimeVariable, nameClassVariables, nameFeatureVariables);
					ValidationMethod validationMethod = ValidationMethodFactory.getValidationMethod("Cross-validation",
							datasetReader, null, 0, FOLDS, ESTIMATEPROBABILITIES, SHUFFLESEQUENCES, seed);
					// Set output to show results
					validationMethod.setWriter(metricsWriter);

					for (MultiCTBNC<CPTNode, CIMNode> model : modelsToEvaluate) {
						logger.info("Evaluating results when learning Multi-CTBNCs using {} ({})...",
								model.getLearningAlgsCTBN().getStructureLearningAlgorithm().getIdentifier(),
								model.getLearningAlgsCTBN().getStructureLearningAlgorithm().getParametersAlgorithm());
						validationMethod.evaluate(model);
					}
				} catch (IOException | UnreadDatasetException | ErroneousValueException exception) {
					logger.error("There was an error performing the cross-validation over the dataset: {}",
							pathDataset + " - " + exception.getMessage());
				}
			}
		}
	}

}