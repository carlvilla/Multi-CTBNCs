package com.cig.mctbnc.learning.parameters;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.nodes.Node;

public interface SufficientStatistics {

	/**
	 * Compute the sufficient statistics of a node.
	 * 
	 * @param node    node whose sufficient statistics are computed
	 * @param dataset dataset from which is extracted the sufficient statistics
	 */
	public void computeSufficientStatistics(Node node, Dataset dataset);

}
