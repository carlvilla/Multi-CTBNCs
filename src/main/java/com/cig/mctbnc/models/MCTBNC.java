package com.cig.mctbnc.models;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.learning.parameters.ParameterLearningAlgorithm;
import com.cig.mctbnc.learning.structure.StructureLearningAlgorithm;
import com.cig.mctbnc.nodes.Node;

/**
 * 
 * Implements the Multi-dimensional Continuous Time Bayesian Network Classifier.
 * 
 * @author Carlos Villa Blanco
 * 
 * @param <NodeTypeBN>   Type of the nodes of the Bayesian network (discrete or
 *                       continuous)
 * @param <NodeTypeCTBN> Type of the nodes of the continuous time Bayesian
 *                       network (discrete or continuous)
 */
public class MCTBNC<NodeTypeBN extends Node, NodeTypeCTBN extends Node> extends AbstractPGM {
	// The subgraph formed by class variables is a Bayesian network
	private BN<NodeTypeBN> bn;
	private CTBN<NodeTypeCTBN> ctbnc;
	static Logger logger = LogManager.getLogger(MCTBNC.class);

	public MCTBNC(Dataset dataset, ParameterLearningAlgorithm ctbnParameterLearningAlgorithm,
			StructureLearningAlgorithm ctbncStructureLearningAlgorithm,
			ParameterLearningAlgorithm bnParameterLearningAlgorithm,
			StructureLearningAlgorithm bnStructureLearningAlgorithm) {
		// Set dataset
		this.dataset = dataset;

		// Define class subgraph with a Bayesian network
		bn = new BN<NodeTypeBN>(dataset, dataset.getNameClassVariables(), bnParameterLearningAlgorithm,
				bnStructureLearningAlgorithm);

		// Define feature and bridge subgraphs with a continuous time Bayesian network
		ctbnc = new CTBN<NodeTypeCTBN>(dataset, dataset.getNameVariables(), ctbnParameterLearningAlgorithm,
				ctbncStructureLearningAlgorithm);
	}

	public void display() {
		Graph graph = new SingleGraph("MCTBNC");
		addNodes(graph, nodes);
		addEdges(graph, nodes);
		graph.display();
	}

	@Override
	public void learn() {
		// Learn structure and parameters of class subgraph (Bayesian network)
		logger.info("Defining structure and parameters of the class subgraph (Bayesian network)");
		bn.learn();
		logger.info("Class subgraph established!");
		// Learn structure and parameters of feature and bridge subgraph. These are
		// modeled by a continuous time Bayesian network classifier where the
		// restriction that the class variable does not depend on the states of the
		// features is extended to more class variables
		logger.info("Defining structure and parameters of the feature and bridge subgraphs (Continuous time "
				+ "Bayesian network)");
		ctbnc.learn();
		logger.info("Feature and bridge subgraphs established!");
		// Join class subgraph with feature and bridge subgraphs
		// Get nodes BN (class variables). Define them as class variables
		List<Node> nodesBN = getNodesBN();
		List<Node> nodesCTBN = getNodesCTBN();
		setStructure(nodesBN, nodesCTBN);
	}

	private List<Node> getNodesBN() {
		List<Node> nodesBN = bn.getNodes();
		// Define nodes of the BN as class variables
		for (Node node : nodesBN) {
			node.isClassVariable(true);
		}
		return nodesBN;
	}

	private List<Node> getNodesCTBN() {
		return ctbnc.getNodes();
	}

	/**
	 * Defines the nodes of the MCTBNC by using the nodes obtained from the BN and
	 * the CTBN.
	 * 
	 * @param nodesBN
	 * @param nodesCTBN
	 */
	private void setStructure(List<Node> nodesBN, List<Node> nodesCTBN) {
		this.nodes = new ArrayList<Node>();

		for (Node nodeBN : nodesBN) {
			// Add to the nodes of the BN the children obtained by the CTBN
			// Obtain the node for the class variable in the BN
			Node nodeInCTBN = nodesBN.stream().filter(nodeCTBN -> nodeCTBN.getName() == nodeBN.getName()).findFirst()
					.orElse(null);

			// Set the children of the node from the BN with the children from the same node
			// in the CTBN
			for (Node child : nodeInCTBN.getChildren()) {
				nodeBN.setChild(child);
			}

			// Nodes of the class variables from the BN are added to the model
			this.nodes.add(nodeBN);
		}

		// Add the nodes of the CTBN that are for features
		for (Node nodeCTBN : nodesCTBN) {
			if (!dataset.getNameClassVariables().contains(nodeCTBN.getName())) {
				this.nodes.add(nodeCTBN);
			}
		}
	}

	@Override
	public void setStructure(boolean[][] adjacencyMatrix) {
		for (int i = 0; i < adjacencyMatrix.length; i++) {
			for (int j = 0; j < adjacencyMatrix.length; j++) {
				if (adjacencyMatrix[i][j]) {
					// Add arc from node i to j
					Node nodeParent = getNodeByIndex(i);
					Node nodeChildren = getNodeByIndex(j);
					nodeParent.setChild(nodeChildren);
					nodeChildren.setParent(nodeParent);
				}
			}
		}
	}

	@Override
	public String getType() {
		return "Multidimensional Continuous time Bayesian network";
	}

	@Override
	public boolean isStructureLegal(boolean[][] adjacencyMatrix) {
		// TODO Auto-generated method stub
		return false;
	}

}
