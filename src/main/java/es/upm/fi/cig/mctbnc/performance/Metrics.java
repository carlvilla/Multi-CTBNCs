package es.upm.fi.cig.mctbnc.performance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.upm.fi.cig.mctbnc.classification.Prediction;
import es.upm.fi.cig.mctbnc.data.representation.Dataset;
import es.upm.fi.cig.mctbnc.data.representation.State;
import es.upm.fi.cig.mctbnc.util.Util;

/**
 * Computes different metrics for the evaluation of multi-dimensional
 * classifications.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class Metrics {
	private static Logger logger = LogManager.getLogger(Metrics.class);

	/**
	 * Uses different performance metrics to evaluate how good are the given
	 * predictions.
	 * 
	 * @param predicted     {@code Prediction} array
	 * @param actualDataset dataset with actual classes
	 * @return Map with the name of the evaluation metrics and their values
	 */
	public static Map<String, Double> evaluate(Prediction[] predicted, Dataset actualDataset) {
		if (predicted.length != actualDataset.getNumDataPoints()) {
			logger.warn("The number of predictions and actual instances does not match");
			return null;
		}
		showPredictions(predicted, actualDataset);
		if (actualDataset.getNumClassVariables() == 1)
			// There is only one class variable
			return evaluateUniDimensionalClassification(predicted, actualDataset);
		// There are more than one class variable
		return evaluateMultiDimensionalClassification(predicted, actualDataset);
	}

	private static Map<String, Double> evaluateUniDimensionalClassification(Prediction[] predicted,
			Dataset actualDataset) {
		// Save results metrics in a map
		Map<String, Double> results = new LinkedHashMap<String, Double>();
		double accuracy = globalAccuracy(predicted, actualDataset);
		double macroPrecision = macroAverage(predicted, actualDataset, Metrics::precision);
		double macroRecall = macroAverage(predicted, actualDataset, Metrics::recall);
		double macroF1Score = macroAverage(predicted, actualDataset, Metrics::f1score);
		double microPrecision = microAverage(predicted, actualDataset, Metrics::precision);
		double microRecall = microAverage(predicted, actualDataset, Metrics::recall);
		double microF1Score = microAverage(predicted, actualDataset, Metrics::f1score);
		results.put("Accuracy", accuracy);
		results.put("Macro-averaged precision", macroPrecision);
		results.put("Macro-averaged recall", macroRecall);
		results.put("Macro-averaged F1 score", macroF1Score);
		results.put("Micro-averaged precision", microPrecision);
		results.put("Micro-averaged recall", microRecall);
		results.put("Micro-averaged F1 score", microF1Score);
		// If the probabilities of the classes are available
		if (predicted[0].getProbabilities() != null) {
			double globalBrierScore = globalBrierScore(predicted, actualDataset);
			results.put("Brier score", globalBrierScore);
		}
		return results;
	}

	private static Map<String, Double> evaluateMultiDimensionalClassification(Prediction[] predicted,
			Dataset actualDataset) {
		// Save results metrics in a map
		Map<String, Double> results = new LinkedHashMap<String, Double>();
		double globalAccuracy = globalAccuracy(predicted, actualDataset);
		double meanAccuracy = meanAccuracy(predicted, actualDataset, results);
		double macroPrecision = macroAverage(predicted, actualDataset, Metrics::precision);
		double macroRecall = macroAverage(predicted, actualDataset, Metrics::recall);
		double macroF1Score = macroAverage(predicted, actualDataset, Metrics::f1score);
		double microPrecision = microAverage(predicted, actualDataset, Metrics::precision);
		double microRecall = microAverage(predicted, actualDataset, Metrics::recall);
		double microF1Score = microAverage(predicted, actualDataset, Metrics::f1score);
		results.put("Global accuracy", globalAccuracy);
		results.put("Mean accuracy", meanAccuracy);
		results.put("Macro-averaged precision", macroPrecision);
		results.put("Macro-averaged recall", macroRecall);
		results.put("Macro-averaged F1 score", macroF1Score);
		results.put("Micro-averaged precision", microPrecision);
		results.put("Micro-averaged recall", microRecall);
		results.put("Micro-averaged F1 score", microF1Score);
		// If the probabilities of the class configurations are available
		if (predicted[0].getProbabilities() != null) {
			double globalBrierScore = globalBrierScore(predicted, actualDataset);
			results.put("Global Brier score", globalBrierScore);
		}
		return results;
	}

	/**
	 * Displays the predictions along with the actual values.
	 * 
	 * @param predicted     {@code Prediction} array
	 * @param actualDataset dataset with actual classes
	 */
	public static void showPredictions(Prediction[] predicted, Dataset actualDataset) {
		// Obtain actual class configurations
		State[] actualCCs = actualDataset.getStatesClassVariables();
		// Iterate over each instance
		int numInstances = actualCCs.length;
		for (int i = 0; i < numInstances; i++) {
			String[] predictedCC = predicted[i].getPredictedClasses().getValues();
			String[] actualCC = actualCCs[i].getValues();
			// Get the estimated probability of our prediction
			double probability = predicted[i].getProbabilityPrediction();
			// Show predicted and actual classes along with the name of the sequence file
			if (predicted[i].getProbabilities() != null)
				logger.trace("File {} / Real classes: {} / Predicted classes {} / Prob. {}",
						actualDataset.getSequences().get(i).getFilePath(), actualCC, predictedCC, probability);
			else
				logger.trace("File {} / Real classes: {} / Predicted classes {}",
						actualDataset.getSequences().get(i).getFilePath(), actualCC, predictedCC);
		}
	}

	/**
	 * Computes the global accuracy, which is the ratio between the number of
	 * instances that were correctly classified for all the class variables and the
	 * total number of instances. A partially correct classification will be
	 * considered as an error (Bielza et al., 2011).
	 * 
	 * @param predicted     {@code Prediction} array
	 * @param actualDataset dataset with actual classes
	 * @return 0/1 subset accuracy
	 */
	public static double globalAccuracy(Prediction[] predicted, Dataset actualDataset) {
		State[] actualValues = actualDataset.getStatesClassVariables();
		int numInstances = actualValues.length;
		double globalAccuracy = 0.0;
		for (int i = 0; i < numInstances; i++)
			if (predicted[i].getPredictedClasses().equals(actualValues[i]))
				globalAccuracy += 1;
		return globalAccuracy / numInstances;
	}

	/**
	 * Computes the mean of the accuracies for each class variable (Bielza et al.,
	 * 2011).
	 * 
	 * @param predicted     {@code Prediction} array
	 * @param actualDataset dataset with actual classes
	 * @return mean accuracy
	 */
	public static double meanAccuracy(Prediction[] predicted, Dataset actualDataset) {
		return meanAccuracy(predicted, actualDataset, null);
	}

	/**
	 * Computes the mean of the accuracies for each class variable (Bielza et al.,
	 * 2011).
	 * 
	 * @param predicted     {@code Prediction} array
	 * @param actualDataset dataset with actual classes
	 * @param results       a {@code Map} to store the accuracies of each class
	 *                      variables
	 * @return mean accuracy
	 */
	public static double meanAccuracy(Prediction[] predicted, Dataset actualDataset, Map<String, Double> results) {
		// Obtain the actual class configurations
		State[] actualCCs = actualDataset.getStatesClassVariables();
		// Obtain number of instances
		int numInstances = actualCCs.length;
		// Initialize mean accuracy
		double meanAccuracy = 0.0;
		// Iterate over every class variable
		List<String> nameCVs = predicted[0].getPredictedClasses().getNameVariables();
		for (String nameCV : nameCVs) {
			double accuracyCV = 0.0;
			// Iterate over every instance
			for (int j = 0; j < numInstances; j++) {
				String predictedClass = predicted[j].getPredictedClasses().getValueVariable(nameCV);
				String actualClass = actualCCs[j].getValueVariable(nameCV);
				if (predictedClass.equals(actualClass)) {
					meanAccuracy += 1;
					accuracyCV += 1;
				}
			}
			accuracyCV = accuracyCV / numInstances;
			if (results != null)
				results.put("Accuracy " + nameCV, accuracyCV);
		}
		meanAccuracy /= numInstances;
		meanAccuracy /= nameCVs.size();
		return meanAccuracy;
	}

	/**
	 * The Brier score measures the performance of probabilistic predictions. Models
	 * that assign a higher probability to correct predictions will have a lower
	 * brier score (0 is the best). This method implements a generalized version for
	 * multi-dimensional problems, which rewards only the probability of the class
	 * configuration where all classes are correct (Fernandes et al., 2013).
	 * 
	 * @param predicted     {@code Prediction} array
	 * @param actualDataset dataset with actual classes
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
	 * Computes the precision evaluation metric from a Map containing a confusion
	 * matrix. The Map should contain, at least, the keys "tp" (true positive) and
	 * "fp" (false positive).
	 * 
	 * @param cm Map representing a confusion matrix
	 * @return precision
	 */
	public static double precision(Map<String, Integer> cm) {
		double precision = (double) cm.get("tp") / (cm.get("tp") + cm.get("fp"));
		return Double.isNaN(precision) ? 0 : precision;
	}

	/**
	 * Computes the recall evaluation metric from a Map containing a confusion
	 * matrix. The Map should contain, at least, the keys "tp" (true positive) and
	 * "fn" (false negative).
	 * 
	 * @param cm Map representing a confusion matrix
	 * @return recall
	 */
	public static double recall(Map<String, Integer> cm) {
		return (double) cm.get("tp") / (cm.get("tp") + cm.get("fn"));
	}

	/**
	 * Compute the F1 score from a Map containing a confusion matrix. The Map should
	 * contain, at least, the keys "tp" (true positive), "fp" (false positive) and
	 * "fn" (false negative).
	 * 
	 * @param cm Map representing a confusion matrix
	 * @return F1 score
	 */
	public static double f1score(Map<String, Integer> cm) {
		return (double) 2 * cm.get("tp") / (2 * cm.get("tp") + cm.get("fn") + cm.get("fp"));
	}

	/**
	 * Computes the value of a given evaluation metric for a multi-dimensional
	 * classification problem using a macro-average approach (Gil‑Begue et al.,
	 * 2020).
	 * 
	 * @param predicted     {@code Prediction} array
	 * @param actualDataset dataset with actual classes
	 * @param metric        evaluation metric
	 * @return result of the evaluation metric
	 */
	public static double macroAverage(Prediction[] predicted, Dataset actualDataset, Metric metric) {
		// Names of the class variables
		List<String> nameCVs = actualDataset.getNameClassVariables();
		// Number of class variables
		int numCVs = nameCVs.size();
		// Predictions (State objects) for each sequence and class variable
		State[] predictedClasses = Arrays.stream(predicted).map(sequence -> sequence.getPredictedClasses())
				.toArray(State[]::new);
		State[] actualClasses = actualDataset.getStatesClassVariables();
		double metricResult = 0.0;
		for (String nameCV : nameCVs) {
			// Predicted classes of each class variables
			String[] predictedClassesCV = Arrays.stream(predictedClasses).map(state -> state.getValueVariable(nameCV))
					.toArray(String[]::new);
			// Actual classes of the class variables from the State objects
			String[] actualClassesCV = Arrays.stream(actualClasses).map(state -> state.getValueVariable(nameCV))
					.toArray(String[]::new);
			// Obtain possible classes of the class variable
			String[] possibleClassesCV = Util.getUnique(actualClassesCV);
			// Apply the metric to the class variable
			double metricResultCV = 0.0;
			if (possibleClassesCV.length > 2) {
				// Categorical variable
				for (String classCV : possibleClassesCV) {
					// Obtain confusion matrix for each class
					Map<String, Integer> cm = getConfusionMatrix(predictedClassesCV, actualClassesCV, classCV);
					// Result of the metric for each class
					metricResultCV += metric.compute(cm);
				}
				metricResultCV /= possibleClassesCV.length;
			} else {
				// Binary variable
				String positiveClass = getPositiveClass(possibleClassesCV);
				logger.trace("Using {} as positive class of '{}'", positiveClass, nameCV);
				Map<String, Integer> cm = getConfusionMatrix(predictedClassesCV, actualClassesCV, positiveClass);
				metricResultCV = metric.compute(cm);
			}
			metricResult += metricResultCV;
		}
		metricResult /= numCVs;
		return metricResult;
	}

	/**
	 * Computes the value of a given evaluation metric for a multi-dimensional
	 * classification problem using a micro-average approach (Gil‑Begue et al.,
	 * 2020).
	 * 
	 * @param predicted     {@code Prediction} array
	 * @param actualDataset dataset with actual classes
	 * @param metric        evaluation metric
	 * @return result of the evaluation metric
	 */
	public static double microAverage(Prediction[] predicted, Dataset actualDataset, Metric metric) {
		// Names of the class variables
		List<String> nameCVs = actualDataset.getNameClassVariables();
		// Predictions (State objects) for each sequence and class variable
		State[] predictedClasses = Arrays.stream(predicted).map(sequence -> sequence.getPredictedClasses())
				.toArray(State[]::new);
		State[] actualClasses = actualDataset.getStatesClassVariables();
		// Confusion matrix to keep the combined results of each class variable
		int tp = 0, fp = 0, tn = 0, fn = 0;
		for (String nameCV : nameCVs) {
			// Predicted classes of each class variables
			String[] predictedClassesCV = Arrays.stream(predictedClasses).map(state -> state.getValueVariable(nameCV))
					.toArray(String[]::new);
			// Actual classes of the class variables from the State objects
			String[] actualClassesCV = Arrays.stream(actualClasses).map(state -> state.getValueVariable(nameCV))
					.toArray(String[]::new);
			// Obtain possible classes of the class variable
			String[] possibleClassesCV = Util.getUnique(actualClassesCV);
			if (possibleClassesCV.length > 2)
				// Categorical variable. If all class variables are multi-class, the precision
				// and recall (and therefore F1 score) will be the same
				for (String classCV : possibleClassesCV) {
					// Obtain confusion matrix for each class
					Map<String, Integer> cm = getConfusionMatrix(predictedClassesCV, actualClassesCV, classCV);
					// Update confusion matrix with results of each class variable and positive
					// class
					tp += cm.get("tp");
					fp += cm.get("fp");
					tn += cm.get("tn");
					fn += cm.get("fn");
				}
			else {
				// Binary variable
				String positiveClass = getPositiveClass(possibleClassesCV);
				logger.trace("Using {} as positive class of '{}'", positiveClass, nameCV);
				Map<String, Integer> cm = getConfusionMatrix(predictedClassesCV, actualClassesCV, positiveClass);
				tp += cm.get("tp");
				fp += cm.get("fp");
				tn += cm.get("tn");
				fn += cm.get("fn");
			}
		}
		// Apply the metric to the aggregated confusion matrix
		Map<String, Integer> cm = Map.of("tp", tp, "fp", fp, "tn", tn, "fn", fn);
		return metric.compute(cm);
	}

	/**
	 * Returns the positive class of a binary class variable. It is assumed that the
	 * class would be either 'True', 'Positive' or '1'. If it is not possible to
	 * determine the positive class, the first one will be returned.
	 * 
	 * @param possibleClasses possible classes
	 * @return positive class
	 */
	private static String getPositiveClass(String[] possibleClasses) {
		for (int i = 0; i < possibleClasses.length; i++) {
			String classI = possibleClasses[i];
			if (classI.equalsIgnoreCase("True") || classI.equalsIgnoreCase("Positive") || classI.equals("1")
					|| classI.contains("A")) {
				return classI;
			}
		}
		return possibleClasses[0];
	}

	/**
	 * Obtains a confusion matrix given a list of predicted values and their actual
	 * values. It also needs to be specified which one is the positive class.
	 * 
	 * @param predicted     {@code Prediction} array
	 * @param actual        dataset with actual classes
	 * @param positiveClass positive class
	 * @return confusion matrix
	 */
	private static Map<String, Integer> getConfusionMatrix(String[] predicted, String[] actual, String positiveClass) {
		// Compute true positives, false positives, true negatives and false negatives
		int tp = 0, fp = 0, tn = 0, fn = 0;
		for (int i = 0; i < predicted.length; i++)
			if (predicted[i].equals(positiveClass) && predicted[i].equals(actual[i]))
				tp++;
			else if (predicted[i].equals(positiveClass) && !predicted[i].equals(actual[i]))
				fp++;
			else if (actual[i].equals(positiveClass))
				fn++;
			else
				tn++;
		// Save the four possible outcomes in a Map object
		Map<String, Integer> confusionMatrix = Map.of("tp", tp, "fp", fp, "tn", tn, "fn", fn);
		return confusionMatrix;
	}

}