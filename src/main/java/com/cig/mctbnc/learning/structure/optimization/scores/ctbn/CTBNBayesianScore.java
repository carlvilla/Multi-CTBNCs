package com.cig.mctbnc.learning.structure.optimization.scores.ctbn;

import org.apache.commons.math3.special.Gamma;

import com.cig.mctbnc.data.representation.State;
import com.cig.mctbnc.learning.parameters.ctbn.CTBNSufficientStatistics;
import com.cig.mctbnc.learning.structure.optimization.CTBNScoreFunction;
import com.cig.mctbnc.models.CTBN;
import com.cig.mctbnc.nodes.CIMNode;
import com.cig.mctbnc.nodes.Node;

/**
 * Implements the Bayesian Dirichlet equivalence metric for CTBNs (Nodelman et
 * al., 2003).
 */
public class CTBNBayesianScore implements CTBNScoreFunction {

	@Override
	public double compute(CTBN<? extends Node> ctbn) {
		double bdeScore = 0;
		for (int indexNode = 0; indexNode < ctbn.getNumNodes(); indexNode++)
			bdeScore += compute(ctbn, indexNode);
		return bdeScore;
	}

	public double compute(CTBN<? extends Node> ctbn, int nodeIndex) {
		// Obtain node to evaluate
		CIMNode node = (CIMNode) ctbn.getNodes().get(nodeIndex);
		// Retrieve sufficient statistics of the node
		CTBNSufficientStatistics ss = node.getSufficientStatistics();
		// Hyperparameters of the hyperprior distribution
		double mxyHP = ss.getMxyHyperparameter();
		double mxHP = ss.getMxHyperparameter();
		double txHP = ss.getTxHyperparameter();
		// Estimate score
		double bdeScore = 0.0;
		for (State state : node.getQx().keySet()) {
			double mx = ss.getMx().get(state);
			double tx = ss.getTx().get(state);
			bdeScore += Gamma.logGamma(mx + 1) + (mxHP + 1) * Math.log(txHP);
			bdeScore -= Gamma.logGamma(mxHP + 1) + (mx + 1) * Math.log(tx);
			bdeScore += Gamma.logGamma(mxHP) - Gamma.logGamma(mx);
			for (State toState : node.getOxy().get(state).keySet()) {
				// Number of times the variable transitions from "state" to "toState"
				double mxy = ss.getMxy().get(state).get(toState);
				bdeScore += Gamma.logGamma(mxyHP) - Gamma.logGamma(mxy);
			}
		}
		return bdeScore;
	}

}
