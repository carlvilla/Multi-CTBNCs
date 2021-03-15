package com.cig.mctbnc.learning.structure;

import java.util.List;

import com.cig.mctbnc.learning.structure.optimization.hillclimbing.FirstChoiceHillClimbing;
import com.cig.mctbnc.learning.structure.optimization.hillclimbing.RandomRestartHillClimbing;
import com.cig.mctbnc.learning.structure.optimization.hillclimbing.implementation.BNHillClimbing;
import com.cig.mctbnc.learning.structure.optimization.hillclimbing.implementation.CTBNHillClimbing;
import com.cig.mctbnc.learning.structure.optimization.hillclimbing.implementation.CTBNHillClimbingIndividual;
import com.cig.mctbnc.learning.structure.optimization.hillclimbing.implementation.HillClimbingImplementation;
import com.cig.mctbnc.learning.structure.optimization.scores.bn.BNBayesianScore;
import com.cig.mctbnc.learning.structure.optimization.scores.bn.BNLogLikelihood;
import com.cig.mctbnc.learning.structure.optimization.scores.bn.BNScoreFunction;
import com.cig.mctbnc.learning.structure.optimization.scores.ctbn.CTBNBayesianScore;
import com.cig.mctbnc.learning.structure.optimization.scores.ctbn.CTBNConditionalLogLikelihood;
import com.cig.mctbnc.learning.structure.optimization.scores.ctbn.CTBNLogLikelihood;
import com.cig.mctbnc.learning.structure.optimization.scores.ctbn.CTBNScoreFunction;

/**
 * Builds the specified structure learning algorithms for Bayesian networks and
 * contious time Bayesian networks.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class StructureLearningAlgorithmFactory {

	/**
	 * Build the specified structure learning algorithm for Bayesian networks.
	 * 
	 * @param algorithm
	 * @param scoreFunction
	 * @param penalizationFunction
	 * @return structure learning algorithm
	 */
	public static StructureLearningAlgorithm getAlgorithmBN(String algorithm, String scoreFunction,
			String penalizationFunction) {
		// Retrieve score function
		BNScoreFunction score = getScoreFunctionBN(scoreFunction, penalizationFunction);
		// Retrieve optimization algorithm
		switch (algorithm) {
		case "Random-restart hill climbing":
			return new RandomRestartHillClimbing(new BNHillClimbing(score), 5);
		default:
			// Hill Climbing
			return new FirstChoiceHillClimbing(new BNHillClimbing(score));
		}
	}

	/**
	 * Build the specified structure learning algorithm for continuous time Bayesian
	 * networks.
	 * 
	 * @param algorithm
	 * @param scoreFunction
	 * @param penalizationFunction
	 * @return structure learning algorithm
	 */
	public static StructureLearningAlgorithm getAlgorithmCTBN(String algorithm, String scoreFunction,
			String penalizationFunction) {
		// Retrieve score function
		CTBNScoreFunction score = getScoreFunctionCTBN(scoreFunction, penalizationFunction);
		switch (algorithm) {
		case ("Random-restart hill climbing"):
			if (score.isDecomposable())
				return new RandomRestartHillClimbing(new CTBNHillClimbingIndividual(score), 5);
			return new RandomRestartHillClimbing(new CTBNHillClimbing(score), 5);
		default:
			// Hill Climbing
			if (score.isDecomposable())
				return new FirstChoiceHillClimbing(new CTBNHillClimbingIndividual(score));
			return new FirstChoiceHillClimbing(new CTBNHillClimbing(score));
		}
	}

	/**
	 * Return the name of available optimization methods.
	 * 
	 * @return optimization methods
	 */
	public static List<String> getAvailableOptimizationMethods() {
		return List.of("Hill climbing", "Random-restart hill climbing");
	}

	private static BNScoreFunction getScoreFunctionBN(String scoreFunction, String penalizationFunction) {
		switch (scoreFunction) {
		case ("Bayesian Dirichlet equivalent"):
			return new BNBayesianScore();
		default:
			return new BNLogLikelihood(penalizationFunction);
		}
	}

	private static CTBNScoreFunction getScoreFunctionCTBN(String scoreFunction, String penalizationFunction) {
		switch (scoreFunction) {
		case ("Conditional log-likelihood"):
			return new CTBNConditionalLogLikelihood(penalizationFunction);
		case ("Bayesian Dirichlet equivalent"):
			return new CTBNBayesianScore();
		default:
			return new CTBNLogLikelihood(penalizationFunction);
		}
	}

}
