package com.cig.mctbnc.learning.parameters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.data.representation.Observation;
import com.cig.mctbnc.data.representation.Sequence;
import com.cig.mctbnc.data.representation.State;
import com.cig.mctbnc.nodes.Node;

/**
 * Compute and store the sufficient statistics of a CTBN node. The sufficient
 * statistics are:
 * 
 * (1) Nxx: number of times a variable transitions from a certain state to
 * another one while its parents take a certain value.
 * 
 * (2) Nx: number of times a variable leaves a certain state (for any other
 * state) while its parents take a certain value.
 * 
 * (3) T: time that a variable stays in a certain state while its parents take a
 * certain value.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class CTBNSufficientStatistics implements SufficientStatistics {
	private Node node;
	// Sufficient statistics
	private Map<State, Map<State, Integer>> Nxy;
	private Map<State, Integer> Nx;
	private Map<State, Double> Tx;
	static Logger logger = LogManager.getLogger(CTBNSufficientStatistics.class);

	public CTBNSufficientStatistics(Node node) {
		this.node = node;
		Nxy = new HashMap<State, Map<State, Integer>>();
		Nx = new HashMap<State, Integer>();
		Tx = new HashMap<State, Double>();
	}

	/**
	 * Compute the sufficient statistics of a CTBN node.
	 * 
	 * @param dataset
	 *            dataset from which sufficient statistics are extracted
	 */
	public void computeSufficientStatistics(Dataset dataset) {
		logger.trace("Computing sufficient statistics CTBN for node {}", node.getName());
		String nameVariable = node.getName();

		// Variables that are only used if the node has parents
		List<Node> parents = node.getParents();
		List<String> nameParents = parents.stream().map(Node::getName).collect(Collectors.toList());

		// Initialize sufficient statistics
		initializeSufficientStatistics(dataset);

		// Iterate over all the sequences and their observations to calculate the
		// sufficient statistics
		for (Sequence sequence : dataset.getSequences()) {
			for (int i = 1; i < sequence.getNumObservations(); i++) {
				// It is obtained two observations representing the states of the variables
				// before and after a transition
				Observation fromObservation = sequence.getObservations().get(i - 1);
				State fromState = new State();
				fromState.addEvent(nameVariable, fromObservation.getValueVariable(nameVariable));

				Observation toObservation = sequence.getObservations().get(i);
				State toState = new State();
				toState.addEvent(nameVariable, toObservation.getValueVariable(nameVariable));

				// Check if the node is transitioning to a different state
				boolean nodeIsTransitioning = !fromState.equals(toState);

				// If the node has parents, their states are added to "fromState"
				for (String nameParent : nameParents) {
					fromState.addEvent(nameParent, fromObservation.getValueVariable(nameParent));
				}

				// If the node is transitioning to a different state, the sufficient statistics
				// Nxy and Nx have to be updated
				if (nodeIsTransitioning) {
					updateOccurrencesNxy(fromState, toState, 1);
					updateOccurrencesNx(fromState, 1);
				}

				// Increase the time the node and its parents are in a certain state
				double transitionTime = toObservation.getTimeValue() - fromObservation.getTimeValue();
				updateOccurrencesTx(fromState, transitionTime);
			}
		}
	}

	/**
	 * Initialize the structures to store the sufficient statistics of the node.
	 * 
	 * @param dataset
	 *            dataset used to compute the sufficient statistics
	 */
	private void initializeSufficientStatistics(Dataset dataset) {
		String nameVariable = node.getName();
		List<State> statesVariable = dataset.getStatesVariable(nameVariable);

		if (node.hasParents()) {
			List<Node> parents = node.getParents();
			List<String> nameParents = parents.stream().map(Node::getName).collect(Collectors.toList());
			List<State> statesParents = dataset.getStatesVariables(nameParents);
			for (State stateParents : statesParents) {
				for (State fromState : statesVariable) {
					State fromStateWithParents = new State(fromState.getEvents());
					fromStateWithParents.addEvents(stateParents.getEvents());
					// Initialize Nx
					updateOccurrencesNx(fromStateWithParents, 0);
					// Initialize Tx
					updateOccurrencesTx(fromStateWithParents, 0);
					for (State toState : statesVariable) {
						if (!fromState.equals(toState)) {
							// Initialize Nxy
							updateOccurrencesNxy(fromStateWithParents, toState, 0);
						}
					}
				}
			}
		} else {
			for (State fromState : statesVariable) {
				// Initialize Nx
				updateOccurrencesNx(fromState, 0);
				// Initialize Tx
				updateOccurrencesTx(fromState, 0);
				for (State toState : statesVariable) {
					if (!fromState.equals(toState)) {
						// Initialize Nxy
						updateOccurrencesNxy(fromState, toState, 0);
					}
				}
			}
		}
	}

	/**
	 * Update the number of occurrences where the node transitions from "fromState"
	 * (with the parents taking a certain value) to "toState".
	 * 
	 * @param fromState
	 *            current state
	 * @param toState
	 *            next state
	 * @param numOccurrences
	 *            number of occurrences
	 */
	private void updateOccurrencesNxy(State fromState, State toState, int numOccurrences) {
		// If the state 'fromState' was never seen before, it is created a map to
		// contain all the occurrences of it transitioning to other states
		if (!Nxy.containsKey(fromState))
			Nxy.put(fromState, new HashMap<State, Integer>());
		// Current value of Nxy for 'fromState' and 'toState'
		int currentNxy = Nxy.get(fromState).containsKey(toState) ? Nxy.get(fromState).get(toState) : 0;
		Nxy.get(fromState).put(toState, currentNxy + numOccurrences);
	}

	/**
	 * Update the number of occurrences where the node transitions from "fromState"
	 * (with the parents taking a certain value) to any other state.
	 * 
	 * @param fromState
	 *            current state
	 * @param numOccurrences
	 *            number of occurrences
	 */
	private void updateOccurrencesNx(State fromState, int numOccurrences) {
		// Current value of Nx for 'fromState'
		int currentNx = Nx.containsKey(fromState) ? Nx.get(fromState) : 0;
		Nx.put(fromState, currentNx + numOccurrences);
	}

	/**
	 * Update the time the node spends in state "state" while its parents are in
	 * certain state (information also included in "state").
	 * 
	 * @param state
	 *            current state of the node and its parents
	 * @param time
	 *            time
	 */
	private void updateOccurrencesTx(State state, double time) {
		// Current time computed for the state
		double currentTime = Tx.containsKey(state) ? Tx.get(state) : 0;
		Tx.put(state, currentTime + time);
	}

	/**
	 * Return the number of times the variable transition from every state to
	 * specific states (including itself) while its parents have certain values.
	 * 
	 * @return number of occurrences of every transition
	 */
	public Map<State, Map<State, Integer>> getNxy() {
		if (Nxy.isEmpty())
			logger.warn("Sufficient statistic Nxy was not computed");
		return Nxy;
	}

	/**
	 * Return the number of times the variable leaves every state (i.e., the state
	 * is changed) while its parents have certain values.
	 * 
	 * @return number of times the variable leaves every state
	 */
	public Map<State, Integer> getNx() {
		if (Nx.isEmpty())
			logger.warn("Sufficient statistic Nx was not computed");
		return Nx;
	}

	/**
	 * Return the time that the variable stay in every state while its parents take
	 * different values.
	 * 
	 * @return time that the variable stay for every state
	 */
	public Map<State, Double> getTx() {
		if (Tx.isEmpty())
			logger.warn("Sufficient statistic Tx was not computed");
		return Tx;
	}

}
