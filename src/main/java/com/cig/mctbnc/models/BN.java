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
import com.cig.mctbnc.nodes.DiscreteNode;
import com.cig.mctbnc.nodes.Node;

/**
 * 
 * @author Carlos Villa Blanco
 *
 * @param <N>
 *            Type of nodes that will be learned, e.g., nodes with conditional
 *            probability table (CPTNode)
 */
public class BN<N extends Node> extends AbstractPGM {
	private List<N> learnedNodes;
	private ParameterLearningAlgorithm parameterLearningAlg;
	private StructureLearningAlgorithm structureLearningAlg;
	private Dataset dataset;

	/**
	 * Initialize a Bayesian network by receiving a list of nodes and a dataset.
	 * This constructor was thought to be used by the MCTBNC.
	 * 
	 * @param nodes
	 * @param dataset
	 */
	public BN(List<Node> nodes, Dataset dataset) {
		super(nodes);
		this.dataset = dataset;
	}

	/**
	 * Initialize a Bayesian network by receiving a list of nodes, a dataset and the
	 * algorithms for parameter and structure learning. This constructor was thought
	 * to be used by the MCTBNC.
	 * 
	 * @param nodes
	 * @param dataset
	 * @param parameterLearningAlg
	 * @param structureLearningAlg
	 */
	public BN(List<Node> nodes, Dataset dataset, ParameterLearningAlgorithm parameterLearningAlg,
			StructureLearningAlgorithm structureLearningAlg) {
		super(nodes);
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
			StructureLearningAlgorithm structureLearningAlg) {
		// Create nodes using dataset
		List<Node> nodes = new ArrayList<Node>();
		for (String nameVariable : dataset.getNameVariables()) {
			int index = dataset.getIndexVariable(nameVariable);
			// THIS SHOULD BE CHANGED TO ADMIT OTHER TYPES OF NODES
			Node node = new DiscreteNode(index, nameVariable, dataset.getStatesVariable(nameVariable));
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
		this.learnedNodes = (List<N>) parameterLearningAlg.getParameters();
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
	 * Return the adjacency matrix of the Bayesian network by analysing the children
	 * of each node.
	 * 
	 * @return bidimensional boolean array representing the adjacency matrix
	 */
	public boolean[][] getAdjacencyMatrix() {
		int numNodes = getNumNodes();
		boolean[][] adjacencyMatrix = new boolean[numNodes][numNodes];
		for (Node node : nodes) {
			List<Node> children = node.getChildren();
			for (Node childNode : children) {
				int indexNode = nodeIndexer.getIndexNodeByName(node.getName());
				int indexChildNode = nodeIndexer.getIndexNodeByName(childNode.getName());
				adjacencyMatrix[indexNode][indexChildNode] = true;
			}
		}
		return adjacencyMatrix;
	}

	/**
	 * Obtain the node with certain name
	 * 
	 * @param name
	 * @return node of the provided name
	 */
	public Node getNodeByName(String name) {
		Node selectedNode = nodes.stream().filter(node -> node.getName().equals(name)).findAny().orElse(null);
		return selectedNode;
	}

	public Dataset getDataset() {
		return dataset;
	}

	/**
	 * Return the nodes with the learned parameters.
	 * 
	 * @return nodes with learned parameters
	 */
	public List<N> getLearnedNodes() {
		return this.learnedNodes;
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
