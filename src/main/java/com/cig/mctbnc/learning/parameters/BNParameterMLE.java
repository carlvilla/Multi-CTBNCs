package com.cig.mctbnc.learning.parameters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.data.representation.State;
import com.cig.mctbnc.nodes.DiscreteNode;
import com.cig.mctbnc.nodes.Node;

public class BNParameterMLE implements BNParameterLearning {

	List<CPTNode> cptNodes;

	@Override
	public void learn(List<Node> nodes, Dataset dataset) {
		BNSufficientStatistics[] ssAllNodes = sufficientStatistics(nodes, dataset);
		setCPTs(nodes, ssAllNodes);
	}

	public void setCPTs(List<Node> nodes, BNSufficientStatistics[] ss) {
		// For each node it is created a new type of node that contains the CPTs.
		cptNodes = new ArrayList<CPTNode>();

		for (int i = 0; i < nodes.size(); i++) {

			// The node has to be discrete
			DiscreteNode node = (DiscreteNode) nodes.get(i);

			Map<State, Integer> N = ss[i].getSufficientStatistics();

			// Compute the parameters for the current node with the sufficient statistics
			Map<State, Double> CPT = estimateCPT(node, N);

			// Create a CPTNode to store the computed CPT and sufficient statistics
			CPTNode cptNode = new CPTNode(node, CPT, N);
			cptNodes.add(cptNode);
		}
	}

	@Override
	public List<CPTNode> getParameters() {
		return getCPT();
	}

	/**
	 * Return a list of CPTNode objects, i.e., an object that extend DiscreteNode in
	 * order to store the CPT tables.
	 * 
	 * @return
	 */
	public List<CPTNode> getCPT() {
		try {
			if (cptNodes.isEmpty())
				throw new Exception("CPT were not learned");
		} catch (Exception e) {
			System.err.println(e);
		}

		return cptNodes;

	}

	/**
	 * Estimate the parameters for a certain node (studiedNode) from its previously
	 * computed sufficient statistics.
	 * 
	 * @param studiedNode
	 * @param N
	 * @return
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

	/**
	 * Obtain for each variable i, the number of times its parents are in the state
	 * j and the variable is in the state k.
	 * 
	 * @param bn
	 * @param dataset
	 * @return
	 */
	public BNSufficientStatistics[] sufficientStatistics(List<Node> nodes, Dataset dataset) {
		int numNodes = nodes.size();
		BNSufficientStatistics[] ss = new BNSufficientStatistics[numNodes];

		for (int i = 0; i < numNodes; i++) {
			BNSufficientStatistics ssNode = new BNSufficientStatistics(nodes.get(i));
			ssNode.computeSufficientStatistics(dataset);
			ss[i] = ssNode;
		}
		return ss;
	}

}
