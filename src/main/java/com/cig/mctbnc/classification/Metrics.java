package com.cig.mctbnc.classification;

import java.util.Arrays;

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
	 * Compute the 0/1 subset accuracy, which is the ratio between the number of
	 * instances that were correctly classified for all the class variables and the
	 * total number of instances. A partially correct classification will be
	 * considered as an error.
	 * 
	 * @param predicted bidimensional array with predicted values
	 * @param actual    bidimensional array with actual values
	 * @return 0/1 subset accuracy
	 */
	public static double subsetAccuracy(String[][] predicted, String[][] actual) {
		if (predicted.length != actual.length) {
			logger.warn("The number of predictions and actual instances does not match");
			return -1;
		}
		int numCorrectInstances = 0;
		int numInstances = predicted.length;
		for (int i = 0; i < numInstances; i++)
			if (Arrays.equals(predicted[i], actual[i]))
				numCorrectInstances++;
		return (double) numCorrectInstances / numInstances;
	}

	/**
	 * 
	 * 
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
