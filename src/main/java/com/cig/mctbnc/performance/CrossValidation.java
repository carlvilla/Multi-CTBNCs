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
import com.cig.mctbnc.models.MCTBNC;
import com.cig.mctbnc.util.Util;

/**
 * Implements cross-validation method.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class CrossValidation implements ValidationMethod {
	Dataset dataset;
	int folds;
	boolean shuffle;
	Logger logger = LogManager.getLogger(CrossValidation.class);

	public CrossValidation(DatasetReader datasetReader, int folds, boolean shuffle) {
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
		if (shuffle) {
			// Shuffle the sequences before performing cross-validation
			Integer seed = 10;
			Util.shuffle(sequences, seed);
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
			// Prepare testing dataset for current fold
			int toIndex = fromIndex + sizeFolds[i];
			List<Sequence> testingSequences = sequences.subList(fromIndex, toIndex);
			Dataset testingDataset = new Dataset(testingSequences);
			// Prepare training dataset for current fold
			List<Sequence> trainingSequences = new ArrayList<Sequence>(sequences);
			trainingSequences.subList(fromIndex, toIndex).clear();
			Dataset trainingDataset = new Dataset(trainingSequences);
			// Train the model
			model.learn(trainingDataset);
			// Make predictions over the current fold
			Prediction[] predictions = model.predict(testingDataset, true);
			// Result of performance metrics when evaluating the model with the current fold
			Map<String, Double> resultsFold = Metrics.evaluate(predictions, testingDataset);
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
	 * Display the results of the cross validation.
	 * 
	 * @param results
	 */
	private void displayResults(Map<String, Double> results) {
		results.forEach((metric, value) -> System.out.println(metric + " = " + value));
	}

}
