package com.cig.mctbnc.learning.parameters.ctbn;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.data.representation.State;
import com.cig.mctbnc.learning.parameters.ParameterLearningAlgorithm;
import com.cig.mctbnc.nodes.CIMNode;
import com.cig.mctbnc.nodes.Node;

/**
 * Define methods for parameter learning algorithms of continuous time Bayesian
 * networks.
 * 
 * @author Carlos Villa Blanco
 *
 */
public abstract class CTBNParameterLearningAlgorithm implements ParameterLearningAlgorithm {
	static Logger logger = LogManager.getLogger(CTBNParameterLearningAlgorithm.class);
	List<CIMNode> cimNodes;

	@Override
	public void learn(List<? extends Node> nodes, Dataset dataset) {
		sufficientStatistics(nodes, dataset);
		setCIMs(nodes);
	}

	/**
	 * Obtain the sufficient statistics of each node of a CTBN.
	 * 
	 * @param nodes
	 * @param dataset
	 */
	private void sufficientStatistics(List<? extends Node> nodes, Dataset dataset) {
		int numNodes = nodes.size();
		for (int i = 0; i < numNodes; i++) {
			CTBNSufficientStatistics ssNode = getSufficientStatisticsNode(nodes.get(i), dataset);
			nodes.get(i).setSufficientStatistics(ssNode);
		}
	}

	private void setCIMs(List<? extends Node> nodes) {
		// For each node is estimated its CIMs
		for (int i = 0; i < nodes.size(); i++) {
			// The node has to be a CIMNode
			CIMNode node = (CIMNode) nodes.get(i);
			// Compute the parameters for the current node with its sufficient statistics.
			// The parameters are stored in the CIMNode object
			estimateParameters(node);
		}
	}

	/**
	 * Estimate the parameters for "node" from its computed sufficient statistics.
	 * It is estimated two parameters that summarize the CIMs of each variable. The
	 * first one contains the probabilities of the variables leaving a state and the
	 * second one the probabilities of leaving a state for a certain one.
	 * 
	 * @param node node with sufficient statistics where it is stored the computed
	 *             parameters
	 */
	protected void estimateParameters(CIMNode node) {
		logger.trace("Learning parameters of CTBN node {}", node.getName());
		// Initialize structures to store the parameters
		Map<State, Double> Qx = new HashMap<State, Double>();
		Map<State, Map<State, Double>> Oxx = new HashMap<State, Map<State, Double>>();
		// Retrieve sufficient statistics
		Map<State, Map<State, Double>> Nxy = node.getSufficientStatistics().getNxy();
		Map<State, Double> Nx = node.getSufficientStatistics().getNx();
		Map<State, Double> Tx = node.getSufficientStatistics().getTx();
		// Parameter with probabilities of leaving a certain state
		for (State fromState : Nx.keySet()) {
			// Number of transitions from this state
			double NxFromState = Nx.get(fromState);
			// Instantaneous probability
			double qx = NxFromState / Tx.get(fromState);
			// qx can be undefined if the priors are 0 (maximum likelihood estimation)
			if (Double.isNaN(qx))
				qx = 0;
			// Save the estimated instantaneous probability
			Qx.put(fromState, qx);
			// Obtain the map with all the transitions from "fromState" to other states
			Map<State, Double> mapNxyFromState = Nxy.get(fromState);
			// It may happen that a variable has not transitions in a training set (e.g.
			// when using CV without stratification)
			if (mapNxyFromState != null) {
				// Iterate over all possible transitions to obtain their probabilities
				for (State toState : mapNxyFromState.keySet()) {
					if (!Oxx.containsKey(fromState))
						Oxx.put(fromState, new HashMap<State, Double>());
					double oxx = mapNxyFromState.get(toState) / NxFromState;
					// The previous operation can be undefined if the priors are 0
					if (Double.isNaN(oxx))
						oxx = 0;
					// Save the probability of transitioning from "fromState" to "toState"
					Oxx.get(fromState).put(toState, oxx);
				}
			}
		}
		// Set parameters in the CIMNode object
		node.setParameters(Qx, Oxx);
	}

	protected abstract CTBNSufficientStatistics getSufficientStatisticsNode(Node node, Dataset dataset);

}
