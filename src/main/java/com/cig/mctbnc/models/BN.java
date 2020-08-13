package com.cig.mctbnc.models;

import java.util.ArrayList;
import java.util.List;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.learning.parameters.ParameterLearningAlgorithm;
import com.cig.mctbnc.learning.structure.StructureLearningAlgorithm;
import com.cig.mctbnc.learning.structure.constraints.StructureConstraints;
import com.cig.mctbnc.nodes.Node;
import com.cig.mctbnc.nodes.NodeFactory;

/**
 * 
 * @author Carlos Villa Blanco
 *
 * @param <NodeType> Type of nodes that will be learned, e.g., nodes with
 *                   conditional probability table (CPTNode)
 */
public class BN<NodeType extends Node> extends AbstractPGM<NodeType> {
	NodeFactory<NodeType> nodeFactory;

	/**
	 * Initialize a Bayesian network by receiving a list of nodes and a dataset.
	 * This constructor was thought to be used by the MCTBNC.
	 * 
	 * @param nodes
	 * @param dataset
	 */
	public BN(List<NodeType> nodes, Dataset dataset) {
		super(nodes);
		this.dataset = dataset;
	}

	/**
	 * Initialize a Bayesian network by receiving a dataset, a list of variables to
	 * use and the algorithms for parameter and structure learning. This constructor
	 * was thought to be used by the MCTBNC.
	 * 
	 * @param nameVariables
	 * @param dataset
	 * @param parameterLearningAlg
	 * @param structureLearningAlg
	 * @param structureConstraints
	 * @param nodeClass
	 */
	public BN(Dataset dataset, List<String> nameVariables, ParameterLearningAlgorithm parameterLearningAlg,
			StructureLearningAlgorithm structureLearningAlg, StructureConstraints structureConstraints,
			Class<NodeType> nodeClass) {
		// Node factory to create nodes of the specified type
		this.nodeFactory = new NodeFactory<NodeType>(nodeClass);
		// Create nodes using dataset
		List<NodeType> nodes = new ArrayList<NodeType>();
		for (String nameVariable : nameVariables) {
			NodeType node = nodeFactory.createNode(nameVariable, dataset);
			nodes.add(node);
		}
		addNodes(nodes);
		// Set necessary algorithms to learn the model
		setParameterLearningAlgorithm(parameterLearningAlg);
		setStructureLearningAlgorithm(structureLearningAlg);
		setStructureConstraints(structureConstraints);
		// Initialize structure of the model
		structureConstraints.initializeStructure(this);
		this.dataset = dataset;

	}

	/**
	 * Initialize a Bayesian network by receiving a dataset and the algorithmms for
	 * parameter and structure. The list of nodes is created from the dataset.
	 * 
	 * @param parameterLearningAlg
	 * @param structureLearningAlg
	 * @param dataset
	 */
	public BN(Dataset dataset, ParameterLearningAlgorithm parameterLearningAlg,
			StructureLearningAlgorithm structureLearningAlg, StructureConstraints structureConstraints,
			Class<NodeType> nodeClass) {
		// Node factory to create nodes of the specified type
		this.nodeFactory = new NodeFactory<NodeType>(nodeClass);
		// Create nodes using dataset
		List<NodeType> nodes = new ArrayList<NodeType>();
		for (String nameVariable : dataset.getNameVariables()) {
			NodeType node = nodeFactory.createNode(nameVariable, dataset);
			nodes.add(node);
		}
		addNodes(nodes);
		// Set necessary algorithms to learn the model
		setParameterLearningAlgorithm(parameterLearningAlg);
		setStructureLearningAlgorithm(structureLearningAlg);
		setStructureConstraints(structureConstraints);
		this.dataset = dataset;
	}
	
	public String[] getNameNodes() {
		return nodes.stream().map(Node::getName).toArray(String[]::new);
	}

	/**
	 * Obtain the node with certain name
	 * 
	 * @param name
	 * @return node of the provided name
	 */
	public NodeType getNodeByName(String name) {
		NodeType selectedNode = nodes.stream().filter(node -> node.getName().equals(name)).findAny().orElse(null);
		return selectedNode;
	}

	/**
	 * Return the nodes with the learned parameters. This can be, for example, a
	 * list of CPTNode objects that store conditional probability tables.
	 * 
	 * @return nodes with learned parameters
	 */
	public List<NodeType> getLearnedNodes() {
		return (List<NodeType>) nodes;
	}

	@Override
	public void display() {
		Graph graph = new SingleGraph("BN");
		addNodes(graph, nodes);
		addEdges(graph, nodes);
		graph.display();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("--Structure Bayesian network--\n");
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
		return "Bayesian network";
	}

}
