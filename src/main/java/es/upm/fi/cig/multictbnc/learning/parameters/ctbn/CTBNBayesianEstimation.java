package es.upm.fi.cig.multictbnc.learning.parameters.ctbn;

import es.upm.fi.cig.multictbnc.data.representation.Dataset;
import es.upm.fi.cig.multictbnc.nodes.DiscreteNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

/**
 * Bayesian parameter estimation for a discrete CTBN. The imaginary counts should be defined based on the number of
 * states of the node and its parents, so the total number of imaginary samples is not influenced by the cardinality of
 * those nodes.
 *
 * @author Carlos Villa Blanco
 */
public class CTBNBayesianEstimation extends CTBNParameterLearningAlgorithm {
	private static final Logger logger = LogManager.getLogger(CTBNBayesianEstimation.class);
	// Hyperparameters Dirichlet prior distribution
	double mxyHP;
	double txHP;

	/**
	 * Constructs a {@code CTBNBayesianEstimation} for the Bayesian estimation of the parameters of a discrete CTBN.
	 *
	 * @param mxyHP number of times a variable transitions from a certain state to another one while its parents take a
	 *              certain instantiation (hyperparameter)
	 * @param txHP  time that a variable stays in a certain state while its parents take a certain instantiation
	 *              (hyperparameter)
	 */
	public CTBNBayesianEstimation(double mxyHP, double txHP) {
		logger.info("Learning parameters of CTBN with Bayesian estimation (Mxy={}, Tx={})", mxyHP, txHP);
		// Definition of imaginary counts of the hyperparameters
		this.mxyHP = mxyHP;
		this.txHP = txHP;
	}

	@Override
	public String getIdentifier() {
		return "Bayesian estimation";
	}

	@Override
	public String getNameMethod() {
		return String.format("Bayesian estimation (Mxy:%s, Tx:%s)", this.mxyHP, this.txHP);
	}

	@Override
	public Map<String, String> getParametersAlgorithm() {
		return Map.of("mxy", String.valueOf(this.mxyHP), "tx", String.valueOf(this.txHP));
	}

	@Override
	protected CTBNSufficientStatistics getSufficientStatisticsNode(DiscreteNode node, Dataset dataset) {
		CTBNSufficientStatistics ssNode = new CTBNSufficientStatistics(this.mxyHP, this.txHP);
		ssNode.computeSufficientStatistics(node, dataset);
		return ssNode;
	}

}