package com.cig.mctbnc.learning.structure.optimization.scores.ctbn;

import org.apache.commons.math3.special.Gamma;

import com.cig.mctbnc.learning.parameters.ctbn.CTBNSufficientStatistics;
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

	@Override
	public double compute(CTBN<? extends Node> ctbn, int nodeIndex) {
		// Obtain node to evaluate
		CIMNode node = (CIMNode) ctbn.getNodes().get(nodeIndex);
		// Retrieve sufficient statistics of the node
		CTBNSufficientStatistics ss = node.getSufficientStatistics();
		// Hyperparameters of the hyperprior distribution
		double mxyHP = ss.getMxyHyperparameter();
		double mxHP = ss.getMxHyperparameter();
		double txHP = ss.getTxHyperparameter();
		// Number of states of the node and its parents
		int numStates = node.getNumStates();
		int numStatesParents = node.getNumStatesParents();
		// Estimate score
		double bdeScore = 0.0;
		// Iterate over all states of the node (from where a transition begins)
		for (int idxFromState = 0; idxFromState < numStates; idxFromState++) {
			// Iterate over all states of the node's parents
			for (int idxStateParents = 0; idxStateParents < numStatesParents; idxStateParents++) {
				// Establish state of the node and its parents
				double mx = ss.getMx()[idxStateParents][idxFromState];
				double tx = ss.getTx()[idxStateParents][idxFromState];
				bdeScore += Gamma.logGamma(mx + 1) + (mxHP + 1) * Math.log(txHP);
				bdeScore -= Gamma.logGamma(mxHP + 1) + (mx + 1) * Math.log(tx);
				bdeScore += Gamma.logGamma(mxHP) - Gamma.logGamma(mx);
				// Iterate over all the states of the node (where a transition ends)
				for (int idxToState = 0; idxToState < numStates; idxToState++) {
					if (idxToState != idxFromState) {
						double mxy = ss.getMxy()[idxStateParents][idxFromState][idxToState];
						bdeScore += Gamma.logGamma(mxy) - Gamma.logGamma(mxyHP);
					}
				}
			}
		}
		return bdeScore;
	}

	@Override
	public boolean isDecomposable() {
		return true;
	}

}
