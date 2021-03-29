package com.cig.mctbnc.nodes;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Links nodes with a unique index. It is necessary that each model has its
 * own indexer, since each model will use different nodes. The index starts from
 * 0 and its maximum value is equal to the number of variables. This is
 * necessary for the adjacency matrices, so the same rows and columns are used
 * to refer to a certain node.
 * 
 * @author Carlos Villa Blanco
 * 
 * @param <NodeType>
 *
 */
public class NodeIndexer<NodeType extends Node> {

	private Map<String, Integer> nodeToIndex;
	private Map<Integer, NodeType> indexToNode;
	static Logger logger = LogManager.getLogger(NodeIndexer.class);

	/**
	 * Constructs a {@code NodeIndexer}.
	 * 
	 * @param nodes
	 */
	public NodeIndexer(List<NodeType> nodes) {
		// Set to each node an index number
		this.nodeToIndex = new WeakHashMap<String, Integer>();
		this.indexToNode = new WeakHashMap<Integer, NodeType>();
		for (int i = 0; i < nodes.size(); i++) {
			this.nodeToIndex.put(nodes.get(i).getName(), i);
			this.indexToNode.put(i, nodes.get(i));
		}
	}

	/**
	 * Get the index of a node by providing its name.
	 * 
	 * @param nameNode
	 * @return index of the node
	 */
	public int getIndexNodeByName(String nameNode) {
		return this.nodeToIndex.get(nameNode);
	}

	/**
	 * Get the name of a node by providing its index.
	 * 
	 * @param indexNode
	 * @return name of the node
	 */
	public NodeType getNodeByIndex(int indexNode) {
		NodeType node = this.indexToNode.get(indexNode);
		if (node == null) {
			logger.warn("There is no node with index {}", indexNode);
		}
		return node;
	}

}
