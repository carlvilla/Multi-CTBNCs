package es.upm.fi.cig.mctbnc.learning.parameters.bn;

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
	 * @param NxHP
	 * @return parameter learning algorithm
	 */
	public static BNParameterLearningAlgorithm getAlgorithm(String algorithm, Double NxHP) {
		switch (algorithm) {
		case ("Bayesian estimation"):
			return new BNBayesianEstimation(NxHP);
		default:
			// Maximum likelihood estimation
			return new BNMaximumLikelihoodEstimation();
		}
	}

}
