package com.cig.mctbnc.nodes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cig.mctbnc.data.representation.State;
import com.cig.mctbnc.util.Util;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public abstract class DiscreteNode extends AbstractNode {
	// Bidirectional map that allow to retrieve states by index and vice versa
	private BiMap<String, Integer> indexerStates;
	// Keep the index of the current state of the node
	private Integer stateIdx;
	// Keep the number of possible states of the parents
	private int numStatesParents = 1;

	private List<State> states;
	// A list to keep the possible states of discrete parents to avoid recompute it
	private List<State> statesParents;

	static Logger logger = LogManager.getLogger(DiscreteNode.class);

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

		// Define HashBiMap to access the state of the node by index and vice versa
		indexerStates = HashBiMap.create();
		for (int i = 0; i < states.size(); i++) {
			indexerStates.put(states.get(i).getValueVariable(name), i);
		}

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

		// Define HashBiMap to access the state of the node by index and vice versa
		indexerStates = HashBiMap.create();
		for (int i = 0; i < states.size(); i++) {
			indexerStates.put(states.get(i).getValueVariable(name), i);
		}

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

		// Define HashBiMap to access the state of the node by index and vice versa
		indexerStates = HashBiMap.create();
		for (int i = 0; i < states.size(); i++) {
			indexerStates.put(states.get(i), i);
		}

	}

	@Override
	public void setParent(Node nodeParent) {
		super.setParent(nodeParent);
		if (nodeParent instanceof DiscreteNode)
			// It is increased the number of states that the parents of the node can take
			numStatesParents *= ((DiscreteNode) nodeParent).getNumStates();
		// As the parents changed, the list of their states is outdated
		this.statesParents = new ArrayList<State>();
	}

	@Override
	public void removeParent(Node nodeParent) {
		super.removeParent(nodeParent);
		if (nodeParent instanceof DiscreteNode)
			// It is decreased the number of states that the parents of the node can take
			numStatesParents /= ((DiscreteNode) nodeParent).getNumStates();
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
	 * Set the state of the node by providing it.
	 * 
	 * @param state
	 * @return
	 */
	public Integer setState(String state) {
		stateIdx = indexerStates.getOrDefault(state, null);
		if (stateIdx == null)
			logger.warn("State {} was never seen for variable {}", state, getName());
		return stateIdx;
	}

	/**
	 * Set the state of the node by providing the state index.
	 * 
	 * @param stateIdx
	 */
	public void setState(int stateIdx) {
		this.stateIdx = stateIdx;
	}

	/**
	 * Set the states of the parents of the node given the index related to their
	 * state.
	 * 
	 * @param idxStateParents
	 */
	public void setStateParents(int idxStateParents) {
		if (getNumParents() > 0) {
			// Keep the number of parent states of still not seen parents in the loop
			int numStatesParents = getNumStatesParents();
			for (int i = getNumParents() - 1; i >= 0; i--) {
				DiscreteNode parentNode = (DiscreteNode) getParents().get(i);
				// Decrease the number of states by not considering the current parent
				numStatesParents /= parentNode.getNumStates();
				// Define state current parent
				int stateParent = idxStateParents / numStatesParents;
				parentNode.setState(stateParent);
				idxStateParents %= numStatesParents;
			}
		}
	}

	/**
	 * Get the state of the node.
	 * 
	 * @return
	 */
	public String getState() {
		return indexerStates.inverse().get(stateIdx);
	}

	/**
	 * Get the index of the current state of the node.
	 * 
	 * @return
	 */
	public int getStateIdx() {
		return stateIdx;
	}

	/**
	 * Return the number of possible states of the node.
	 * 
	 * @return number of states of the node
	 */
	public int getNumStates() {
		return indexerStates.size();
	}

	/**
	 * Get the index that represents the current state of the parents of the node.
	 * 
	 * @return
	 */
	public int getIdxStateParents() {
		int idxStateParents = 0;
		// Count the number of parent states of parents seen so far in the loop
		int numStatesParents = 1;
		// In each iteration it is multiplied the number of seen so far parents with
		// the state index of the current parent in the loop. As the maximum index is
		// the number of state, it is ensure that the maximum returned index for the
		// parents state is equal to their number of combinations minus 1.
		for (int i = 0; i < getNumParents(); i++) {
			DiscreteNode nodeParent = (DiscreteNode) getParents().get(i);
			idxStateParents += nodeParent.getStateIdx() * numStatesParents;
			// Increase the number of states by considering the current parent
			numStatesParents *= nodeParent.getNumStates();
		}
		return idxStateParents;
	}

	/**
	 * Return the number of possible states of the parents of the node.
	 * 
	 * @return number of states of the parents
	 */
	public int getNumStatesParents() {
		return numStatesParents;
	}

	/**
	 * Get an State object with the current state of the node and its parents.
	 * 
	 * @return
	 */
	public State getStateNodeAndParents() {
		String stateValueNode = indexerStates.inverse().get(stateIdx);
		State state = new State(getName(), stateValueNode);
		for (int i = 0; i < getNumParents(); i++) {
			DiscreteNode parentNode = (DiscreteNode) getParents().get(i);
			String stateValueParent = parentNode.getState();
			state.addEvent(parentNode.getName(), stateValueParent);
		}
		return state;
	}

	/**
	 * Get a State object with the state of the node related to the given index.
	 * 
	 * @param index
	 * @return
	 */
	public State indexToState(int index) {
		String valueState = indexerStates.inverse().get(index);
		return new State(getName(), valueState);
	}

	public String toString() {
		String commonDescription = super.toString();
		StringBuilder sb = new StringBuilder();
		sb.append(commonDescription);
		sb.append("Possible states: " + getStates());
		return sb.toString();
	}

}
