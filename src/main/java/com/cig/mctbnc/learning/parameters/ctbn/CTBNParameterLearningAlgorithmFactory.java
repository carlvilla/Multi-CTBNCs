package com.cig.mctbnc.learning.parameters.ctbn;

/**
 * Builds the specified parameter learning algorithm for a CTBN.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class CTBNParameterLearningAlgorithmFactory {

	/**
	 * Build the specified parameter learning algorithm.
	 * 
	 * @param algorithm
	 * @param nxy
	 * @param tx
	 * @return parameter learning algorithm
	 */
	public static CTBNParameterLearningAlgorithm getAlgorithm(String algorithm, Double nxy, Double tx) {
		switch (algorithm) {
		case ("Bayesian estimation"):
			return new CTBNBayesianEstimation(nxy, tx);
		default:
			// Maximum likelihood estimation
			return new CTBNMaximumLikelihoodEstimation();
		}
	}

}