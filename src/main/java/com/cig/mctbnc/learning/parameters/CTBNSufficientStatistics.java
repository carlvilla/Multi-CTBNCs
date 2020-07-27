package com.cig.mctbnc.learning.parameters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.data.representation.State;
import com.cig.mctbnc.nodes.Node;

public class CTBNSufficientStatistics implements SufficientStatistics {

	private Node node;
	private Map<State, Map<State, Integer>> N;
	private Map<State, Double> T;

	public CTBNSufficientStatistics(Node node) {
		this.node = node;
		N = new HashMap<State, Map<State, Integer>>();
	}

	/**
	 * Compute the sufficient statistics of a node in a CTBN. These are (1) number
	 * of times the variable transition from a certain state to another while its
	 * parents have a certain value and (2) time that the variable stay in a certain
	 * state while its parents take a certain value.
	 * 
	 * @param dataset
	 *            dataset from which sufficient statistics are extracted
	 */
	public void computeSufficientStatistics(Dataset dataset) {
		computeTransitions(dataset);
		computeTime(dataset);
	}

	/**
	 * Compute the number of times the variable transition from a certain state to
	 * another while its parents have a certain value
	 * 
	 * @param dataset
	 *            dataset from which the sufficient statistic is extracted
	 */
	private void computeTransitions(Dataset dataset) {
		String nameVariable = node.getName();
		List<State> statesVariable = dataset.getStatesVariable(nameVariable);
		for (State fromState : statesVariable) {
			for (State toState : statesVariable) {
				// Computation of sufficient statistics if node has parents
				if (node.hasParents()) {
					List<Node> parents = node.getParents();
					List<String> nameParents = parents.stream().map(Node::getName).collect(Collectors.toList());
					List<State> statesParents = dataset.getStatesVariables(nameParents);
					// Obtain number of times the variable transitions from "fromState" to "toState"
					// while parents take state "stateParents"
					for (State stateParents : statesParents) {
						// Modifications in "fromState" object would affect "toState". As
						// it is necessary to include the state of the parents to "fromState", it is
						// created a new State object
						State fromStateWithParents = new State(fromState.getEvents());
						fromStateWithParents.addEvents(stateParents.getEvents());
						int Nijk = dataset.getNumOccurrencesTransition(fromStateWithParents, toState);
						addOccurrences(fromStateWithParents, toState, Nijk);
					}
				}
				// Computation of sufficient statistics if node has no parents
				else {
					int Nijk = dataset.getNumOccurrencesTransition(fromState, toState);
					addOccurrences(fromState, toState, Nijk);
				}
			}
		}
	}

	/**
	 * Compute the time that the variable stay in a certain state while its parents
	 * take a certain value
	 * 
	 * @param dataset
	 *            dataset from which the sufficient statistic is extracted
	 */
	private void computeTime(Dataset dataset) {
		String nameVariable = node.getName();
		List<State> statesVariable = dataset.getStatesVariable(nameVariable);
		for (State state : statesVariable) {
			if (node.hasParents()) {
				List<Node> parents = node.getParents();
				List<String> nameParents = parents.stream().map(Node::getName).collect(Collectors.toList());
				List<State> statesParents = dataset.getStatesVariables(nameParents);
				// Obtain number of times the variable transitions from "fromState" to "toState"
				// while parents take state "stateParents"
				for (State stateParents : statesParents) {
					// Modifications in "state" object would remain. Therefore, it is created
					// a new State object to include the states of the parents
					State stateWithParents = new State(state.getEvents());
					stateWithParents.addEvents(stateParents.getEvents());
					double Tijk = dataset.getTimeState(stateWithParents);
					T.put(stateWithParents, Tijk);
				}
			}
			else {
				double Tijk = dataset.getTimeState(state);
				T.put(state, Tijk);
			}
		}
	}

	/**
	 * Register the number of occurrences "numOccurrences" of transitioning from
	 * "fromState" to "toState".
	 * 
	 * @param fromState
	 *            current state
	 * @param toState
	 *            next state
	 * @param numOccurrences
	 *            number of occurrences
	 */
	private void addOccurrences(State fromState, State toState, int numOccurrences) {
		if (!N.containsKey(fromState))
			N.put(fromState, new HashMap<State, Integer>());
		N.get(fromState).put(toState, numOccurrences);
	}

	@Override
	public Map<State, Map<State, Integer>> getSufficientStatistics() {
		return N;
	}

}
