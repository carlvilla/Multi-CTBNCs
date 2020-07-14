package com.cig.mctbnc.nodes;

import java.util.List;

public interface Node {

	public int getIndex();

	public String getName();

	public List<Node> getChildren();

	public List<Node> getParents();

	public boolean hasParents();

	/**
	 * Include nodeChild in the children list of the node, while including the node
	 * in the list of parents of nodeChild.
	 * @param nodeChild
	 */
	public void setChild(Node nodeChild);

	/**
	 * Include nodeParent in the parent list of the node, while including the node
	 * in the list of children of nodeParent.
	 * @param nodeParent
	 */
	public void setParent(Node nodeParent);

	/**
	 * Remove a certain child of the node. This implies that the node is removed
	 * from the list of parents of that child.
	 * 
	 * @param nodeChild
	 */
	public void removeChild(Node nodeChild);

	/**
	 * Remove a certain parent of the node. This implies that the node is removed
	 * from the list of children of that parent.
	 * 
	 * @param nodeParent
	 */
	public void removeParent(Node nodeParent);

	/**
	 * Remove the children and parents.
	 */
	public void removeAllEdges();

}
