package com.cig.mctbnc.learning.structure;

import com.cig.mctbnc.learning.structure.optimization.hillclimbing.BNHillClimbing;

/**
 * Builds the specified structure learning algorithm for a BN.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class BNStructureLearningAlgorihtmFactory {

	/**
	 * Build the specified structure learning algorithm.
	 * 
	 * @param algorithm
	 * @return structure learning algorithm
	 */
	public static BNStructureLearningAlgorithm getAlgorithm(String algorithm) {
		switch (algorithm) {
		default:
			// Hill Climbing
			return new BNHillClimbing("Log-likelihood");
		}
	}
}
