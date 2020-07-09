package main.java.com.cig.mctbnc.learning.parameters;

import java.util.Map;

import main.java.com.cig.mctbnc.models.DiscreteNode;
import main.java.com.cig.mctbnc.models.State;

/**
 * Extends DiscreteNode in order to store a CPT and the sufficient statistics.
 * 
 * @author Carlos Villa (carlos.villa@upm.es)
 *
 */
public class CPTNode extends DiscreteNode {

	Map<State, Double> CPT;
	Map<State, Integer> sufficientStatistics;

	public CPTNode(DiscreteNode node, Map<State, Double> CPT, Map<State, Integer> sufficientStatistics) {
		super(node.getIndex(), node.getName(), node.getStates());
		this.CPT = CPT;
		this.sufficientStatistics = sufficientStatistics;
	}

	public Map<State, Double> getCPT() {
		return CPT;
	}

	public Map<State, Integer> getSufficientStatistics() {
		return sufficientStatistics;
	}

}
