package es.upm.fi.cig.multictbnc.performance;

import es.upm.fi.cig.multictbnc.classification.Prediction;
import es.upm.fi.cig.multictbnc.data.representation.Dataset;
import es.upm.fi.cig.multictbnc.data.representation.State;
import es.upm.fi.cig.multictbnc.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * Computes different metrics for the evaluation of multi-dimensional classifications.
 *
 * @author Carlos Villa Blanco
 */
public class Metrics {
	private static final Logger logger = LogManager.getLogger(Metrics.class);

	/**
	 * Uses different performance metrics to evaluate how good the given predictions are.
	 *
	 * @param predicted     {@code Prediction} array
	 * @param actualDataset dataset with actual classes
	 * @return {@code Map} with the name of the evaluation metrics and their values
	 */
	public static Map<String, Double> evaluate(Prediction[] predicted, Dataset actualDataset) {
		if (predicted == null) {
			logger.warn("No predictions were made");
			return null;
		}
		if (predicted.length != actualDataset.getNumDataPoints()) {
			logger.warn("The number of predictions and actual instances does not match");
			return null;
		}
		showPredictions(predicted, actualDataset);
		if (actualDataset.getNumClassVariables() == 1)
			// There is only one class variable
			return evaluateOneDimensionalClassification(predicted, actualDataset);
		// There is more than one class variable
		return evaluateMultiDimensionalClassification(predicted, actualDataset);
	}

	private static Map<String, Double> evaluateOneDimensionalClassification(Prediction[] predicted,
																			Dataset actualDataset) {
		// Save results metrics in a map
		Map<String, Double> results = new LinkedHashMap<>();
		double accuracy = globalAccuracy(predicted, actualDataset);
		double macroPrecision = macroAveraging(predicted, actualDataset, Metrics::precision);
		double macroRecall = macroAveraging(predicted, actualDataset, Metrics::recall);
		double macroF1Score = macroAveraging(predicted, actualDataset, Metrics::f1score);
		double microPrecision = microAveraging(predicted, actualDataset, Metrics::precision);
		double microRecall = microAveraging(predicted, actualDataset, Metrics::recall);
		double microF1Score = microAveraging(predicted, actualDataset, Metrics::f1score);
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
		results = getPredictionTime(predicted, results);
		return results;
	}

	private static Map<String, Double> getPredictionTime(Prediction[] predicted, Map<String, Double> results) {
		double totalPredictionTime = 0;
		for (Prediction prediction : predicted)
			totalPredictionTime += prediction.getPredictionTime();
		results.put("Classification time", totalPredictionTime);
		return results;
	}

	private static Map<String, Double> evaluateMultiDimensionalClassification(Prediction[] predicted,
																			  Dataset actualDataset) {
		// Save results metrics in a map
		Map<String, Double> results = new LinkedHashMap<>();
		double globalAccuracy = globalAccuracy(predicted, actualDataset);
		double meanAccuracy = meanAccuracy(predicted, actualDataset, results);
		double macroPrecision = macroAveraging(predicted, actualDataset, Metrics::precision);
		double macroRecall = macroAveraging(predicted, actualDataset, Metrics::recall);
		double macroF1Score = macroAveraging(predicted, actualDataset, Metrics::f1score);
		double microPrecision = microAveraging(predicted, actualDataset, Metrics::precision);
		double microRecall = microAveraging(predicted, actualDataset, Metrics::recall);
		double microF1Score = microAveraging(predicted, actualDataset, Metrics::f1score);
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
		results = getPredictionTime(predicted, results);
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
	 * Computes the global accuracy, which is the ratio between the number of instances that were correctly classified
	 * for all the class variables and the total number of instances. A partially correct classification will be
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
	 * Computes the mean of the accuracies for each class variable (Bielza et al., 2011).
	 *
	 * @param predicted     {@code Prediction} array
	 * @param actualDataset dataset with actual classes
	 * @return mean accuracy
	 */
	public static double meanAccuracy(Prediction[] predicted, Dataset actualDataset) {
		return meanAccuracy(predicted, actualDataset, null);
	}

	/**
	 * Computes the mean of the accuracies for each class variable (Bielza et al., 2011).
	 *
	 * @param predicted     {@code Prediction} array
	 * @param actualDataset dataset with actual classes
	 * @param results       a {@code Map} to store the accuracies of each class variables
	 * @return mean accuracy
	 */
	public static double meanAccuracy(Prediction[] predicted, Dataset actualDataset, Map<String, Double> results) {
		// Obtain the actual class configurations
		State[] actualCCs = actualDataset.getStatesClassVariables();
		// Obtain the number of instances
		int numInstances = actualCCs.length;
		// Initialise mean accuracy
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
	 * The Brier score measures the performance of probabilistic predictions. Models that assign a higher
	 * probability to
	 * correct predictions will have a lower brier score (0 is the best). This method implements a generalised version
	 * for multi-dimensional problems, which rewards only the probability of the class configuration where all classes
	 * are correct (Fernandes et al., 2013).
	 *
	 * @param predicted     {@code Prediction} array
	 * @param actualDataset dataset with actual classes
	 * @return global brier score
	 */
	public static double globalBrierScore(Prediction[] predicted, Dataset actualDataset) {
		int numInstances = predicted.length;
		// Obtain names class variables
		// Obtain all possible class configurations
		List<State> possibleCCs = new ArrayList<>(predicted[0].getProbabilities().keySet());
		// Obtain the actual class configuration of every instance
		State[] actualCCs = actualDataset.getStatesClassVariables();
		// Initialise global brier score
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
				// Determine if the evaluated class configuration is the correct one
				int kroneckerDelta = Util.kroneckerDelta(valuesCC, actualValues);
				// Update the global brier score
				gBs += Math.pow(probabilityClass - kroneckerDelta, 2);
			}
		}
		gBs /= numInstances;
		return gBs;
	}

