package es.upm.fi.cig.multictbnc.learning.structure.optimisation.hillclimbing.implementation;

import es.upm.fi.cig.multictbnc.learning.structure.optimisation.hillclimbing.HillClimbingSolution;
import es.upm.fi.cig.multictbnc.models.PGM;
import es.upm.fi.cig.multictbnc.nodes.Node;

import java.util.List;
import java.util.Map;

/**
 * Defines an interface for different implementations of the hill climbing algorithm. These can be implementations for
 * Bayesian networks, continuous time Bayesian networks, decomposable score functions, etc.
 *
 * @author Carlos Villa Blanco
 */
public interface HillClimbingImplementation {

	/**
	 * Finds a structure for a given PGM.
	 *
	 * @param pgm a probabilistic graphical model
	 * @return solution given by the hill climbing algorithm
	 */
	HillClimbingSolution findStructure(PGM<? extends Node> pgm);

	/**
	 * Finds the local structure of a given node of a PGM.
	 *
	 * @param pgm     a probabilistic graphical model
	 * @param idxNode node index
	 * @return a {@code HillClimbingSolution}
	 */
	HillClimbingSolution findStructure(PGM<? extends Node> pgm, int idxNode);

	/**
	 * Finds the local structure of some given nodes of a PGM.
	 *
	 * @param pgm      a probabilistic graphical model
	 * @param idxNodes node indexes
	 * @return a {@code HillClimbingSolution}
	 */
	HillClimbingSolution findStructure(PGM<? extends Node> pgm, List<Integer> idxNodes);

	/**
	 * Returns a unique identifier for the hill climbing-based algorithm.
	 *
	 * @return unique identifier for the hill climbing-based algorithm
	 */
	String getIdentifier();

	/**
	 * Returns a {@code Map} with the name of the score function that is optimised and the name of the applied
	 * penalisation function (if any).
	 *
	 * @return a {@code Map} with the name of the score function that is optimised and the name of the penalisation
	 * function that is applied
	 */
	Map<String, String> getInfoScoreFunction();

	/**
	 * Returns the parameters that are used by the hill climbing implementation.
	 *
	 * @return a {@code Map} with the parameters used by the algorithm
	 */
	Map<String, String> getParametersAlgorithm();

}