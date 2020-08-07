package com.cig.mctbnc.models;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;

import com.cig.mctbnc.classification.Classifier;
import com.cig.mctbnc.data.reader.DatasetReader;
import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.data.representation.Sequence;
import com.cig.mctbnc.learning.parameters.ParameterLearningAlgorithm;
import com.cig.mctbnc.learning.structure.StructureLearningAlgorithm;
import com.cig.mctbnc.nodes.Node;

/**
 * 
 * Implements the multi-dimensional continuous time Bayesian network classifier
 * (MCTBNC).
 * 
 * @author Carlos Villa Blanco
 * 
 * @param <NodeTypeBN>   Type of the nodes of the Bayesian network (discrete or
 *                       continuous)
 * @param <NodeTypeCTBN> Type of the nodes of the continuous time Bayesian
 *                       network (discrete or continuous)
 */
public class MCTBNC<NodeTypeBN extends Node, NodeTypeCTBN extends Node> extends AbstractPGM<Node>
		implements Classifier {
	// The subgraph formed by class variables is a Bayesian network
	private BN<NodeTypeBN> bn;
	private CTBN<NodeTypeCTBN> ctbn;
	static Logger logger = LogManager.getLogger(MCTBNC.class);

	/**
	 * 
	 * Constructor for MCTBNC.
	 * 
	 * @param dataset                        dataset used to learn the MCTBNC
	 * @param ctbnParameterLearningAlgorithm algorithm used to learn the parameters
	 *                                       of a CTBN
	 * @param ctbnStructureLearningAlgorithm algorithm used to learn the structure
	 *                                       of a CTBN
	 * @param bnParameterLearningAlgorithm   algorithm used to learn the parameters
	 *                                       of a BN
	 * @param bnStructureLearningAlgorithm   algorithm used to learn the structure
	 *                                       of a BN
	 * @param bnNodeClass                    type of the BN nodes
	 * @param ctbnNodeClass                  type of the CTBN nodes
	 */
	public MCTBNC(Dataset dataset, ParameterLearningAlgorithm ctbnParameterLearningAlgorithm,
			StructureLearningAlgorithm ctbnStructureLearningAlgorithm,
			ParameterLearningAlgorithm bnParameterLearningAlgorithm,
			StructureLearningAlgorithm bnStructureLearningAlgorithm, Class<NodeTypeBN> bnNodeClass,
			Class<NodeTypeCTBN> ctbnNodeClass) {
		// Set dataset
		this.dataset = dataset;

		// Define class subgraph with a Bayesian network
		bn = new BN<NodeTypeBN>(dataset, dataset.getNameClassVariables(), bnParameterLearningAlgorithm,
				bnStructureLearningAlgorithm, bnNodeClass);

		// Define feature and bridge subgraphs with a continuous time Bayesian network
		ctbn = new CTBN<NodeTypeCTBN>(dataset, dataset.getNameVariables(), ctbnParameterLearningAlgorithm,
				ctbnStructureLearningAlgorithm, ctbnNodeClass);
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
		ctbn.learn();
		logger.info("Feature and bridge subgraphs established!");
		// Join class subgraph with feature and bridge subgraphs
		// Get nodes BN (class variables). Define them as class variables
		List<NodeTypeBN> nodesBN = getNodesBN();
		List<NodeTypeCTBN> nodesCTBN = getNodesCTBN();
		setStructure(nodesBN, nodesCTBN);
	}

	private List<NodeTypeBN> getNodesBN() {
		List<NodeTypeBN> nodesBN = bn.getNodes();
		// Define nodes of the BN as class variables
		for (Node node : nodesBN) {
			node.isClassVariable(true);
		}
		return nodesBN;
	}

	private List<NodeTypeCTBN> getNodesCTBN() {
		return ctbn.getNodes();
	}

	/**
	 * Defines the nodes of the MCTBNC by using the nodes obtained from the BN and
	 * the CTBN.
	 * 
	 * @param nodesBN
	 * @param nodesCTBN
	 */
	private void setStructure(List<NodeTypeBN> nodesBN, List<NodeTypeCTBN> nodesCTBN) {
		List<Node> nodes = new ArrayList<Node>();

		// It is important to respect the original order of the variables to generate a
		// correct adjacency matrix
		for (String nameVariable : dataset.getNameVariables()) {
			// It is obtained the node of each variable from the BN (if it exists) and from
			// the CTBN
			Node nodeInBN = nodesBN.stream().filter(nodeBN -> nodeBN.getName().equals(nameVariable)).findFirst()
					.orElse(null);
			Node nodeInCTBN = nodesCTBN.stream().filter(nodeCTBN -> nodeCTBN.getName().equals(nameVariable)).findFirst()
					.orElse(null);
			if (nodeInBN != null) {
				// It is a class variable node. The children obtained by the CTBN are added to
				// the node of the BN
				for (Node child : nodeInCTBN.getChildren())
					nodeInBN.setChild(child);
				nodes.add(nodeInBN);
			} else {
				// It is a feature node
				nodes.add(nodeInCTBN);
			}
		}
		// The nodes are added to the MCTBNC
		addNodes(nodes);
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

	@Override
	public String[][] predict(Dataset dataset) {
		int numSequences = dataset.getNumDataPoints();
		int numClassVariables = this.dataset.getNumClassVariables();
		String[][] predictions = new String[numSequences][numClassVariables];

		for (int i = 0; i < numSequences; i++) {
			Sequence evidenceSequence = dataset.getSequences().get(i);
			predictions[i] = predict(evidenceSequence);
		}

		return predictions;
	}

	public String[] predict(Sequence sequence) {

		return null;
	}

}
