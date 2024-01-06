package es.upm.fi.cig.multictbnc.learning.parameters.ctbn;

/**
 * Builds the specified parameter learning algorithm for a CTBN.
 *
 * @author Carlos Villa Blanco
 */
public class CTBNParameterLearningAlgorithmFactory {

	/**
	 * Builds the specified parameter learning algorithm.
	 *
	 * @param algorithm name of the parameter learning algorithm
	 * @param mxyHP     number of times a variable transitions from a certain state to another one (hyperparameter)
	 * @param txHP      time that a variable stays in a certain state (hyperparameter)
	 * @return parameter learning algorithm
	 */
	public static CTBNParameterLearningAlgorithm getAlgorithm(String algorithm, Double mxyHP, Double txHP) {
		switch (algorithm) {
			case ("Bayesian estimation"):
				return new CTBNBayesianEstimation(mxyHP, txHP);
			default:
				// Maximum likelihood estimation
				return new CTBNMaximumLikelihoodEstimation();
		}
	}

}