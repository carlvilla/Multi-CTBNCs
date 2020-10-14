package com.cig.mctbnc.learning.structure.optimization.scores.ctbn;

import java.util.Map;

import org.apache.commons.math3.special.Gamma;

import com.cig.mctbnc.data.representation.State;
import com.cig.mctbnc.learning.parameters.ctbn.CTBNSufficientStatistics;
import com.cig.mctbnc.learning.structure.optimization.CTBNScoreFunction;
import com.cig.mctbnc.models.CTBN;
import com.cig.mctbnc.nodes.CIMNode;
import com.cig.mctbnc.nodes.Node;

public class CTBNBayesianScore implements CTBNScoreFunction {

	/**
	 * Bayesian score defined by Nodelman et al. (2003). (BDe?)
	 */
	public double compute(CTBN<? extends Node> ctbn, int nodeIndex) {
		// Obtain node to evaluate
		CIMNode node = (CIMNode) ctbn.getNodes().get(nodeIndex);
		// Retrieve sufficient statistics of the node
		CTBNSufficientStatistics ss = node.getSufficientStatistics();
		// Hyperparameters of the hyperprior distribution
		double nxyHP = ss.getNxyHyperparameter();
		double nxHP = ss.getNxHyperparameter();
		double txHP = ss.getTxHyperparameter();
		// Obtain parameters and sufficient statistics of the node
		// Contains the probabilities of transitioning from one state to another
		Map<State, Map<State, Double>> Oxx = node.getOxx();
		// CIMs. Given the state of a variable and its parents is obtained the
		// instantaneous probability
		Map<State, Double> Qx = node.getQx();
		// Store marginal log likelihood
		double ll = 0.0;
		for (State state : Qx.keySet()) {
			double nx = ss.getNx().get(state);
			double tx = ss.getTx().get(state);
			ll += Gamma.logGamma(nx + 1) + (nxHP + 1) + Math.log(txHP);
			ll -= Gamma.logGamma(nxHP + 1) + (nx + 1) + Math.log(tx);
			ll += Gamma.logGamma(nxHP);
			ll -= Gamma.logGamma(nx);
			for (State toState : Oxx.get(state).keySet()) {
				// Number of times the variable transitions from "state" to "toState"
				double nxx = ss.getNxy().get(state).get(toState);
				ll += Gamma.logGamma(nxyHP);
				ll -= Gamma.logGamma(nxx);
			}
		}
		return ll;
	}

}
