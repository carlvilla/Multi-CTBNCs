package com.cig.mctbnc.nodes;

import java.util.List;
import java.util.Map;

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
	Map<State, Map<State, Double>> Oxx;

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
	 * Construct a CIMNode given its name, possible states and if it is a class
	 * variable or not.
	 * 
	 * @param name
	 * @param states
	 * @param isClassVariable
	 */
	public CIMNode(String name, List<State> states, boolean isClassVariable) {
		super(name, states, isClassVariable);
	}

	@Override
	public void setSufficientStatistics(SufficientStatistics sufficientStatistics) {
		this.sufficientStatistics = (CTBNSufficientStatistics) sufficientStatistics;
	}

	/**
	 * Set the parameters of a node.
	 * 
	 * @param Qx
	 * @param Oxx
	 */
	public void setParameters(Map<State, Double> Qx, Map<State, Map<State, Double>> Oxx) {
		this.Qx = Qx;
		this.Oxx = Oxx;
	}

	/**
	 * Sample the time that the node stays in a certain state given the state of its
	 * parents.
	 * 
	 * @param evidence contains the state of the node and its parents
	 * @return sampled time
	 */
	public double sampleTimeState(State evidence) {
		return 0.0;
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
	 * @return parameter Oxx
	 */
	public Map<State, Map<State, Double>> getOxx() {
		return Oxx;
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
