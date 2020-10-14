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
	private double NxyHP;
	private double TxHP;

	/**
	 * Constructor Bayesian parameter estimator for CTBNs.
	 * 
	 * @param NxyHP
	 * @param TxHP
	 */
	public CTBNBayesianEstimation(double NxyHP, double TxHP) {
		logger.info("Learning parameters of CTBN with Bayesian estimation (Nxy={}, Tx={})", NxyHP, TxHP);
		// Definition of imaginary counts of the hyperparameters
		this.NxyHP = NxyHP;
		this.TxHP = TxHP;
	}

	@Override
	protected CTBNSufficientStatistics getSufficientStatisticsNode(Node node, Dataset dataset) {
		CTBNSufficientStatistics ssNode = new CTBNSufficientStatistics(NxyHP, TxHP);
		ssNode.computeSufficientStatistics(node, dataset);
		return ssNode;
	}

}
