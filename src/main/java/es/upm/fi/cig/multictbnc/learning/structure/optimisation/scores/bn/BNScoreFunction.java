package es.upm.fi.cig.multictbnc.learning.structure.optimisation.scores.bn;

import es.upm.fi.cig.multictbnc.models.BN;
import es.upm.fi.cig.multictbnc.nodes.Node;

/**
 * Interface used to define scores for Bayesian networks.
 *
 * @author Carlos Villa Blanco
 */
public interface BNScoreFunction {

	/**
	 * Computes the score of a Bayesian network.
	 *
	 * @param bn a Bayesian network
	 * @return score
	 */
	double compute(BN<? extends Node> bn);

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

}