package es.upm.fi.cig.multictbnc.nodes;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class defining common variables and methods for discrete nodes.
 *
 * @author Carlos Villa Blanco
 */
public abstract class DiscreteStateNode extends AbstractNode {
    private static final Logger logger = LogManager.getLogger(DiscreteStateNode.class);
    // Bidirectional map that allows retrieving states by index and vice versa
    private BiMap<String, Integer> indexerStates;
    // Keep the index of the current state of the node
    private int stateIdx;
    // Keep the number of possible states of the parents
    private int numStatesParents = 1;

    /**
     * Initialises a discrete node given a list of states.
     *
     * @param name   name of the node
     * @param states list of strings representing the states that the variable related to the node can take
     */
    public DiscreteStateNode(String name, List<String> states) {
        super(name);
        // Define HashBiMap to access the state of the node by index and vice versa
        initialiseIndexerStates(states);
    }

    /**
     * Initialises a discrete node specifying if the node is for a class variable or a feature.
     *
     * @param name            name of the node
     * @param states          list of strings representing the states that the variable related to the node can take
     * @param isClassVariable true if the node represent a class variable, false otherwise
     */
    public DiscreteStateNode(String name, List<String> states, boolean isClassVariable) {
        super(name, isClassVariable);
        initialiseIndexerStates(states);
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
     * Gets the index for the current state of the node's parents. Return -1 if the state of one parent was not seen
     * during training. Algorithm from <a href="https://github.com/dcodecasa/CTBNCToolkit">https://github.com/dcodecasa/CTBNCToolkit</a>.
     *
     * @return index for the current state of the node's parents
     */
    public int getIdxStateParents() {
        int idxStateParents = 0;
        // Count the number of parent states of parents seen so far in the loop
        int numStatesParents = 1;
        for (int i = 0; i < getNumParents(); i++) {
            DiscreteStateNode nodeParent = (DiscreteStateNode) getParents().get(i);
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
     * Gets the index for the current state of the specified parents of the node. Returns -1 if the state of one parent
     * was not seen during training or if the parent does not exist. Returns 0 if the provided parent list is null or
     * empty.
     *
     * @param nameParents name of the parent nodes
     * @return index for the current state of the specified subset of parents
     */
    public int getIdxStateParents(List<String> nameParents) {
        int idxStateParents = 0;
        // Count the number of parent states of parents seen so far in the loop
        int numStatesParents = 1;
        if (nameParents != null && nameParents.size() > 0) {
            for (int i = 0; i < getNumParents(); i++) {
                if (nameParents.contains(getParents().get(i).getName())) {
                    DiscreteStateNode nodeParent = (DiscreteStateNode) getParents().get(i);
                    int idxStateParent = nodeParent.getIdxState();
                    if (idxStateParent == -1)
                        // The state of the parent was not seen during training.
                        return -1;
                    idxStateParents += idxStateParent * numStatesParents;
                    // Increase the number of states by considering the current parent
                    numStatesParents *= nodeParent.getNumStates();
                }
            }
        }
        return idxStateParents;
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
     * Returns the number of possible states of the parents of the node.
     *
     * @return number of states of the node's parents
     */
    public int getNumStatesParents() {
        return this.numStatesParents;
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
     * Sets the state index of the node. The state index is not changed if the provided index was not seen during
     * training.
     *
     * @param stateIdx node state index
     */
    public void setState(int stateIdx) {
        if (this.indexerStates.size() <= stateIdx) {
            logger.warn("State with index {} was never seen for variable {}", stateIdx, getName());
        } else
            this.stateIdx = stateIdx;
    }

    /**
     * Returns a list of the states that the node can take.
     *
     * @return list of strings representing the states that the variable related to the node can take
     */
    public List<String> getStates() {
        return new ArrayList<>(this.indexerStates.keySet());
    }

    /**
     * Sets the state of the node and returns its id. If the state was not seen during training, -1 is returned.
     *
     * @param state state of the node
     * @return id of the state
     */
    public Integer setState(String state) {
        this.stateIdx = this.indexerStates.getOrDefault(state, -1);
        if (this.stateIdx == -1)
            logger.warn("State {} was never seen for variable {}", state, getName());
        return this.stateIdx;
    }

    /**
     * Sets the states of the parents of the node given the index related to their state. Algorithm from <a href="https://github.com/dcodecasa/CTBNCToolkit">https://github.com/dcodecasa/CTBNCToolkit</a>.
     *
     * @param idxStateParents index of the state of the node's parents
     */
    public void setStateParents(int idxStateParents) {
        // State index of the subset of parents which have not been processed yet
        int idxStateSubParents = idxStateParents;
        if (getNumParents() > 0) {
            // Number of states of parents which have not been processed yet
            int numStatesParents = getNumStatesParents();
            for (int i = getNumParents() - 1; i >= 0; i--) {
                DiscreteStateNode parentNode = (DiscreteStateNode) getParents().get(i);
                // Decrease the number of states by not considering the current parent
                numStatesParents /= parentNode.getNumStates();
                // Define the state of the current parent
                int stateParent = idxStateSubParents / numStatesParents;
                parentNode.setState(stateParent);
                // Define the state index for the remaining subset of parents
                idxStateSubParents %= numStatesParents;
            }
        }
    }

    @Override
    public void clearParentAndChildrenSets() {
        super.clearParentAndChildrenSets();
        this.numStatesParents = 1;
    }

    @Override
    public void removeParent(Node nodeParent) {
        if (nodeParent != null) {
            super.removeParent(nodeParent);
            if (nodeParent instanceof DiscreteStateNode)
                // Decrease the number of states that the parents of the node can take
                this.numStatesParents /= ((DiscreteStateNode) nodeParent).getNumStates();
        }
    }

    @Override
    public void setParent(Node nodeParent) {
        if (nodeParent != null) {
            super.setParent(nodeParent);
            if (nodeParent instanceof DiscreteStateNode)
                // Increase the number of states that the parents of the node can take
                this.numStatesParents *= ((DiscreteStateNode) nodeParent).getNumStates();
        }
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
     * Receives the states of the node and creates a HashBiMap to store them with their indexes.
     *
     * @param states list of strings representing the states that the variable related to the node can take
     */
    private void initialiseIndexerStates(List<String> states) {
        // Define HashBiMap to access the state of the node by index and vice versa
        this.indexerStates = HashBiMap.create();
        for (int i = 0; i < states.size(); i++) {
            this.indexerStates.put(states.get(i), i);
        }
    }

}