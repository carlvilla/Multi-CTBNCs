package es.upm.fi.cig.multictbnc.learning.parameters.ctbn;

import java.util.Map;

import es.upm.fi.cig.multictbnc.data.representation.Dataset;
import es.upm.fi.cig.multictbnc.nodes.DiscreteNode;

/**
 * Bayesian parameter estimation for a discrete CTBN. It is assumed all of the
 * hyperparameters to be equal to "mxyHP" or "txHP" (Lindstone rule).
 * 
 * @author Carlos Villa Blanco
 *
 */
public class CTBNBayesianEstimation extends CTBNParameterLearningAlgorithm {
	// Hyperparameters Dirichlet prior distribution
	private double mxyHP;
	private double txHP;

	/**
	 * Constructs a {@code CTBNBayesianEstimation} for the Bayesian estimation of
	 * the parameters of a discrete CTBN.
	 * 
	 * @param mxyHP number of times a variable transitions from a certain state to
	 *              another one while its parents take a certain instantiation
	 *              (hyperparameter)
	 * @param txHP  time that a variable stays in a certain state while its parents
	 *              take a certain instantiation (hyperparameter)
	 */
	public CTBNBayesianEstimation(double mxyHP, double txHP) {
		logger.info("Learning parameters of CTBN with Bayesian estimation (Mxy={}, Tx={})", mxyHP, txHP);
		// Definition of imaginary counts of the hyperparameters
		this.mxyHP = mxyHP;
		this.txHP = txHP;
	}

	@Override
	protected CTBNSufficientStatistics getSufficientStatisticsNode(DiscreteNode node, Dataset dataset) {
		CTBNSufficientStatistics ssNode = new CTBNSufficientStatistics(this.mxyHP, this.txHP);
		ssNode.computeSufficientStatistics(node, dataset);
		return ssNode;
	}

	@Override
	public String getNameMethod() {
		return String.format("Bayesian estimation (Mxy:%s, Tx:%s)", this.mxyHP, this.txHP);
	}

	@Override
	public String getIdentifier() {
		return "Bayesian estimation";
	}

	@Override
	public Map<String, String> getParametersAlgorithm() {
		return Map.of("mxy", String.valueOf(this.mxyHP), "tx", String.valueOf(this.txHP));
	}

}