package com.cig.mctbnc.learning.structure.optimization.scores.ctbn;

import java.util.Map;

import com.cig.mctbnc.data.representation.State;
import com.cig.mctbnc.learning.parameters.ctbn.CTBNSufficientStatistics;
import com.cig.mctbnc.learning.structure.optimization.CTBNScoreFunction;
import com.cig.mctbnc.learning.structure.optimization.scores.AbstractLogLikelihood;
import com.cig.mctbnc.models.CTBN;
import com.cig.mctbnc.nodes.CIMNode;
import com.cig.mctbnc.nodes.Node;

public class CTBNLogLikelihood extends AbstractLogLikelihood implements CTBNScoreFunction {

	/**
	 * Receives the name of the penalization function used for the structure
	 * complexity.
	 * 
	 * @param penalizationFunction
	 */
	public CTBNLogLikelihood(String penalizationFunction) {
		super(penalizationFunction);
	}

	/**
	 * Compute the (penalized) log-likelihood score at a given node of a discrete
	 * continuous time Bayesian network.
	 * 
	 * @param ctbn                 continuous time Bayesian network
	 * @param nodeIndex            index of the node
	 * @param penalizationFunction penalization function
	 * @return penalized log-likelihood score
	 */
	public double compute(CTBN<? extends Node> ctbn, int nodeIndex) {
		// Obtain node to evaluate
		CIMNode node = (CIMNode) ctbn.getNodes().get(nodeIndex);

//		System.out.println("-----------");
//		System.out.println("Nodo: " + node.getName());
//		System.out.println(Arrays.toString(node.getParents().stream().map(nodop -> nodop.getName()).toArray()));

		double ll = 0.0;
		ll += logLikelihoodScore(node);
		// Apply the specified penalization function (if available)
		if (penalizationFunctionMap.containsKey(penalizationFunction)) {
			// Overfitting is avoid by penalizing the complexity of the network
			// Number of possible transitions
			int numStates = node.getStates().size();
			int numTransitions = (numStates - 1) * numStates;
			// Number of states of the parents
			int numStatesParents = node.getNumStateParents();
			// Complexity of the network
			int networkComplexity = numTransitions * numStatesParents;
			// Sample size (number of sequences)
			int sampleSize = ctbn.getDataset().getNumDataPoints();
			// Non-negative penalization
			double penalization = penalizationFunctionMap.get(penalizationFunction).applyAsDouble(sampleSize);
			ll -= (double) networkComplexity * penalization;
		}

//		System.out.println("Total: " + llScore);

		return ll;
	}

	/**
	 * Compute the log-likelihood for a certain CIM node.
	 * 
	 * @param node
	 * @return log likelihood
	 */
	private double logLikelihoodScore(CIMNode node) {
		// Retrieve sufficient statistics of the node
		CTBNSufficientStatistics ss = node.getSufficientStatistics();
		// Obtain parameters and sufficient statistics of the node
		// Contains the probabilities of transitioning from one state to another
		Map<State, Map<State, Double>> Oxx = node.getOxy();
		// CIMs. Given the state of a variable and its parents is obtained the
		// instantaneous probability
		Map<State, Double> Qx = node.getQx();
		// Store marginal log likelihood
		double ll = 0.0;
		for (State state : Qx.keySet()) {
			double qx = Qx.get(state);
			double nx = ss.getMx().get(state);
			double tx = ss.getTx().get(state);
			// Probability density function of the exponential distribution. If it is 0,
			// there are no transitions from this state
			if (qx != 0) {
				ll += nx * Math.log(qx) - qx * tx;
				for (State toState : Oxx.get(state).keySet()) {
					// Probability of transitioning from "state" to "toState"
					double oxx = Oxx.get(state).get(toState);
					// Number of times the variable transitions from "state" to "toState"
					double nxx = ss.getMxy().get(state).get(toState);
					if (oxx != 0)
						ll += nxx * Math.log(oxx);
				}
			}
		}
		return ll;
	}

}
