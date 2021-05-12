package es.upm.fi.cig.mctbnc.learning.parameters.bn;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.upm.fi.cig.mctbnc.data.representation.Dataset;
import es.upm.fi.cig.mctbnc.nodes.DiscreteNode;

/**
 * Implements the Bayesian estimation to estimate the parameters of a BN. It is
 * assumed a Dirichlet prior distribution over the probabilities of each state
 * of the variables, given the state of their parents, with all of its
 * hyperparameters being equal to "nxHP". Thus, the posterior distribution will
 * be Dirichlet with hyperameters equal to the frequency of each state of the
 * variables plus "nxHP" (Lindstone rule).
 * 
 * @author Carlos Villa Blanco
 *
 */
public class BNBayesianEstimation extends BNParameterLearningAlgorithm {
	// Hyperparameters of the Dirichlet prior distribution
	private double nxHP;
	static Logger logger = LogManager.getLogger(BNBayesianEstimation.class);

	/**
	 * Receives the hyperparameter of the Dirichlet prior distribution over the
	 * parameters (i.e. imaginary counts).
	 * 
	 * @param nxHP number of times the variables are in a certain state while its
	 *             parents take a certain instantiation (hyperparameter)
	 */
	public BNBayesianEstimation(double nxHP) {
		logger.info("Learning parameters of BN with Bayesian estimation (Nx={})", nxHP);
		this.nxHP = nxHP;
	}

	@Override
	protected BNSufficientStatistics getSufficientStatisticsNode(DiscreteNode node, Dataset dataset) {
		BNSufficientStatistics ssNode = new BNSufficientStatistics(this.nxHP);
		ssNode.computeSufficientStatistics(node, dataset);
		return ssNode;
	}

	@Override
	public String getNameMethod() {
		return String.format("Bayesian estimation (Nx:%s)", this.nxHP);
	}

	@Override
	public String getIdentifier() {
		return "Bayesian estimation";
	}

	@Override
	public Map<String, String> getParametersAlgorithm() {
		return Map.of("nx", String.valueOf(this.nxHP));
	}

}