	/**
	 * Computes the precision evaluation metric from a {@code Map} containing a confusion matrix. The {@code Map}
	 * should
	 * contain at least the keys "tp" (true positive) and "fp" (false positive). If there are no cases predicted as
	 * positive (tp = 0 and fp = 0), a division by 0 will occur. In those cases, the precision is ill-defined and
	 * set to
	 * 0.
	 *
	 * @param cm {@code Map} representing a confusion matrix
	 * @return precision
	 */
	public static double precision(Map<String, Double> cm) {
		double precision = cm.get("tp") / (cm.get("tp") + cm.get("fp"));
		return Double.isNaN(precision) ? 0 : precision;
	}

	/**
	 * Computes the recall evaluation metric from a {@code Map} containing a confusion matrix. The {@code Map} should
	 * contain at least the keys "tp" (true positive) and "fn" (false negative). If there are no positive examples in
	 * the test dataset (tp = 0 and fn = 0), a division by 0 will occur. In those cases, the recall is ill-defined and
	 * set to 0.
	 *
	 * @param cm {@code Map} representing a confusion matrix
	 * @return recall
	 */
	public static double recall(Map<String, Double> cm) {
		double recall = cm.get("tp") / (cm.get("tp") + cm.get("fn"));
		return Double.isNaN(recall) ? 0 : recall;
	}

	/**
	 * Compute the F1 score from a {@code Map} containing a confusion matrix. The {@code Map} should contain, at least,
	 * the keys "tp" (true positive), "fp" (false positive) and "fn" (false negative). If there are no positive
	 * examples
	 * in the test dataset (tp = 0 and fn = 0) and no false positives (fp = 0), a division by 0 will occur. In those
	 * cases, the F1 score is ill-defined and set to 0.
	 *
	 * @param cm {@code Map} representing a confusion matrix
	 * @return F1 score
	 */
	public static double f1score(Map<String, Double> cm) {
		double f1Score = 2 * cm.get("tp") / (2 * cm.get("tp") + cm.get("fn") + cm.get("fp"));
		return Double.isNaN(f1Score) ? 0 : f1Score;
	}

	/**
	 * Computes the value of a given evaluation metric for a multi-dimensional classification problem using
	 * macro-averaging (Gil-Begue et al., 2021).
	 *
	 * @param predicted     {@code Prediction} array
	 * @param actualDataset dataset with actual classes
	 * @param metric        evaluation metric
	 * @return result of the evaluation metric
	 */
	public static double macroAveraging(Prediction[] predicted, Dataset actualDataset, Metric metric) {
		// Names of the class variables
		List<String> nameCVs = actualDataset.getNameClassVariables();
		// Number of class variables
		int numCVs = nameCVs.size();
		// Predictions (State objects) for each sequence and class variable
		State[] predictedClasses = Arrays.stream(predicted).map(sequence -> sequence.getPredictedClasses()).toArray(
				State[]::new);
		State[] actualClasses = actualDataset.getStatesClassVariables();
		double metricResult = 0.0;
		for (String nameCV : nameCVs) {
			// Predicted classes of each class variables
			String[] predictedClassesCV = Arrays.stream(predictedClasses).map(
					state -> state.getValueVariable(nameCV)).toArray(String[]::new);
			// Actual classes of the class variables from the State objects
			String[] actualClassesCV = Arrays.stream(actualClasses).map(
					state -> state.getValueVariable(nameCV)).toArray(String[]::new);
			// Obtain possible classes of the class variable
			List<String> possibleClassesCV = actualDataset.getPossibleStatesVariable(nameCV);
			// Apply the metric to the class variable
			double metricResultCV = 0.0;
			if (possibleClassesCV.size() > 2) {
				// Categorical variable
				for (String classCV : possibleClassesCV) {
					// Obtain confusion matrix for each class
					Map<String, Double> cm = getConfusionMatrix(predictedClassesCV, actualClassesCV, classCV);
					// Result of the metric for each class
					metricResultCV += metric.compute(cm);
				}
				metricResultCV /= possibleClassesCV.size();
			} else {
				// Binary variable
				String positiveClass = getPositiveClass(possibleClassesCV);
				logger.trace("Using {} as positive class of '{}'", positiveClass, nameCV);
				Map<String, Double> cm = getConfusionMatrix(predictedClassesCV, actualClassesCV, positiveClass);
				metricResultCV = metric.compute(cm);
			}
			metricResult += metricResultCV;
		}
		metricResult /= numCVs;
		return metricResult;
	}

