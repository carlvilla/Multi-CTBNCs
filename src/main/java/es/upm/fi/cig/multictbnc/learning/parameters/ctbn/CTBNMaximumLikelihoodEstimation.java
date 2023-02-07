package es.upm.fi.cig.multictbnc.learning.parameters.ctbn;

import es.upm.fi.cig.multictbnc.data.representation.Dataset;
import es.upm.fi.cig.multictbnc.nodes.DiscreteStateNode;

import java.util.Map;

/**
 * Maximum likelihood estimation of CTBN parameters.
 *
 * @author Carlos Villa Blanco
 */
public class CTBNMaximumLikelihoodEstimation extends CTBNParameterLearningAlgorithm {

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
	protected CTBNSufficientStatistics getSufficientStatisticsNode(DiscreteStateNode node, Dataset dataset) {
		CTBNSufficientStatistics ssNode = new CTBNSufficientStatistics(0, 0);
		ssNode.computeSufficientStatistics(node, dataset);
		return ssNode;
	}

}