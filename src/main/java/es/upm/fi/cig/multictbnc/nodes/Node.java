package es.upm.fi.cig.multictbnc.nodes;

import es.upm.fi.cig.multictbnc.learning.parameters.SufficientStatistics;

import java.util.List;

/**
 * Interface for a generic node of a PGM.
 *
 * @author Carlos Villa Blanco
 */
public interface Node {
	/**
	 * Returns true if the parameters of the node were estimated.
	 *
	 * @return true if the parameters of the node were estimated, false otherwise
	 */
	boolean areParametersEstimated();

	/**
	 * Remove the set of parents and children of the node. This method should be used with caution, as references to
	 * this node from others will not be affected. It is recommended its use when creating an empty PGM, i.e., we are
	 * removing all edges between nodes.
	 */
	void clearParentAndChildrenSets();

	/**
	 * Returns the local log-likelihood for the node.
	 *
	 * @return local log-likelihood
	 */
	double estimateLogLikelihood();

	/**
	 * Returns the children of the node.
	 *
	 * @return child node list
	 */
	List<Node> getChildren();

	/**
	 * Returns the name of the node.
	 *
	 * @return node name
	 */
	String getName();

	/**
	 * Returns the number of parents of the node.
	 *
	 * @return parent node list
	 */
	int getNumParents();

	/**
	 * Returns the parent of the node whose name is given.
	 *
	 * @param name parent node name
	 * @return the parent node or null if the parent was not found
	 */
	Node getParent(String name);

	/**
	 * Returns the parents of the node.
	 *
	 * @return parent node list
	 */
	List<Node> getParents();

	/**
	 * Specifies if the node has parents.
	 *
	 * @return true if the node has parents, false otherwise
	 */
	boolean hasParents();

	/**
	 * Defines if the node is a class variable.
	 *
	 * @param isClassVariable true if the node is a class variable, false otherwise
	 */
	void isClassVariable(boolean isClassVariable);

	/**
	 * Specifies if the node is a class variable.
	 *
	 * @return true if the node is a class variable, false otherwise
	 */
	boolean isClassVariable();

	/**
	 * Removes the parents and children of the node.
	 */
	void removeAllEdges();

	/**
	 * Removes a certain child of the node.
	 *
	 * @param childNode child node
	 */
	void removeChild(Node childNode);

	/**
	 * Removes the children of the node.
	 */
	void removeChildren();

	/**
	 * Removes a certain parent of the node.
	 *
	 * @param parentNode parent node
	 */
	void removeParent(Node parentNode);

	/**
	 * Removes the parents of the node.
	 */
	void removeParents();

	/**
	 * Defines a provided node as a child of this one.
	 *
	 * @param childNode child node
	 */
	void setChild(Node childNode);

	/**
	 * Defines a provided node as a parent of this one.
	 *
	 * @param parentNode parent node
	 */
	void setParent(Node parentNode);

	/**
	 * Establishes the sufficient statistics of the node.
	 *
	 * @param ss sufficient statistics
	 */
	void setSufficientStatistics(SufficientStatistics ss);

}