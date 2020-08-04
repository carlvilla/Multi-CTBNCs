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

}
