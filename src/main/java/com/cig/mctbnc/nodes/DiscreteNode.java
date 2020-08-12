package com.cig.mctbnc.nodes;

import java.util.List;

import com.cig.mctbnc.data.representation.State;

public abstract class DiscreteNode extends AbstractNode {

	private List<State> states;
	private int numStatesParents = 1;

	/**
	 * Initialize a discrete node.
	 * 
	 * @param index
	 * @param name
	 * @param list
	 */
	public DiscreteNode(String name, List<State> list) {
		super(name);
		this.states = list;
	}

	/**
	 * Initialize a discrete node with the possibility of specifying if the node is
	 * for a class variable or a feature.
	 * 
	 * @param index
	 * @param name
	 * @param classVariable
	 * @param list
	 */
	public DiscreteNode(String name, List<State> list, boolean classVariable) {
		super(name, classVariable);
		this.states = list;
	}

	@Override
	public void setParent(Node nodeParent) {
		super.setParent(nodeParent);
		if (nodeParent instanceof DiscreteNode)
			// It is increased the number of states that the parents of the node can take
			numStatesParents *= ((DiscreteNode) nodeParent).getStates().size();
	}
	
	@Override
	public void removeParent(Node nodeParent) {
		super.removeParent(nodeParent);
		if (nodeParent instanceof DiscreteNode)
			// It is decreased the number of states that the parents of the node can take
			numStatesParents /= ((DiscreteNode) nodeParent).getStates().size();
	}

	/**
	 * Return a list of the states that the node can take.
	 * 
	 * @return list of State objects
	 */
	public List<State> getStates() {
		return states;
	}

	/**
	 * Return the number of possible states of the parents of the node.
	 * 
	 * @return number of states of the parents
	 */
	public int getNumStateParents() {
		return numStatesParents;
	}

	public String toString() {
		String commonDescription = super.toString();
		StringBuilder sb = new StringBuilder();
		sb.append(commonDescription);
		sb.append("Possible states: " + getStates());
		return sb.toString();
	}

}
