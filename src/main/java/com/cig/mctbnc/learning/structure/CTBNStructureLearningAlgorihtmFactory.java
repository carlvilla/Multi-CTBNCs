package com.cig.mctbnc.learning.structure;

import com.cig.mctbnc.learning.structure.optimization.CTBNScoreFunction;
import com.cig.mctbnc.learning.structure.optimization.hillclimbing.CTBNHillClimbing;
import com.cig.mctbnc.learning.structure.optimization.hillclimbing.CTBNHillClimbingIndividual;
import com.cig.mctbnc.learning.structure.optimization.scores.ctbn.CTBNBayesianScore;
import com.cig.mctbnc.learning.structure.optimization.scores.ctbn.CTBNConditionalLogLikelihood;
import com.cig.mctbnc.learning.structure.optimization.scores.ctbn.CTBNLogLikelihood;

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
	 * @param scoreFunction
	 * @param penalizationFunction
	 * @return structure learning algorithm
	 */
	public static CTBNStructureLearningAlgorithm getAlgorithm(String algorithm, String scoreFunction,
			String penalizationFunction) {
		switch (algorithm) {
		default:
			// Hill Climbing
			CTBNScoreFunction score = getScoreFunction(scoreFunction, penalizationFunction);
			if (scoreFunction.equals("Conditional log-likelihood"))
				// Scores that cannot be optimized individually by node
				return new CTBNHillClimbing(score);
			else
				// Scores that can be optimized individually by node
				return new CTBNHillClimbingIndividual(score);

		}
	}

	private static CTBNScoreFunction getScoreFunction(String scoreFunction, String penalizationFunction) {
		switch (scoreFunction) {
		case ("Conditional log-likelihood"):
			return new CTBNConditionalLogLikelihood(penalizationFunction);
		case ("Bayesian score"):
			return new CTBNBayesianScore();
		default:
			return new CTBNLogLikelihood(penalizationFunction);
		}
	}

}
