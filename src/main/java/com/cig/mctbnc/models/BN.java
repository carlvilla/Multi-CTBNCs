package com.cig.mctbnc.models;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.learning.parameters.ParameterLearningAlgorithm;
import com.cig.mctbnc.learning.structure.StructureLearningAlgorithm;
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
	private ParameterLearningAlgorithm parameterLearningAlg;
	private StructureLearningAlgorithm structureLearningAlg;
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
	 */
	public BN(Dataset dataset, List<String> nameVariables, ParameterLearningAlgorithm parameterLearningAlg,
			StructureLearningAlgorithm structureLearningAlg, Class<NodeType> nodeClass) {
		// Node factory to create nodes of the specified type
		this.nodeFactory = new NodeFactory<NodeType>(nodeClass);
		// Create nodes using dataset
		List<NodeType> nodes = new ArrayList<NodeType>();
		for (String nameVariable : nameVariables) {
			NodeType node = nodeFactory.createNode(nameVariable, dataset);
			nodes.add(node);
		}
		addNodes(nodes);
		setParameterLearningAlgorithm(parameterLearningAlg);
		setStructureLearningAlgorithm(structureLearningAlg);
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
			StructureLearningAlgorithm structureLearningAlg, Class<NodeType> nodeClass) {
		// Node factory to create nodes of the specified type
		this.nodeFactory = new NodeFactory<NodeType>(nodeClass);
		// Create nodes using dataset
		List<NodeType> nodes = new ArrayList<NodeType>();
		for (String nameVariable : dataset.getNameVariables()) {
			NodeType node = nodeFactory.createNode(nameVariable, dataset);
			nodes.add(node);
		}
		addNodes(nodes);
		setParameterLearningAlgorithm(parameterLearningAlg);
		setStructureLearningAlgorithm(structureLearningAlg);
		this.dataset = dataset;
	}

	/**
	 * Establish the algorithm that will be used to learn the parameters of the
	 * Bayesian network.
	 * 
	 * @param parameterLearningAlg
	 */
	public void setParameterLearningAlgorithm(ParameterLearningAlgorithm parameterLearningAlg) {
		this.parameterLearningAlg = parameterLearningAlg;
	}

	/**
	 * Establish the algorithm that will be used to learn the structure of the
	 * Bayesian network.
	 * 
	 * @param structureLearningAlg
	 */
	public void setStructureLearningAlgorithm(StructureLearningAlgorithm structureLearningAlg) {
		this.structureLearningAlg = structureLearningAlg;
	}

	@Override
	public void setStructure(boolean[][] adjacencyMatrix) {
		// Current edges are removed
		for (Node node : this.nodes) {
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
		// Learn the sufficient statistics and parameters for each node
		parameterLearningAlg.learn(nodes, dataset);
	}

	public BN() {
	}

	@Override
	public void learn() {
		structureLearningAlg.learn(this, dataset, parameterLearningAlg);
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

	/**
	 * Check if the structure (given by an adjacencyMatrix) is legal for a Bayesian
	 * network. The method determines if there are cycles in an adjacency matrix.
	 * Modified version of code in
	 * https://www.geeksforgeeks.org/detect-cycle-in-a-directed-graph-using-bfs/.
	 * 
	 * @param adjacencyMatrix
	 * @return boolean that determines if the structure is valid
	 */
	public boolean isStructureLegal(boolean[][] adjacencyMatrix) {
		int numNodes = adjacencyMatrix.length;

		// Indegrees of all nodes.
		int[] inDegree = new int[numNodes];

		for (int i = 0; i < numNodes; i++) {
			for (int j = 0; j < numNodes; j++) {
				if (i != j && adjacencyMatrix[i][j]) {
					inDegree[j]++;
				}
			}
		}

		// Enqueue all nodes with indegree 0
		Queue<Integer> q = new LinkedList<Integer>();
		for (int i = 0; i < numNodes; i++)
			if (inDegree[i] == 0)
				q.add(i);

		// Initialize count of visited vertices
		int countVisitedNodes = 0;

		// One by one dequeue vertices from queue and enqueue
		// adjacents if indegree of adjacent becomes 0
		while (!q.isEmpty()) {

			// Extract node from queue
			int i = q.poll();

			// Iterate over all children nodes of dequeued node i and
			// decrease their in-degree by 1
			for (int j = 0; j < numNodes; j++)
				if (i != j && adjacencyMatrix[i][j] && --inDegree[j] == 0)
					q.add(j);
			countVisitedNodes++;
		}

		// There are cycles if the number of visited nodes are different from the number
		// of nodes in the graph
		return countVisitedNodes == numNodes;
	}

}
