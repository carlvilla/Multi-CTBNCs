package com.cig.mctbnc.nodes;

import java.util.Map;

import com.cig.mctbnc.data.representation.State;

/**
 * Extends the DiscreteNode class in order to store a CIM and the sufficient
 * statistics for a CTBN.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class CIMNode extends DiscreteNode {

	Map<State, Double> CIM;
	Map<State, Map<State, Integer>> sufficientStatistics;

	public CIMNode(DiscreteNode node, Map<State, Double> CIM, Map<State, Map<State, Integer>> sufficientStatistics) {
		super(node.getIndex(), node.getName(), node.isClassVariable(), node.getStates());
		this.CIM = CIM;
		this.sufficientStatistics = sufficientStatistics;
	}

	public Map<State, Double> getCIM() {
		return CIM;
	}

	public Map<State, Map<State, Integer>> getSufficientStatistics() {
		return sufficientStatistics;
	}

	public String toString() {
		String discreteNodeDescription = super.toString();
		StringBuilder sb = new StringBuilder();
		sb.append(discreteNodeDescription + "\n");
		sb.append("--CIM--\n");
		sb.append(CIM);
		return sb.toString();
	}

}
