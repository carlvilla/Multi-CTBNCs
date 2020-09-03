package com.cig.mctbnc.learning.parameters.bn;

/**
 * Builds the specified parameter learning algorithm for a BN.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class BNParameterLearningAlgorithmFactory {

	/**
	 * Build the specified parameter learning algorithm.
	 * 
	 * @param algorithm
	 * @param alpha
	 * @return parameter learning algorithm
	 */
	public static BNParameterLearningAlgorithm getAlgorithm(String algorithm, Double alpha) {
		switch (algorithm) {
		case ("Bayesian estimation"):
			return new BNBayesianEstimation(alpha);
		default:
			// Maximum likelihood estimation
			return new BNMaximumLikelihoodEstimation();
		}
	}

}
