package es.upm.fi.cig.multictbnc.performance;

import es.upm.fi.cig.multictbnc.classification.Prediction;
import es.upm.fi.cig.multictbnc.data.reader.DatasetReader;
import es.upm.fi.cig.multictbnc.data.representation.Dataset;
import es.upm.fi.cig.multictbnc.data.representation.Sequence;
import es.upm.fi.cig.multictbnc.exceptions.ErroneousValueException;
import es.upm.fi.cig.multictbnc.exceptions.UnreadDatasetException;
import es.upm.fi.cig.multictbnc.models.MultiCTBNC;
import es.upm.fi.cig.multictbnc.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;
import java.util.*;

/**
 * Implements cross-validation method.
 *
 * @author Carlos Villa Blanco
 */
public class CrossValidationMethod extends ValidationMethod {
	private final Logger logger = LogManager.getLogger(CrossValidationMethod.class);
	private Dataset dataset;
	private int numFolds;
	private boolean estimateProbabilities;
	private boolean shuffle;
	private Long seed;
	private DatasetReader datasetReader;

	/**
	 * Constructor for cross-validation method.
	 *
	 * @param datasetReader         a {@code DatasetReader} to read the dataset
	 * @param folds                 number of folds
	 * @param estimateProbabilities true to estimate the probabilities of the class configurations, false otherwise
	 * @param shuffle               true to shuffle the sequences, false otherwise
	 * @param seed                  seed used to shuffle the sequences
	 */
	public CrossValidationMethod(DatasetReader datasetReader, int folds, boolean estimateProbabilities,
								 boolean shuffle,
								 Long seed) {
		this.logger.info("Preparing {}-cross-validation / Shuffle: {} / Estimate probabilities: {}", folds, shuffle,
				estimateProbabilities);
		this.datasetReader = datasetReader;
		this.numFolds = folds;
		this.estimateProbabilities = estimateProbabilities;
		this.shuffle = shuffle;
		this.seed = seed;
	}

	/**
	 * Evaluates the performance of the specified model using cross-validation.
	 *
	 * @param model model to evaluate
	 * @return the results of the cross-validation in a {@code Map}
	 * @throws UnreadDatasetException if the provided dataset could not be read
	 */
	@Override
	public Map<String, Double> evaluate(MultiCTBNC<?, ?> model) throws UnreadDatasetException,
			ErroneousValueException {
		List<Sequence> sequences = getSequencesDataset();
		int numSequences = sequences.size();
		// Obtain the size of each fold
		int[] sizeFolds = defineSizeFolds(numSequences);
		Map<String, Double> resultsCrossValidation = performCrossValidation(model, sequences, sizeFolds, 0);
		// The average of each metric is computed
		resultsCrossValidation.forEach((metric, value) -> resultsCrossValidation.put(metric, value / this.numFolds));
		displayResultsCrossValidation(resultsCrossValidation);
		return resultsCrossValidation;
	}

	@Override
	public Map<String, Double> evaluate(MultiCTBNC<?, ?> model, double preprocessingExecutionTime)
			throws UnreadDatasetException, ErroneousValueException {
		List<Sequence> sequences = getSequencesDataset();
		int numSequences = sequences.size();
		// Obtain the size of each fold
		int[] sizeFolds = defineSizeFolds(numSequences);
		Map<String, Double> resultsCrossValidation = performCrossValidation(model, sequences, sizeFolds,
				preprocessingExecutionTime);
		logger.info("Adding preprocessing execution time ({}) to the final learning time for the current fold",
				preprocessingExecutionTime);
		// The average of each metric is computed
		resultsCrossValidation.forEach((metric, value) -> resultsCrossValidation.put(metric, value / this.numFolds));
		displayResultsCrossValidation(resultsCrossValidation);
		return resultsCrossValidation;
	}

	private int[] defineSizeFolds(int numSequences) {
		int[] sizeFolds = new int[this.numFolds];
		Arrays.fill(sizeFolds, numSequences / this.numFolds);
		// Sequences without fold are added one by one to the first folds
		for (int i = 0; i < numSequences % this.numFolds; i++)
			sizeFolds[i] += 1;
		return sizeFolds;
	}

	private void displayResultsCrossValidation(Map<String, Double> resultsCrossValidation) {
		// Display results
		System.out.println("----------------------------Results cross-validation----------------------------");
		displayResults(resultsCrossValidation);
		System.out.println("--------------------------------------------------------------------------------");
	}

	/**
	 * Displays the results of a fold.
	 *
	 * @param results a {@code Map} with the results obtained when testing on a fold
	 * @param model   the {@code MultiCTBNC} used for testing
	 */
	private void displayResultsFold(Map<String, Double> results, int foldNumber, MultiCTBNC<?, ?> model) {
		logger.info(MessageFormat.format(
				"---------------------------------Results fold {0}---------------------------------", foldNumber));
		results.forEach((metric, value) -> logger.info(metric + " = " + value));
		logger.info(model);
		logger.info("--------------------------------------------------------------------------------");
	}

