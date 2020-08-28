package com.cig.mctbnc.learning.parameters.ctbn;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.learning.parameters.ParameterLearningAlgorithm;
import com.cig.mctbnc.nodes.CIMNode;
import com.cig.mctbnc.nodes.Node;

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
			CTBNSufficientStatistics ssNode = new CTBNSufficientStatistics();
			ssNode.computeSufficientStatistics(nodes.get(i), dataset);
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

	protected abstract void estimateParameters(CIMNode node);

}
