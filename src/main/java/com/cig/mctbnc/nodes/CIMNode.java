package com.cig.mctbnc.nodes;

import java.util.Map;

import com.cig.mctbnc.data.representation.State;
import com.cig.mctbnc.learning.parameters.CTBNSufficientStatistics;

/**
 * Extends the DiscreteNode class in order to store a CIM and the sufficient
 * statistics for a CTBN.
 * Ø
 * @author Carlos Villa Blanco
 *
 */
public class CIMNode extends DiscreteNode {

	// The conditional intensity matrix can be summarized by two types of parameters
	// Instantaneous probability of the variable leaving a certain state while its
	// parents take a certain value
	Map<State, Double> Qx;
	// Probability of the variable leaving a certain state for another one while its
	// parents take a certain value
	Map<State, Map<State, Double>> Oxx;

	CTBNSufficientStatistics sufficientStatistics;

	public CIMNode(DiscreteNode node, CTBNSufficientStatistics sufficientStatistics) {
		super(node.getIndex(), node.getName(), node.isClassVariable(), node.getStates());
		this.sufficientStatistics = sufficientStatistics;
	}

	public CTBNSufficientStatistics getSufficientStatistics() {
		return sufficientStatistics;
	}

	public String toString() {
		String discreteNodeDescription = super.toString();
		StringBuilder sb = new StringBuilder();
		sb.append(discreteNodeDescription + "\n");
		//sb.append("--CIM--\n");
		// sb.append(CIM);
		return sb.toString();
	}

	public void setParameters(Map<State, Double> Qx, Map<State, Map<State, Double>> Oxx) {
		this.Qx = Qx;
		this.Oxx = Oxx;
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

}
