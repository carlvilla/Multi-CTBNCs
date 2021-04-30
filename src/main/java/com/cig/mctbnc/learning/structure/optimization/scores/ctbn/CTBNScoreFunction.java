package com.cig.mctbnc.learning.structure.optimization.scores.ctbn;

import com.cig.mctbnc.models.CTBN;
import com.cig.mctbnc.nodes.Node;

/**
 * Interface used to define scores for continuous time Bayesian networks.
 * 
 * @author Carlos Villa Blanco
 *
 */
public interface CTBNScoreFunction {

	/**
	 * Compute score for provided continuous time Bayesian network.
	 * 
	 * @param ctbn
	 * @return score
	 */
	public double compute(CTBN<? extends Node> ctbn);

	/**
	 * Compute score for provided continuous time Bayesian network at defined node.
	 * 
	 * @param ctbn
	 * @param nodeIndex
	 * @return score
	 */
	public double compute(CTBN<? extends Node> ctbn, int nodeIndex);

	/**
	 * Determine if the score is decomposable.
	 * 
	 * @return true if the score is decomposable, false otherwise
	 */
	public boolean isDecomposable();
	
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
