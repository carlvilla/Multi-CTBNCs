package com.cig.mctbnc.performance;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cig.mctbnc.classification.Prediction;
import com.cig.mctbnc.data.reader.DatasetReader;
import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.data.representation.Sequence;
import com.cig.mctbnc.data.representation.State;
import com.cig.mctbnc.models.MCTBNC;
import com.cig.mctbnc.util.Util;

/**
 * Implements cross-validation method that learn one model for each class
 * variable and merge the results.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class CrossValidationSeveralModels implements ValidationMethod {
	Dataset dataset;
	int folds;
	boolean shuffle;
	Logger logger = LogManager.getLogger(CrossValidation.class);

	public CrossValidationSeveralModels(DatasetReader datasetReader, int folds, boolean shuffle) {
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
		// Save performance metrics
		Map<String, Double> resultsCV = new LinkedHashMap<String, Double>();

		// Iterate over each fold
		int fromIndex = 0;
		// TODO PARALELLIZE
		for (int i = 0; i < folds; i++) {
			logger.info("Testing on fold {}", i);

			Prediction[] predictionsFold = null;

			// Prepare training and testing datasets for current fold
			int toIndex = fromIndex + sizeFolds[i];
			// Prepare training dataset for current fold
			Dataset trainingDataset = extractTrainingDataset(sequences, fileNames, fromIndex, toIndex);
			// Prepare testing dataset for current fold
			Dataset testingDataset = extractTestingDataset(sequences, fileNames, fromIndex, toIndex);

			for (String classVariable : dataset.getNameClassVariables()) {

				// Model only consider one class variable

				List<String> nameClassVariables = new ArrayList<String>(dataset.getNameClassVariables());
				nameClassVariables.remove(classVariable);

				trainingDataset.setIgnoredClassVariables(nameClassVariables);

				// Train the model
				model.learn(trainingDataset);
				// model.display();
				// Make predictions over the current fold
				Prediction[] predictions = model.predict(testingDataset, true);

				// Merge predictions
				if (predictionsFold == null) {
					predictionsFold = predictions;
					

					
				} else {
					for (int k = 0; k < predictionsFold.length; k++) {
						State prediction = predictions[k].getPredictedClasses();
						predictionsFold[k].getPredictedClasses().addEvent(classVariable,
								prediction.getValueVariable(classVariable));
						predictionsFold[k].probabilities = null;
					}
				}

			}

			// Result of performance metrics when evaluating the model with the current fold
			Map<String, Double> resultsFold = Metrics.evaluate(predictionsFold, testingDataset);
			// Update the final results of the metrics after seeing all the folds
			resultsFold.forEach((metric, value) -> resultsCV.merge(metric, value, (a, b) -> a + b));
			// Display results fold
			System.out.println(MessageFormat.format("--------------------Results fold {0}--------------------", i));
			displayResults(resultsFold);
			System.out.println("------------------------------------------------------");
			fromIndex += sizeFolds[i];

		}

		// The average of each metric is computed
		resultsCV.forEach((metric, value) -> resultsCV.put(metric, value / folds));
		// Display results
		System.out.println("--------------------Results cross-validation--------------------");
		displayResults(resultsCV);
		System.out.println("----------------------------------------------------------------");
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
	 * Display the results of the cross validation.
	 * 
	 * @param results
	 */
	private void displayResults(Map<String, Double> results) {
		results.forEach((metric, value) -> System.out.println(metric + " = " + value));
	}

}
