package com.cig.mctbnc.nodes;

import java.util.List;
import java.util.Map;

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
	
	public CPTNode(String nameVariable, List<State> statesVariable) {
		super(nameVariable, statesVariable);
	}

	public Map<State, Double> getCPT() {
		return CPT;
	}

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

	public void setSufficientStatistics(SufficientStatistics sufficientStatistics) {
		this.sufficientStatistics = (BNSufficientStatistics) sufficientStatistics;
		
	}

	public void setCPT(Map<State, Double> CPT) {
		this.CPT = CPT;
	}

}
