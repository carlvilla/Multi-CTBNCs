package com.cig.mctbnc.classification;

/**
 * Contains a multidimensional prediction and its probability.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class Prediction {
	String[] prediction;
	double probability;

	/**
	 * Constructor that receives a prediction and its probability.
	 * 
	 * @param prediction
	 * @param probability
	 */
	public Prediction(String[] prediction, double probability) {
		this.prediction = prediction;
		this.probability = probability;
	}

	/**
	 * Return the prediction.
	 * 
	 * @return prediction
	 */
	public String[] getPrediction() {
		return prediction;
	}

	/**
	 * Return probability of the prediction.
	 * 
	 * @return probability
	 */
	public double getProbability() {
		return probability;
	}

}
