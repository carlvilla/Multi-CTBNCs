package com.cig.mctbnc.models;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.graphstream.graph.Graph;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.nodes.Node;
import com.cig.mctbnc.nodes.NodeIndexer;

public abstract class AbstractPGM implements PGM {

	protected List<Node> nodes;
	protected NodeIndexer nodeIndexer;
	protected Dataset dataset;

	public AbstractPGM() {
	}

	/**
	 * Common initialization for PGM.
	 * 
	 * @param nodes
	 */
	public AbstractPGM(List<Node> nodes) {
		addNodes(nodes);
	}

	@Override
	public void addNodes(List<Node> nodes) {
		if (this.nodes == null) {
			this.nodes = new ArrayList<Node>();
		}
		this.nodes.addAll(nodes);
		nodeIndexer = new NodeIndexer(this.nodes);
	}

	@Override
	public void addNodes(Graph graph, List<Node> nodes) {
		for (Node node : nodes) {
			String nameNode = node.getName();
			graph.addNode(nameNode).addAttribute("ui.label", nameNode);
		}
	}

	@Override
	public void addEdges(Graph graph, List<Node> nodes) {
		for (Node node : nodes) {
			String nameNode = node.getName();
			for (Node child : node.getChildren()) {
				String nameChild = child.getName();
				graph.addEdge(nameNode + nameChild, nameNode, nameChild, true);
			}
		}
	}

	/**
	 * Return the adjacency matrix of the PGM by analysing the children of each
	 * node.
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

	@Override
	public List<Node> getNodes() {
		return nodes;
	}

	@Override
	public Node getNodeByIndex(int index) {
		return nodeIndexer.getNodeByIndex(index);
	}

	@Override
	public List<Node> getNodesClassVariables() {
		return nodes.stream().filter(node -> node.isClassVariable()).collect(Collectors.toList());
	}

	@Override
	public List<Node> getNodesFeatures() {
		return nodes.stream().filter(node -> !node.isClassVariable()).collect(Collectors.toList());
	}

	@Override
	public int getNumNodes() {
		return nodes.size();
	}

	public Dataset getDataset() {
		return dataset;
	}

	/**
	 * Display the PGM using GraphStream.
	 * 
	 * @param graph
	 */
	public void display() {
		// addNodes(graph, nodes);
		// addEdges(graph, nodes);
		// graph.display();
	}

}
