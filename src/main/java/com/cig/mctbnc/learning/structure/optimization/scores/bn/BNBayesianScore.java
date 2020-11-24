package com.cig.mctbnc.learning.structure.optimization.scores.bn;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.special.Gamma;

import com.cig.mctbnc.data.representation.State;
import com.cig.mctbnc.learning.structure.optimization.BNScoreFunction;
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
			// Retrieve sufficient statistics of the node
			Map<State, Double> nx = node.getSufficientStatistics().getNx();
			// All the possible states between the studied variable and its parents
			Set<State> states = nx.keySet();
			for (State state : states) {
				// Number of states of the node
				int numStatesNode = node.getStates().size();
				// Number of times the parents have a certain state independently of the node
				double n = 0;
				for (State stateNode : node.getStates()) {
					State query = new State(state.getEvents());
					query.modifyEventValue(node.getName(), stateNode.getValues()[0]);
					n += nx.get(query);
					bdeScore += Gamma.logGamma(nxHP + nx.get(query)) - Gamma.logGamma(nxHP);
				}
				bdeScore += Gamma.logGamma(nxHP * numStatesNode) - Gamma.logGamma(nxHP * numStatesNode + n);
			}
		}
		return bdeScore;
	}

}
