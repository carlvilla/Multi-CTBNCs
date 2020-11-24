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
import com.cig.mctbnc.nodes.Node;

/**
 * Define methods for parameter learning algorithms of Bayesian networks.
 * 
 * @author Carlos Villa Blanco
 *
 */
public abstract class BNParameterLearningAlgorithm implements ParameterLearningAlgorithm {
	static Logger logger = LogManager.getLogger(BNParameterLearningAlgorithm.class);

	@Override
	public void learn(List<? extends Node> nodes, Dataset dataset) {
		setSufficientStatistics(nodes, dataset);
		setCPTs(nodes);
	}

	@Override
	public void learn(Node node, Dataset dataset) {
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
			BNSufficientStatistics ssNode = getSufficientStatisticsNode(nodes.get(i), dataset);
			nodes.get(i).setSufficientStatistics(ssNode);
		}
	}

	/**
	 * Given a set of nodes whose sufficient statistics where estimated, compute
	 * their conditional probability tables (CPT).
	 * 
	 * @param nodes
	 */
	public void setCPTs(List<? extends Node> nodes) {
		// For each node it is created a new type of node that contains the CPTs.
		for (int i = 0; i < nodes.size(); i++) {
			// The node has to have a CPT
			CPTNode node = (CPTNode) nodes.get(i);
			// Compute the parameters for the current node with its sufficient statistics
			Map<State, Double> CPT = estimateCPT(node);
			// CPTNode stores the computed CPT
			node.setCPT(CPT);
		}
	}

	/**
	 * Estimate the parameters for a certain node from its previously computed
	 * sufficient statistics.
	 * 
	 * @param node
	 * @param N
	 * @return a Map object with the probabilities of each possible state of a
	 *         variable and its parents
	 */
	public Map<State, Double> estimateCPT(CPTNode node) {
		logger.trace("Learning parameters of BN node {} with maximum likelihood estimation", node.getName());
		Map<State, Double> CPT = new HashMap<State, Double>();
		// Retrieve sufficient statistics
		Map<State, Double> Nx = node.getSufficientStatistics().getNx();
		// Obtain an array with the values that the studied variable can take
		String[] possibleValuesStudiedNode = node.getStates().stream()
				.map(stateAux -> stateAux.getValueVariable(node.getName())).toArray(String[]::new);
		// All the possible states between the studied variable and its parents
		Set<State> states = Nx.keySet();
		for (State state : states) {
			// Number of times the studied variable and its parents take a certain value
			double Nijk = Nx.get(state);
			// Number of times the parents of the studied variable have a certain
			// state independently of the studied variable
			double Nij = 0;
			for (String k : possibleValuesStudiedNode) {
				State query = new State(state.getEvents());
				query.modifyEventValue(node.getName(), k);
				Nij += Nx.get(query);
			}
			// Probability that the studied variable has a state k given that
			// its parents have a state j.
			double prob = Nijk / Nij;
			// The state may not occur in the training dataset. In that case the probability
			// is set to 0.
			if (Double.isNaN(prob))
				prob = 0;
			// Save the probability of the state
			CPT.put(state, prob);
		}
		return CPT;
	}

	protected abstract BNSufficientStatistics getSufficientStatisticsNode(Node node, Dataset dataset);
}
