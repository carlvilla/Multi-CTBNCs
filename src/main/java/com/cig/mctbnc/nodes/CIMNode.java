package com.cig.mctbnc.nodes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.cig.mctbnc.data.representation.State;
import com.cig.mctbnc.learning.parameters.SufficientStatistics;
import com.cig.mctbnc.learning.parameters.ctbn.CTBNSufficientStatistics;

/**
 * Extends the DiscreteNode class in order to store a CIM and the sufficient
 * statistics for a CTBN. Ø
 * 
 * @author Carlos Villa Blanco
 *
 */
public class CIMNode extends DiscreteNode {

	// The conditional intensity matrix can be summarized by two types of parameters
	// (Nodelman et al., 2012): (1) instantaneous probability of the variable
	// leaving a certain state while its parents take a certain value
	Map<State, Double> Qx;
	// (2) probability of the variable leaving a certain state for another one while
	// its parents take a certain value
	Map<State, Map<State, Double>> Oxy;

	CTBNSufficientStatistics sufficientStatistics;

	/**
	 * Construct a CIMNode given its name and possible states.
	 * 
	 * @param name
	 * @param states
	 */
	public CIMNode(String name, List<State> states) {
		super(name, states);
	}

	/**
	 * Initialize a CIMNode given its name, possible states and if it is a class
	 * variable.
	 * 
	 * @param name
	 * @param states
	 * @param isClassVariable
	 */
	public CIMNode(String name, List<State> states, boolean isClassVariable) {
		super(name, states, isClassVariable);
	}

	/**
	 * Initialize a CIMNode node given a list of strings with its states. The order
	 * of parameters is changed with respect to the other constructor to avoid both
	 * of them having the same erasure.
	 * 
	 * @param name
	 * @param isClassVariable
	 * @param states
	 * 
	 */
	public CIMNode(String name, boolean isClassVariable, List<String> states) {
		super(name, isClassVariable, states);
	}

	@Override
	public void setSufficientStatistics(SufficientStatistics sufficientStatistics) {
		this.sufficientStatistics = (CTBNSufficientStatistics) sufficientStatistics;
	}

	/**
	 * Set the parameters of a node.
	 * 
	 * @param Qx
	 * @param Oxy
	 */
	public void setParameters(Map<State, Double> Qx, Map<State, Map<State, Double>> Oxy) {
		this.Qx = Qx;
		this.Oxy = Oxy;
	}

	/**
	 * Sample the time that the node stays in a certain state given the state of its
	 * parents. Returns -1 if not all the states of the parents were provided.
	 * 
	 * @param evidence contains the state of the node and its parents
	 * @return sampled time
	 */
	public double sampleTimeState(State evidence) {
		// Query object to retrieve intensity that the node transits given the evidence
		State query = new State(evidence.getEvents());
		// Ignore nodes in the evidence that are not parents or the node itself
		List<String> requiredNodes = getParents().stream().map(parent -> parent.getName()).collect(Collectors.toList());
		requiredNodes.add(getName());
		query.removeAllEventsExcept(requiredNodes);

		double q = 0.0;
		try {
			// Retrieve intensity of the node of transitioning from the state specified in
			// "evidence"
			q = getQx().get(query);
		} catch (NullPointerException e) {
			// It is necessary the previous state of all the parents
			return -1;
		}

		if (q == 0)
			return Double.POSITIVE_INFINITY;
		else
			// Sample time from exponential distribution with parameter 'q'
			return -Math.log(1 - Math.random()) / q;

	}

	/**
	 * Sample the next state of the node given its current state and that of its
	 * parents. Returns -1 if not all the states of the parents were provided.
	 * 
	 */
	public State sampleNextState(State evidence) {
		// Query object to retrieve the probability of transiting to each possible state
		State query = new State(evidence.getEvents());
		// Ignore nodes in the evidence that are not parents or the node itself
		List<String> requiredNodes = getParents().stream().map(parent -> parent.getName()).collect(Collectors.toList());
		requiredNodes.add(getName());
		query.removeAllEventsExcept(requiredNodes);

		State sampledState = null;

		// Sample from uniform distribution
		double probUniform = Math.random();
		// Accumulated probability
		double accProb = 0;

		for (State nextState : getOxy().get(query).keySet()) {
			// Probability of transitioning to "nextState"
			accProb += getOxy().get(query).get(nextState);

			if (probUniform <= accProb) {
				// Generated state for the node
				sampledState = nextState;
				break;
			}

		}

		return sampledState;

	}

	/**
	 * Get the sufficient statistics of a CIM node.
	 * 
	 * @return sufficient statistics.
	 */
	public CTBNSufficientStatistics getSufficientStatistics() {
		return sufficientStatistics;
	}

	/**
	 * Return the parameter containing the probabilities of the variable leaving
	 * every state for a different one.
	 * 
	 * @return parameter Qx
	 */
	public Map<State, Double> getQx() {
		return Qx;
	}

	/**
	 * Return the parameter containing the probabilities of the variable leaving a
	 * state for a certain one
	 * 
	 * @return parameter Oxy
	 */
	public Map<State, Map<State, Double>> getOxy() {
		return Oxy;
	}

	public String toString() {
		String discreteNodeDescription = super.toString();
		StringBuilder sb = new StringBuilder();
		sb.append(discreteNodeDescription + "\n");
		// sb.append("--CIM--\n");
		// sb.append(CIM);
		return sb.toString();
	}

}
