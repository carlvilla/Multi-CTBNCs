package com.cig.mctbnc.nodes;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractNode implements Node {
	private int index;
	private String name;
	private boolean classVariable;
	private List<Node> children;
	private List<Node> parents;

	/**
	 * Common initialization of a node. This constructor initializes the node as not
	 * being a class variables, a type of variables that do not appear in those
	 * models that are not meant for classification. It could be confusion to
	 * specify this attribute for that kind of models.
	 * 
	 * @param index
	 * @param name
	 */
	public AbstractNode(int index, String name) {
		this.index = index;
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
	public AbstractNode(int index, String name, boolean classVariable) {
		this.index = index;
		this.name = name;
		this.classVariable = classVariable;
		children = new ArrayList<Node>();
		parents = new ArrayList<Node>();
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
	public int getIndex() {
		return index;
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
	public void setChild(Node nodeChild) {
		if (!children.contains(nodeChild))
			children.add(nodeChild);
		if (!nodeChild.getParents().contains(this))
			nodeChild.setParent(this);
	}

	@Override
	public void setParent(Node nodeParent) {
		if (!parents.contains(nodeParent))
			parents.add(nodeParent);
		if (!nodeParent.getChildren().contains(this))
			nodeParent.setChild(this);
	}

	@Override
	public void removeChild(Node nodeChild) {
		if (children.contains(nodeChild))
			children.remove(nodeChild);
		// if(nodeChild.getParents().contains(this))
		// nodeChild.removeParent(this);
	}

	@Override
	public void removeParent(Node nodeParent) {
		if (parents.contains(nodeParent))
			parents.remove(nodeParent);
		if (nodeParent.getChildren().contains(this))
			nodeParent.removeChild(this);

	}

	@Override
	public void removeAllEdges() {
		// Remove children
		// The node is removed from the parent list of its children
		for (Node child : children) {
			child.getParents().remove(this);
		}
		// The child list is cleared
		children.clear();

		// Remove parents
		for (Node parent : parents) {
			parent.getChildren().remove(this);
		}
		parents.clear();
	}

	@Override
	public boolean hasParents() {
		return parents.size() > 0;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Name: " + getName() + "\n");
		sb.append("Index: " + getIndex() + "\n");
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
