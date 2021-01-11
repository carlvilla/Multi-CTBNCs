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
		setSufficientStatistics(nodes, dataset);
		setCIMs(nodes);
	}

	@Override
	public void learn(Node node, Dataset dataset) {
		sufficientStatistics(node, dataset);
		setCIMs(node);
	}

	/**
	 * Obtain the sufficient statistics of each node of a CTBN.
	 * 
	 * @param nodes
	 * @param dataset
	 */
	private void setSufficientStatistics(List<? extends Node> nodes, Dataset dataset) {
		for (int i = 0; i < nodes.size(); i++) {
			CTBNSufficientStatistics ssNode = getSufficientStatisticsNode(nodes.get(i), dataset);
			nodes.get(i).setSufficientStatistics(ssNode);
		}
	}

	/**
	 * Set the conditional intensity matrices of the nodes of a CTBN.
	 * 
	 * @param nodes
	 */
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
	 * Obtain the sufficient statistics of a CTBN node.
	 * 
	 * @param nodes
	 * @param dataset
	 */
	private void sufficientStatistics(Node node, Dataset dataset) {
		CTBNSufficientStatistics ssNode = getSufficientStatisticsNode(node, dataset);
		node.setSufficientStatistics(ssNode);
	}

	/**
	 * Set the conditional intensity matrices of a CTBN node.
	 * 
	 * @param nodes
	 * @param dataset
	 */
	private void setCIMs(Node node) {
		CIMNode cimNode = (CIMNode) node;
		estimateParameters(cimNode);
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
		Map<State, Map<State, Double>> Mxy = node.getSufficientStatistics().getMxy();
		Map<State, Double> Mx = node.getSufficientStatistics().getMx();
		Map<State, Double> Tx = node.getSufficientStatistics().getTx();
		// Parameter with probabilities of leaving a certain state
		for (State fromState : Mx.keySet()) {
			// Number of transitions from this state
			double MxFromState = Mx.get(fromState);
			// Instantaneous probability
			double qx = MxFromState / Tx.get(fromState);
			// qx can be undefined if the priors are 0 (maximum likelihood estimation)
			if (Double.isNaN(qx))
				qx = 0;
			// Save the estimated instantaneous probability
			Qx.put(fromState, qx);
			// Obtain the map with all the transitions from "fromState" to other states
			Map<State, Double> mapMxyFromState = Mxy.get(fromState);
			// It may happen that a variable has not transitions in a training set (e.g.
			// when using CV without stratification)
			if (mapMxyFromState != null) {
				// Iterate over all possible transitions to obtain their probabilities
				for (State toState : mapMxyFromState.keySet()) {
					if (!Oxx.containsKey(fromState))
						Oxx.put(fromState, new HashMap<State, Double>());
					double oxx = mapMxyFromState.get(toState) / MxFromState;
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
