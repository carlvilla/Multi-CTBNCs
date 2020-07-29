package com.cig.mctbnc.learning.parameters;

import com.cig.mctbnc.data.representation.Dataset;

public interface SufficientStatistics {

	/**
	 * Compute the sufficient statistics of a node.
	 * 
	 * @param dataset
	 */
	public void computeSufficientStatistics(Dataset dataset);

}
