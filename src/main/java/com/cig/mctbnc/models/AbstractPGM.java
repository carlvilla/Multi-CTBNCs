package main.java.com.cig.mctbnc.models;

import java.util.List;

import org.graphstream.graph.Graph;

public abstract class AbstractPGM implements PGM {
	
	/**
	 * Add nodes to specified graph.
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
	 * Add to the specified graph the nodes passed as an argument and edges with
	 * their children.
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

}
