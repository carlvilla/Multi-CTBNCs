package com.cig.mctbnc.nodes;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * Abstract class defining common variables and methods for discrete nodes.
 * 
 * @author Carlos Villa Blanco
 *
 */
public abstract class DiscreteNode extends AbstractNode {
	// Bidirectional map that allow to retrieve states by index and vice versa
	private BiMap<String, Integer> indexerStates;
	// Keep the index of the current state of the node
	private int stateIdx;
	// Keep the number of possible states of the parents
	private int numStatesParents = 1;
	static Logger logger = LogManager.getLogger(DiscreteNode.class);

	/**
	 * Initialize a discrete node given a list of states.
	 * 
	 * @param name
	 * @param states
	 * 
	 */
	public DiscreteNode(String name, List<String> states) {
		super(name);
		// Define HashBiMap to access the state of the node by index and vice versa
		initializeIndexerStates(states);
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
	public DiscreteNode(String name, List<String> states, boolean isClassVariable) {
		super(name, isClassVariable);
		initializeIndexerStates(states);
	}

	@Override
	public void setParent(Node nodeParent) {
		super.setParent(nodeParent);
		if (nodeParent instanceof DiscreteNode)
			// It is increased the number of states that the parents of the node can take
			this.numStatesParents *= ((DiscreteNode) nodeParent).getNumStates();
	}

	@Override
	public void removeParent(Node nodeParent) {
		super.removeParent(nodeParent);
		if (nodeParent instanceof DiscreteNode)
			// It is decreased the number of states that the parents of the node can take
			this.numStatesParents /= ((DiscreteNode) nodeParent).getNumStates();
	}

	/**
	 * Return a list of the states that the node can take.
	 * 
	 * @return list of State objects
	 */
	public List<String> getStates() {
		return new ArrayList<String>(this.indexerStates.keySet());
	}

	/**
	 * Set the state of the node and returns its id. If the state was not seen
	 * during training, -1 is returned.
	 * 
	 * @param state
	 * @return id of the state
	 */
	public Integer setState(String state) {
		this.stateIdx = this.indexerStates.getOrDefault(state, -1);
		if (this.stateIdx == -1)
			logger.trace("State {} was never seen for variable {}", state, getName());
		return this.stateIdx;
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
		int idxStateParentsCounter = idxStateParents;
		if (getNumParents() > 0) {
			// Keep the number of parent states of still not seen parents in the loop
			int numStatesParents = getNumStatesParents();
			for (int i = getNumParents() - 1; i >= 0; i--) {
				DiscreteNode parentNode = (DiscreteNode) getParents().get(i);
				// Decrease the number of states by not considering the current parent
				numStatesParents /= parentNode.getNumStates();
				// Define state current parent
				int stateParent = idxStateParentsCounter / numStatesParents;
				parentNode.setState(stateParent);
				idxStateParentsCounter %= numStatesParents;
			}
		}
	}

	/**
	 * Get the state of the node.
	 * 
	 * @return state of the node
	 */
	public String getState() {
		return this.indexerStates.inverse().get(this.stateIdx);
	}

	/**
	 * Get the index of the current state of the node.
	 * 
	 * @return index of the current state of the node
	 */
	public int getIdxState() {
		return this.stateIdx;
	}

	/**
	 * Return the number of possible states of the node.
	 * 
	 * @return number of states of the node
	 */
	public int getNumStates() {
		return this.indexerStates.size();
	}

	/**
	 * Get the index for the current state of the parents of the node. Return -1 if
	 * the state of one parent was not seen during training.
	 * 
	 * @return index for the current state of the parents of the node
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
			int idxStateParent = nodeParent.getIdxState();
			if (idxStateParent == -1)
				// The state of the parent was not seen during training.
				return -1;
			idxStateParents += idxStateParent * numStatesParents;
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
		return this.numStatesParents;
	}

	@Override
	public String toString() {
		String commonDescription = super.toString();
		StringBuilder sb = new StringBuilder();
		sb.append(commonDescription);
		sb.append("Possible states: " + this.indexerStates);
		return sb.toString();
	}

	/**
	 * Receive the states of the node and create a HashBiMap to store them with
	 * their indexes.
	 * 
	 * @param states
	 */
	private void initializeIndexerStates(List<String> states) {
		// Define HashBiMap to access the state of the node by index and vice versa
		this.indexerStates = HashBiMap.create();
		for (int i = 0; i < states.size(); i++) {
			this.indexerStates.put(states.get(i), i);
		}
	}

}
