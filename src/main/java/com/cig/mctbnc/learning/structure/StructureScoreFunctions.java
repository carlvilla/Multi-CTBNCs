package com.cig.mctbnc.learning.structure;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.cig.mctbnc.data.representation.State;
import com.cig.mctbnc.models.BN;
import com.cig.mctbnc.models.PGM;
import com.cig.mctbnc.nodes.CPTNode;
import com.cig.mctbnc.nodes.DiscreteNode;

public class StructureScoreFunctions {

	/**
	 * Compute the log-likelihood score for a discrete Bayesian network
	 * 
	 * @param nodes
	 * @return
	 */
	public static double penalizedLogLikelihoodScore(PGM pgm) {
		if (pgm instanceof BN) {
			return bnPenalizedLogLikelihoodScore((BN<CPTNode>) pgm);
		}
		return 0;
	}

	private static double bnPenalizedLogLikelihoodScore(BN<CPTNode> bn) {
		// Obtain nodes of the Bayesian networks with the CPTs
		List<CPTNode> nodes = bn.getLearnedNodes();

		double llScore = 0.0;
		for (CPTNode node : nodes) {
			Map<State, Double> CPT = node.getCPT();
			Map<State, Integer> N = node.getSufficientStatistics();

			// Obtain an array with the values that the studied variable can take
			String[] possibleValuesStudiedVariable = node.getPossibleStates().stream()
					.map(stateAux -> stateAux.getValueNode(node.getName())).toArray(String[]::new);

			// All the possible states between the studied variable and its parents
			Set<State> states = N.keySet();
			for (State state : states) {
				for (String k : possibleValuesStudiedVariable) {
					State query = new State(state.getEvents());
					query.modifyEventValue(node.getName(), k);
					// Number of times the studied variable and its parents take a certain value
					int Nijk = N.get(query);
					if (Nijk != 0)
						llScore += Nijk * Math.log(CPT.get(query));
				}
			}
			// Overfitting is avoid by penalizing the complexity of the network

			// Number of possible states of the parents of the currently studied variable
			int numPossibleStatesParents = node.getParents().stream().map(parent -> (DiscreteNode) parent)
					.map(parent -> parent.getStates()).map(listStates -> listStates.size()).reduce(1, (a, b) -> a * b);

			// Number of possible states of the currently studied variable
			int numPossibleStatesStudiedVariable = possibleValuesStudiedVariable.length;

			// Compute network complexity
			double networkComplexity = numPossibleStatesParents * (numPossibleStatesStudiedVariable - 1);

			// Compute non-negative penalization (For now it is performing a BIC
			// penalization)
			double penalization = Math.log(bn.getDataset().getNumDataPoints()) / 2;

			// Obtain number of states of the parents of the currently studied variable
			llScore -= networkComplexity * penalization;
		}

		return llScore;
	}

}
