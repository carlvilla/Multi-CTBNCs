package es.upm.fi.cig.multictbnc.learning.parameters.bn;

import es.upm.fi.cig.multictbnc.data.representation.Dataset;
import es.upm.fi.cig.multictbnc.nodes.DiscreteNode;

import java.util.Map;

/**
 * Maximum likelihood estimation of BN parameters.
 *
 * @author Carlos Villa Blanco
 */
public class BNMaximumLikelihoodEstimation extends BNParameterLearningAlgorithm {

	@Override
	public String getIdentifier() {
		return getNameMethod();
	}

	@Override
	public String getNameMethod() {
		return "Maximum likelihood estimation";
	}

	@Override
	public Map<String, String> getParametersAlgorithm() {
		return Map.of();
	}

	@Override
	protected BNSufficientStatistics getSufficientStatisticsNode(DiscreteNode node, Dataset dataset) {
		BNSufficientStatistics ssNode = new BNSufficientStatistics(0);
		ssNode.computeSufficientStatistics(node, dataset);
		return ssNode;
	}

}