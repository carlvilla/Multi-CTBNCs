package com.cig.mctbnc.learning.parameters;

import java.util.Map;

import com.cig.mctbnc.data.representation.Dataset;

public interface SufficientStatistics {
	
	/**
	 * Compute the sufficient statistics of a node.
	 * 
	 * @param dataset
	 */
	public void computeSufficientStatistics(Dataset dataset);
	
	/**
	 * Return the sufficient statistics of a node.
	 * @return Map object containing the sufficient statistics
	 */
	public Map<?, ?> getSufficientStatistics();

}
