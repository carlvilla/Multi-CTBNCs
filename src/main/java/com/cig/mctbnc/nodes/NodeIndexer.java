package com.cig.mctbnc.nodes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Link each node with a unique index. It is necessary that each model has its
 * own indexer, since each model will use different nodes. The index starts from
 * 0 and its maximum value is equal to the number of variables. This is
 * necessary for the adjacency matrices, so the same rows and columns are used
 * to refer to a certain node.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class NodeIndexer {

	private Map<String, Integer> nodeToIndex;
	private Map<Integer, Node> indexToNode;
	static Logger logger = LogManager.getLogger(NodeIndexer.class);

	public NodeIndexer(List<Node> nodes) {
		// Set to each node an index number
		nodeToIndex = new HashMap<String, Integer>();
		indexToNode = new HashMap<Integer, Node>();
		for (int i = 0; i < nodes.size(); i++) {
			nodeToIndex.put(nodes.get(i).getName(), i);
			indexToNode.put(i, nodes.get(i));
		}
	}

	public int getIndexNodeByName(String nameNode) {
		return nodeToIndex.get(nameNode);
	}

	public Node getNodeByIndex(int indexNode) {
		Node node = indexToNode.get(indexNode);
		if (node == null) {
			logger.warn("There is no node with index {}", indexNode);
		}
		return node;
	}

}
