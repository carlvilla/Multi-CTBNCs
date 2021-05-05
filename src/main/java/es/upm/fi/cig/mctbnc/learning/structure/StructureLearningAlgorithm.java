package es.upm.fi.cig.mctbnc.learning.structure;

import java.util.Map;

import es.upm.fi.cig.mctbnc.models.PGM;
import es.upm.fi.cig.mctbnc.nodes.Node;

/**
 * 
 * Interface used to define algorithms for learning the structure of PGMs.
 * 
 * @author Carlos Villa Blanco
 *
 */
public interface StructureLearningAlgorithm {

	/**
	 * Learn the structure of a certain PGM
	 * 
	 * @param pgm
	 */
	public void learn(PGM<? extends Node> pgm);

	/**
	 * Return a unique identifier for the structure learning algorithm.
	 * 
	 * @return unique identifier for the structure learning algorithm
	 */
	public String getIdentifier();

	/**
	 * Return the parameters that are used by the algorithm.
	 * 
	 * @return a {@code Map} with the parameters used by the algorithm
	 */
	public Map<String, String> getParametersAlgorithm();

}
