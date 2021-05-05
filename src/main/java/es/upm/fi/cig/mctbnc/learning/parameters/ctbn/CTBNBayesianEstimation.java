package es.upm.fi.cig.mctbnc.learning.parameters.ctbn;

import java.util.Map;

import es.upm.fi.cig.mctbnc.data.representation.Dataset;
import es.upm.fi.cig.mctbnc.nodes.DiscreteNode;

/**
 * Bayesian parameter estimation for a discrete CTBN. It is assumed all of the
 * hyperparameters to be equal to "MxyHP" or "TxHP" (Lindstone rule).
 * 
 * @author Carlos Villa Blanco
 *
 */
public class CTBNBayesianEstimation extends CTBNParameterLearningAlgorithm {
	// Hyperparameters Dirichlet prior distribution
	private double MxyHP;
	private double TxHP;

	/**
	 * Constructs a {@code CTBNBayesianEstimation} for the Bayesian estimation of
	 * the parameters of a discrete CTBN.
	 * 
	 * @param MxyHP
	 * @param TxHP
	 */
	public CTBNBayesianEstimation(double MxyHP, double TxHP) {
		logger.info("Learning parameters of CTBN with Bayesian estimation (Nxy={}, Tx={})", MxyHP, TxHP);
		// Definition of imaginary counts of the hyperparameters
		this.MxyHP = MxyHP;
		this.TxHP = TxHP;
	}

	@Override
	protected CTBNSufficientStatistics getSufficientStatisticsNode(DiscreteNode node, Dataset dataset) {
		CTBNSufficientStatistics ssNode = new CTBNSufficientStatistics(this.MxyHP, this.TxHP);
		ssNode.computeSufficientStatistics(node, dataset);
		return ssNode;
	}

	@Override
	public String getNameMethod() {
		return String.format("Bayesian estimation (Mxy:%s, TxHP:%s)", this.MxyHP, this.TxHP);
	}

	@Override
	public String getIdentifier() {
		return "Bayesian estimation";
	}

	@Override
	public Map<String, String> getParametersAlgorithm() {
		return Map.of("mxy", String.valueOf(this.MxyHP), "tx", String.valueOf(this.TxHP));
	}

}