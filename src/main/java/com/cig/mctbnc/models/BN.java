package main.java.com.cig.mctbnc.models;

import java.util.List;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;

import main.java.com.cig.mctbnc.data.representation.Dataset;
import main.java.com.cig.mctbnc.learning.parameters.BNParameterLearning;
import main.java.com.cig.mctbnc.learning.structure.BNStructureLearning;
import main.java.com.cig.mctbnc.nodes.Node;

/**
 * 
 * @author Carlos Villa (carlos.villa@upm.es)
 *
 * @param <T>
 *            Type of nodes that will be learned, e.g., nodes with conditional
 *            probability table (CPTNode)
 */
public class BN<T extends Node> extends AbstractPGM<String> {
	private List<T> learnedNodes;
	private BNParameterLearning bnParameterLearning;
	private BNStructureLearning bnStructureLearning;
	private Dataset dataset;

	public BN(List<Node> nodes, Dataset dataset) {
		super(nodes);
		this.dataset = dataset;
	}

	public BN(List<Node> nodes, BNParameterLearning bnParameterLearning, BNStructureLearning bnStructureLearning,
			Dataset dataset) {
		super(nodes);
		this.bnParameterLearning = bnParameterLearning;
		this.bnStructureLearning = bnStructureLearning;
		this.dataset = dataset;
	}

	/**
	 * Establish the algorithm that will be used to learn the parameters of the
	 * Bayesian network.
	 * 
	 * @param bnParameterLearning
	 */
	public void setParameterLearningAlgorithm(BNParameterLearning bnParameterLearning) {
		this.bnParameterLearning = bnParameterLearning;
	}

	/**
	 * Establish the algorithm that will be used to learn the structure of the
	 * Bayesian network.
	 * 
	 * @param bnParameterLearning
	 */
	public void setStructureLearningAlgorithm(BNStructureLearning bnStructureLearning) {
		this.bnStructureLearning = bnStructureLearning;
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

		bnParameterLearning.learn(nodes, dataset);
		this.learnedNodes = (List<T>) bnParameterLearning.getParameters();
	}

	@Override
	public void learn() {
		bnStructureLearning.learn(this, bnParameterLearning, dataset);
	}

	public String[] getNameNodes() {
		return nodes.stream().map(Node::getName).toArray(String[]::new);
	}

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
	 * Obtain the node with certain index
	 * 
	 * @param index
	 * @return
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
	 * @return
	 */
	public List<T> getLearnedNodes() {
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
	public String[][] predict() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void sample() {
		// TODO Auto-generated method stub

	}

}
