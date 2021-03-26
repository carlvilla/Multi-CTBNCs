package com.cig.mctbnc.learning.parameters;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.nodes.DiscreteNode;

/**
 * Interface for sufficient statistics of discrete nodes.
 * 
 * @author Carlos Villa Blanco
 *
 */
public interface SufficientStatistics {

	/**
	 * Compute the sufficient statistics of a discrete node.
	 * 
	 * @param node    node whose sufficient statistics are computed
	 * @param dataset dataset from which is extracted the sufficient statistics
	 */
	public void computeSufficientStatistics(DiscreteNode node, Dataset dataset);

}
