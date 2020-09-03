package com.cig.mctbnc.learning.parameters.bn;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cig.mctbnc.data.representation.State;
import com.cig.mctbnc.nodes.CPTNode;

/**
 * Implements the Bayesian estimation to estimate the parameters of a BN. It is
 * assumed a Dirichlet prior distribution over the probabilities of each state
 * of the variables, given the state of their parents, with all of its
 * hyperparameters being equal to "alpha". Thus, the posterior distribution will
 * be Dirichlet with hyperameters equal to the frequency of each state of the
 * variables plus "alpha" (Lindstone rule).
 * 
 * @author Carlos Villa Blanco
 *
 */
public class BNBayesianEstimation extends BNParameterLearningAlgorithm {
	// Hyperparameters of the Dirichlet prior distribution (lindstone rule)
	private double alpha;
	static Logger logger = LogManager.getLogger(BNBayesianEstimation.class);

	public BNBayesianEstimation(double alpha) {
		this.alpha = alpha;
	}

	@Override
	protected Map<State, Double> estimateCPT(CPTNode node, Map<State, Integer> N) {
		logger.trace("Learning parameters of BN node {} with bayesian estimation", node.getName());
		Map<State, Double> CPT = new HashMap<State, Double>();
		// Obtain an array with the states (strings) that the studied variable can take
		String[] possibleStatesVariable = node.getStates().stream()
				.map(stateAux -> stateAux.getValueVariable(node.getName())).toArray(String[]::new);
		int numPossibleStatesVariables = possibleStatesVariable.length;
		// All the possible states between the studied variable and its parents
		Set<State> states = N.keySet();
		for (State state : states) {
			// Number of times the studied variable and its parents take a certain value
			double Nijk = N.get(state);
			// Number of times the parents of the studied variable have a certain
			// state independently of the studied variable
			double Nij = 0;
			for (String k : possibleStatesVariable) {
				State query = new State(state.getEvents());
				query.modifyEventValue(node.getName(), k);
				Nij += N.get(query);
			}
			// Probability that the studied variable has a state k given that
			// its parents have a state j.
			double prob = (Nijk + alpha) / (Nij + numPossibleStatesVariables * alpha);
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
