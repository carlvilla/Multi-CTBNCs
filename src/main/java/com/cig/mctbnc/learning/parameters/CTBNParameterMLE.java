package com.cig.mctbnc.learning.parameters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.data.representation.State;
import com.cig.mctbnc.nodes.CIMNode;
import com.cig.mctbnc.nodes.Node;

public class CTBNParameterMLE implements ParameterLearningAlgorithm {

	static Logger logger = LogManager.getLogger(CTBNParameterMLE.class);
	List<CIMNode> cimNodes;

	@Override
	public void learn(List<? extends Node> nodes, Dataset dataset) {
		logger.trace("Learning parameters CTBN with maximum likelihood estimation");
		sufficientStatistics(nodes, dataset);
		setCIMs(nodes);
	}

	/**
	 * Obtain the sufficient statistics of each node of a CTBN.
	 * 
	 * @param nodes
	 * @param dataset
	 */
	protected void sufficientStatistics(List<? extends Node> nodes, Dataset dataset) {
		int numNodes = nodes.size();
		for (int i = 0; i < numNodes; i++) {
			CTBNSufficientStatistics ssNode = new CTBNSufficientStatistics(nodes.get(i));
			ssNode.computeSufficientStatistics(dataset);
			((CIMNode) nodes.get(i)).setSufficientStatistics(ssNode);
		}
	}

	public void setCIMs(List<? extends Node> nodes) {
		// For each node it is created a new type of node that contains the CIMs.
		for (int i = 0; i < nodes.size(); i++) {
			// The node has to be discrete
			CIMNode node = (CIMNode) nodes.get(i);
			Map<State, Map<State, Integer>> Nxx = node.getSufficientStatistics().getNxy();
			Map<State, Integer> Nx = node.getSufficientStatistics().getNx();
			Map<State, Double> T = node.getSufficientStatistics().getTx();
			// Compute the parameters for the current node with its sufficient statistics.
			// The parameters are stored in the CIMNode object
			estimateParameters(node, Nxx, Nx, T);
		}
	}

	/**
	 * Estimate the parameters for "node" from its previously computed sufficient
	 * statistics. It is estimated two MLE parameters that summarize the CIMs of
	 * each variable. The first one contains the probabilities of the variables
	 * leaving a state and the second one the probabilities of leaving a state for a
	 * certain one.
	 * 
	 * @param node node where it is stored the parameters
	 * @param Nxy  sufficient statistic with the number of times the variables
	 *             transition from a certain state to another while their parents
	 *             have a certain value
	 * @param Nx   sufficient statistic with the number of times the variables leave
	 *             their current state while their parents have a certain value
	 * @param T    sufficient statistic with the time that the variables stay in a
	 *             certain state while their parents take a certain value
	 */
	public void estimateParameters(CIMNode node, Map<State, Map<State, Integer>> Nxy, Map<State, Integer> Nx,
			Map<State, Double> T) {
		Map<State, Double> Qx = new HashMap<State, Double>();
		Map<State, Map<State, Double>> Oxx = new HashMap<State, Map<State, Double>>();
		// Parameter with probabilities of leaving a certain state
		for (State fromState : Nx.keySet()) {
			double qx = Nx.get(fromState) / T.get(fromState);
			// If the operation gives NaN (state does not occur in the dataset), the
			// parameter is set to zero
			if (Double.isNaN(qx))
				qx = 0;
			Qx.put(fromState, qx);
		}
		// Parameter with probabilities of leaving a certain state for another
		for (State fromState : Nxy.keySet()) {
			for (State toState : Nxy.get(fromState).keySet()) {
				if (!Oxx.containsKey(fromState))
					Oxx.put(fromState, new HashMap<State, Double>());
				double oxx = (double) Nxy.get(fromState).get(toState) / Nx.get(fromState);
				// If the operation gives NaN (state does not occur in the dataset), the
				// parameter is set to zero
				if (Double.isNaN(oxx))
					oxx = 0;
				Oxx.get(fromState).put(toState, oxx);
			}
		}
		// Set parameters in the CIMNode object
		node.setParameters(Qx, Oxx);
	}
}
