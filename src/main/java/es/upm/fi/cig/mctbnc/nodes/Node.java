package es.upm.fi.cig.mctbnc.nodes;

import java.util.List;

import es.upm.fi.cig.mctbnc.learning.parameters.SufficientStatistics;

/**
 * Interface for a generic node of a PGM.
 * 
 * @author Carlos Villa Blanco
 *
 */
public interface Node {
	/**
	 * Returns the name of the node.
	 * 
	 * @return node name
	 */
	public String getName();

	/**
	 * Returns the children of the node.
	 * 
	 * @return child node list
	 */
	public List<Node> getChildren();

	/**
	 * Returns the parents of the node.
	 * 
	 * @return parent node list
	 */
	public List<Node> getParents();

	/**
	 * Returns the number of parents of the node.
	 * 
	 * @return parent node list
	 */
	public int getNumParents();

	/**
	 * Returns the name of the parents of the node.
	 * 
	 * @return list of parents' names
	 */
	public List<String> getNameParents();

	/**
	 * Defines if the node is a class variable.
	 * 
	 * @param isClassVariable true if the node is a class variable, false otherwise
	 */
	public void isClassVariable(boolean isClassVariable);

	/**
	 * Specifies if the node is a class variable.
	 * 
	 * @return true if the node is a class variable, false otherwise
	 */
	public boolean isClassVariable();

	/**
	 * Specifies if the node has parents.
	 * 
	 * @return true if the node has parents, false otherwise
	 */
	public boolean hasParents();

	/**
	 * Defines a provided node as a parent of this one.
	 * 
	 * @param parentNode parent node
	 */
	public void setParent(Node parentNode);

	/**
	 * Defines a provided node as a child of this one.
	 * 
	 * @param childNode child node
	 */
	public void setChild(Node childNode);

	/**
	 * Removes a certain parent of the node.
	 * 
	 * @param parentNode parent node
	 */
	public void removeParent(Node parentNode);

	/**
	 * Removes a certain child of the node.
	 * 
	 * @param childNode child node
	 */
	public void removeChild(Node childNode);

	/**
	 * Removes the parents of the node.
	 */
	public void removeParents();

	/**
	 * Removes the children of the node.
	 */
	public void removeChildren();

	/**
	 * Removes the parents and children of the node.
	 */
	public void removeAllEdges();

	/**
	 * Establishes the sufficient statistics of the node.
	 * 
	 * @param ss sufficient statistics
	 */
	public void setSufficientStatistics(SufficientStatistics ss);

	/**
	 * Returns true if the parameters of the node were estimated.
	 * 
	 * @return true if the parameters of the node were estimated, false otherwise
	 */
	public boolean areParametersEstimated();

}
