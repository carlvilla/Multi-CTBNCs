package com.cig.mctbnc.nodes;

import java.util.List;

import com.cig.mctbnc.data.representation.State;

public abstract class DiscreteNode extends AbstractNode {

	private List<State> states;

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

	public List<State> getPossibleStates() {
		return states;
	}

	public List<State> getStates() {
		return states;
	}

	public String toString() {
		String commonDescription = super.toString();
		StringBuilder sb = new StringBuilder();
		sb.append(commonDescription);
		sb.append("Possible states: " + getPossibleStates());
		return sb.toString();
	}

}
