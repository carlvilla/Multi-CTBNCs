package com.cig.mctbnc.performance;

import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cig.mctbnc.classification.Prediction;
import com.cig.mctbnc.data.representation.Dataset;

/**
 * Compute different metrics for the evaluation of multi-dimensional
 * classifications.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class Metrics {
	private static Logger logger = LogManager.getLogger(Metrics.class);

	/**
	 * Use different performance metrics to evaluate how good are the given
	 * predictions.
	 * 
	 * @param predicted
	 * @param actualDataset dataset with actual classes
	 */
	public static void evaluate(Prediction[] predicted, Dataset actualDataset) {
		if (predicted.length != actualDataset.getNumDataPoints()) {
			logger.warn("The number of predictions and actual instances does not match");
			return;
		}
		showPredictions(predicted, actualDataset);
		double globalAccuracy = globalAccuracy(predicted, actualDataset);
		double meanAccuracy = meanAccuracy(predicted, actualDataset);
		// Display results
		logger.info("Global accuracy: {}", globalAccuracy);
		logger.info("Mean accuracy: {}", meanAccuracy);
	}

	/**
	 * Display the predictions along with the actual values.
	 * 
	 * @param predicted
	 * @param actualDataset dataset with actual classes
	 */
	private static void showPredictions(Prediction[] predicted, Dataset actualDataset) {
		String[][] actualValues = actualDataset.getValuesClassVariables();
		int numInstances = actualValues.length;
		List<String> nameFiles = actualDataset.getNameFiles();
		for (int i = 0; i < numInstances; i++) {
			// If there are as many files as instances, show the predicted and actual
			// classes along with the name of the file
			if (nameFiles.size() == numInstances) {
				logger.info("File {} / Real classes: {} / Predicted classes {} / Prob. {}",
						actualDataset.getNameFiles().get(i), actualValues[i], predicted[i].getPrediction(),
						predicted[i].getProbability());
			} else {
				logger.info("Real classes: {} / Predicted classes {} / Prob. {}", actualValues[i],
						predicted[i].getPrediction(), predicted[i].getProbability());
			}

		}
	}

	/**
	 * Compute the global accuracy, which is the ratio between the number of
	 * instances that were correctly classified for all the class variables and the
	 * total number of instances. A partially correct classification will be
	 * considered as an error (Bielza et al. 2011).
	 * 
	 * @param predicted     array of Prediction objects with predicted classes
	 * @param actualDataset dataset with actual classes
	 * @return 0/1 subset accuracy
	 */
	private static double globalAccuracy(Prediction[] predicted, Dataset actualDataset) {
		String[][] actualValues = actualDataset.getValuesClassVariables();
		int numCorrectInstances = 0;
		int numInstances = actualValues.length;
		for (int i = 0; i < numInstances; i++)
			if (compareArrays(predicted[i].getPrediction(), actualValues[i]))
				numCorrectInstances++;
		double subsetAccuracy = (double) numCorrectInstances / numInstances;
		return subsetAccuracy;
	}

	/**
	 * Compute the mean of the accuracies for each class variable (Bielza et al.
	 * 2011).
	 * 
	 * @param predicted
	 * @param actual
	 * @return
	 */
	private static double meanAccuracy(Prediction[] predicted, Dataset actualDataset) {
		String[][] actualValues = actualDataset.getValuesClassVariables();
		int numClassVariables = actualValues[0].length;
		int numInstances = actualValues.length;
		double meanAccuracy = 0.0;
		for (int i = 0; i < numClassVariables; i++) {
			int numCorrectInstances = 0;
			for (int j = 0; j < numInstances; j++) {
				if (predicted[j].getPrediction()[i].equals(actualValues[j][i]))
					numCorrectInstances++;
			}
			meanAccuracy += (double) numCorrectInstances / numInstances;
		}
		meanAccuracy /= numClassVariables;
		return meanAccuracy;
	}

	/**
	 * The Brier score measures the performance of probabilistic predictions. Models
	 * that assign a higher probability to correct predictions will have a lower
	 * brier score. The score ranges from 0 (best) to 1 (worst). This method
	 * implements a generalized version for multi-dimensional problems (Fernandes et
	 * al. 2013).
	 * 
	 * @param predicted
	 * @param actual
	 * @param approach
	 * @return
	 */
	private static double globalBrierScore(Prediction[] predicted, String[][] actual, String approach) {
		return 0.0;
	}

	/**
	 * Determine if two arrays are equal.
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	private static boolean compareArrays(String[] a, String[] b) {
		return Arrays.equals(a, b);
	}

}
