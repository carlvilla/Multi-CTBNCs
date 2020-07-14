package com.cig.mctbnc.nodes;

import java.util.List;

import com.cig.mctbnc.data.representation.State;

public class DiscreteNode extends AbstractNode {

	private List<State> states;

	public DiscreteNode(int index, String name, List<State> list) {
		super(index, name);
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
