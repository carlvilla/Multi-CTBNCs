package com.cig.mctbnc.learning.parameters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.data.representation.State;
import com.cig.mctbnc.nodes.Node;

public class CTBNSufficientStatistics {

	private Node node;
	private Map<State, Map<State, Integer>> N;

	public CTBNSufficientStatistics(Node node) {
		this.node = node;
		N = new HashMap<State, Map<State, Integer>>();
	}

	/**
	 * Compute the sufficient statistics of a node in a CTBN. These are (1) number
	 * of times the variable transition from a certain state to another while its
	 * parents have a certain value and (2) time that the variable stay in certain
	 * state while its parents take a certain value.
	 * 
	 * @param dataset
	 */
	public void computeSufficientStatistics(Dataset dataset) {
		String nameVariable = node.getName();
		List<State> statesVariable = dataset.getStatesVariable(nameVariable);
		for (State fromState : statesVariable) {
			// This HashMap will keep all the occurrences when the variable start from state
			// "fromState"
			HashMap<State, Integer> occurrenciesFromState = new HashMap<State, Integer>();
			for (State toState : statesVariable) {
				// Computation of sufficient statistics if node has parents
				if (node.hasParents()) {
					List<Node> parents = node.getParents();
					List<String> nameParents = parents.stream().map(Node::getName).collect(Collectors.toList());
					List<State> statesParents = dataset.getStatesVariables(nameParents);
					// Obtain number of times the variable transitions from "fromState" to "toState"
					// while parents take state "stateParents"
					for (State stateParents : statesParents) {
						fromState.addEvents(stateParents.getEvents());
						int Nijk = dataset.getNumOccurrencesTransition(fromState, toState);
						occurrenciesFromState.put(toState, Nijk);
					}
				}
				// Computation of sufficient statistics if node has no parents
				else {
					int Nijk = dataset.getNumOccurrencesTransition(fromState, toState);
					occurrenciesFromState.put(toState, Nijk);
				}
			}
			N.put(fromState, occurrenciesFromState);
		}
	}
}
