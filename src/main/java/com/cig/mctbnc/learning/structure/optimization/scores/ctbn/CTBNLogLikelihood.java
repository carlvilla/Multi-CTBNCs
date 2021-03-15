package com.cig.mctbnc.learning.structure.optimization.scores.ctbn;

import com.cig.mctbnc.learning.parameters.ctbn.CTBNSufficientStatistics;
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

	@Override
	public boolean isDecomposable() {
		return true;
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
		// Number of states of the node and its parents
		int numStates = node.getNumStates();
		int numStatesParents = node.getNumStatesParents();
		// Store marginal log likelihood
		double ll = 0.0;
		for (int idxStateParents = 0; idxStateParents < numStatesParents; idxStateParents++) {
			for (int idxFromState = 0; idxFromState < numStates; idxFromState++) {
				double qx = node.getQx(idxStateParents, idxFromState);
				// Cases where there are no transitions are ignored to avoid NaNs
				if (qx > 0) {
					double mx = ss.getMx()[idxStateParents][idxFromState];
					double tx = ss.getTx()[idxStateParents][idxFromState];
					// Log probability density function of the exponential distribution
					ll += mx * Math.log(qx) - qx * tx;
					for (int idxToState = 0; idxToState < numStates; idxToState++) {
						if (idxToState != idxFromState) {
							// Probability of transitioning from "state" to "toState"
							double oxx = node.getOxy(idxStateParents, idxFromState, idxToState);
							// Cases without transitions between the states are ignored to avoid NaNs
							if (oxx != 0) {
								// Number of times the variable transitions from "idxFromState" to "idxToState"
								double mxx = ss.getMxy()[idxStateParents][idxFromState][idxToState];
								ll += mxx * Math.log(oxx);
							}
						}
					}
				}
			}
		}
		return ll;
	}

}
