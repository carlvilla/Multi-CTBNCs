package com.cig.mctbnc.nodes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.cig.mctbnc.data.representation.State;
import com.cig.mctbnc.learning.parameters.SufficientStatistics;
import com.cig.mctbnc.learning.parameters.bn.BNSufficientStatistics;

/**
 * Extends the DiscreteNode class to store a CPT and the sufficient statistics
 * for a BN.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class CPTNode extends DiscreteNode {
	Map<State, Double> CPT;
	BNSufficientStatistics sufficientStatistics;

	/**
	 * Constructor to create a node containing a conditional probability table (CPT)
	 * given the name of the variable and its possible states.
	 * 
	 * @param nameVariable
	 * @param statesVariable
	 */
	public CPTNode(String nameVariable, List<State> statesVariable) {
		super(nameVariable, statesVariable);
	}

	/**
	 * Establish the sufficient statistics of a CPT node.
	 * 
	 * @param CPT
	 */
	public void setSufficientStatistics(SufficientStatistics sufficientStatistics) {
		this.sufficientStatistics = (BNSufficientStatistics) sufficientStatistics;

	}

	/**
	 * Establish the CPT of the node.
	 * 
	 * @param CPT
	 */
	public void setCPT(Map<State, Double> CPT) {
		this.CPT = CPT;
	}

	/**
	 * Sample the state of the node given an evidence using forward sampling. First
	 * it is sampled from a uniform distribution, then it is iterated over the state
	 * of the nodes and accumulated the probability of each of them. Once this
	 * accumulated probability is more than the sampled value from the uniform
	 * distribution, the current state under study is returned.
	 * 
	 * @param evidence
	 * @return sampled state of the node. Null is returned if the state could not be
	 *         sampled
	 */
	public State sampleState(State evidence) {
		// Sample from uniform distribution
		double probUniform = Math.random();
		// Accumulated probability
		double accProb = 0;
		// Sampled state
		State sampledState = null;
		// Iterate over all the states of the node
		for (State state : getStates()) {
			// Get names of the parents of the node
			List<String> nameParents = getParents().stream().map(parent -> parent.getName())
					.collect(Collectors.toList());
			// Define a query object to retrieve probabilities
			State query = new State(evidence.getEvents());
			// Ignore nodes in the evidence that are not parents of this node
			query.removeAllEventsExcept(nameParents);
			// Include the current state under evaluation of the node
			query.addEvents(state.getEvents());
			try {
				// Retrieve probability given the evidence and certain state of the node
				accProb += getCPT().get(query);
			} catch (NullPointerException e) {
				// The evidence does not contain all
				return sampledState;
			}
			if (probUniform <= accProb) {
				// Generated state for the node
				sampledState = state;
				break;
			}
		}
		return sampledState;
	}

	/**
	 * Return CPT of the node.
	 * 
	 * @return CPT
	 */
	public Map<State, Double> getCPT() {
		return CPT;
	}

	/**
	 * Return sufficient statistics of the node.
	 * 
	 * @return sufficient statistics
	 */
	public Map<State, Integer> getSufficientStatistics() {
		return sufficientStatistics.getSufficientStatistics();
	}

	public String toString() {
		String discreteNodeDescription = super.toString();
		StringBuilder sb = new StringBuilder();
		sb.append(discreteNodeDescription + "\n");
		sb.append("--CPT--\n");
		sb.append(CPT);
		return sb.toString();
	}

}
