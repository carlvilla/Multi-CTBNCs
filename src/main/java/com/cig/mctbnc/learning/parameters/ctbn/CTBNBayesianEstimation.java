package com.cig.mctbnc.learning.parameters.ctbn;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.nodes.Node;

/**
 * Bayesian parameter estimation for CTBN. It is assumed all of the
 * hyperparameters to be equal to "MxyHP" or "TxHP" (Lindstone rule).
 * 
 * @author Carlos Villa Blanco
 *
 */
public class CTBNBayesianEstimation extends CTBNParameterLearningAlgorithm {
	// Hyperparameters Dirichlet prior distribution
	private double MxyHP;
	private double TxHP;

	/**
	 * Constructor Bayesian parameter estimator for CTBNs.
	 * 
	 * @param MxyHP
	 * @param TxHP
	 */
	public CTBNBayesianEstimation(double MxyHP, double TxHP) {
		logger.info("Learning parameters of CTBN with Bayesian estimation (Nxy={}, Tx={})", MxyHP, TxHP);
		// Definition of imaginary counts of the hyperparameters
		this.MxyHP = MxyHP;
		this.TxHP = TxHP;
	}

	@Override
	protected CTBNSufficientStatistics getSufficientStatisticsNode(Node node, Dataset dataset) {
		CTBNSufficientStatistics ssNode = new CTBNSufficientStatistics(MxyHP, TxHP);
		ssNode.computeSufficientStatistics(node, dataset);
		return ssNode;
	}

	@Override
	public String getNameMethod() {
		return String.format("Bayesian estimation (Mxy:%s, TxHP:%s)", MxyHP, TxHP);
	}

}