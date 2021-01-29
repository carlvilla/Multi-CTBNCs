package com.cig.mctbnc.learning.structure.optimization.scores.ctbn;

import java.util.Map;

import com.cig.mctbnc.data.representation.State;
import com.cig.mctbnc.learning.parameters.ctbn.CTBNSufficientStatistics;
import com.cig.mctbnc.learning.structure.optimization.scores.AbstractLogLikelihood;
import com.cig.mctbnc.models.CTBN;
import com.cig.mctbnc.nodes.CIMNode;
import com.cig.mctbnc.nodes.DiscreteNode;
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

	@Override
	public double compute(CTBN<? extends Node> ctbn) {
		double ll = 0;
		for (int indexNode = 0; indexNode < ctbn.getNumNodes(); indexNode++)
			ll += compute(ctbn, indexNode);
		return ll;
	}

	/**
	 * Compute the (penalized) log-likelihood score at a given node of a discrete
	 * continuous time Bayesian network.
	 * 
	 * @param ctbn                 continuous time Bayesian network
	 * @param indexNode            index of the node
	 * @param penalizationFunction penalization function
	 * @return penalized log-likelihood score
	 */
	public double compute(CTBN<? extends Node> ctbn, int indexNode) {
		// Obtain node to evaluate
		CIMNode node = (CIMNode) ctbn.getNodes().get(indexNode);
		double ll = 0.0;
		ll += logLikelihoodScore(node);
		// Apply the specified penalization function (if available)
		if (penalizationFunctionMap.containsKey(penalizationFunction)) {
			// Overfitting is avoid by penalizing the complexity of the network
			// Number of possible transitions
			int numStates = node.getNumStates();
			int numTransitions = (numStates - 1) * numStates;
			// Number of states of the parents
			int numStatesParents = node.getNumStatesParents();
			// Complexity of the network
			int networkComplexity = numTransitions * numStatesParents;
			// Sample size (number of sequences)
			int sampleSize = ctbn.getDataset().getNumDataPoints();
			// Non-negative penalization
			double penalization = penalizationFunctionMap.get(penalizationFunction).applyAsDouble(sampleSize);
			ll -= (double) networkComplexity * penalization;
		}
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
		// Obtain parameters of the node
		Map<State, Map<State, Double>> Oxx = node.getOxy();
		Map<State, Double> Qx = node.getQx();
		// Store marginal log likelihood
		double ll = 0.0;
		for (State state : Qx.keySet()) {
			double qx = Qx.get(state);
			for(int i=0;i<node.getNumParents();i++) {
				DiscreteNode parentNode = (DiscreteNode) node.getParents().get(i);
				parentNode.setState(state.getValueVariable(parentNode.getName()));
			}
			node.setState(state.getValueVariable(node.getName()));	
			int idxParents = node.getIdxStateParents();
			int idxFromState = node.getStateIdx();
			double nx = ss.getMx()[idxParents][idxFromState]; 
			double tx = ss.getTx()[idxParents][idxFromState];
			// Probability density function of the exponential distribution. If it is 0,
			// there are no transitions from this state
			if (qx != 0) {
				ll += nx * Math.log(qx) - qx * tx;								
				Map<State, Double> fromStateOxx = Oxx.get(state);
				for (State toState : fromStateOxx.keySet()) {
					// Probability of transitioning from "state" to "toState"
					double oxx = fromStateOxx.get(toState);					
					// Number of times the variable transitions from "state" to "toState"
					int idxToState = node.setState(toState.getValueVariable(node.getName()));
					double nxx = ss.getMxy()[idxParents][idxFromState][idxToState];
					if (oxx != 0)
						ll += nxx * Math.log(oxx);
				}
			}
		}
		return ll;
	}

}
