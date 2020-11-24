package com.cig.mctbnc.learning.structure;

import com.cig.mctbnc.learning.structure.optimization.BNScoreFunction;
import com.cig.mctbnc.learning.structure.optimization.hillclimbing.BNHillClimbing;
import com.cig.mctbnc.learning.structure.optimization.scores.bn.BNBayesianScore;
import com.cig.mctbnc.learning.structure.optimization.scores.bn.BNLogLikelihood;

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
	 * @param scoreFunction
	 * @param penalizationFunction
	 * @return structure learning algorithm
	 */
	public static BNStructureLearningAlgorithm getAlgorithm(String algorithm, String scoreFunction,
			String penalizationFunction) {
		switch (algorithm) {
		default:
			// Hill Climbing
			BNScoreFunction score = getScoreFunction(scoreFunction, penalizationFunction);
			return new BNHillClimbing(score);
		}
	}

	private static BNScoreFunction getScoreFunction(String scoreFunction, String penalizationFunction) {
		switch (scoreFunction) {
		case ("Bayesian score"):
			return new BNBayesianScore();
		default:
			return new BNLogLikelihood(penalizationFunction);
		}
	}
}
