package com.cig.mctbnc.learning.parameters.bn;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cig.mctbnc.data.representation.State;
import com.cig.mctbnc.nodes.CPTNode;

/**
 * Maximum likelihood estimation of BN parameters.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class BNMaximumLikelihoodEstimation extends BNParameterEstimation {
	static Logger logger = LogManager.getLogger(BNMaximumLikelihoodEstimation.class);

	/**
	 * Estimate the parameters for a certain node from its previously computed
	 * sufficient statistics.
	 * 
	 * @param node
	 * @param N
	 * @return a Map object with the probabilities of each possible state of a
	 *         variable and its parents
	 */
	public Map<State, Double> estimateCPT(CPTNode node, Map<State, Integer> N) {
		logger.trace("Learning parameters of BN node {} with maximum likelihood estimation", node.getName());
		Map<State, Double> CPT = new HashMap<State, Double>();
		// Obtain an array with the values that the studied variable can take
		String[] possibleValuesStudiedNode = node.getStates().stream()
				.map(stateAux -> stateAux.getValueVariable(node.getName())).toArray(String[]::new);
		// All the possible states between the studied variable and its parents
		Set<State> states = N.keySet();
		for (State state : states) {
			// Number of times the studied variable and its parents take a certain value
			double Nijk = N.get(state);
			// Number of times the parents of the studied variable have a certain
			// state independently of the studied variable
			double Nij = 0;
			for (String k : possibleValuesStudiedNode) {
				State query = new State(state.getEvents());
				query.modifyEventValue(node.getName(), k);
				Nij += N.get(query);
			}
			// Probability that the studied variable has a state k given that
			// its parents have a state j.
			double prob = Nijk / Nij;
			// The state may not occur in the training dataset. In that case the probability
			// is set to 0.
			if (Double.isNaN(prob))
				prob = 0;
			// Save the probability of the state
			CPT.put(state, prob);
		}
		return CPT;
	}

}
