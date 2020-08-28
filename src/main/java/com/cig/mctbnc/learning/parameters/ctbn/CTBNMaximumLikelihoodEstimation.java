package com.cig.mctbnc.learning.parameters.ctbn;

import java.util.HashMap;
import java.util.Map;

import com.cig.mctbnc.data.representation.State;
import com.cig.mctbnc.nodes.CIMNode;

/**
 * Maximum likelihood estimation of CTBN parameters.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class CTBNMaximumLikelihoodEstimation extends CTBNParameterLearningAlgorithm {

	/**
	 * Estimate the parameters for "node" from its computed sufficient statistics.
	 * It is estimated two MLE parameters that summarize the CIMs of each variable.
	 * The first one contains the probabilities of the variables leaving a state and
	 * the second one the probabilities of leaving a state for a certain one.
	 * 
	 * @param node node with sufficient statistics where it is stored the computed
	 *             parameters
	 */
	@Override
	public void estimateParameters(CIMNode node) {
		logger.trace("Learning parameters of CTBN node {} with maximum likelihood estimation", node.getName());
		// Initialize structures to store the parameters
		Map<State, Double> Qx = new HashMap<State, Double>();
		Map<State, Map<State, Double>> Oxx = new HashMap<State, Map<State, Double>>();
		// Retrieve sufficient statistics
		Map<State, Map<State, Integer>> Nxy = node.getSufficientStatistics().getNxy();
		Map<State, Integer> Nx = node.getSufficientStatistics().getNx();
		Map<State, Double> Tx = node.getSufficientStatistics().getTx();
		// Parameter with probabilities of leaving a certain state
		for (State fromState : Nx.keySet()) {
			double qx = Nx.get(fromState) / Tx.get(fromState);
			// If the operation gives NaN (the states of the node and its parents do not
			// occur simultaneously in the training dataset), the parameter is set to zero
			if (Double.isNaN(qx))
				qx = 0;
			Qx.put(fromState, qx);
		}
		// Parameter with probabilities of leaving (transitioning) a certain state for another
		for (State fromState : Nxy.keySet()) {
			for (State toState : Nxy.get(fromState).keySet()) {
				if (!Oxx.containsKey(fromState))
					Oxx.put(fromState, new HashMap<State, Double>());
				double oxx = (double) Nxy.get(fromState).get(toState) / Nx.get(fromState);
				// If the operation gives NaN (the transition does not occur in the training
				// dataset), the parameter is set to zero
				if (Double.isNaN(oxx))
					oxx = 0;
				Oxx.get(fromState).put(toState, oxx);
			}
		}
		// Set parameters in the CIMNode object
		node.setParameters(Qx, Oxx);
	}

}
