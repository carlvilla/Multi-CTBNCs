package com.cig.mctbnc.learning.structure.optimization.scores.bn;

import java.util.List;
import java.util.Set;

import com.cig.mctbnc.data.representation.State;
import com.cig.mctbnc.learning.structure.optimization.scores.AbstractLogLikelihood;
import com.cig.mctbnc.models.BN;
import com.cig.mctbnc.nodes.CPTNode;
import com.cig.mctbnc.nodes.DiscreteNode;
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
			// Obtain an array with the values that the studied variable can take
			String[] possibleValuesStudiedVariable = node.getStates().stream()
					.map(stateAux -> stateAux.getValueVariable(node.getName())).toArray(String[]::new);
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
				// Number of possible states of the parents of the currently studied variable
				int numPossibleStatesParents = node.getNumStatesParents();
				// Number of possible states of the currently studied variable
				int numPossibleStatesStudiedVariable = possibleValuesStudiedVariable.length;
				// Compute network complexity
				double networkComplexity = numPossibleStatesParents * (numPossibleStatesStudiedVariable - 1);
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
		node.setState(idxState);
		node.setStateParents(idxStateParents);
		State state = node.getStateNodeAndParents();
		double classProbability = 0.0;
		if (Nijk != 0)
			classProbability = Nijk * Math.log(node.getCPT().get(state));
		return classProbability;
	}
}