	/**
	 * Computes the value of a given evaluation metric for a multi-dimensional classification problem using a
	 * micro-averaging (Gil-Begue et al., 2021).
	 *
	 * @param predicted     {@code Prediction} array
	 * @param actualDataset dataset with actual classes
	 * @param metric        evaluation metric
	 * @return result of the evaluation metric
	 */
	public static double microAveraging(Prediction[] predicted, Dataset actualDataset, Metric metric) {
		// Names of the class variables
		List<String> nameCVs = actualDataset.getNameClassVariables();
		// Predictions (State objects) for each sequence and class variable
		State[] predictedClasses = Arrays.stream(predicted).map(sequence -> sequence.getPredictedClasses()).toArray(
				State[]::new);
		State[] actualClasses = actualDataset.getStatesClassVariables();
		// Confusion matrix to keep the combined results of each class variable
		double tp = 0, fp = 0, tn = 0, fn = 0;
		for (String nameCV : nameCVs) {
			// Predicted classes of each class variables
			String[] predictedClassesCV = Arrays.stream(predictedClasses).map(
					state -> state.getValueVariable(nameCV)).toArray(String[]::new);
			// Actual classes of the class variables from the State objects
			String[] actualClassesCV = Arrays.stream(actualClasses).map(
					state -> state.getValueVariable(nameCV)).toArray(String[]::new);
			// Obtain possible classes of the class variable
			List<String> possibleClassesCV = actualDataset.getPossibleStatesVariable(nameCV);
			if (possibleClassesCV.size() > 2) {
				// Categorical variable. If all class variables are multi-class (with the same
				// number of classes), the precision and recall (and therefore F1 score) will be
				// the same
				double tpCV = 0, fpCV = 0, tnCV = 0, fnCV = 0;
				for (String classCV : possibleClassesCV) {
					// Obtain confusion matrix for each class
					Map<String, Double> cm = getConfusionMatrix(predictedClassesCV, actualClassesCV, classCV);
					// Update confusion matrix with results of each class variable and positive
					// class
					tpCV += cm.get("tp");
					fpCV += cm.get("fp");
					tnCV += cm.get("tn");
					fnCV += cm.get("fn");
				}
				tp += tpCV / possibleClassesCV.size();
				fp += fpCV / possibleClassesCV.size();
				tn += tnCV / possibleClassesCV.size();
				fn += fnCV / possibleClassesCV.size();
			} else {
				// Binary variable
				String positiveClass = getPositiveClass(possibleClassesCV);
				logger.trace("Using {} as positive class of '{}'", positiveClass, nameCV);
				Map<String, Double> cm = getConfusionMatrix(predictedClassesCV, actualClassesCV, positiveClass);
				tp += cm.get("tp");
				fp += cm.get("fp");
				tn += cm.get("tn");
				fn += cm.get("fn");
			}
		}
		// Apply the metric to the aggregated confusion matrix
		Map<String, Double> cm = Map.of("tp", tp, "fp", fp, "tn", tn, "fn", fn);
		return metric.compute(cm);
	}

	/**
	 * Returns the positive class of a binary class variable. It is assumed that the class would be either 'True',
	 * 'Positive' or '1'. If it is not possible to determine the positive class, the first one will be returned.
	 *
	 * @param possibleClasses possible classes
	 * @return positive class
	 */
	private static String getPositiveClass(List<String> possibleClasses) {
		for (int i = 0; i < possibleClasses.size(); i++) {
			String classI = possibleClasses.get(i);
			if (classI.equalsIgnoreCase("True") || classI.equalsIgnoreCase("Positive") || classI.equals("1") ||
					classI.contains("A")) {
				return classI;
			}
		}
		return possibleClasses.get(0);
	}

	/**
	 * Obtains a confusion matrix given a list of predicted values and their actual values. It also needs to be
	 * specified which one is the positive class.
	 *
	 * @param predicted     {@code Prediction} array
	 * @param actual        dataset with actual classes
	 * @param positiveClass positive class
	 * @return confusion matrix
	 */
	private static Map<String, Double> getConfusionMatrix(String[] predicted, String[] actual, String positiveClass) {
		// Compute true positives, false positives, true negatives and false negatives
		double tp = 0, fp = 0, tn = 0, fn = 0;
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
		return Map.of("tp", tp, "fp", fp, "tn", tn, "fn", fn);
	}

}