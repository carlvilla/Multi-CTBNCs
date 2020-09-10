package com.cig.mctbnc.learning.parameters.bn;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.data.representation.State;
import com.cig.mctbnc.learning.parameters.SufficientStatistics;
import com.cig.mctbnc.nodes.DiscreteNode;
import com.cig.mctbnc.nodes.Node;

public class BNSufficientStatistics implements SufficientStatistics {
	private Map<State, Integer> N;
	static Logger logger = LogManager.getLogger(BNSufficientStatistics.class);

	public BNSufficientStatistics() {
		N = new HashMap<State, Integer>();
	}

	/**
	 * Compute the sufficient statistics of a node in a BN. This is the number of
	 * times the variable is in a certain state while its parents take a certain
	 * value.
	 * 
	 * @param dataset
	 */
	public void computeSufficientStatistics(Node node, Dataset dataset) {
		logger.trace("Computing sufficient statistics BN for node {}", node.getName());
		List<State> statesVariable = ((DiscreteNode) node).getStates();

		if (node.hasParents()) {
			List<Node> parents = node.getParents();
			List<String> nameParents = parents.stream().map(Node::getName).collect(Collectors.toList());
			List<State> statesParents = dataset.getPossibleStatesVariables(nameParents);

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
					// TODO Instead of looking for the occurrences of each state, iterate the
					// observations just once
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

	/**
	 * Return the sufficient statistics of the node. This is the number of the
	 * variable is in a certain state while its parents take a certain value.
	 * 
	 * @return Map object that relates the states of a node with the number of
	 *         appearances in a dataset.
	 */
	public Map<State, Integer> getSufficientStatistics() {
		return N;
	}

}
