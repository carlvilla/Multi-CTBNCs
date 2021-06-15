package es.upm.fi.cig.multictbnc.nodes;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Abstract class defining common variables and methods for any kind of node.
 * 
 * @author Carlos Villa Blanco
 *
 */
public abstract class AbstractNode implements Node {
	private String name;
	private boolean isClassVariable;
	private List<Node> children;
	protected List<Node> parents;

	/**
	 * Common initialization for nodes. This constructor initializes the node as not
	 * being a class variables, a type of variables that do not appear in those
	 * models that are not meant for classification.
	 * 
	 * @param name name of the node
	 */
	public AbstractNode(String name) {
		this.name = name;
		this.children = new ArrayList<Node>();
		this.parents = new ArrayList<Node>();
	}

	/**
	 * Common initialization for nodes. This constructor allows to specify if the
	 * node is a class variable.
	 * 
	 * @param name            name of the node
	 * @param isClassVariable true if the node represent a class variable, false
	 *                        otherwise
	 */
	public AbstractNode(String name, boolean isClassVariable) {
		this.name = name;
		this.isClassVariable = isClassVariable;
		this.children = new ArrayList<Node>();
		this.parents = new ArrayList<Node>();
	}

	@Override
	public void setParent(Node nodeParent) {
		if (!this.parents.contains(nodeParent))
			this.parents.add(nodeParent);
		if (!nodeParent.getChildren().contains(this))
			nodeParent.getChildren().add(this);
	}

	@Override
	public void setChild(Node nodeChild) {
		nodeChild.setParent(this);
	}

	@Override
	public void removeParent(Node nodeParent) {
		if (this.parents.contains(nodeParent))
			this.parents.remove(nodeParent);
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
		List<Node> tempList = new ArrayList<Node>(this.parents);
		for (Node parentNode : tempList) {
			removeParent(parentNode);
		}
	}

	@Override
	public void removeChildren() {
		// Child nodes are removed using a temporary list to avoid a concurrent
		// modification exception
		List<Node> tempList = new ArrayList<Node>(this.children);
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
		this.isClassVariable = isClassVariable;
	}

	@Override
	public boolean isClassVariable() {
		return this.isClassVariable;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public List<Node> getChildren() {
		return this.children;
	}

	@Override
	public List<Node> getParents() {
		return this.parents;
	}

	@Override
	public int getNumParents() {
		return this.parents.size();
	}

	@Override
	public List<String> getNameParents() {
		return this.parents.stream().map(parent -> parent.getName()).collect(Collectors.toList());
	}

	@Override
	public boolean hasParents() {
		return this.parents.size() > 0;
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
