package com.cig.mctbnc.models;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.learning.CTBNLearningAlgorithms;
import com.cig.mctbnc.learning.structure.constraints.StructureConstraints;
import com.cig.mctbnc.nodes.Node;
import com.cig.mctbnc.nodes.NodeFactory;

/**
 * Implements a continuous time Bayesian network.
 * 
 * @author Carlos Villa Blanco
 *
 * @param <NodeType> type of the learned nodes (e.g. nodes that learn a CIM)
 */
public class CTBN<NodeType extends Node> extends AbstractPGM<NodeType> {
	BN<? extends Node> bnClassSubgraph;
	NodeFactory<NodeType> nodeFactory;
	static Logger logger = LogManager.getLogger(CTBN.class);

	/**
	 * Initialize a continuous Time Bayesian network given dataset, the list of
	 * variables to use and the algorithms for parameter and structure learning.
	 * This constructor was thought to be used by the MCTBNC.
	 * 
	 * @param trainingDataset
	 * @param nameVariables
	 * @param ctbnLearningAlgs
	 * @param structureConstraints
	 * @param bnClassSubgraph      Bayesian network that models the class subgraph
	 *                             (necessary to estimate conditional
	 *                             log-likelihood)
	 * @param nodeClass            type of the CTBN nodes
	 */
	public CTBN(Dataset trainingDataset, List<String> nameVariables, CTBNLearningAlgorithms ctbnLearningAlgs,
			StructureConstraints structureConstraints, BN<? extends Node> bnClassSubgraph, Class<NodeType> nodeClass) {
		// Set variables to use
		this.nameVariables = nameVariables;
		// Set node type
		this.nodeClass = nodeClass;
		// Set necessary algorithms to learn the model
		setParameterLearningAlgorithm(ctbnLearningAlgs.getParameterLearningAlgorithm());
		setStructureLearningAlgorithm(ctbnLearningAlgs.getStructureLearningAlgorithm());
		setStructureConstraints(structureConstraints);
		// Set the training dataset
		setTrainingDataset(trainingDataset);
		// Set the class subgraph
		this.bnClassSubgraph = bnClassSubgraph;
	}

	/**
	 * Initialize a continuous Time Bayesian network given dataset, the list of
	 * variables to use and the algorithms for parameter and structure learning.
	 * This constructor is used for tests.
	 * 
	 * @param trainingDataset
	 * @param nameVariables
	 * @param ctbnLearningAlgs
	 * @param structureConstraints
	 * @param bnClassSubgraph      Bayesian network that models the class subgraph
	 *                             (necessary to estimate conditional
	 *                             log-likelihood)
	 * @param nodeClass            type of the CTBN nodes
	 */
	public CTBN(Dataset trainingDataset, List<String> nameVariables, CTBNLearningAlgorithms ctbnLearningAlgs,
			StructureConstraints structureConstraints, Class<NodeType> nodeClass) {
		// Set variables to use
		this.nameVariables = nameVariables;
		// Set node type
		this.nodeClass = nodeClass;
		// Set necessary algorithms to learn the model
		setParameterLearningAlgorithm(ctbnLearningAlgs.getParameterLearningAlgorithm());
		setStructureLearningAlgorithm(ctbnLearningAlgs.getStructureLearningAlgorithm());
		setStructureConstraints(structureConstraints);
		// Set the training dataset
		setTrainingDataset(trainingDataset);
	}

	/**
	 * Modify the structure of the CTBN by changing the parent set of an specified
	 * node and update the parameters of the model. This method is necessary to
	 * learn the structure of a CTBN by optimizing the parent set of its nodes.
	 * 
	 * @param nodeIndex
	 * @param adjacencyMatrix
	 */
	public void setStructure(int nodeIndex, boolean[][] adjacencyMatrix) {
		Node node = nodeIndexer.getNodeByIndex(nodeIndex);
		// Current parents of the node are removed
		node.removeParents();
		for (int i = 0; i < adjacencyMatrix.length; i++)
			if (adjacencyMatrix[i][nodeIndex]) {
				Node parentNode = nodeIndexer.getNodeByIndex(i);
				node.setParent(parentNode);
			}
		parameterLearningAlg.learn(node, dataset);
	}

	/**
	 * Return the class subgraph (Bayesian network). This is necessary when the
	 * structure is defined by optimizing the conditional log-likelihood.
	 * 
	 * @return Bayesian network modelling the class subgraph
	 */
	public BN<? extends Node> getBnClassSubgraph() {
		return bnClassSubgraph;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("--Structure continuous time Bayesian network--\n");
		for (Node node : nodes) {
			if (node.getChildren().isEmpty())
				sb.append("(" + node.getName() + ")");
			else {
				sb.append("(" + node.getName() + ") => ");
				for (Node child : node.getChildren()) {
					sb.append(String.join(", ", "(" + child.getName() + ")"));
				}
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	@Override
	public String getType() {
		return "Continuous time Bayesian network";
	}

}
