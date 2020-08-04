package com.cig.mctbnc.nodes;

import java.util.List;
import java.util.Map;

import com.cig.mctbnc.data.representation.State;

/**
 * Extends the DiscreteNode class to store a CPT and the sufficient statistics
 * for a BN.
 * 
 * @author Carlos Villa (carlos.villa@upm.es)
 *
 */
public class CPTNode extends DiscreteNode {

	Map<State, Double> CPT;
	Map<State, Integer> sufficientStatistics;

	public CPTNode(DiscreteNode node) {
		super(node.getIndex(), node.getName(), node.isClassVariable(), node.getStates());
	}
	
	public CPTNode(DiscreteNode node, Map<State, Double> CPT, Map<State, Integer> sufficientStatistics) {
		super(node.getIndex(), node.getName(), node.isClassVariable(), node.getStates());
		this.CPT = CPT;
		this.sufficientStatistics = sufficientStatistics;
	}

	public CPTNode(int index, String nameVariable, List<State> statesVariable) {
		super(index, nameVariable, statesVariable);
	}

	public Map<State, Double> getCPT() {
		return CPT;
	}

	public Map<State, Integer> getSufficientStatistics() {
		return sufficientStatistics;
	}

	public String toString() {
		String discreteNodeDescription = super.toString();
		StringBuilder sb = new StringBuilder();
		sb.append(discreteNodeDescription + "\n");
		sb.append("--CPT--\n");
		sb.append(CPT);
		return sb.toString();
	}

	public void setSufficientStatistics(Map<State, Integer> sufficientStatistics) {
		this.sufficientStatistics = sufficientStatistics;
		
	}

	public void setCPT(Map<State, Double> CPT) {
		this.CPT = CPT;
	}

}
