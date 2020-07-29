package com.cig.mctbnc.learning.parameters;

import java.util.ArrayList;
import java.util.HashMap;
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
		logger.trace("Learning parameters CTBN with maximum likelihood estimation");
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
			Map<State, Map<State, Integer>> Nxx = ss[i].getNxx();
			Map<State, Integer> Nx = ss[i].getNx();
			Map<State, Double> T = ss[i].getT();
			// Create a CIMNode to store the parameters and sufficient statistics
			CIMNode cimNode = new CIMNode(node, ss[i]);
			// Compute the parameters for the current node with its sufficient statistics.
			// The parameters are stored in the CIMNode object
			estimateParameters(cimNode, Nxx, Nx, T);

			cimNodes.add(cimNode);
		}
	}

	/**
	 * Estimate the parameters for "node" from its previously computed sufficient
	 * statistics. It is estimated two parameters that summarized the CIMs of each
	 * variable. The first one contains the probabilities of the variables leaving a
	 * state and the second one the probabilities of leaving a state for a certain
	 * one.
	 * 
	 * @param node
	 *            node where it is stored the parameters
	 * @param Nxx
	 *            sufficient statistic with the number of times the variables
	 *            transition from a certain state to another while their parents
	 *            have a certain value
	 * @param Nx
	 *            sufficient statistic with the number of times the variables leave
	 *            their current state while their parents have a certain value
	 * @param T
	 *            sufficient statistic with the time that the variables stay in a
	 *            certain state while their parents take a certain value
	 */
	public void estimateParameters(CIMNode node, Map<State, Map<State, Integer>> Nxx, Map<State, Integer> Nx,
			Map<State, Double> T) {
		Map<State, Double> Qx = new HashMap<State, Double>();
		Map<State, Map<State, Double>> Oxx = new HashMap<State, Map<State, Double>>();
		// Parameter with probabilities of leaving a certain state
		for (State fromState : Nx.keySet()) {
			Qx.put(fromState, Nx.get(fromState) / T.get(fromState));
		}
		// Parameter with probabilities of leaving a certain state for another
		for (State fromState : Nxx.keySet()) {
			for (State toState : Nxx.get(fromState).keySet()) {
				if (!Oxx.containsKey(fromState))
					Oxx.put(fromState, new HashMap<State, Double>());
				Oxx.get(fromState).put(toState, (double) Nxx.get(fromState).get(toState) / Nx.get(fromState));
			}

		}
		// Set parameters in the CIMNode object
		node.setParameters(Qx, Oxx);
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
