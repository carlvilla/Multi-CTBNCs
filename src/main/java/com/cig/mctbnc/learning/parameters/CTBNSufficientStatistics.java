package com.cig.mctbnc.learning.parameters;

import java.util.Map;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.data.representation.State;
import com.cig.mctbnc.nodes.Node;

public class CTBNSufficientStatistics {

	private Node node;
	private Map<State, Integer> N;

	public CTBNSufficientStatistics(Node node) {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Compute the sufficient statistics of a node in a CTBN. These are (1) number
	 * of times the variable transition from a certain state to another while its
	 * parents have a certain value and (2) time that the variable stay in certain
	 * state while its parents take a certain value.
	 * 
	 * @param dataset
	 */
	public void computeSufficientStatistics(Dataset dataset) {
		// TODO Auto-generated method stub

	}

}
