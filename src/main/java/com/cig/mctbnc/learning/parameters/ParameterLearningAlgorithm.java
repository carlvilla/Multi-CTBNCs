package com.cig.mctbnc.learning.parameters;

import java.util.List;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.nodes.Node;

public interface ParameterLearningAlgorithm {

	/**
	 * Learn the parameters of a certain PGM.
	 * 
	 * @param nodes
	 * @param dataset
	 */
	public void learn(List<? extends Node> nodes, Dataset dataset);

	/**
	 * Learn the parameters of a certain node of a PGM.
	 * 
	 * @param node
	 * @param dataset
	 */
	public void learn(Node node, Dataset dataset);

	/**
	 * Get the name of the method to learn the parameters.
	 * 
	 * @return name of the method to learn the parameters
	 */
	public String getNameMethod();

}
