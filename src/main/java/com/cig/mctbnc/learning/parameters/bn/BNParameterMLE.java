package com.cig.mctbnc.learning.parameters.bn;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.data.representation.State;
import com.cig.mctbnc.learning.parameters.ParameterLearningAlgorithm;
import com.cig.mctbnc.nodes.CPTNode;
import com.cig.mctbnc.nodes.DiscreteNode;
import com.cig.mctbnc.nodes.Node;

public class BNParameterMLE implements ParameterLearningAlgorithm {
	static Logger logger = LogManager.getLogger(BNParameterMLE.class);

	@Override
	public void learn(List<? extends Node> nodes, Dataset dataset) {
		logger.trace("Learning parameters BN with maximum likelihood estimation");
		setSufficientStatistics(nodes, dataset);
		setCPTs(nodes);
	}

	/**
	 * Obtain for each variable i, the number of times its parents are in the state
	 * j and the variable is in the state k.
	 * 
	 * @param nodes
	 * @param dataset
	 */
	public void setSufficientStatistics(List<? extends Node> nodes, Dataset dataset) {
		for (int i = 0; i < nodes.size(); i++) {
			BNSufficientStatistics ss = new BNSufficientStatistics();
			ss.computeSufficientStatistics(nodes.get(i), dataset);
			nodes.get(i).setSufficientStatistics(ss);
		}
	}

	public void setCPTs(List<? extends Node> nodes) {
		// For each node it is created a new type of node that contains the CPTs.
		for (int i = 0; i < nodes.size(); i++) {
			// The node has to have a CPT
			CPTNode node = (CPTNode) nodes.get(i);
			// Compute the parameters for the current node with its sufficient statistics
			Map<State, Double> CPT = estimateCPT(node, node.getSufficientStatistics());
			// CPTNode stores the computed CPT
			node.setCPT(CPT);
		}
	}

	/**
	 * Estimate the parameters for a certain node (studiedNode) from its previously
	 * computed sufficient statistics.
	 * 
	 * @param studiedNode
	 * @param N
	 * @return a Map object with the probabilities of each possible state of a
	 *         variable and its parents
	 */
	public Map<State, Double> estimateCPT(DiscreteNode studiedNode, Map<State, Integer> N) {
		Map<State, Double> CPT = new HashMap<State, Double>();
		// Obtain an array with the values that the studied variable can take
		String[] possibleValuesStudiedNode = studiedNode.getPossibleStates().stream()
				.map(stateAux -> stateAux.getValueNode(studiedNode.getName())).toArray(String[]::new);
		// All the possible states between the studied variable and its parents
		Set<State> states = N.keySet();
		for (State state : states) {
			// Number of times the studied variable and its parents take a certain value
			double Nijk = N.get(state);
			// Number of times the parents of the studied variable have a certain
			// state independently of the studied variable
			double Nij = 0;
			for (String k : possibleValuesStudiedNode) {
				State query = new State(state.getEvents());
				query.modifyEventValue(studiedNode.getName(), k);
				Nij += N.get(query);
			}
			// Probability that the studied variable has a state k given that
			// its parents have a state j.
			double prob = Nijk / Nij;
			CPT.put(state, prob);
		}
		return CPT;
	}

}
