package es.upm.fi.cig.mctbnc.learning.parameters.bn;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.upm.fi.cig.mctbnc.data.representation.Dataset;
import es.upm.fi.cig.mctbnc.nodes.DiscreteNode;

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

	@Override
	public String getIdentifier() {
		return getNameMethod();
	}

	@Override
	public Map<String, String> getParametersAlgorithm() {
		return Map.of();
	}

}
