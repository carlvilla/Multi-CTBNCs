package es.upm.fi.cig.mctbnc.learning.structure;

import java.util.List;

import es.upm.fi.cig.mctbnc.learning.structure.optimization.hillclimbing.FirstChoiceHillClimbing;
import es.upm.fi.cig.mctbnc.learning.structure.optimization.hillclimbing.RandomRestartHillClimbing;
import es.upm.fi.cig.mctbnc.learning.structure.optimization.hillclimbing.implementation.BNHillClimbing;
import es.upm.fi.cig.mctbnc.learning.structure.optimization.hillclimbing.implementation.CTBNHillClimbing;
import es.upm.fi.cig.mctbnc.learning.structure.optimization.hillclimbing.implementation.CTBNHillClimbingIndividual;
import es.upm.fi.cig.mctbnc.learning.structure.optimization.scores.bn.BNBayesianScore;
import es.upm.fi.cig.mctbnc.learning.structure.optimization.scores.bn.BNLogLikelihood;
import es.upm.fi.cig.mctbnc.learning.structure.optimization.scores.bn.BNScoreFunction;
import es.upm.fi.cig.mctbnc.learning.structure.optimization.scores.ctbn.CTBNBayesianScore;
import es.upm.fi.cig.mctbnc.learning.structure.optimization.scores.ctbn.CTBNConditionalLogLikelihood;
import es.upm.fi.cig.mctbnc.learning.structure.optimization.scores.ctbn.CTBNLogLikelihood;
import es.upm.fi.cig.mctbnc.learning.structure.optimization.scores.ctbn.CTBNScoreFunction;

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
	 * @param numRestarts          number of restarts for the random-restart hill
	 *                             climbing
	 * @return structure learning algorithm
	 */
	public static StructureLearningAlgorithm getAlgorithmBN(String algorithm, String scoreFunction,
			String penalizationFunction, int numRestarts) {
		// Retrieve score function
		BNScoreFunction score = getScoreFunctionBN(scoreFunction, penalizationFunction);
		// Retrieve optimization algorithm
		switch (algorithm) {
		case "Random-restart hill climbing":
			return new RandomRestartHillClimbing(new BNHillClimbing(score), numRestarts);
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
	 * @param numRestarts 
	 * @return structure learning algorithm
	 */
	public static StructureLearningAlgorithm getAlgorithmCTBN(String algorithm, String scoreFunction,
			String penalizationFunction, int numRestarts) {
		// Retrieve score function
		CTBNScoreFunction score = getScoreFunctionCTBN(scoreFunction, penalizationFunction);
		switch (algorithm) {
		case ("Random-restart hill climbing"):
			if (score.isDecomposable())
				return new RandomRestartHillClimbing(new CTBNHillClimbingIndividual(score), numRestarts);
			return new RandomRestartHillClimbing(new CTBNHillClimbing(score), numRestarts);
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
