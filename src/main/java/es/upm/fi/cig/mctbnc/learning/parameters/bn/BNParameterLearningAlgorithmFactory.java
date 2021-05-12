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
	 * @param algorithm name of the parameter learning algorithm
	 * @param nxHP      number of times the variables are in a certain state while
	 *                  its parents take a certain instantiation (hyperparameter)
	 * @return parameter learning algorithm
	 */
	public static BNParameterLearningAlgorithm getAlgorithm(String algorithm, Double nxHP) {
		switch (algorithm) {
		case ("Bayesian estimation"):
			return new BNBayesianEstimation(nxHP);
		default:
			// Maximum likelihood estimation
			return new BNMaximumLikelihoodEstimation();
		}
	}

}
