package es.upm.fi.cig.multictbnc.nodes;

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
	 * Initializes a discrete node given a list of states.
	 * 
	 * @param name   name of the node
	 * @param states list of strings representing the states that the variable
	 *               related to the node can take
	 * 
	 */
	public DiscreteNode(String name, List<String> states) {
		super(name);
		// Define HashBiMap to access the state of the node by index and vice versa
		initializeIndexerStates(states);
	}

	/**
	 * Initializes a discrete node specifying if the node is for a class variable or
	 * a feature.
	 * 
	 * @param name            name of the node
	 * @param states          list of strings representing the states that the
	 *                        variable related to the node can take
	 * @param isClassVariable true if the node represent a class variable, false
	 *                        otherwise
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
			// Increase the number of states that the parents of the node can take
			this.numStatesParents *= ((DiscreteNode) nodeParent).getNumStates();
	}

	@Override
	public void removeParent(Node nodeParent) {
		super.removeParent(nodeParent);
		if (nodeParent instanceof DiscreteNode)
			// Decrease the number of states that the parents of the node can take
			this.numStatesParents /= ((DiscreteNode) nodeParent).getNumStates();
	}

	/**
	 * Returns a list of the states that the node can take.
	 * 
	 * @return list of strings representing the states that the variable related to
	 *         the node can take
	 */
	public List<String> getStates() {
		return new ArrayList<String>(this.indexerStates.keySet());
	}

	/**
	 * Sets the state of the node and returns its id. If the state was not seen
	 * during training, -1 is returned.
	 * 
	 * @param state state of the node
	 * @return id of the state
	 */
	public Integer setState(String state) {
		this.stateIdx = this.indexerStates.getOrDefault(state, -1);
		if (this.stateIdx == -1)
			logger.trace("State {} was never seen for variable {}", state, getName());
		return this.stateIdx;
	}

	/**
	 * Sets the state of the node by providing the state index.
	 * 
	 * @param stateIdx node state index
	 */
	public void setState(int stateIdx) {
		this.stateIdx = stateIdx;
	}

	/**
	 * Sets the states of the parents of the node given the index related to their
	 * state. Algorithm from https://github.com/dcodecasa/CTBNCToolkit.
	 * 
	 * @param idxStateParents index of the state of the node's parents
	 */
	public void setStateParents(int idxStateParents) {
		// State index of the subset of parents which have not been processed yet
		int idxStateSubParents = idxStateParents;
		if (getNumParents() > 0) {
			// Number of states of parents which have not been processed yet
			int numStatesParents = getNumStatesParents();
			for (int i = 0; i < getNumParents(); i++) {
				DiscreteNode parentNode = (DiscreteNode) getParents().get(i);
				// Decrease the number of states by not considering the current parent
				numStatesParents /= parentNode.getNumStates();
				// Define state of the current parent
				int stateParent = idxStateSubParents / numStatesParents;
				parentNode.setState(stateParent);
				// Define the state index for the remaining subset of parents
				idxStateSubParents %= numStatesParents;
			}
		}
	}

	/**
	 * Gets the state of the node.
	 * 
	 * @return state of the node
	 */
	public String getState() {
		return this.indexerStates.inverse().get(this.stateIdx);
	}

	/**
	 * Gets the index of the current state of the node.
	 * 
	 * @return index of the current state of the node
	 */
	public int getIdxState() {
		return this.stateIdx;
	}

	/**
	 * Returns the number of possible states of the node.
	 * 
	 * @return number of states of the node
	 */
	public int getNumStates() {
		return this.indexerStates.size();
	}

	/**
	 * Gets the index for the current state of the parents of the node. Return -1 if
	 * the state of one parent was not seen during training. Algorithm from
	 * https://github.com/dcodecasa/CTBNCToolkit.
	 * 
	 * @return index for the current state of the node's parents
	 */
	public int getIdxStateParents() {
		int idxStateParents = 0;
		// Count the number of parent states of parents seen so far in the loop
		int numStatesParents = 1;
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
	 * Returns the number of possible states of the parents of the node.
	 * 
	 * @return number of states of the node's parents
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
	 * Receives the states of the node and create a HashBiMap to store them with
	 * their indexes.
	 * 
	 * @param states list of strings representing the states that the variable
	 *               related to the node can take
	 */
	private void initializeIndexerStates(List<String> states) {
		// Define HashBiMap to access the state of the node by index and vice versa
		this.indexerStates = HashBiMap.create();
		for (int i = 0; i < states.size(); i++) {
			this.indexerStates.put(states.get(i), i);
		}
	}

}
