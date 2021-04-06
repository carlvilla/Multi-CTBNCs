package com.cig.mctbnc.learning.structure.optimization.scores.bn;

import java.util.List;
import java.util.function.DoubleUnaryOperator;

import com.cig.mctbnc.learning.structure.optimization.scores.AbstractLikelihood;
import com.cig.mctbnc.models.BN;
import com.cig.mctbnc.nodes.CPTNode;
import com.cig.mctbnc.nodes.Node;

/**
 * Implements the log-likelihood score for Bayesian networks.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class BNLogLikelihood extends AbstractLikelihood implements BNScoreFunction {

	/**
	 * Receives the name of the penalization function used for the structure
	 * complexity.
	 * 
	 * @param penalizationFunction
	 */
	public BNLogLikelihood(String penalizationFunction) {
		super(penalizationFunction);
	}

	/**
	 * Compute the (penalized) log-likelihood score for a discrete Bayesian network.
	 * This is done by computing the marginal log-likelihood of the graph. It is
	 * assumed a uniform prior structure.
	 * 
	 * @param bn Bayesian network
	 * @return penalized log-likelihood score
	 */
	@Override
	public double compute(BN<? extends Node> bn) {
		// Obtain nodes of the Bayesian networks with the CPTs
		List<CPTNode> nodes = (List<CPTNode>) bn.getLearnedNodes();
		double llScore = 0.0;
		for (CPTNode node : nodes) {
			// Number of states of the node and its parents
			int numStates = node.getNumStates();
			int numStatesParents = node.getNumStatesParents();
			// Iterate over all states of the node (from where a transition begins)
			for (int idxState = 0; idxState < numStates; idxState++)
				// Iterate over all states of the node's parents
				for (int idxStateParents = 0; idxStateParents < numStatesParents; idxStateParents++)
					llScore += classProbability(node, idxState, idxStateParents);
			// If the specified penalization function is available, it is applied
			DoubleUnaryOperator penalizationFunction = this.penalizationFunctionMap.get(this.penalizationFunction);
			if (penalizationFunction != null) {
				// Overfitting is avoid by penalizing the complexity of the network
				// Compute network complexity
				double networkComplexity = numStatesParents * (numStates - 1);
				// Compute non-negative penalization (For now it is performing a BIC
				// penalization)
				int sampleSize = bn.getDataset().getNumDataPoints();
				double penalization = penalizationFunction.applyAsDouble(sampleSize);
				// Obtain number of states of the parents of the currently studied variable
				llScore -= networkComplexity * penalization;
			}
		}
		return llScore;
	}

	/**
	 * Estimate the probability of a class variable taking a provided state given
	 * the state of its parents.
	 * 
	 * @param node            class variable node
	 * @param idxState        index of the state of the class variable node
	 * @param idxStateParents index of the states of the parents of the node
	 * @return probability of a class configuration
	 */
	private double classProbability(CPTNode node, int idxState, int idxStateParents) {
		// Number of times the studied variable and its parents take a certain state
		double Nijk = node.getSufficientStatistics().getNx()[idxStateParents][idxState];
		double classProbability = 0.0;
		// The zero count problem occurs if the class variable never takes the evaluated
		// state given the parents. This case is ignored to avoid NaNs
		if (Nijk != 0)
			classProbability = Nijk * Math.log(node.getCP(idxStateParents, idxState));
		return classProbability;
	}
}
