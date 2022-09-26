package es.upm.fi.cig.multictbnc.learning.structure.optimisation.scores.ctbn;

import es.upm.fi.cig.multictbnc.models.CTBN;
import es.upm.fi.cig.multictbnc.nodes.Node;

/**
 * Interface used to define scores for continuous-time Bayesian networks.
 *
 * @author Carlos Villa Blanco
 */
public interface CTBNScoreFunction {

	/**
	 * Computes the score for a continuous-time Bayesian network.
	 *
	 * @param ctbn a continuous-time Bayesian network
	 * @return score
	 */
	double compute(CTBN<? extends Node> ctbn);

	/**
	 * Computes the score of a continuous-time Bayesian network at a given node.
	 *
	 * @param ctbn      a continuous-time Bayesian network
	 * @param nodeIndex node index
	 * @return score
	 */
	double compute(CTBN<? extends Node> ctbn, int nodeIndex);

	/**
	 * Gets an identifier for the score function.
	 *
	 * @return identifier for the score function.
	 */
	String getIdentifier();

	/**
	 * Gets the name of the penalisation applied (if any) to the score function.
	 *
	 * @return name of the penalisation applied to the score function
	 */
	String getNamePenalisationFunction();

	/**
	 * Determines if the score is decomposable.
	 *
	 * @return true if the score is decomposable, false otherwise
	 */
	boolean isDecomposable();

}