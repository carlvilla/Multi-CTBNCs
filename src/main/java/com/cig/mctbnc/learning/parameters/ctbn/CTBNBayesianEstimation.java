package com.cig.mctbnc.learning.parameters.ctbn;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.nodes.Node;

/**
 * Bayesian parameter estimation for CTBN.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class CTBNBayesianEstimation extends CTBNParameterLearningAlgorithm {
	// Hyperparameters Dirichlet prior distribution
	private double NxyPrior;
	private double TxPrior;

	/**
	 * Constructor Bayesian parameter estimator for CTBNs.
	 * 
	 * @param NxyPrior
	 * @param TxPrior
	 */
	public CTBNBayesianEstimation(double NxyPrior, double TxPrior) {
		logger.info("Learning parameters of CTBN with Bayesian estimation (Nxy={}, Tx={})", NxyPrior, TxPrior);
		// Definition of imaginary counts of the hyperparameters
		this.NxyPrior = NxyPrior;
		this.TxPrior = TxPrior;
	}

	@Override
	protected CTBNSufficientStatistics getSufficientStatisticsNode(Node node, Dataset dataset) {
		CTBNSufficientStatistics ssNode = new CTBNSufficientStatistics(NxyPrior, TxPrior);
		ssNode.computeSufficientStatistics(node, dataset);
		return ssNode;
	}

}
