package es.upm.fi.cig.multictbnc.learning.parameters;

import es.upm.fi.cig.multictbnc.data.representation.Dataset;
import es.upm.fi.cig.multictbnc.nodes.Node;

import java.util.List;
import java.util.Map;

/**
 * Interface for parameter learning algorithms.
 *
 * @author Carlos Villa Blanco
 */
public interface ParameterLearningAlgorithm {

	/**
	 * Returns a unique identifier for the parameter learning algorithm.
	 *
	 * @return unique identifier for the parameter learning algorithm
	 */
	String getIdentifier();

	/**
	 * Gets the name of the method to learn the parameters.
	 *
	 * @return name of the method to learn the parameters
	 */
	String getNameMethod();

	/**
	 * Returns the parameters that are used by the algorithm.
	 *
	 * @return a {@code Map} with the parameters used by the algorithm
	 */
	Map<String, String> getParametersAlgorithm();

	/**
	 * Learns the parameters of a certain node of a PGM.
	 *
	 * @param node    node of the PGM
	 * @param dataset dataset used to learn the parameters
	 */
	void learn(Node node, Dataset dataset);

	/**
	 * Learns the parameters of a certain PGM.
	 *
	 * @param nodes   nodes of the PGM
	 * @param dataset dataset used to learn the parameters
	 */
	void learn(List<? extends Node> nodes, Dataset dataset);

	/**
	 * Obtains the sufficient statistics of a BN node.
	 *
	 * @param node    node whose sufficient statistics will be computed
	 * @param dataset dataset used to compute the sufficient statistics
	 */
	void setSufficientStatistics(Node node, Dataset dataset);

}