package com.cig.mctbnc.learning.structure.optimization.scores.bn;

import java.util.List;

import com.cig.mctbnc.learning.structure.optimization.scores.AbstractLogLikelihood;
import com.cig.mctbnc.models.BN;
import com.cig.mctbnc.nodes.CPTNode;
import com.cig.mctbnc.nodes.Node;

public class BNLogLikelihood extends AbstractLogLikelihood implements BNScoreFunction {

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
	 * @param bn                   Bayesian network
	 * @param penalizationFunction penalization function
	 * @return penalized log-likelihood score
	 */
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
			if (penalizationFunctionMap.containsKey(penalizationFunction)) {
				// Overfitting is avoid by penalizing the complexity of the network
				// Compute network complexity
				double networkComplexity = numStatesParents * (numStates - 1);
				// Compute non-negative penalization (For now it is performing a BIC
				// penalization)
				int sampleSize = bn.getDataset().getNumDataPoints();
				double penalization = penalizationFunctionMap.get(penalizationFunction).applyAsDouble(sampleSize);
				// Obtain number of states of the parents of the currently studied variable
				llScore -= networkComplexity * penalization;
			}
		}
		return llScore;
	}

	/**
	 * Estimate the probability of a class variable taking a certain state.
	 * 
	 * @param node            class variable node
	 * @param idxState        index of the state of the class variable node
	 * @param idxStateParents index of the states of the parents of the node
	 * @return
	 */
	private double classProbability(CPTNode node, int idxState, int idxStateParents) {
		// Number of times the studied variable and its parents take a certain state
		double Nijk = node.getSufficientStatistics().getNx()[idxStateParents][idxState];
		double classProbability = 0.0;
		if (Nijk != 0)
			classProbability = Nijk * Math.log(node.getCP(idxStateParents, idxState));
		return classProbability;
	}
}
