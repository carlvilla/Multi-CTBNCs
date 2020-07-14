package com.cig.mctbnc.nodes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * It index each node with a unique index. It is necessary that each model has
 * its own indexer, since each model will use different nodes.
 * 
 * @author Carlos Villa (carlos.villa@upm.es)
 *
 */
public class NodeIndexer {

	private Map<String, Integer> nodeToIndex;
	private Map<Integer, Node> indexToNode;

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
		return indexToNode.get(indexNode);
	}

}
