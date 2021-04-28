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
import com.cig.mctbnc.exceptions.UnreadDatasetException;
import com.cig.mctbnc.models.MCTBNC;
import com.cig.mctbnc.util.Util;

/**
 * Implements cross-validation method.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class CrossValidation extends ValidationMethod {
	Dataset dataset;
	int folds;
	boolean estimateProbabilities;
	boolean shuffle;
	Long seed;
	DatasetReader datasetReader;
	Logger logger = LogManager.getLogger(CrossValidation.class);

	/**
	 * Constructor for cross-validation method.
	 * 
	 * @param datasetReader         dataset reader
	 * @param folds                 number of folds
	 * @param estimateProbabilities determines if the probabilities of the class
	 *                              configurations are estimated
	 * @param shuffle               determines if the sequences should be shuffled
	 * @param seed
	 * @throws UnreadDatasetException
	 */
	public CrossValidation(DatasetReader datasetReader, int folds, boolean estimateProbabilities, boolean shuffle,
			Long seed) throws UnreadDatasetException {
		this.logger.info("Preparing {}-cross validation / Shuffle: {} / Estimate probabilities: {}", folds, shuffle,
				estimateProbabilities);
		this.datasetReader = datasetReader;
		this.folds = folds;
		this.estimateProbabilities = estimateProbabilities;
		this.shuffle = shuffle;
		this.seed = seed;
	}

	/**
	 * Evaluate the performance of the specified model using cross-validation.
	 * 
	 * @param model model to evaluate
	 * @throws UnreadDatasetException
	 */
	@Override
	public void evaluate(MCTBNC<?, ?> model) throws UnreadDatasetException {
		// Read dataset (if it was not done before)
		readDataset();
		// Get sequences from the dataset
		List<Sequence> sequences = this.dataset.getSequences();
		int numSequences = sequences.size();
		if (this.shuffle) {
			// Shuffle the sequences before performing the cross-validation
			Util.shuffle(sequences, this.seed);
			this.logger.info("Sequences shuffled");
		}
		// Obtain size of each fold
		int[] sizeFolds = new int[this.folds];
		Arrays.fill(sizeFolds, numSequences / this.folds);
		// Sequences without fold are added one by one to the first folds
		for (int i = 0; i < numSequences % this.folds; i++)
			sizeFolds[i] += 1;
		// Save performance metrics
		Map<String, Double> resultsCV = new LinkedHashMap<String, Double>();
		// Iterate over each fold
		int fromIndex = 0;
		for (int i = 0; i < this.folds; i++) {
			System.out.println("+++++++++++++++++++++++ Testing on fold " + i + " +++++++++++++++++++++++");
			// Prepare training and testing datasets for current fold
			int toIndex = fromIndex + sizeFolds[i];
			// Prepare training dataset for current fold
			Dataset trainingDataset = extractTrainingDataset(sequences, fromIndex, toIndex);
			// Prepare testing dataset for current fold
			Dataset testingDataset = extractTestingDataset(sequences, fromIndex, toIndex);
			// Train the model
			model.learn(trainingDataset);
			// Make predictions over the current fold
			Prediction[] predictions = model.predict(testingDataset, this.estimateProbabilities);
			// Result of performance metrics when evaluating the model with the current fold
			Map<String, Double> resultsFold = Metrics.evaluate(predictions, testingDataset);
			// Update the final results of the metrics after seeing all the folds
			resultsFold.forEach((metric, value) -> resultsCV.merge(metric, value, (a, b) -> a + b));
			// Display results fold
			System.out.println(MessageFormat.format("--------------------Results fold {0}--------------------", i));
			displayResultsFold(resultsFold, model);
			System.out.println("------------------------------------------------------");
			fromIndex += sizeFolds[i];
		}
		// The average of each metric is computed
		resultsCV.forEach((metric, value) -> resultsCV.put(metric, value / this.folds));
		// Display results
		System.out.println("--------------------Results cross-validation--------------------");
		displayResults(resultsCV);
		System.out.println("----------------------------------------------------------------");
	}

	/**
	 * Read dataset.
	 * 
	 * @throws UnreadDatasetException
	 */
	private void readDataset() throws UnreadDatasetException {
		this.dataset = this.datasetReader.readDataset();
		// Check that the specified number of folds is valid
		if (this.folds < 2 || this.folds > this.dataset.getNumDataPoints()) {
			this.logger.warn("Number of folds must be between 2 and the dataset size (2 folds will be used)");
			this.folds = 2;
		}
		this.logger.info("Time variable: {}", this.dataset.getNameTimeVariable());
		this.logger.info("Features: {}", this.dataset.getNameFeatures());
		this.logger.info("Class variables: {}", (this.dataset.getNameClassVariables()));
	}

	/**
	 * Given all the sequences of a dataset, create a training dataset that include
	 * all the sequences but those between some specified indexes.
	 * 
	 * @param sequences
	 * @param fromIndex index of the first sequence to ignore
	 * @param toIndex   index of the last sequence to ignore
	 * @return training dataset
	 */
	private Dataset extractTrainingDataset(List<Sequence> sequences, int fromIndex, int toIndex) {
		List<Sequence> trainingSequences = new ArrayList<Sequence>(sequences);
		// Remove instances that will be used for testing
		trainingSequences.subList(fromIndex, toIndex).clear();
		return new Dataset(trainingSequences);
	}

	/**
	 * Given all the sequences of a dataset, create a testing dataset using the
	 * sequences between some specified indexes.
	 * 
	 * @param sequences
	 * @param fromIndex index of the first sequence of the extracted dataset
	 * @param toIndex   index of the last sequence of the extracted dataset
	 * @return testing dataset
	 */
	private Dataset extractTestingDataset(List<Sequence> sequences, int fromIndex, int toIndex) {
		List<Sequence> testingSequences = sequences.subList(fromIndex, toIndex);
		return new Dataset(testingSequences);
	}

	/**
	 * Display the results of a fold.
	 * 
	 * @param results
	 */
	private void displayResultsFold(Map<String, Double> results, MCTBNC<?, ?> model) {
		results.forEach((metric, value) -> System.out.println(metric + " = " + value));
		System.out.println(model);
	}

}
