package com.cig.mctbnc.performance;

import java.text.MessageFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cig.mctbnc.classification.ClassifierFactory;
import com.cig.mctbnc.classification.Prediction;
import com.cig.mctbnc.data.reader.DatasetReader;
import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.data.representation.Sequence;
import com.cig.mctbnc.data.representation.State;
import com.cig.mctbnc.exceptions.UnreadDatasetException;
import com.cig.mctbnc.models.MCTBNC;
import com.cig.mctbnc.util.Util;

/**
 * Implements a cross-validation method used to learn one CTBNC for each class
 * variable and merge the results.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class CrossValidationBinaryRelevance extends ValidationMethod {
	Dataset dataset;
	int folds;
	boolean estimateProbabilities;
	boolean shuffle;
	long seed;
	Logger logger = LogManager.getLogger(CrossValidation.class);

	/**
	 * Constructor for cross-validation method.
	 * 
	 * @param datasetReader
	 * @param folds                 number of folds
	 * @param estimateProbabilities determines if the probabilities of the class
	 *                              configurations are estimated
	 * @param shuffle               determines if the sequences should be shuffled
	 * @param seed
	 * @throws UnreadDatasetException
	 */
	public CrossValidationBinaryRelevance(DatasetReader datasetReader, int folds, boolean estimateProbabilities,
			boolean shuffle, long seed) throws UnreadDatasetException {
		super();
		this.logger.info(
				"Preparing {}-cross validation for independent CTBNCs / Shuffle: {} / Estimate probabilities: {}",
				folds, shuffle, estimateProbabilities);
		// Obtain dataset and the number of sequence it contains
		this.dataset = datasetReader.readDataset();
		this.logger.info("Time variable: {}", this.dataset.getNameTimeVariable());
		this.logger.info("Features: {}", this.dataset.getNameFeatures());
		this.logger.info("Class variables: {}", (this.dataset.getNameClassVariables()));
		// Check that the specified number of folds is valid
		if (folds < 2 || folds > this.dataset.getNumDataPoints())
			this.logger.warn("Number of folds must be between 2 and the dataset size (leave-one-out cross validation)");
		this.folds = folds;
		this.estimateProbabilities = estimateProbabilities;
		this.shuffle = shuffle;
		this.seed = seed;
	}

	/**
	 * Evaluate the performance of the specified model using cross-validation.
	 * 
	 * @param model model to evaluate
	 */
	@Override
	public void evaluate(MCTBNC<?, ?> model) {
		// Get sequences from the dataset
		List<Sequence> sequences = this.dataset.getSequences();
		int numSequences = sequences.size();
		// Obtain files from which the dataset was read
		List<String> fileNames = new ArrayList<String>(this.dataset.getNameFiles());
		if (this.shuffle) {
			// Shuffle the sequences before performing cross-validation
			Util.shuffle(sequences, this.seed);
			Util.shuffle(fileNames, this.seed);
			this.logger.info("Sequences shuffled");
		}
		// Obtain size of each fold
		int[] sizeFolds = new int[this.folds];
		Arrays.fill(sizeFolds, numSequences / this.folds);
		// Sequences without fold are added one by one to the first folds
		for (int i = 0; i < numSequences % this.folds; i++)
			sizeFolds[i] += 1;
		// Save evaluation metrics obtained with the cross validation
		Map<String, Double> resultsCrossValidation = new LinkedHashMap<String, Double>();
		// Iterate over each fold
		int fromIndex = 0;
		for (int i = 0; i < this.folds; i++) {
			System.out.println("+++++++++++++++++++++++ Testing on fold " + i + " +++++++++++++++++++++++");
			// Prepare training and testing datasets for current fold
			int toIndex = fromIndex + sizeFolds[i];
			// Prepare training dataset for current fold
			Dataset trainingDataset = extractTrainingDataset(sequences, fileNames, fromIndex, toIndex);
			// Prepare testing dataset for current fold
			Dataset testingDataset = extractTestingDataset(sequences, fileNames, fromIndex, toIndex);
			// Learn one model per class variable in parallel
			List<MCTBNC<?, ?>> models = learnModels(model, trainingDataset);
			// Perform predictions with each model and merge the results
			Prediction[] predictionsFold = predict(models, testingDataset);
			// Result of performance metrics when evaluating the model with the current fold
			Map<String, Double> resultsFold = Metrics.evaluate(predictionsFold, testingDataset);
			// Update the final results of the metrics after seeing all the folds
			resultsFold.forEach((metric, value) -> resultsCrossValidation.merge(metric, value, (a, b) -> a + b));
			displayResultsFold(i, resultsFold);
			fromIndex += sizeFolds[i];
		}
		// The average of each metric is computed
		resultsCrossValidation.forEach((metric, value) -> resultsCrossValidation.put(metric, value / this.folds));
		displayResultsCV(resultsCrossValidation);
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
	 * @return training dataset
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
	 * @return testing dataset
	 */
	private Dataset extractTestingDataset(List<Sequence> sequences, List<String> fileNames, int fromIndex,
			int toIndex) {
		List<Sequence> testingSequences = sequences.subList(fromIndex, toIndex);
		Dataset testingDataset = new Dataset(testingSequences);
		// Get name of the files from which the testing sequences were extracted
		testingDataset.setNameFiles(fileNames.subList(fromIndex, toIndex));
		return testingDataset;
	}

	private List<MCTBNC<?, ?>> learnModels(MCTBNC<?, ?> model, Dataset trainingDataset) {
		// Get name class variables
		List<String> nameCVs = trainingDataset.getNameClassVariables();
		// Create as many models as class variables. Required for parallelization
		List<MCTBNC<?, ?>> models = new ArrayList<MCTBNC<?, ?>>();
		// Training datasets for each class variable. Sequences are not duplicated
		List<Dataset> datasets = new ArrayList<Dataset>();
		for (int i = 0; i < nameCVs.size(); i++) {
			// Define model for one class variable
			models.add(ClassifierFactory.getMCTBNC(model.getModelIdentifier(), model.getLearningAlgsBN(),
					model.getLearningAlgsCTBN(), model.getHyperparameters(), model.getTypeNodeClassVariable(),
					model.getTypeNodeFeature()));
			// Define training dataset that ignore all class variables except one
			Dataset dataset = new Dataset(trainingDataset.getSequences());
			List<String> nameClassVariables = new ArrayList<String>(nameCVs);
			nameClassVariables.remove(nameCVs.get(i));
			dataset.setIgnoredClassVariables(nameClassVariables);
			datasets.add(dataset);
		}
		// Train models in parallel
		Instant start = Instant.now();
		IntStream.range(0, nameCVs.size()).parallel().forEach(indexModel -> {
			// Train the model
			models.get(indexModel).learn(datasets.get(indexModel));
		});
		Instant end = Instant.now();
		this.logger.info("CTBNCs learnt in {}", Duration.between(start, end));
		return models;
	}

	private Prediction[] predict(List<MCTBNC<?, ?>> models, Dataset testingDataset) {
		List<String> nameCVs = testingDataset.getNameClassVariables();
		Prediction[] predictionsFold = null;
		for (int i = 0; i < models.size(); i++) {
			// Display learned model
			System.out.println(MessageFormat
					.format("--------------------Model for class variable {0}--------------------", nameCVs.get(i)));
			displayModel(models.get(i));
			System.out.println("------------------------------------------------------");
			Prediction[] predictionsCV = models.get(i).predict(testingDataset, this.estimateProbabilities);
			predictionsFold = updatePredictionsFold(predictionsFold, this.dataset.getNameClassVariables().get(i),
					predictionsCV, false);
		}
		return predictionsFold;
	}

	/**
	 * Update the current predictions of a fold with the predictions for a new class
	 * variable.
	 * 
	 * @param predictionsFold       current predictions of the fold
	 * @param nameCV                name of the class variable whose predictions are
	 *                              included in the predictions of the fold
	 * @param predictionsCV         predictions of the class variable
	 * @param estimateProbabilities determines if the probabilities of the classes
	 *                              are estimated
	 * @return updated predictions for the fold
	 */
	private Prediction[] updatePredictionsFold(Prediction[] predictionsFold, String nameCV, Prediction[] predictionsCV,
			boolean estimateProbabilities) {
		if (predictionsFold == null)
			// First class is predicted
			return predictionsCV;
		// Update the predicted class configuration for each sequence and the
		// probabilities given to each class configuration (necessary for Brier score)
		for (int i = 0; i < predictionsFold.length; i++) {
			// Get prediction current class variable and sequence
			State prediction = predictionsCV[i].getPredictedClasses();
			// Update current class configuration (CC) of the sequence
			State predictedCC = predictionsFold[i].getPredictedClasses();
			predictedCC.addEvent(nameCV, prediction.getValueVariable(nameCV));
			// Define probabilities of each class configuration for the current sequence
			Map<State, Double> newProbabilitiesCCs = new HashMap<State, Double>();
			if (estimateProbabilities) {
				// Iterate over class configurations including states current class variable
				Set<State> statesCC = predictionsFold[i].getProbabilities().keySet();
				Set<State> statesCV = predictionsCV[i].getProbabilities().keySet();
				for (State stateCC : statesCC)
					for (State classCV : statesCV) {
						// Get new class configuration that includes state of the class variable
						State newStateCC = new State(stateCC.getEvents());
						newStateCC.addEvents(classCV.getEvents());
						// Get probability new class configuration
						double previousProbCC = predictionsFold[i].getProbabilities().get(stateCC);
						double newProbCC = previousProbCC * predictionsCV[i].getProbabilities().get(classCV);
						// Save new class configuration and its probability
						newProbabilitiesCCs.put(newStateCC, newProbCC);
					}
				// Save probabilities class configurations for the sequence
				predictionsFold[i].setProbabilities(newProbabilitiesCCs);
				// Get probability predicted class configuration for the sequence
				double probabilityPredictedCC = predictionsFold[i].getProbabilityPrediction();
				// Get probability predicted class for the current class variable
				double probabilityPredictedClass = predictionsCV[i].getProbabilityPrediction();
				// Save new probability predicted class configuration
				double probabilityNewPredictedCC = probabilityPredictedCC * probabilityPredictedClass;
				predictionsFold[i].setProbabilityPrediction(probabilityNewPredictedCC);
			}
		}
		return predictionsFold;
	}

	private void displayResultsFold(int foldNumber, Map<String, Double> resultsFold) {
		System.out.println(MessageFormat.format("-------------------Results fold {0}-------------------", foldNumber));
		resultsFold.forEach((metric, value) -> System.out.println(metric + " = " + value));
		System.out.println("-----------------------------------------------------");
	}

	private void displayResultsCV(Map<String, Double> resultsCrossValidation) {
		System.out.println("-------------------Results cross-validation-------------------");
		displayResults(resultsCrossValidation);
		System.out.println("--------------------------------------------------------------");
	}

}