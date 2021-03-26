package com.cig.mctbnc.learning.parameters.ctbn;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.nodes.DiscreteNode;

/**
 * Maximum likelihood estimation of CTBN parameters.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class CTBNMaximumLikelihoodEstimation extends CTBNParameterLearningAlgorithm {

	@Override
	protected CTBNSufficientStatistics getSufficientStatisticsNode(DiscreteNode node, Dataset dataset) {
		CTBNSufficientStatistics ssNode = new CTBNSufficientStatistics(0, 0);
		ssNode.computeSufficientStatistics(node, dataset);
		return ssNode;
	}

	@Override
	public String getNameMethod() {
		return "Maximum likelihood estimation";
	}

}
