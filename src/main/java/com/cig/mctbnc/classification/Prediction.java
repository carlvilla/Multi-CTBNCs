package com.cig.mctbnc.classification;

import java.util.HashMap;
import java.util.Map;

import com.cig.mctbnc.data.representation.State;

/**
 * Contains a multidimensional prediction and its probability.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class Prediction {
	State predictedClasses;
	double probabilityPrediction;
	Map<State, Double> probabilities;

	/**
	 * Set the predicted classes
	 * 
	 * @param predictedClasses
	 */
	public void setPredictedClasses(State predictedClasses) {
		this.predictedClasses = predictedClasses;
	}

	/**
	 * Set the probability of the prediction.
	 * 
	 * @param probabilityPrediction
	 */
	public void setProbabilityPrediction(double probabilityPrediction) {
		this.probabilityPrediction = probabilityPrediction;
	}

	/**
	 * Set the probability of the specified classes.
	 * 
	 * @param classes
	 * @param probability
	 */
	public void setProbability(State classes, double probability) {
		if (probabilities == null)
			probabilities = new HashMap<State, Double>();
		probabilities.put(classes, probability);
	}

	/**
	 * Return the prediction.
	 * 
	 * @return prediction
	 */
	public State getPredictedClasses() {
		return predictedClasses;
	}

	/**
	 * Return the probability of the prediction.
	 * 
	 * @return probability
	 */
	public double getProbabilityPrediction() {
		return probabilityPrediction;
	}

	/**
	 * Return the probabilities of every possible classes.
	 * 
	 * @return probability
	 */
	public Map<State, Double> getProbabilities() {
		return probabilities;
	}
}
