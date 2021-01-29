package com.cig.mctbnc.nodes;

import java.util.List;

import com.cig.mctbnc.learning.parameters.SufficientStatistics;

/**
 * Interface for a generic node of a PGM.
 * 
 * @author Carlos Villa Blanco
 *
 */
public interface Node {
	/**
	 * Return the name of the node.
	 * 
	 * @return node name
	 */
	public String getName();

	/**
	 * Return the children of the node.
	 * 
	 * @return child node list
	 */
	public List<Node> getChildren();

	/**
	 * Return the parents of the node.
	 * 
	 * @return parent node list
	 */
	public List<Node> getParents();

	/**
	 * Return the number of parents of the node.
	 * 
	 * @return parent node list
	 */
	public int getNumParents();

	/**
	 * Return the name of the parents of the node.
	 * 
	 * @return list of parents' names
	 */
	public List<String> getNameParents();

	/**
	 * Define if the node is a class variable.
	 * 
	 * @param isClassVariable boolean that determines if the node is a class
	 *                        variable
	 */
	public void isClassVariable(boolean isClassVariable);

	/**
	 * Specify if the node is a class variable.
	 * 
	 * @return boolean that determines if the node is a class variable
	 */
	public boolean isClassVariable();

	/**
	 * Specify if the node has parents.
	 * 
	 * @return boolean that determines if the node has parents
	 */
	public boolean hasParents();

	/**
	 * Include nodeParent in the parent list of the node, while including the node
	 * in the list of children of nodeParent.
	 * 
	 * @param parentNode
	 */
	public void setParent(Node parentNode);

	/**
	 * Include nodeChild in the children list of the node, while including the node
	 * in the list of parents of nodeChild.
	 * 
	 * @param childNode
	 */
	public void setChild(Node childNode);

	/**
	 * Remove a certain parent of the node. This implies that the node is removed
	 * from the list of children of that parent.
	 * 
	 * @param parentNode
	 */
	public void removeParent(Node parentNode);

	/**
	 * Remove a certain child of the node. This implies that the node is removed
	 * from the list of parents of that child.
	 * 
	 * @param childNode
	 */
	public void removeChild(Node childNode);

	/**
	 * Remove the parents of the node.
	 */
	public void removeParents();

	/**
	 * Remove the children of the node.
	 */
	public void removeChildren();

	/**
	 * Remove the children and parents of the node.
	 */
	public void removeAllEdges();

	/**
	 * Establish the sufficient statistics of the node.
	 * 
	 * @param ss sufficient statistics
	 */
	public void setSufficientStatistics(SufficientStatistics ss);

	/**
	 * Return true if the parameters of the node were estimated.
	 * 
	 * @return true if the parameters of the node were estimated
	 */
	public boolean areParametersEstimated();

}
