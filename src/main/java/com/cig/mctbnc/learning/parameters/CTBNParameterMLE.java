package com.cig.mctbnc.learning.parameters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.data.representation.State;
import com.cig.mctbnc.nodes.CIMNode;
import com.cig.mctbnc.nodes.DiscreteNode;
import com.cig.mctbnc.nodes.Node;

public class CTBNParameterMLE implements ParameterLearningAlgorithm {

	static Logger logger = LogManager.getLogger(CTBNParameterMLE.class);
	List<CIMNode> cimNodes;

	@Override
	public void learn(List<Node> nodes, Dataset dataset) {
		CTBNSufficientStatistics[] ssNodes = sufficientStatistics(nodes, dataset);
		setCIMs(nodes, ssNodes);
	}

	/**
	 * Obtain the sufficient statistics of a CTBN
	 * 
	 * @param nodes
	 * @param dataset
	 * @return sufficient statistics for each variable
	 */
	protected CTBNSufficientStatistics[] sufficientStatistics(List<Node> nodes, Dataset dataset) {
		int numNodes = nodes.size();
		CTBNSufficientStatistics[] ss = new CTBNSufficientStatistics[numNodes];
		for (int i = 0; i < numNodes; i++) {
			CTBNSufficientStatistics ssNode = new CTBNSufficientStatistics(nodes.get(i));
			ssNode.computeSufficientStatistics(dataset);
			ss[i] = ssNode;
		}
		return ss;
	}

	public void setCIMs(List<Node> nodes, CTBNSufficientStatistics[] ss) {
		// For each node it is created a new type of node that contains the CIMs.
		cimNodes = new ArrayList<CIMNode>();
		for (int i = 0; i < nodes.size(); i++) {
			// The node has to be discrete
			DiscreteNode node = (DiscreteNode) nodes.get(i);
			Map<State,Map<State, Integer>> N = ss[i].getSufficientStatistics();
			// Compute the parameters for the current node with its sufficient statistics
			Map<State, Double> CIM = estimateCIM(node, N);
			// Create a CIMNode to store the computed CIM and sufficient statistics
			CIMNode cimNode = new CIMNode(node, CIM, N);
			cimNodes.add(cimNode);
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
	public Map<State, Double> estimateCIM(DiscreteNode studiedNode, Map<State, Map<State,Integer>> N) {
		/*
		Map<State, Double> CIM = new HashMap<State, Double>();
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
			CIM.put(state, prob);
		}
		return CIM;*/
		return null;
	}

	@Override
	public List<CIMNode> getParameters() {
		return getCIMs();
	}

	/**
	 * Return a list of CIMNode objects, i.e., an object that extend DiscreteNode in
	 * order to store the conditional intensity matrices.
	 * 
	 * @return List of CPTNodes
	 */
	public List<CIMNode> getCIMs() {
		if (cimNodes.isEmpty())
			logger.warn("CIMs were not learned");
		return cimNodes;
	}

}
