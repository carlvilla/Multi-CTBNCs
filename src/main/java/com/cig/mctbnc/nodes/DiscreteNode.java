package com.cig.mctbnc.nodes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.cig.mctbnc.data.representation.State;
import com.cig.mctbnc.util.Util;

public abstract class DiscreteNode extends AbstractNode {
	private List<State> states;
	// A list to keep the possible states of discrete parents to avoid recompute it
	private List<State> statesParents;
	private int numStatesParents = 1;

	/**
	 * Initialize a discrete node given a list of states.
	 * 
	 * @param name
	 * @param states
	 * 
	 */
	public DiscreteNode(String name, List<State> states) {
		super(name);
		this.states = states;
		this.statesParents = new ArrayList<State>();
	}

	/**
	 * Initialize a discrete node specifying if the node is for a class variable or
	 * a feature.
	 * 
	 * @param name
	 * @param states
	 * @param isClassVariable
	 * 
	 */
	public DiscreteNode(String name, List<State> states, boolean isClassVariable) {
		super(name, isClassVariable);
		this.states = (List<State>) states;
		this.statesParents = new ArrayList<State>();
	}

	/**
	 * Initialize a discrete node given a list of strings with its states. The order
	 * of parameters is changed with respect to the other constructor to avoid both
	 * of them having the same erasure.
	 * 
	 * @param name
	 * @param isClassVariable
	 * @param states
	 * 
	 */
	public DiscreteNode(String name, boolean isClassVariable, List<String> states) {
		super(name, isClassVariable);
		this.states = new ArrayList<State>();
		for (String valueState : states)
			this.states.add(new State(Map.of(name, valueState)));
		this.statesParents = new ArrayList<State>();
	}

	@Override
	public void setParent(Node nodeParent) {
		super.setParent(nodeParent);
		if (nodeParent instanceof DiscreteNode)
			// It is increased the number of states that the parents of the node can take
			numStatesParents *= ((DiscreteNode) nodeParent).getStates().size();
		// As the parents changed, the list of their states is outdated
		this.statesParents = new ArrayList<State>();
	}

	@Override
	public void removeParent(Node nodeParent) {
		super.removeParent(nodeParent);
		if (nodeParent instanceof DiscreteNode)
			// It is decreased the number of states that the parents of the node can take
			numStatesParents /= ((DiscreteNode) nodeParent).getStates().size();
		// As the parents changed, the list of their states is outdated
		this.statesParents = new ArrayList<State>();
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
	 * Return a list of the states that the parents can take.
	 * 
	 * @return possible states of the parents
	 */
	public List<State> getStatesParents() {
		if (statesParents.isEmpty())
			if (parents.isEmpty())
				// The node has no parents. It will be returned a list with an empty state. This
				// is necessary to enter into loops over the states of the parents once without
				// complicating the code
				statesParents.add(new State());
			else {
				// Retrieve states of the parents
				List<List<State>> statesDiscreteParents = new ArrayList<List<State>>();
				for (Node parentNode : parents)
					if (parentNode instanceof DiscreteNode)
						statesDiscreteParents.add(((DiscreteNode) parentNode).getStates());
				statesParents = Util.cartesianProduct(statesDiscreteParents);
			}
		return statesParents;
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
