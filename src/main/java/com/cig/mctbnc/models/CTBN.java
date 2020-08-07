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
import com.cig.mctbnc.learning.structure.constraints.StructureConstraints;
import com.cig.mctbnc.nodes.Node;
import com.cig.mctbnc.nodes.NodeFactory;

/**
 * Implements a continuous time Bayesian network classifier. This is a modified
 * version of the original where there can be more than one class variable.
 * 
 * @author Carlos Villa Blanco
 *
 * @param <NodeType> type of the learned nodes (e.g. nodes that learn a CIM)
 */
public class CTBN<NodeType extends Node> extends AbstractPGM<NodeType> {
	NodeFactory<NodeType> nodeFactory;
	static Logger logger = LogManager.getLogger(CTBN.class);

	/**
	 * Initialize a continuous Time Bayesian network given dataset, the list of
	 * variables to use and the algorithms for parameter and structure learning.
	 * This constructor was thought to be used by the MCTBNC.
	 * 
	 * @param nameVariables
	 * @param dataset
	 * @param parameterLearningAlg
	 * @param structureLearningAlg
	 * @param structureConstraints 
	 * @param nodeClass type of the CTBN nodes
	 */
	public CTBN(Dataset dataset, List<String> nameVariables, ParameterLearningAlgorithm parameterLearningAlg,
			StructureLearningAlgorithm structureLearningAlg, StructureConstraints structureConstraints, Class<NodeType> nodeClass) {
		// Node factory to create nodes of the specified type
		this.nodeFactory = new NodeFactory<NodeType>(nodeClass);
		List<NodeType> nodes = new ArrayList<NodeType>();
		for (String nameVariable : dataset.getNameVariables()) {
			NodeType node = nodeFactory.createNode(nameVariable, dataset);
			nodes.add(node);
		}
		addNodes(nodes);
		setParameterLearningAlgorithm(parameterLearningAlg);
		setStructureLearningAlgorithm(structureLearningAlg);
		setStructureConstraints(structureConstraints);
		this.dataset = dataset;
	}

	@Override
	public void learn() {
		structureLearningAlg.learn(this, dataset, parameterLearningAlg);
	}

	@Override
	public void setStructure(boolean[][] adjacencyMatrix) {
		// Current edges are removed
		for (Node node : nodes) {
			node.removeAllEdges();
		}
		for (int i = 0; i < adjacencyMatrix.length; i++) {
			Node node = nodeIndexer.getNodeByIndex(i);

			for (int j = 0; j < adjacencyMatrix.length; j++) {
				if (adjacencyMatrix[i][j]) {
					Node childNode = nodeIndexer.getNodeByIndex(j);
					node.setChild(childNode);
				}
			}
		}
		parameterLearningAlg.learn(nodes, dataset);
	}

	@Override
	public String getType() {
		return "Continuous time Bayesian network";
	}

	@Override
	public void display() {
		Graph graph = new SingleGraph("CTBN");
		addNodes(graph, nodes);
		addEdges(graph, nodes);
		graph.display();
	}

}
