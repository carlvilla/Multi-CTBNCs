package es.upm.fi.cig.multictbnc.classification;

import es.upm.fi.cig.multictbnc.data.representation.State;

import java.util.HashMap;
import java.util.Map;

/**
 * Contains a multidimensional prediction and its probability.
 *
 * @author Carlos Villa Blanco
 */
public class Prediction {
	State predictedClasses;
	double probabilityPrediction;
	Map<State, Double> probabilities;

	double predictionTime;

	/**
	 * Returns the prediction.
	 *
	 * @return prediction a {@code State} object with the prediction
	 */
	public State getPredictedClasses() {
		return this.predictedClasses;
	}

	/**
	 * Returns the prediction time.
	 *
	 * @return prediction time
	 */
	public double getPredictionTime() {
		return this.predictionTime;
	}

	/**
	 * Returns the probabilities of every possible class configuration.
	 *
	 * @return probabilities of each class configuration
	 */
	public Map<State, Double> getProbabilities() {
		return this.probabilities;
	}

	/**
	 * Returns the probability of the prediction.
	 *
	 * @return probability probability of the prediction
	 */
	public double getProbabilityPrediction() {
		return this.probabilityPrediction;
	}

	/**
	 * Sets the predicted classes.
	 *
	 * @param predictedClasses {@code State} object with the predicted classes
	 */
	public void setPredictedClasses(State predictedClasses) {
		this.predictedClasses = predictedClasses;
	}

	/**
	 * Sets the prediction time.
	 *
	 * @param predictionTime prediction time.
	 */
	public void setPredictionTime(double predictionTime) {
		this.predictionTime = predictionTime;
	}

	/**
	 * Sets the probabilities of every possible class configuration.
	 *
	 * @param probabilities probabilities of each class configuration
	 */
	public void setProbabilities(Map<State, Double> probabilities) {
		this.probabilities = probabilities;
	}

	/**
	 * Sets the probability of a class configuration.
	 *
	 * @param classes     a {@code State} object with the class configuration
	 * @param probability probability for the class configuration
	 */
	public void setProbability(State classes, double probability) {
		if (this.probabilities == null)
			this.probabilities = new HashMap<>();
		this.probabilities.put(classes, probability);
	}

	/**
	 * Sets the probability of the prediction.
	 *
	 * @param probabilityPrediction probability of the prediction
	 */
	public void setProbabilityPrediction(double probabilityPrediction) {
		this.probabilityPrediction = probabilityPrediction;
	}
}