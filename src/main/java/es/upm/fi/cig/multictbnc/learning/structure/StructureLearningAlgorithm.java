package es.upm.fi.cig.multictbnc.learning.structure;

import java.util.Map;

import es.upm.fi.cig.multictbnc.models.PGM;
import es.upm.fi.cig.multictbnc.nodes.Node;

/**
 * 
 * Interface used to define algorithms for learning the structure of PGMs.
 * 
 * @author Carlos Villa Blanco
 *
 */
public interface StructureLearningAlgorithm {

	/**
	 * Learns the structure of a certain PGM.
	 * 
	 * @param pgm a probabilistic graphical model
	 */
	public void learn(PGM<? extends Node> pgm);

	/**
	 * Returns a unique identifier for the structure learning algorithm.
	 * 
	 * @return unique identifier for the structure learning algorithm
	 */
	public String getIdentifier();

	/**
	 * Returns the parameters that are used by the algorithm.
	 * 
	 * @return a {@code Map} with the parameters used by the algorithm
	 */
	public Map<String, String> getParametersAlgorithm();

}
