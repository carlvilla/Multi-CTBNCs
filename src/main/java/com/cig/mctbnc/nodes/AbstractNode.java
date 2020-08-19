package com.cig.mctbnc.nodes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.cig.mctbnc.data.representation.State;

/**
 * Contains common variables and methods for any kind of node.
 * 
 * @author Carlos Villa Blanco
 *
 */
public abstract class AbstractNode implements Node {
	private String name;
	private boolean classVariable;
	private List<Node> children;
	protected List<Node> parents;

	/**
	 * Common initialization of a node. This constructor initializes the node as not
	 * being a class variables, a type of variables that do not appear in those
	 * models that are not meant for classification. It could be confusion to
	 * specify this attribute for that kind of models.
	 * 
	 * @param index
	 * @param name
	 */
	public AbstractNode(String name) {
		this.name = name;
		children = new ArrayList<Node>();
		parents = new ArrayList<Node>();
	}

	/**
	 * Common initialization of a node. In this constructor is possible to specify
	 * if the node is a class variable.
	 * 
	 * @param index
	 * @param name
	 * @param classVariable
	 */
	public AbstractNode(String name, boolean classVariable) {
		this.name = name;
		this.classVariable = classVariable;
		children = new ArrayList<Node>();
		parents = new ArrayList<Node>();
	}

	@Override
	public void setParent(Node nodeParent) {
		if (!parents.contains(nodeParent))
			parents.add(nodeParent);
		if (!nodeParent.getChildren().contains(this))
			nodeParent.getChildren().add(this);
	}

	@Override
	public void setChild(Node nodeChild) {
		nodeChild.setParent(this);
	}

	@Override
	public void removeParent(Node nodeParent) {
		if (parents.contains(nodeParent))
			parents.remove(nodeParent);
		if (nodeParent.getChildren().contains(this))
			nodeParent.getChildren().remove(this);

	}

	@Override
	public void removeChild(Node nodeChild) {
		nodeChild.removeParent(this);
	}

	@Override
	public void removeParents() {
		// Parent nodes are removed using a temporary list to avoid a concurrent
		// modification exception
		List<Node> tempList = new ArrayList<Node>(parents);
		for (Node parentNode : tempList) {
			removeParent(parentNode);
		}
	}

	@Override
	public void removeChildren() {
		// Child nodes are removed using a temporary list to avoid a concurrent
		// modification exception
		List<Node> tempList = new ArrayList<Node>(children);
		for (Node childNode : tempList) {
			removeChild(childNode);
		}
	}

	@Override
	public void removeAllEdges() {
		// Remove children
		removeChildren();
		// Remove parents
		removeParents();
	}

	@Override
	public void isClassVariable(boolean isClassVariable) {
		this.classVariable = isClassVariable;
	}

	@Override
	public boolean isClassVariable() {
		return classVariable;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public List<Node> getChildren() {
		return children;
	}

	@Override
	public List<Node> getParents() {
		return parents;
	}

	@Override
	public boolean hasParents() {
		return parents.size() > 0;
	}

	@Override
	public boolean equals(Object object) {
		if (object == null || object.getClass() != this.getClass())
			return false;
		// The object if of Node type
		Node otherNode = (Node) object;
		// Nodes must have a unique name, which is enough to determine if two are equal.
		// This method will not differentiate nodes from different models
		return this.name.equals(otherNode.getName());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		return this.name.hashCode() * prime;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Name: " + getName() + "\n");
		sb.append("Class variable: " + isClassVariable() + "\n");
		sb.append("Children: ");
		String[] nameChildren = getChildren().stream().map(Node::getName).toArray(String[]::new);
		sb.append(String.join(", ", nameChildren));
		sb.append("\n");
		sb.append("Parents: ");
		String[] nameParents = getParents().stream().map(Node::getName).toArray(String[]::new);
		sb.append(String.join(", ", nameParents));
		sb.append("\n");
		return sb.toString();
	}

}
