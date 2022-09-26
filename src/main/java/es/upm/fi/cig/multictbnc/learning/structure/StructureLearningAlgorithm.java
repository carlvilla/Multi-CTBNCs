package es.upm.fi.cig.multictbnc.learning.structure;

import es.upm.fi.cig.multictbnc.exceptions.ErroneousValueException;
import es.upm.fi.cig.multictbnc.models.PGM;
import es.upm.fi.cig.multictbnc.nodes.Node;

import java.util.List;
import java.util.Map;

/**
 * Interface used to define algorithms for learning the structure of PGMs.
 *
 * @author Carlos Villa Blanco
 */
public interface StructureLearningAlgorithm {

	/**
	 * Returns a unique identifier for the structure learning algorithm.
	 *
	 * @return unique identifier for the structure learning algorithm
	 */
	String getIdentifier();

	/**
	 * Returns the parameters that are used by the algorithm.
	 *
	 * @return a {@code Map} with the parameters used by the algorithm
	 */
	Map<String, String> getParametersAlgorithm();

	/**
	 * Learns the local structure of certain nodes of a PGM.
	 *
	 * @param pgm      a probabilistic graphical model
	 * @param idxNodes node indexes
	 */
	void learn(PGM<? extends Node> pgm, List<Integer> idxNodes);

	/**
	 * Learns the structure of a certain PGM.
	 *
	 * @param pgm a probabilistic graphical model
	 * @throws ErroneousValueException if a parameter provided is invalid for the requested task
	 */
	void learn(PGM<? extends Node> pgm) throws ErroneousValueException;

	/**
	 * Learn the local structure of a certain node of a PGM.
	 *
	 * @param pgm     a probabilistic graphical model
	 * @param idxNode node index
	 */
	void learn(PGM<? extends Node> pgm, int idxNode);

}