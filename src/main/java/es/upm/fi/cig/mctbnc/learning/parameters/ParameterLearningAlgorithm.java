package es.upm.fi.cig.mctbnc.learning.parameters;

import java.util.List;
import java.util.Map;

import es.upm.fi.cig.mctbnc.data.representation.Dataset;
import es.upm.fi.cig.mctbnc.nodes.Node;

/**
 * Interface for parameter learning algorithms.
 * 
 * @author Carlos Villa Blanco
 *
 */
public interface ParameterLearningAlgorithm {

	/**
	 * Learns the parameters of a certain PGM.
	 * 
	 * @param nodes   nodes of the PGM
	 * @param dataset dataset used to learn the parameters
	 */
	public void learn(List<? extends Node> nodes, Dataset dataset);

	/**
	 * Learns the parameters of a certain node of a PGM.
	 * 
	 * @param node    node of the PGM
	 * @param dataset dataset used to learn the parameters
	 */
	public void learn(Node node, Dataset dataset);

	/**
	 * Gets the name of the method to learn the parameters.
	 * 
	 * @return name of the method to learn the parameters
	 */
	public String getNameMethod();

	/**
	 * Returns a unique identifier for the parameter learning algorithm.
	 * 
	 * @return unique identifier for the parameter learning algorithm
	 */
	public String getIdentifier();

	/**
	 * Returns the parameters that are used by the algorithm.
	 * 
	 * @return a {@code Map} with the parameters used by the algorithm
	 */
	public Map<String, String> getParametersAlgorithm();

}
