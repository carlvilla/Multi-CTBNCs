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
	 * Compute score for a Bayesian network.
	 * 
	 * @param bn
	 * @return score
	 */
	public double compute(BN<? extends Node> bn);

	/**
	 * Get an identifier for the score function.
	 * 
	 * @return identifier for the score function.
	 */
	public String getIdentifier();
	
	/**
	 * Get the name of the penalization applied (if any) to the score function.
	 * 
	 * @return name of the penalization applied to the score function
	 */
	public String getPenalization();
	
}
