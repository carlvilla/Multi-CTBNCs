package es.upm.fi.cig.multictbnc.performance;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.upm.fi.cig.multictbnc.classification.Prediction;
import es.upm.fi.cig.multictbnc.data.reader.DatasetReader;
import es.upm.fi.cig.multictbnc.data.representation.Dataset;
import es.upm.fi.cig.multictbnc.data.representation.Sequence;
import es.upm.fi.cig.multictbnc.exceptions.UnreadDatasetException;
import es.upm.fi.cig.multictbnc.models.MultiCTBNC;
import es.upm.fi.cig.multictbnc.util.Util;

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
	 * @param datasetReader         a {@code DatasetReader} to read the dataset
	 * @param folds                 number of folds
	 * @param estimateProbabilities true to estimate the probabilities of the class
	 *                              configurations, false otherwise
	 * @param shuffle               true to shuffle the sequences, false otherwise
	 * @param seed                  seed used to shuffle the sequences
	 * @throws UnreadDatasetException if the provided dataset could not be read
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
	 * Evaluates the performance of the specified model using cross-validation.
	 * 
	 * @param model model to evaluate
	 * @throws UnreadDatasetException if the provided dataset could not be read
	 */
	@Override
	public void evaluate(MultiCTBNC<?, ?> model) throws UnreadDatasetException {
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
			this.logger.info("Testing model on fold " + i);
			// Prepare training and testing datasets for current fold
			int toIndex = fromIndex + sizeFolds[i];
			// Prepare training dataset for current fold
			Dataset trainingDataset = extractTrainingDataset(sequences, fromIndex, toIndex);
			// Prepare testing dataset for current fold
			Dataset testingDataset = extractTestingDataset(sequences, fromIndex, toIndex);
			// Warn both the training and testing set about all the possible states the
			// variables can take (categorical variable are assumed for now)
			trainingDataset.setStatesVariables(this.dataset.getStatesVariables());
			testingDataset.setStatesVariables(this.dataset.getStatesVariables());
			// Train the model
			model.learn(trainingDataset);
			// Make predictions over the current fold
			Prediction[] predictions = model.predict(testingDataset, this.estimateProbabilities);
			// Result of performance metrics when evaluating the model with the current fold
			Map<String, Double> resultsFold = Metrics.evaluate(predictions, testingDataset);
			// Update the final results of the metrics after seeing all the folds
			resultsFold.forEach((metric, value) -> resultsCV.merge(metric, value, (a, b) -> a + b));
			// Display results fold
			System.out.println(MessageFormat
					.format("---------------------------------Results fold {0}---------------------------------", i));
			displayResultsFold(resultsFold, model);
			System.out.println("--------------------------------------------------------------------------------");
			fromIndex += sizeFolds[i];
		}
		// The average of each metric is computed
		resultsCV.forEach((metric, value) -> resultsCV.put(metric, value / this.folds));
		// Display results
		System.out.println("----------------------------Results cross-validation----------------------------");
		displayResults(resultsCV);
		System.out.println("--------------------------------------------------------------------------------");
	}

	/**
	 * Reads the dataset.
	 * 
	 * @throws UnreadDatasetException if the provided dataset could not be read
	 */
	private void readDataset() throws UnreadDatasetException {
		this.dataset = this.datasetReader.readDataset();
		// Define all the states of the class variables in the dataset
		this.dataset.initialiazeStatesClassVariables();
		// Check that the specified number of folds is valid
		if (this.folds < 2 || this.folds > this.dataset.getNumDataPoints()) {
			this.logger.warn("Number of folds must be between 2 and the dataset size (2 folds will be used)");
			this.folds = 2;
		}
		this.logger.info("Time variable: {}", this.dataset.getNameTimeVariable());
		this.logger.info("Feature variables: {}", this.dataset.getNameFeatureVariables());
		this.logger.info("Class variables: {}", (this.dataset.getNameClassVariables()));
	}

	/**
	 * Given all the sequences of a dataset, creates a training dataset that include
	 * all the sequences but those between some specified indexes.
	 * 
	 * @param sequences sequences of a dataset
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
	 * Given all the sequences of a dataset, creates a testing dataset using the
	 * sequences between some specified indexes.
	 * 
	 * @param sequences sequences of a dataset
	 * @param fromIndex index of the first sequence of the extracted dataset
	 * @param toIndex   index of the last sequence of the extracted dataset
	 * @return testing dataset
	 */
	private Dataset extractTestingDataset(List<Sequence> sequences, int fromIndex, int toIndex) {
		List<Sequence> testingSequences = sequences.subList(fromIndex, toIndex);
		return new Dataset(testingSequences);
	}

	/**
	 * Displays the results of a fold.
	 * 
	 * @param results a {@code Map} with the results obtained when testing on a fold
	 * @param model   the {@code MultiCTBNC} used for testing
	 */
	private void displayResultsFold(Map<String, Double> results, MultiCTBNC<?, ?> model) {
		results.forEach((metric, value) -> System.out.println(metric + " = " + value));
		System.out.println(model);
	}

}
