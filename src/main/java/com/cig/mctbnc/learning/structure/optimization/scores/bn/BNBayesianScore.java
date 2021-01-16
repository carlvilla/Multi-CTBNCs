package com.cig.mctbnc.learning.structure.optimization.scores.bn;

import java.util.List;
import java.util.Map;

import org.apache.commons.math3.special.Gamma;

import com.cig.mctbnc.data.representation.State;
import com.cig.mctbnc.models.BN;
import com.cig.mctbnc.nodes.CPTNode;
import com.cig.mctbnc.nodes.Node;

/**
 * Implements the Bayesian Dirichlet equivalence metric for BNs (Heckerman et
 * al., 1995).
 */
public class BNBayesianScore implements BNScoreFunction {

	@Override
	public double compute(BN<? extends Node> bn) {
		// Obtain nodes of the Bayesian networks with the CPTs
		List<CPTNode> nodes = (List<CPTNode>) bn.getLearnedNodes();
		// Hyperparameters of the hyperprior distribution (The same one is assumed for
		// every node and state)
		double nxHP = nodes.get(0).getSufficientStatistics().getNxHyperparameter();
		double bdeScore = 0.0;
		for (CPTNode node : nodes) {
			// Retrieve sufficient statistics of the node. They already include the
			// hyperparameters
			Map<State, Double> nx = node.getSufficientStatistics().getNx();
			// All possible states of the node's parents
			List<State> statesParents = node.getStatesParents();
			for (State stateParents : statesParents) {
				// Number of states of the node
				int numStatesNode = node.getStates().size();
				// Number of times the parents have a certain state independently of the node
				double n = 0;
				for (State stateNode : node.getStates()) {
					State query = new State(stateParents.getEvents());
					query.addEvents(stateNode.getEvents());
					n += nx.get(query);
					bdeScore += Gamma.logGamma(nx.get(query)) - Gamma.logGamma(nxHP);
				}
				bdeScore += Gamma.logGamma(nxHP * numStatesNode) - Gamma.logGamma(n);
			}
		}
		return bdeScore;
	}
}
