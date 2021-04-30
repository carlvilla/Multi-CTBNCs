package com.cig.mctbnc.learning.parameters.bn;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.nodes.DiscreteNode;

/**
 * Implements the Bayesian estimation to estimate the parameters of a BN. It is
 * assumed a Dirichlet prior distribution over the probabilities of each state
 * of the variables, given the state of their parents, with all of its
 * hyperparameters being equal to "NxHP". Thus, the posterior distribution will
 * be Dirichlet with hyperameters equal to the frequency of each state of the
 * variables plus "NxHP" (Lindstone rule).
 * 
 * @author Carlos Villa Blanco
 *
 */
public class BNBayesianEstimation extends BNParameterLearningAlgorithm {
	// Hyperparameters of the Dirichlet prior distribution
	private double NxHP;
	static Logger logger = LogManager.getLogger(BNBayesianEstimation.class);

	/**
	 * Receive the hyperparameter of the Dirichlet prior distribution over the
	 * parameters (i.e. imaginary counts).
	 * 
	 * @param NxHP
	 */
	public BNBayesianEstimation(double NxHP) {
		logger.info("Learning parameters of BN with Bayesian estimation (hyperparameter={})", NxHP);
		this.NxHP = NxHP;
	}

	@Override
	protected BNSufficientStatistics getSufficientStatisticsNode(DiscreteNode node, Dataset dataset) {
		BNSufficientStatistics ssNode = new BNSufficientStatistics(this.NxHP);
		ssNode.computeSufficientStatistics(node, dataset);
		return ssNode;
	}

	@Override
	public String getNameMethod() {
		return String.format("Bayesian estimation (Nx:%s)", this.NxHP);
	}

	@Override
	public String getIdentifier() {
		return "Bayesian estimation";
	}

	@Override
	public Map<String, String> getParametersAlgorithm() {
		return Map.of("nx", String.valueOf(this.NxHP));
	}

}
