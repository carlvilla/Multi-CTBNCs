package main.java.com.cig.mctbnc.models;

import java.util.List;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;

import main.java.com.cig.mctbnc.learning.parameters.BNParameterLearning;
import main.java.com.cig.mctbnc.learning.parameters.CPTNode;
import main.java.com.cig.mctbnc.learning.structure.BNStructureLearning;

/**
 * 
 * @author carlosvillablanco
 *
 * @param <T>
 *            Type of nodes that will be learned, e.g., nodes with conditional
 *            probability table (CPTNode)
 */
public class BN<T extends Node> extends AbstractPGM {

	private List<Node> nodes;
	private List<T> learnedNodes;
	private NodeIndexer nodeIndexer;
	private BNParameterLearning bnParameterLearning;
	private BNStructureLearning bnStructureLearning;
	private Dataset dataset;

	public BN(List<Node> nodes, Dataset dataset) {
		this.nodes = nodes;
		this.dataset = dataset;
		nodeIndexer = new NodeIndexer(nodes);
	}

	public BN(List<Node> nodes, BNParameterLearning bnParameterLearning, BNStructureLearning bnStructureLearning,
			Dataset dataset) {
		this.nodes = nodes;
		this.bnParameterLearning = bnParameterLearning;
		this.bnStructureLearning = bnStructureLearning;
		this.dataset = dataset;
		nodeIndexer = new NodeIndexer(nodes);
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
		for(int i = 0; i < adjacencyMatrix.length; i++) {
			Node node = nodeIndexer.getNodeByIndex(i);
			node.removeAllEdges();
			
			for(int j = 0;j < adjacencyMatrix.length; j++) {
				if(adjacencyMatrix[i][j]) {
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

	public List<Node> getNodes() {
		return nodes;
	}

	public int getNumNodes() {
		return nodes.size();
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
		Graph graph = new SingleGraph("MCTBNC");
		addNodes(graph, nodes);
		addEdges(graph, nodes);
		graph.display();
	}

	@Override
	public Object[][] predict() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void sample() {
		// TODO Auto-generated method stub

	}

}
