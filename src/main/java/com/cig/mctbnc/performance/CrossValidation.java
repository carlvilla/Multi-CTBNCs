package com.cig.mctbnc.performance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cig.mctbnc.classification.Prediction;
import com.cig.mctbnc.data.reader.DatasetReader;
import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.data.representation.Sequence;
import com.cig.mctbnc.models.MCTBNC;

/**
 * Implements cross-validation method.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class CrossValidation {
	Dataset dataset;
	// Number of folds
	int folds;
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
	}

	/**
	 * Evaluate the performance of the specified model using hold-out validation.
	 * 
	 * @param model model to evaluate
	 */
	public void evaluate(MCTBNC<?, ?> model) {
		// Get sequences from the dataset
		List<Sequence> sequences = dataset.getSequences();
		int numSequences = sequences.size();
		// Obtain size of each fold
		int[] sizeFolds = new int[folds];
		Arrays.fill(sizeFolds, numSequences / folds);
		// Sequences without fold are added one by one to the first folds
		for (int i = 0; i < numSequences % folds; i++)
			sizeFolds[i] += 1;
		// Performance metrics (for now only for classification)
		double globalAccuracy = 0.0;
		double meanAccuracy = 0.0;
		// Iterate over each fold
		int fromIndex = 0;
		// TODO PARALELLIZE
		for (int i = 0; i < folds; i++) {
			logger.info("Fold {}", i);
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
			Prediction[] predictions = model.predict(testingDataset);
			// Metrics to evaluate the performance of the model for the current fold
			globalAccuracy += Metrics.globalAccuracy(predictions, testingDataset);
			meanAccuracy += Metrics.meanAccuracy(predictions, testingDataset);
			fromIndex += sizeFolds[i];
		}
		// Display results
		logger.info("Global accuracy ({}-cross validation): {}", folds, globalAccuracy / folds);
		logger.info("Mean accuracy ({}-cross validation): {}", folds, meanAccuracy / folds);
	}

}
