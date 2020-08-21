package com.cig.mctbnc.performance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cig.mctbnc.classification.Prediction;
import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.data.representation.State;
import com.cig.mctbnc.util.Util;

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
	public static Map<String, Double> evaluate(Prediction[] predicted, Dataset actualDataset) {
		if (predicted.length != actualDataset.getNumDataPoints()) {
			logger.warn("The number of predictions and actual instances does not match");
			return null;
		}
		showPredictions(predicted, actualDataset);
		double globalAccuracy = globalAccuracy(predicted, actualDataset);
		double meanAccuracy = meanAccuracy(predicted, actualDataset);
		double globalBrierScore = globalBrierScore(predicted, actualDataset);

		// Save results in a map
		Map<String, Double> results = new LinkedHashMap<String, Double>();
		results.put("Global accuracy", globalAccuracy);
		results.put("Mean accuracy", meanAccuracy);
		results.put("Global Brier score", globalBrierScore);
		return results;
	}

	/**
	 * Display the predictions along with the actual values.
	 * 
	 * @param predicted
	 * @param actualDataset dataset with actual classes
	 */
	public static void showPredictions(Prediction[] predicted, Dataset actualDataset) {
		// Obtain actual class configurations
		State[] actualCCs = actualDataset.getStatesClassVariables();
		// Obtain the files used to generate the dataset
		List<String> nameFiles = actualDataset.getNameFiles();
		// Iterate over each instance
		int numInstances = actualCCs.length;
		for (int i = 0; i < numInstances; i++) {
			String[] predictedCC = predicted[i].getPredictedClasses().getValues();
			String[] actualCC = actualCCs[i].getValues();
			// Get the estimated probability of our prediction
			double probability = predicted[i].getProbabilityPrediction();
			// If there are as many files as instances, show the predicted and actual
			// classes along with the name of the file
			if (nameFiles != null && nameFiles.size() == numInstances) {
				logger.info("File {} / Real classes: {} / Predicted classes {} / Prob. {}",
						actualDataset.getNameFiles().get(i), actualCC, predictedCC, probability);
			} else {
				logger.info("Real classes: {} / Predicted classes {} / Prob. {}", actualCC, predictedCC, probability);
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
	public static double globalAccuracy(Prediction[] predicted, Dataset actualDataset) {
		State[] actualValues = actualDataset.getStatesClassVariables();
		int numCorrectInstances = 0;
		int numInstances = actualValues.length;
		for (int i = 0; i < numInstances; i++)
			if (predicted[i].getPredictedClasses().equals(actualValues[i]))
				numCorrectInstances++;
		double subsetAccuracy = (double) numCorrectInstances / numInstances;
		return subsetAccuracy;
	}

	/**
	 * Compute the mean of the accuracies for each class variable (Bielza et al.
	 * 2011).
	 * 
	 * @param predicted
	 * @param actualDataset
	 * @return mean accuracy
	 */
	public static double meanAccuracy(Prediction[] predicted, Dataset actualDataset) {
		// Obtain the actual class configurations
		State[] actualCCs = actualDataset.getStatesClassVariables();
		// Obtain number of instances
		int numInstances = actualCCs.length;
		// Initialize mean accuracy
		double meanAccuracy = 0.0;
		// Iterate over every class variable
		List<String> nameCVs = predicted[0].getPredictedClasses().getNameVariables();
		for (String nameCV : nameCVs) {
			int numCorrectInstances = 0;
			// Iterate over every instance
			for (int j = 0; j < numInstances; j++) {
				String predictedClass = predicted[j].getPredictedClasses().getValueVariable(nameCV);
				String actualClass = actualCCs[j].getValueVariable(nameCV);
				if (predictedClass.equals(actualClass))
					numCorrectInstances++;
			}
			meanAccuracy += (double) numCorrectInstances / numInstances;
		}
		int numCVs = nameCVs.size();
		meanAccuracy /= numCVs;
		return meanAccuracy;
	}

	/**
	 * The Brier score measures the performance of probabilistic predictions. Models
	 * that assign a higher probability to correct predictions will have a lower
	 * brier score (0 is the best). This method implements a generalized version for
	 * multi-dimensional problems, which rewards only the probability of the class
	 * configuration where all classes are correct (Fernandes et al. 2013).
	 * 
	 * @param predicted
	 * @param actualDataset
	 * @return global brier score
	 */
	public static double globalBrierScore(Prediction[] predicted, Dataset actualDataset) {
		int numInstances = predicted.length;
		// Obtain names class variables
		// Obtain all possible class configurations
		List<State> possibleCCs = new ArrayList<State>(predicted[0].getProbabilities().keySet());
		// Obtain the actual class configuration of every instance
		State[] actualCCs = actualDataset.getStatesClassVariables();
		// Initialize global brier score
		double gBs = 0.0;
		// Iterate over all instances
		for (int i = 0; i < numInstances; i++) {
			// Extract the values of the actual class configuration
			String[] actualValues = actualCCs[i].getValues();
			// Iterate over all possible class configurations
			for (State cc : possibleCCs) {
				// Extract the values of the class configuration
				String[] valuesCC = cc.getEvents().values().toArray(String[]::new);
				// Extract the probability given to the class configuration
				double probabilityClass = predicted[i].getProbabilities().get(cc);
				// Determine if the evaluated class configuration is actually the correct one
				int kroneckerDelta = Util.kroneckerDelta(valuesCC, actualValues);
				// Update the global brier score
				gBs += Math.pow(probabilityClass - kroneckerDelta, 2);
			}
		}
		gBs /= numInstances;
		return gBs;
	}

	/**
	 * (Fernandes et al. 2013).
	 * 
	 * @param predicted
	 * @param actualDataset
	 * @return
	 */
	private static double meanBrierScore(Prediction[] predicted, Dataset actualDataset) {
		return 0.0;
	}

	/**
	 * (Fernandes et al. 2013).
	 * 
	 * @param predicted
	 * @param actualDataset
	 * @return
	 */
	private static double calibratedBrierScore(Prediction[] predicted, Dataset actualDataset) {
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
