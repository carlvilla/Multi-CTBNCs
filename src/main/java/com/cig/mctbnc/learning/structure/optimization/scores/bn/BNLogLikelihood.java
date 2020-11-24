package com.cig.mctbnc.learning.structure.optimization.scores.bn;

import java.util.List;
import java.util.Set;

import com.cig.mctbnc.data.representation.State;
import com.cig.mctbnc.learning.structure.optimization.BNScoreFunction;
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
			// All the possible states between the studied variable and its parents
			Set<State> states = node.getSufficientStatistics().getNx().keySet();
			for (State state : states)
				for (String k : possibleValuesStudiedVariable) {
					State query = new State(state.getEvents());
					query.modifyEventValue(node.getName(), k);
					llScore += classProbability(node, state);
				}
			// If the specified penalization function is available, it is applied
			if (penalizationFunctionMap.containsKey(penalizationFunction)) {
				// Overfitting is avoid by penalizing the complexity of the network
				// Number of possible states of the parents of the currently studied variable
				int numPossibleStatesParents = node.getParents().stream().map(parent -> (DiscreteNode) parent)
						.map(parent -> parent.getStates()).map(listStates -> listStates.size())
						.reduce(1, (a, b) -> a * b);
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
	 * Estimate the probability of a class variable taking a certain value.
	 * 
	 * @param node  class variable node
	 * @param state state of the class variable node and its parents
	 * @return
	 */
	private double classProbability(CPTNode node, State state) {
		// Number of times the studied variable and its parents take a certain value
		double Nijk = node.getSufficientStatistics().getNx().get(state);
		double classProbability = 0.0;
		if (Nijk != 0)
			classProbability = Nijk * Math.log(node.getCPT().get(state));
		return classProbability;
	}
}
