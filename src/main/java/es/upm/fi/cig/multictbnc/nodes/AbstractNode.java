package es.upm.fi.cig.multictbnc.nodes;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class defining common variables and methods for any kind of node.
 *
 * @author Carlos Villa Blanco
 */
public abstract class AbstractNode implements Node {
	List<Node> children;
	List<Node> parents;
	private String name;
	private boolean isClassVariable;

	/**
	 * Common initialisation for nodes. This constructor initialises the node as not being a class variable, a type of
	 * variable that does not appear in those models that are not meant for classification.
	 *
	 * @param name name of the node
	 */
	public AbstractNode(String name) {
		this.name = name;
		this.children = new ArrayList<>();
		this.parents = new ArrayList<>();
	}

	/**
	 * Common initialisation for nodes. This constructor allows specifying if the node is a class variable.
	 *
	 * @param name            name of the node
	 * @param isClassVariable true if the node represents a class variable, false otherwise
	 */
	public AbstractNode(String name, boolean isClassVariable) {
		this.name = name;
		this.isClassVariable = isClassVariable;
		this.children = new ArrayList<>();
		this.parents = new ArrayList<>();
	}

	@Override
	public void clearParentAndChildrenSets() {
		this.children.clear();
		this.parents.clear();
	}

	@Override
	public List<Node> getChildren() {
		return this.children;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public int getNumParents() {
		return this.parents.size();
	}

	@Override
	public Node getParent(String nameParent) {
		for (Node parentNode : this.parents) {
			if (parentNode.getName().equals(nameParent))
				return parentNode;
		}
		return null;
	}

	@Override
	public List<Node> getParents() {
		return this.parents;
	}

	@Override
	public boolean hasParents() {
		return this.parents.size() > 0;
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
	public void removeAllEdges() {
		// Remove children
		removeChildren();
		// Remove parents
		removeParents();
	}

	@Override
	public void removeChild(Node nodeChild) {
		nodeChild.removeParent(this);
	}

	@Override
	public void removeChildren() {
		// Child nodes are removed using a temporary list to avoid a concurrent
		// modification exception
		List<Node> tempList = new ArrayList<>(this.children);
		for (Node childNode : tempList) {
			removeChild(childNode);
		}
	}

	@Override
	public void removeParent(Node nodeParent) {
		if (nodeParent != null) {
			if (this.parents.contains(nodeParent))
				this.parents.remove(nodeParent);
			if (nodeParent.getChildren().contains(this))
				nodeParent.getChildren().remove(this);
		}
	}

	@Override
	public void removeParents() {
		// Parent nodes are removed using a temporary list to avoid a concurrent
		// modification exception
		List<Node> tempList = new ArrayList<>(this.parents);
		for (Node parentNode : tempList) {
			removeParent(parentNode);
		}
	}

	@Override
	public void setChild(Node nodeChild) {
		if (nodeChild != null)
			nodeChild.setParent(this);
	}

	@Override
	public void setParent(Node nodeParent) {
		if (nodeParent != null) {
			if (!this.parents.contains(nodeParent))
				this.parents.add(nodeParent);
			if (!nodeParent.getChildren().contains(this))
				nodeParent.getChildren().add(this);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		return this.name.hashCode() * prime;
	}

	@Override
	public boolean equals(Object object) {
		if (object == null || object.getClass() != this.getClass())
			return false;
		Node otherNode = (Node) object;
		// Nodes must have a unique name, which is enough to determine if the two are
		// equal. This method will not differentiate nodes from different models
		return this.name.equals(otherNode.getName());
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