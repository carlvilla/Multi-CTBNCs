package com.cig.mctbnc.learning.parameters.bn;

import java.util.List;
import java.util.Map;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.data.representation.State;
import com.cig.mctbnc.learning.parameters.ParameterLearningAlgorithm;
import com.cig.mctbnc.nodes.CPTNode;
import com.cig.mctbnc.nodes.Node;

public abstract class BNParameterEstimation implements ParameterLearningAlgorithm {

	@Override
	public void learn(List<? extends Node> nodes, Dataset dataset) {
		setSufficientStatistics(nodes, dataset);
		setCPTs(nodes);
	}

	/**
	 * Obtain for each variable i, the number of times its parents are in the state
	 * j and the variable is in the state k.
	 * 
	 * @param nodes
	 * @param dataset
	 */
	public void setSufficientStatistics(List<? extends Node> nodes, Dataset dataset) {
		for (int i = 0; i < nodes.size(); i++) {
			BNSufficientStatistics ss = new BNSufficientStatistics();
			ss.computeSufficientStatistics(nodes.get(i), dataset);
			nodes.get(i).setSufficientStatistics(ss);
		}
	}

	public void setCPTs(List<? extends Node> nodes) {
		// For each node it is created a new type of node that contains the CPTs.
		for (int i = 0; i < nodes.size(); i++) {
			// The node has to have a CPT
			CPTNode node = (CPTNode) nodes.get(i);
			// Compute the parameters for the current node with its sufficient statistics
			Map<State, Double> CPT = estimateCPT(node, node.getSufficientStatistics());
			// CPTNode stores the computed CPT
			node.setCPT(CPT);
		}
	}
	
	protected abstract Map<State, Double> estimateCPT(CPTNode node, Map<State, Integer> ss);

}
