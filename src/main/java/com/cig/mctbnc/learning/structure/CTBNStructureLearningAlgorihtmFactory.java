package com.cig.mctbnc.learning.structure;

import com.cig.mctbnc.learning.structure.optimization.hillclimbing.CTBNHillClimbing;

/**
 * Builds the specified structure learning algorithm for a CTBN.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class CTBNStructureLearningAlgorihtmFactory {

	/**
	 * Build the specified structure learning algorithm.
	 * 
	 * @param algorithm
	 * @return structure learning algorithm
	 */
	public static CTBNStructureLearningAlgorithm getAlgorithm(String algorithm, String scoreFunction) {
		switch (algorithm) {
		default:
			// Hill Climbing
			return new CTBNHillClimbing(scoreFunction);
		}
	}

}
