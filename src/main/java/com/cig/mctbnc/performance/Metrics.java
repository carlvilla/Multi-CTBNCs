package com.cig.mctbnc.performance;

import java.util.Arrays;
import com.cig.mctbnc.classification.Prediction;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
	 * @param actual
	 */
	public static void evaluate(Prediction[] predicted, String[][] actual) {
		showPredictions(predicted, actual);
		double subsetAccuracy = subsetAccuracy(predicted, actual);
		logger.info("1/0 subset accuracy: {}", subsetAccuracy);
	}

	/**
	 * Display the predictions along with the actual values.
	 * 
	 * @param predicted
	 * @param actual
	 */
	public static void showPredictions(Prediction[] predicted, String[][] actual) {
		if (predicted.length != actual.length) {
			logger.warn("The number of predictions and actual instances does not match");
			return;
		}
		int numInstances = predicted.length;
		for (int i = 0; i < numInstances; i++) {
			logger.info("Real classes: {} / Predicted classes {} / Prob. {}", actual[i], predicted[i].getPrediction(),
					predicted[i].getProbability());
		}
	}

	/**
	 * Compute the 0/1 subset accuracy, which is the ratio between the number of
	 * instances that were correctly classified for all the class variables and the
	 * total number of instances. A partially correct classification will be
	 * considered as an error.
	 * 
	 * @param predicted array of Prediction objects with predicted classes
	 * @param actual    bidimensional array with actual classes
	 * @return 0/1 subset accuracy
	 */
	public static double subsetAccuracy(Prediction[] predicted, String[][] actual) {
		if (predicted.length != actual.length) {
			logger.warn("The number of predictions and actual instances does not match");
			return -1;
		}
		int numCorrectInstances = 0;
		int numInstances = predicted.length;
		for (int i = 0; i < numInstances; i++)
			if (Arrays.equals(predicted[i].getPrediction(), actual[i]))
				numCorrectInstances++;
		double subsetAccuracy = (double) numCorrectInstances / numInstances;
		return subsetAccuracy;
	}

	/**
	 * Specify macro or micro approaches.
	 * 
	 * @param predicted
	 * @param actual
	 * @param approach
	 * @return
	 */
	public static double averagePrecision(String[][] predicted, String[][] actual, String approach) {
		return 0.0;
	}

	public static double averageF1(String[][] predicted, String[][] actual, String approach) {
		return 0.0;
	}

}
