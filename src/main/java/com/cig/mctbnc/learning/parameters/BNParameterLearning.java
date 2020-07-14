package com.cig.mctbnc.learning.parameters;

import java.util.List;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.nodes.Node;

public interface BNParameterLearning {
	
	public void learn(List<Node> nodes, Dataset dataset);
	
	/**
	 * Return a list of nodes. They should contain the learned parameters, which 
	 * are obtained calling the learn function.
	 * @return
	 */
	public List<? extends Node> getParameters();

}
