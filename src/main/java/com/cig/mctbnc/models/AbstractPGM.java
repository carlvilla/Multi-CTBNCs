package com.cig.mctbnc.models;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.graphstream.graph.Graph;

import com.cig.mctbnc.nodes.Node;
import com.cig.mctbnc.nodes.NodeIndexer;

public abstract class AbstractPGM implements PGM {

	protected List<Node> nodes;
	protected NodeIndexer nodeIndexer;

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

	@Override
	public List<Node> getNodes() {
		return nodes;
	}

	@Override
	public Node getNodeByIndex(int index) {
		Node selectedNode = nodes.stream().filter(node -> node.getIndex() == index).findAny().orElse(null);
		return selectedNode;
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

}
