package es.upm.fi.cig.mctbnc.nodes;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Links nodes with a unique index. It is necessary that each model has its own
 * indexer, since each model will use different nodes. The index starts from 0
 * and its maximum value is equal to the number of variables. This is necessary
 * for the adjacency matrices, so the same rows and columns are used to refer to
 * a certain node.
 * 
 * @author Carlos Villa Blanco
 * 
 * @param <NodeType> type of nodes that are indexed, e.g., nodes with
 *                   conditional probability tables ({@code CPTNode}) or
 *                   conditional intensity matrices (@code CIMNode)
 *
 */
public class NodeIndexer<NodeType extends Node> {

	private Map<String, Integer> nodeToIndex;
	private Map<Integer, NodeType> indexToNode;
	static Logger logger = LogManager.getLogger(NodeIndexer.class);

	/**
	 * Constructs a {@code NodeIndexer}.
	 * 
	 * @param nodes nodes to index
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
	 * Gets the index of a node by providing its name.
	 * 
	 * @param nameNode node name
	 * @return node index
	 */
	public int getIndexNodeByName(String nameNode) {
		return this.nodeToIndex.get(nameNode);
	}

	/**
	 * Gets the name of a node by providing its index.
	 * 
	 * @param indexNode node index
	 * @return node name
	 */
	public NodeType getNodeByIndex(int indexNode) {
		NodeType node = this.indexToNode.get(indexNode);
		if (node == null)
			logger.warn("There is no node with index {}", indexNode);
		return node;
	}

}