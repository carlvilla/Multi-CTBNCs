package es.upm.fi.cig.multictbnc.nodes;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Links nodes with a unique index. Each model should have its indexer since each model will use different nodes. The
 * index starts from zero, and its maximum value is equal to the number of variables. This is necessary for the
 * adjacency matrices, so the same rows and columns are used to refer to a certain node.
 *
 * @param <NodeType> type of nodes that are indexed, e.g., nodes with conditional probability tables ({@code CPTNode})
 *                   or conditional intensity matrices (@code CIMNode)
 * @author Carlos Villa Blanco
 */
public class NodeIndexer<NodeType extends Node> {
	private static final Logger logger = LogManager.getLogger(NodeIndexer.class);
	private Map<String, Integer> nodeToIndex;
	private Map<Integer, NodeType> indexToNode;

	/**
	 * Constructs a {@code NodeIndexer}.
	 *
	 * @param nodes nodes to index
	 */
	public NodeIndexer(List<NodeType> nodes) {
		// Set to each node an index number
		this.nodeToIndex = new LinkedHashMap<>();
		this.indexToNode = new LinkedHashMap<>();
		for (int i = 0; i < nodes.size(); i++) {
			this.nodeToIndex.put(nodes.get(i).getName(), i);
			this.indexToNode.put(i, nodes.get(i));
		}
	}

	/**
	 * Returns the index of a node whose name is provided.
	 *
	 * @param nameNode node name
	 * @return node index
	 */
	public int getIndexNodeByName(String nameNode) {
		return this.nodeToIndex.get(nameNode);
	}

	/**
	 * Returns the indexes of all the nodes.
	 *
	 * @return indexes of all the nodes
	 */
	public List<Integer> getIndexNodes() {
		return new ArrayList<>(indexToNode.keySet());
	}

	/**
	 * Returns the name of a node whose index is provided.
	 *
	 * @param idxNode node index
	 * @return node name
	 */
	public NodeType getNodeByIndex(int idxNode) {
		NodeType node = this.indexToNode.get(idxNode);
		if (node == null)
			logger.warn("There is no node with index {}", idxNode);
		return node;
	}

}