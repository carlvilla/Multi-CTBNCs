package com.cig.mctbnc.models;

import java.util.ArrayList;
import java.util.List;

import org.graphstream.graph.Graph;

import com.cig.mctbnc.nodes.Node;
import com.cig.mctbnc.nodes.NodeIndexer;

public abstract class AbstractPGM<T> implements PGM<T> {

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

	/**
	 * Add the nodes to list of the PGM. It is necessary to include a NodeIndexer
	 * object in order to keep track of their index.
	 * 
	 * @param nodes
	 */
	public void addNodes(List<Node> nodes) {
		if(this.nodes == null) {
			this.nodes = new ArrayList<Node>();
		}
		this.nodes.addAll(nodes);
		nodeIndexer = new NodeIndexer(this.nodes);
	}

	/**
	 * Add nodes to a Graph object. This graph will be used to display the PGM.
	 * 
	 * @param graph
	 * @param nodes
	 */
	protected void addNodes(Graph graph, List<Node> nodes) {
		for (Node node : nodes) {
			String nameNode = node.getName();
			graph.addNode(nameNode).addAttribute("ui.label", nameNode);
		}
	}

	/**
	 * Add to a Graph object the nodes passed as an argument and edges with their
	 * children. This graph will be used to display the PGM.
	 * 
	 * @param graph
	 * @param nodes
	 */
	protected void addEdges(Graph graph, List<Node> nodes) {
		for (Node node : nodes) {
			String nameNode = node.getName();
			for (Node child : node.getChildren()) {
				String nameChild = child.getName();
				graph.addEdge(nameNode + nameChild, nameNode, nameChild, true);
			}
		}
	}

	public List<Node> getNodes() {
		return nodes;
	}

	public int getNumNodes() {
		return nodes.size();
	}

}
