package com.cig.mctbnc.nodes;

import java.util.List;
import java.util.Map;

import com.cig.mctbnc.data.representation.State;

/**
 * Extends the DiscreteNode class in order to store a CIM and the sufficient
 * statistics for a CTBN.
 * 
 * @author Carlos Villa (carlos.villa@upm.es)
 *
 */
public class CIMNode extends DiscreteNode {

	Map<State, Double> CIM;
	Map<State, Integer> sufficientStatistics;

	public CIMNode(int index, String name, boolean classVariable, List<State> list) {
		super(index, name, classVariable, list);
	}

	public Map<State, Double> getCIM() {
		return CIM;
	}

	public Map<State, Integer> getSufficientStatistics() {
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
