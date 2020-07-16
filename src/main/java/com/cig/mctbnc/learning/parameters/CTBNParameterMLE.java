package com.cig.mctbnc.learning.parameters;

import java.util.List;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.nodes.Node;

public class CTBNParameterMLE implements ParameterLearningAlgorithm{

	@Override
	public void learn(List<Node> nodes, Dataset dataset) {
		// TODO Auto-generated method stub
		
	}
		
	/**
	 * Obtain the sufficient statistics of a CTBN
	 * @param nodes
	 * @param dataset
	 * @return sufficient statistics for each variable
	 */
	protected CTBNSufficientStatistics[] sufficientStatistics(List<Node> nodes, Dataset dataset) {
		int numNodes = nodes.size();
		CTBNSufficientStatistics[] ss = new CTBNSufficientStatistics[numNodes];

		for (int i = 0; i < numNodes; i++) {
			CTBNSufficientStatistics ssNode = new CTBNSufficientStatistics(nodes.get(i));
			ssNode.computeSufficientStatistics(dataset);
			ss[i] = ssNode;
		}
		return ss;
	}

	@Override
	public List<? extends Node> getParameters() {
		// TODO Auto-generated method stub
		return null;
	}

}
