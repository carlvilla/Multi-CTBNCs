package com.cig.mctbnc.performance;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cig.mctbnc.classification.Prediction;
import com.cig.mctbnc.data.reader.DatasetReader;
import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.data.representation.Sequence;
import com.cig.mctbnc.data.representation.State;
import com.cig.mctbnc.exceptions.UnreadDatasetException;
import com.cig.mctbnc.models.MCTBNC;
import com.cig.mctbnc.util.Util;

/**
 * Implements cross-validation method that learn one model for each class
 * variable and merge the results.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class CrossValidationSeveralModels extends ValidationMethod {
	Dataset dataset;
	int folds;
	boolean shuffle;
	Logger logger = LogManager.getLogger(CrossValidation.class);

	public CrossValidationSeveralModels(DatasetReader datasetReader, int folds, boolean shuffle) throws UnreadDatasetException {
		super();
		logger.info("Preparing {}-cross validation / Shuffle: {}", folds, shuffle);
		// Obtain dataset and the number of sequence it contains
		dataset = datasetReader.readDataset();
		logger.info("Time variable: {}", dataset.getNameTimeVariable());
		logger.info("Features: {}", dataset.getNameFeatures());
		logger.info("Class variables: {}", (dataset.getNameClassVariables()));
		// Check that the specified number of folds is valid
		if (folds < 2 || folds > dataset.getNumDataPoints())
			logger.warn("Number of folds must be between 2 and the dataset size (leave-one-out cross validation)");
		// Set number of folds
		this.folds = folds;
		// Set if the sequences should be shuffled
		this.shuffle = shuffle;
	}

	/**
	 * Evaluate the performance of the specified model using cross-validation.
	 * 
	 * @param model model to evaluate
	 */
	public void evaluate(MCTBNC<?, ?> model) {
		// Get sequences from the dataset
		List<Sequence> sequences = dataset.getSequences();
		int numSequences = sequences.size();
		// Obtain files from which the dataset was read
		List<String> fileNames = new ArrayList<String>(dataset.getNameFiles());
		if (shuffle) {
			// Shuffle the sequences before performing cross-validation
			Integer seed = 10;
			Util.shuffle(sequences, seed);
			Util.shuffle(fileNames, seed);
			logger.info("Sequences shuffled");
		}
		// Obtain size of each fold
		int[] sizeFolds = new int[folds];
		Arrays.fill(sizeFolds, numSequences / folds);
		// Sequences without fold are added one by one to the first folds
		for (int i = 0; i < numSequences % folds; i++)
			sizeFolds[i] += 1;
		// Save evaluation metrics obtained with the cross validation
		Map<String, Double> resultsCrossValidation = new LinkedHashMap<String, Double>();
		// Iterate over each fold
		int fromIndex = 0;
		for (int i = 0; i < folds; i++) {
			logger.info("Testing on fold {}", i);
			Prediction[] predictionsFold = null;
			// Prepare training and testing datasets for current fold
			int toIndex = fromIndex + sizeFolds[i];
			// Prepare training dataset for current fold
			Dataset trainingDataset = extractTrainingDataset(sequences, fileNames, fromIndex, toIndex);
			// Prepare testing dataset for current fold
			Dataset testingDataset = extractTestingDataset(sequences, fileNames, fromIndex, toIndex);
			// Train and predict considering only one class variable
			for (String classVariable : dataset.getNameClassVariables()) {
				List<String> nameClassVariables = new ArrayList<String>(dataset.getNameClassVariables());
				nameClassVariables.remove(classVariable);
				trainingDataset.setIgnoredClassVariables(nameClassVariables);
				// Train the model
				model.learn(trainingDataset);
				// Make predictions over the current fold and class variable
				Prediction[] predictionsCV = model.predict(testingDataset, true);
				// Display learned model for current class variable
				System.out.println(MessageFormat
						.format("--------------------Model for class variable {0}--------------------", classVariable));
				displayModel(model);
				System.out.println("------------------------------------------------------");
				// Merge predictions for each class variable
				predictionsFold = updatePredictionsFold(predictionsFold, classVariable, predictionsCV);
			}
			// Result of performance metrics when evaluating the model with the current fold
			Map<String, Double> resultsFold = Metrics.evaluate(predictionsFold, testingDataset);
			// Update the final results of the metrics after seeing all the folds
			resultsFold.forEach((metric, value) -> resultsCrossValidation.merge(metric, value, (a, b) -> a + b));
			// Display results fold
			System.out.println(MessageFormat.format("--------------------Results fold {0}--------------------", i));
			displayResultsFold(resultsFold);
			System.out.println("------------------------------------------------------");
			fromIndex += sizeFolds[i];
		}
		// The average of each metric is computed
		resultsCrossValidation.forEach((metric, value) -> resultsCrossValidation.put(metric, value / folds));
		// Display results
		System.out.println("--------------------Results cross-validation--------------------");
		displayResults(resultsCrossValidation);
		System.out.println("----------------------------------------------------------------");
	}

	/**
	 * Update the current predictions of a fold with the predictions for a new class
	 * variable.
	 * 
	 * @param predictionsFold current predictions of the fold
	 * @param nameCV          name of the class variable whose predictions are
	 *                        included in the predictions of the fold
	 * @param predictionsCV   predictions of the class variable
	 * @return
	 */
	private Prediction[] updatePredictionsFold(Prediction[] predictionsFold, String nameCV,
			Prediction[] predictionsCV) {
		if (predictionsFold == null) {
			// First class is predicted
			predictionsFold = predictionsCV;
		} else {
			// Update the predicted class configuration for each sequence and the
			// probabilities given to each class configuration (necessary for Brier score)
			for (int k = 0; k < predictionsFold.length; k++) {
				// Get prediction current class variable and sequence
				State prediction = predictionsCV[k].getPredictedClasses();
				// Update current class configuration (CC) of the sequence
				State predictedCC = predictionsFold[k].getPredictedClasses();
				predictedCC.addEvent(nameCV, prediction.getValueVariable(nameCV));
				// Define probabilities of each class configuration for the current sequence
				Map<State, Double> newProbabilitiesCCs = new HashMap<State, Double>();
				// Iterate over class configurations including states current class variable
				Set<State> statesCC = predictionsFold[k].probabilities.keySet();
				Set<State> statesCV = predictionsCV[k].probabilities.keySet();
				for (State stateCC : statesCC)
					for (State classCV : statesCV) {
						// Get new class configuration that includes state of the class variable
						State newStateCC = new State(stateCC.getEvents());
						newStateCC.addEvents(classCV.getEvents());
						// Get probability new class configuration
						double previousProbCC = predictionsFold[k].probabilities.get(stateCC);
						double newProbCC = previousProbCC * predictionsCV[k].probabilities.get(classCV);
						// Save new class configuration and its probability
						newProbabilitiesCCs.put(newStateCC, newProbCC);
					}
				// Save probabilities class configurations for the sequence
				predictionsFold[k].probabilities = newProbabilitiesCCs;
				// Get probability predicted class configuration for the sequence
				double probabilityPredictedCC = predictionsFold[k].getProbabilityPrediction();
				// Get probability predicted class for the current class variable
				double probabilityPredictedClass = predictionsCV[k].getProbabilityPrediction();
				// Save new probability predicted class configuration
				double probabilityNewPredictedCC = probabilityPredictedCC * probabilityPredictedClass;
				predictionsFold[k].setProbabilityPrediction(probabilityNewPredictedCC);
			}
		}
		return predictionsFold;
	}

	/**
	 * Given all the sequences of a dataset and the names of the files from where
	 * they were extracted, create a training dataset that include all the sequences
	 * but those between some specified indexes.
	 * 
	 * @param sequences
	 * @param fileNames
	 * @param fromIndex index of the first sequence to ignore
	 * @param toIndex   index of the last sequence to ignore
	 * @return
	 */
	private Dataset extractTrainingDataset(List<Sequence> sequences, List<String> fileNames, int fromIndex,
			int toIndex) {
		List<Sequence> trainingSequences = new ArrayList<Sequence>(sequences);
		// Remove instances that will be used for testing
		trainingSequences.subList(fromIndex, toIndex).clear();
		Dataset trainingDataset = new Dataset(trainingSequences);
		// Get name of the files from which the training sequences were extracted
		// A new list is created to avoid concurrent modification exceptions
		List<String> fileNamesAux = new ArrayList<String>(fileNames);
		// Remove filenames of sequences that will be used for testing
		fileNamesAux.subList(fromIndex, toIndex).clear();
		trainingDataset.setNameFiles(fileNamesAux);
		return trainingDataset;
	}

	/**
	 * Given all the sequences of a dataset and the names of the files from where
	 * they were extracted, create a testing dataset using the sequences between
	 * some specified indexes.
	 * 
	 * @param sequences
	 * @param fileNames
	 * @param fromIndex index of the first sequence of the extracted dataset
	 * @param toIndex   index of the last sequence of the extracted dataset
	 * @return
	 */
	private Dataset extractTestingDataset(List<Sequence> sequences, List<String> fileNames, int fromIndex,
			int toIndex) {
		List<Sequence> testingSequences = sequences.subList(fromIndex, toIndex);
		Dataset testingDataset = new Dataset(testingSequences);
		// Get name of the files from which the testing sequences were extracted
		testingDataset.setNameFiles(fileNames.subList(fromIndex, toIndex));
		return testingDataset;
	}

	/**
	 * Display the results of a fold.
	 * 
	 * @param results
	 */
	private void displayResultsFold(Map<String, Double> results) {
		results.forEach((metric, value) -> System.out.println(metric + " = " + value));
	}

}
