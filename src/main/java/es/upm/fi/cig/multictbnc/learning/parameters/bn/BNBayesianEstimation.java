package es.upm.fi.cig.multictbnc.learning.parameters.bn;

import es.upm.fi.cig.multictbnc.data.representation.Dataset;
import es.upm.fi.cig.multictbnc.nodes.DiscreteStateNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

/**
 * Implements the Bayesian estimation to estimate the parameters of a BN. It is assumed a Dirichlet prior distribution
 * over the probabilities of each state of the variables, given the state of their parents. The imaginary counts should
 * be defined based on the number of states of the node and its parents, so the total number of imaginary samples is not
 * influenced by the cardinality of those nodes.
 *
 * @author Carlos Villa Blanco
 */
public class BNBayesianEstimation extends BNParameterLearningAlgorithm {
	private static final Logger logger = LogManager.getLogger(BNBayesianEstimation.class);
	// Hyperparameters of the Dirichlet prior distribution
	private final double nxHP;

	/**
	 * Receives the hyperparameter of the Dirichlet prior distribution over the parameters (i.e. imaginary counts).
	 *
	 * @param nxHP number of times the variables are in a certain state while its parents take a certain instantiation
	 *             (hyperparameter)
	 */
	public BNBayesianEstimation(double nxHP) {
		logger.info("Learning parameters of BN with Bayesian estimation (Nx={})", nxHP);
		this.nxHP = nxHP;
	}

	@Override
	public String getIdentifier() {
		return "Bayesian estimation";
	}

	@Override
	public String getNameMethod() {
		return String.format("Bayesian estimation (Nx:%s)", this.nxHP);
	}

	@Override
	public Map<String, String> getParametersAlgorithm() {
		return Map.of("nx", String.valueOf(this.nxHP));
	}

	@Override
	protected BNSufficientStatistics getSufficientStatisticsNode(DiscreteStateNode node, Dataset dataset) {
		BNSufficientStatistics ssNode = new BNSufficientStatistics(this.nxHP);
		ssNode.computeSufficientStatistics(node, dataset);
		return ssNode;
	}

}