package es.upm.fi.cig.multictbnc.learning.structure.optimization.scores.ctbn;

import es.upm.fi.cig.multictbnc.models.CTBN;
import es.upm.fi.cig.multictbnc.nodes.Node;

/**
 * Interface used to define scores for continuous time Bayesian networks.
 * 
 * @author Carlos Villa Blanco
 *
 */
public interface CTBNScoreFunction {

	/**
	 * Computes the score for a continuous time Bayesian network.
	 * 
	 * @param ctbn a continuous time Bayesian network
	 * @return score
	 */
	public double compute(CTBN<? extends Node> ctbn);

	/**
	 * Computes the score for a continuous time Bayesian network at a given node.
	 * 
	 * @param ctbn      a continuous time Bayesian network
	 * @param nodeIndex node index
	 * @return score
	 */
	public double compute(CTBN<? extends Node> ctbn, int nodeIndex);

	/**
	 * Determines if the score is decomposable.
	 * 
	 * @return true if the score is decomposable, false otherwise
	 */
	public boolean isDecomposable();

	/**
	 * Gets an identifier for the score function.
	 * 
	 * @return identifier for the score function.
	 */
	public String getIdentifier();

	/**
	 * Gets the name of the penalization applied (if any) to the score function.
	 * 
	 * @return name of the penalization applied to the score function
	 */
	public String getPenalization();

}
