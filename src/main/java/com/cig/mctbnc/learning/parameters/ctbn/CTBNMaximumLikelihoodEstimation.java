package com.cig.mctbnc.learning.parameters.ctbn;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.nodes.Node;

/**
 * Maximum likelihood estimation of CTBN parameters.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class CTBNMaximumLikelihoodEstimation extends CTBNParameterLearningAlgorithm {

	@Override
	protected CTBNSufficientStatistics getSufficientStatisticsNode(Node node, Dataset dataset) {
		CTBNSufficientStatistics ssNode = new CTBNSufficientStatistics(0, 0);
		ssNode.computeSufficientStatistics(node, dataset);
		return ssNode;
	}
	
	@Override
	public String getNameMethod() {
		return "Maximum likelihood estimation";
	}

}
