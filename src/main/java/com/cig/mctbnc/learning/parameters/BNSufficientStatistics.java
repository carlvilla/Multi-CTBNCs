package com.cig.mctbnc.learning.parameters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.data.representation.State;
import com.cig.mctbnc.nodes.Node;

public class BNSufficientStatistics {

	private Node node;
	private Map<State, Integer> N;

	public BNSufficientStatistics(Node node) {
		this.node = node;
		N = new HashMap<State, Integer>();
	}

	/**
	 * Obtain the number of times the parents of the variable are in the state j and
	 * the variable is in the state k.
	 */
	public void computeSufficientStatistics(Dataset dataset) {
		String nameVariable = node.getName();
		List<State> statesVariable = dataset.getStatesVariable(nameVariable);

		if (node.hasParents()) {
			List<Node> parents = node.getParents();
			List<String> nameParents = parents.stream().map(Node::getName).collect(Collectors.toList());
			List<State> statesParents = dataset.getStatesVariables(nameParents);

			for (int j = 0; j < statesParents.size(); j++)
				for (int k = 0; k < statesVariable.size(); k++) {
					// Get number of times variable "nameVariable" has value statesVariable[k]
					// while its parents have values statesParents[j]
					State stateVariable = statesVariable.get(k);
					State stateParents = statesParents.get(j);

					// Create State object where the variable has value k and its parents
					// are in the state j
					State query = new State();
					query.addEvents(stateParents.getEvents());
					query.addEvents(stateVariable.getEvents());

					// Query the dataset with the state k for the variables and
					// the state j for its parents
					int Nijk = dataset.getNumOccurrences(query);
					N.put(query, Nijk);
				}

		} else {
			for (int k = 0; k < statesVariable.size(); k++) {
				State stateVariable = statesVariable.get(k);
				N.put(stateVariable, dataset.getNumOccurrences(stateVariable));
			}
		}
	}

	public Map<State, Integer> getSufficientStatistics() {
		return N;
	}

}
