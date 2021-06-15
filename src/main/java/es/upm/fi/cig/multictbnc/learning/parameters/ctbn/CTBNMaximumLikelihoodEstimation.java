package es.upm.fi.cig.multictbnc.learning.parameters.ctbn;

import java.util.Map;

import es.upm.fi.cig.multictbnc.data.representation.Dataset;
import es.upm.fi.cig.multictbnc.nodes.DiscreteNode;

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

	@Override
	public String getIdentifier() {
		return getNameMethod();
	}

	@Override
	public Map<String, String> getParametersAlgorithm() {
		return Map.of();
	}

}
