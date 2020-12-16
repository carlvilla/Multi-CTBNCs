package com.cig.mctbnc;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import com.cig.mctbnc.classification.ClassifierFactory;
import com.cig.mctbnc.data.reader.DatasetReader;
import com.cig.mctbnc.data.reader.MultipleCSVReader;
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
import com.cig.mctbnc.performance.CrossValidation;
import com.cig.mctbnc.performance.CrossValidationSeveralModels;
import com.cig.mctbnc.performance.ValidationMethod;
import com.cig.mctbnc.performance.ValidationMethodFactory;

/**
 * Class used to perform experiments.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class MainExperiments {

	static List<String> datasets = List.of("src/main/resources/datasets/Experiment7/extreme/D1",
			"src/main/resources/datasets/Experiment7/extreme/D2", "src/main/resources/datasets/Experiment7/extreme/D3",
			"src/main/resources/datasets/Experiment7/extreme/D4", "src/main/resources/datasets/Experiment7/extreme/D5",
			"src/main/resources/datasets/Experiment7/noExtreme/D1",
			"src/main/resources/datasets/Experiment7/noExtreme/D2",
			"src/main/resources/datasets/Experiment7/noExtreme/D3",
			"src/main/resources/datasets/Experiment7/noExtreme/D4",
			"src/main/resources/datasets/Experiment7/noExtreme/D5");

	static String nameTimeVariable = "t";
	static List<String> nameClassVariables = List.of("CV1", "CV2", "CV3", "CV4");
	static List<String> nameSelectedFeatures = List.of("X1", "X2", "X3", "X4", "X5");

	// static List<String> models = List.of("Empty-kDB MCTBNC", "MCTBNC");
	static List<String> models = List.of("MCTBNC");

	// Get names learning algorithms
	static String nameBnPLA = "Bayesian estimation";
	static String nameBnSLA = "Hill climbing";
	// Get hyperparameters
	static double alpha = 1;

	// Get names learning algorithms
	static String nameCtbnPLA = "Bayesian estimation";
	static String nameCtbnSLA = "Hill climbing";
	// Get hyperparameters
	static double nxy = 1;
	static double tx = 0.001;

	// Get score function
	static List<String> scoreFunctions = List.of("Log-likelihood", "Bayesian score", "Conditional log-likelihood");
	// Define penalization function (if any)
	static String penalizationFunction = "BIC"; // "AIC", "No"

	static String maxK = "6";

	static String initialStructure = "Empty";

	static String selectedValidationMethod = "Cross-validation";
	static int folds = 5;
	static boolean shuffleSequences = true;

	static int trainingSize = 0;

	/**
	 * Class use to perform experiments.
	 * 
	 * @param args
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args) throws FileNotFoundException {

		for (String scoreFunction : scoreFunctions) {
			System.out.printf("------------------------------ Score function: %s ------------------------------\n",
					scoreFunction);

			for (String pathDataset : datasets) {
				System.out.printf("############################## DATASET: %s ##############################\n",
						pathDataset);
				for (String selectedModel : models) {
					System.out.printf("****************************** MODEL: %s ******************************\n",
							selectedModel);
					DatasetReader datasetReader = new MultipleCSVReader(pathDataset);
					// Set the variables that will be used
					datasetReader.setVariables(nameTimeVariable, nameClassVariables, nameSelectedFeatures);

					// Define learning algorithms for the class subgraph (Bayesian network)
					BNParameterLearningAlgorithm bnPLA = BNParameterLearningAlgorithmFactory.getAlgorithm(nameBnPLA,
							alpha);
					BNStructureLearningAlgorithm bnSLA = BNStructureLearningAlgorihtmFactory.getAlgorithm(nameBnSLA,
							scoreFunction, penalizationFunction);
					BNLearningAlgorithms bnLearningAlgs = new BNLearningAlgorithms(bnPLA, bnSLA);

					// Define learning algorithms for the feature and class subgraph (Continuous
					// time Bayesian network)
					CTBNParameterLearningAlgorithm ctbnPLA = CTBNParameterLearningAlgorithmFactory
							.getAlgorithm(nameCtbnPLA, nxy, tx);
					CTBNStructureLearningAlgorithm ctbnSLA = CTBNStructureLearningAlgorihtmFactory
							.getAlgorithm(nameCtbnSLA, scoreFunction, penalizationFunction);
					CTBNLearningAlgorithms ctbnLearningAlgs = new CTBNLearningAlgorithms(ctbnPLA, ctbnSLA);

					// Parameters that could be necessary for the generation of the model
					Map<String, String> parameters = new WeakHashMap<String, String>();
					parameters.put("maxK", maxK);

					// Generate selected model

					MCTBNC<CPTNode, CIMNode> model = ClassifierFactory.<CPTNode, CIMNode>getMCTBNC(selectedModel,
							bnLearningAlgs, ctbnLearningAlgs, parameters, CPTNode.class, CIMNode.class);
					// Define initial structure
					model.setIntialStructure(initialStructure);

					// Define the validation method
					// Get selected validation method

					// Retrieve algorithm of the validation method
					// ValidationMethod validationMethod =
					// ValidationMethodFactory.getValidationMethod(
					// selectedValidationMethod, datasetReader, shuffleSequences, trainingSize,
					// folds);
					ValidationMethod validationMethod = new CrossValidationSeveralModels(datasetReader, folds,
							shuffleSequences);

					// Evaluate the performance of the model
					validationMethod.evaluate(model);
				}
			}
		}
	}

}