	/**
	 * Given all the sequences of a dataset, creates a test dataset using the sequences between some specified indexes.
	 *
	 * @param sequences sequences of a dataset
	 * @param fromIndex index of the first sequence of the extracted dataset
	 * @param toIndex   index of the last sequence of the extracted dataset
	 * @return test dataset
	 */
	private Dataset extractTestingDataset(List<Sequence> sequences, int fromIndex, int toIndex) {
		List<Sequence> testingSequences = sequences.subList(fromIndex, toIndex);
		return new Dataset(testingSequences);
	}

	/**
	 * Given all the sequences of a dataset, creates a training dataset that includes all the sequences but those
	 * between some specified indexes.
	 *
	 * @param sequences sequences of a dataset
	 * @param fromIndex index of the first sequence to ignore
	 * @param toIndex   index of the last sequence to ignore
	 * @return training dataset
	 */
	private Dataset extractTrainingDataset(List<Sequence> sequences, int fromIndex, int toIndex) {
		List<Sequence> trainingSequences = new ArrayList<>(sequences);
		// Remove instances that will be used for testing
		trainingSequences.subList(fromIndex, toIndex).clear();
		return new Dataset(trainingSequences);
	}

	private List<Sequence> getSequencesDataset() throws UnreadDatasetException {
		// Read dataset (if it was not done before)
		readDataset();
		// Get sequences from the dataset
		List<Sequence> sequences = this.dataset.getSequences();
		if (this.shuffle) {
			// Shuffle the sequences before performing the cross-validation
			Util.shuffle(sequences, this.seed);
			this.logger.info("Sequences shuffled");
		}
		return sequences;
	}

	private Map<String, Double> performCrossValidation(MultiCTBNC<?, ?> model, List<Sequence> sequences,
													   int[] sizeFolds, double preprocessingExecutionTime)
			throws ErroneousValueException {
		// Save performance metrics
		Map<String, Double> resultsCrossValidation = new LinkedHashMap<>();
		// Iterate over each fold
		int fromIndex = 0;
		for (int foldNumber = 0; foldNumber < this.numFolds; foldNumber++) {
			// Get index of the last sequences in the fold to test
			int toIndex = fromIndex + sizeFolds[foldNumber];
			Map<String, Double> resultsFold = testFold(model, sequences, fromIndex, toIndex, foldNumber);
			if (preprocessingExecutionTime > 0) {
				logger.info("Adding preprocessing execution time ({}) to the final learning time for the fold",
						preprocessingExecutionTime);
				resultsFold.computeIfPresent("Learning time", (k, v) -> v + preprocessingExecutionTime);
			}
			// Display results fold
			displayResultsFold(resultsFold, foldNumber, model);
			// Define first sequences next fold for testing
			fromIndex += sizeFolds[foldNumber];
			// Update the final results of the metrics after seeing all the folds
			resultsFold.forEach((metric, value) -> resultsCrossValidation.merge(metric, value, (a, b) -> a + b));
		}
		return resultsCrossValidation;
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
		if (this.numFolds < 2 || this.numFolds > this.dataset.getNumDataPoints()) {
			this.logger.warn("Number of folds must be between 2 and the dataset size (2 folds will be used)");
			this.numFolds = 2;
		}
		this.logger.info("Time variable: {}", this.dataset.getNameTimeVariable());
		this.logger.info("Feature variables: {}", this.dataset.getNameFeatureVariables());
		this.logger.info("Class variables: {}", (this.dataset.getNameClassVariables()));
	}

	private Map<String, Double> testFold(MultiCTBNC<?, ?> model, List<Sequence> sequences, int fromIndex, int toIndex,
										 int i) throws ErroneousValueException {
		this.logger.info("Testing model on fold " + i);
		// Prepare training dataset for current fold
		Dataset trainingDataset = extractTrainingDataset(sequences, fromIndex, toIndex);
		// Prepare test dataset for current fold
		Dataset testingDataset = extractTestingDataset(sequences, fromIndex, toIndex);
		// Warn both the training and testing set about all the possible states the
		// variables can take (categorical variables are assumed for now)
		trainingDataset.setStatesVariables(this.dataset.getStatesVariables());
		testingDataset.setStatesVariables(this.dataset.getStatesVariables());
		// Train the model
		long learningTime = model.learn(trainingDataset);
		double learningTimeSeconds = learningTime / 1000.f;
		// Make predictions over the current fold
		Prediction[] predictions = model.predict(testingDataset, this.estimateProbabilities);
		// Result of performance metrics when evaluating the model with the current fold
		Map<String, Double> resultsFold = Metrics.evaluate(predictions, testingDataset);
		// Add learning time to the results
		resultsFold.put("Learning time", learningTimeSeconds);
		return resultsFold;
	}

}