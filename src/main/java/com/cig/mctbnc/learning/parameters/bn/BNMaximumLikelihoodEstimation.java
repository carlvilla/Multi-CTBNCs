package com.cig.mctbnc.learning.parameters.bn;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.nodes.DiscreteNode;

/**
 * Maximum likelihood estimation of BN parameters.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class BNMaximumLikelihoodEstimation extends BNParameterLearningAlgorithm {
	static Logger logger = LogManager.getLogger(BNMaximumLikelihoodEstimation.class);

	@Override
	protected BNSufficientStatistics getSufficientStatisticsNode(DiscreteNode node, Dataset dataset) {
		BNSufficientStatistics ssNode = new BNSufficientStatistics(0);
		ssNode.computeSufficientStatistics(node, dataset);
		return ssNode;
	}

	@Override
	public String getNameMethod() {
		return "Maximum likelihood estimation";
	}

}
