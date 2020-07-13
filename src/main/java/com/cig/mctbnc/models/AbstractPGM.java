package main.java.com.cig.mctbnc.models;

import java.util.ArrayList;
import java.util.List;

import org.graphstream.graph.Graph;

public abstract class AbstractPGM implements PGM {
	
	protected List<Node> nodes;
	protected NodeIndexer nodeIndexer;
	
	public AbstractPGM() {}
	
	public AbstractPGM(List<Node> nodes) {
		this.nodes = new ArrayList<Node>();
		this.nodes.addAll(nodes);
		nodeIndexer = new NodeIndexer(this.nodes);
	}
	
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
	
	public List<Node> getNodes() {
		return nodes;
	}

	public int getNumNodes() {
		return nodes.size();
	}

}
