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
import com.cig.mctbnc.performance.CrossValidationSeveralModels;
import com.cig.mctbnc.performance.ValidationMethod;
import com.cig.mctbnc.performance.ValidationMethodFactory;
import com.cig.mctbnc.performance.writers.ExcelExperimentsWriter;
import com.cig.mctbnc.performance.writers.MetricsWriter;

/**
 * Class used to perform experiments.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class MainExperiments {

//	static List<String> datasets = List.of("src/main/resources/datasets/Experiment12/extreme/D1",
//			"src/main/resources/datasets/Experiment12/extreme/D2",
//			"src/main/resources/datasets/Experiment12/extreme/D3",
//			"src/main/resources/datasets/Experiment12/extreme/D4",
//			"src/main/resources/datasets/Experiment12/extreme/D5",
//			"src/main/resources/datasets/Experiment12/noExtreme/D1",
//			"src/main/resources/datasets/Experiment12/noExtreme/D2",
//			"src/main/resources/datasets/Experiment12/noExtreme/D3",
//			"src/main/resources/datasets/Experiment12/noExtreme/D4",
//			"src/main/resources/datasets/Experiment12/noExtreme/D5");

	static List<String> datasets = List.of("/Users/carlosvillablanco/Desktop/Datasets/prueba/D1");

	static String nameTimeVariable = "t";

	static List<String> nameClassVariables = List.of("CV1", "CV2", "CV3", "CV4", "CV5");
	static List<String> nameFeatureVariables = List.of("X1", "X2", "X3", "X4", "X5");

	static List<String> models = List.of("CTBNCs", "Empty-kDB MCTBNC", "MCTBNC"); // "MCTBNC", , "MCTBNC",

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
	static List<String> scoreFunctions = List.of("Log-likelihood"); // "Conditional log-likelihood", , "Bayesian score"
	// Define penalization function (if any)
	static String penalizationFunction = "BIC"; // "AIC", "No"

	static String maxK = "6";

	static String initialStructure = "Empty";

	static String selectedValidationMethod = "Cross-validation";
	static int folds = 5;
	static boolean shuffleSequences = true;

	static int trainingSize = 0;

	static MetricsWriter metricsWriter;

	/**
	 * Class use to perform experiments.
	 * 
	 * @param args
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args) throws FileNotFoundException {
		// Define parameter learning algorithms
		BNParameterLearningAlgorithm bnPLA = BNParameterLearningAlgorithmFactory.getAlgorithm(nameBnPLA, alpha);
		CTBNParameterLearningAlgorithm ctbnPLA = CTBNParameterLearningAlgorithmFactory.getAlgorithm(nameCtbnPLA, nxy,
				tx);

		// Define output to store the results of the experiments
		metricsWriter = new ExcelExperimentsWriter(scoreFunctions, datasets, models, nameFeatureVariables,
				nameClassVariables, bnPLA, ctbnPLA, penalizationFunction);

		for (String scoreFunction : scoreFunctions) {
			System.out.printf("------------------------------ Score function: %s ------------------------------\n",
					scoreFunction);

			for (String pathDataset : datasets) {
				System.out.printf("############################## DATASET: %s ##############################\n",
						pathDataset);

				DatasetReader datasetReader = new MultipleCSVReader(pathDataset);
				// Set the variables that will be used
				datasetReader.setVariables(nameTimeVariable, nameClassVariables, nameFeatureVariables);

				for (String selectedModel : models) {
					System.out.printf("****************************** MODEL: %s ******************************\n",
							selectedModel);

					// Define structure learning algorithms
					BNStructureLearningAlgorithm bnSLA = BNStructureLearningAlgorihtmFactory.getAlgorithm(nameBnSLA,
							scoreFunction, penalizationFunction);
					CTBNStructureLearningAlgorithm ctbnSLA = CTBNStructureLearningAlgorihtmFactory
							.getAlgorithm(nameCtbnSLA, scoreFunction, penalizationFunction);
					BNLearningAlgorithms bnLearningAlgs = new BNLearningAlgorithms(bnPLA, bnSLA);
					CTBNLearningAlgorithms ctbnLearningAlgs = new CTBNLearningAlgorithms(ctbnPLA, ctbnSLA);

					// Parameters that could be necessary for the generation of the model
					Map<String, String> parameters = new WeakHashMap<String, String>();
					parameters.put("maxK", maxK);

					// Generate selected model and validation method
					MCTBNC<CPTNode, CIMNode> model;
					ValidationMethod validationMethod;

					if (selectedModel.equals("CTBNCs")) {
						model = ClassifierFactory.<CPTNode, CIMNode>getMCTBNC("MCTBNC", bnLearningAlgs,
								ctbnLearningAlgs, parameters, CPTNode.class, CIMNode.class);
						validationMethod = new CrossValidationSeveralModels(datasetReader, folds, shuffleSequences);
					} else {
						model = ClassifierFactory.<CPTNode, CIMNode>getMCTBNC(selectedModel, bnLearningAlgs,
								ctbnLearningAlgs, parameters, CPTNode.class, CIMNode.class);
						validationMethod = ValidationMethodFactory.getValidationMethod(selectedValidationMethod,
								datasetReader, shuffleSequences, trainingSize, folds);
					}

					// Set output to show results
					validationMethod.setWriter(metricsWriter);

					// Define initial structure
					model.setIntialStructure(initialStructure);
					// Evaluate the performance of the model
					validationMethod.evaluate(model);
				}
			}
		}

		metricsWriter.close();

	}

}
