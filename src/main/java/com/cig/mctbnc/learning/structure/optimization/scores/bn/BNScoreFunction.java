package com.cig.mctbnc.learning.structure.optimization.scores.bn;

import com.cig.mctbnc.models.BN;
import com.cig.mctbnc.nodes.Node;

/**
 * Interface used to define scores for Bayesian networks.
 * 
 * @author Carlos Villa Blanco
 *
 */
public interface BNScoreFunction {

	/**
	 * Compute score for provided Bayesian network.
	 * 
	 * @param bn
	 * @return score
	 */
	public double compute(BN<? extends Node> bn);

}
