package es.upm.fi.cig.multictbnc.learning.structure.optimisation.scores.bn;

import es.upm.fi.cig.multictbnc.learning.structure.optimisation.scores.AbstractLikelihood;
import es.upm.fi.cig.multictbnc.models.BN;
import es.upm.fi.cig.multictbnc.nodes.CPTNode;
import es.upm.fi.cig.multictbnc.nodes.Node;

import java.util.List;
import java.util.function.DoubleUnaryOperator;

/**
 * Implements the log-likelihood score for Bayesian networks with nodes that have CPTs.
 *
 * @author Carlos Villa Blanco
 */
public class BNLogLikelihood extends AbstractLikelihood implements BNScoreFunction {

	/**
	 * Receives the name of the penalisation function for the structure complexity.
	 *
	 * @param penalisationFunction name of the penalisation function
	 */
	public BNLogLikelihood(String penalisationFunction) {
		super(penalisationFunction);
	}

	/**
	 * Computes the (penalised) log-likelihood score for a discrete Bayesian network. This is done by computing the
	 * marginal log-likelihood of the graph. A uniform prior structure is assumed.
	 *
	 * @param bn a Bayesian network
	 * @return penalised log-likelihood score
	 */
	@Override
	@SuppressWarnings("unchecked")
	public double compute(BN<? extends Node> bn) {
		// Obtain nodes of the Bayesian networks with the CPTs
		List<CPTNode> nodes = (List<CPTNode>) bn.getLearntNodes();
		double llScore = 0.0;
		for (CPTNode node : nodes) {
			// Number of states of the node and its parents
			int numStates = node.getNumStates();
			int numStatesParents = node.getNumStatesParents();
			llScore += node.estimateLogLikelihood();
			// If the specified penalisation function is available, it is applied
			DoubleUnaryOperator penalisationFunction = this.getPenalisationFunction();
			if (penalisationFunction != null) {
				// Overfitting is avoid by penalising the complexity of the network
				// Compute network complexity
				double networkComplexity = numStatesParents * (numStates - 1);
				// Compute non-negative penalisation
				int sampleSize = bn.getDataset().getNumDataPoints();
				double penalisation = penalisationFunction.applyAsDouble(sampleSize);
				// Obtain the number of states of the parents of the currently studied variable
				llScore -= networkComplexity * penalisation;
			}
		}
		return llScore;
	}

	@Override
	public String getIdentifier() {
		return "Log-likelihood";
	}

}