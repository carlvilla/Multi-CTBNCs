package es.upm.fi.cig.multictbnc.learning.structure;

import es.upm.fi.cig.multictbnc.learning.structure.constraintlearning.PC.*;
import es.upm.fi.cig.multictbnc.learning.structure.hybrid.BNHybridStructureLearningAlgorithm;
import es.upm.fi.cig.multictbnc.learning.structure.hybrid.CTBNHybridStructureLearningAlgorithm;
import es.upm.fi.cig.multictbnc.learning.structure.optimisation.hillclimbing.FirstChoiceHillClimbing;
import es.upm.fi.cig.multictbnc.learning.structure.optimisation.hillclimbing.RandomRestartHillClimbing;
import es.upm.fi.cig.multictbnc.learning.structure.optimisation.hillclimbing.implementation.BNHillClimbing;
import es.upm.fi.cig.multictbnc.learning.structure.optimisation.hillclimbing.implementation.CTBNHillClimbing;
import es.upm.fi.cig.multictbnc.learning.structure.optimisation.hillclimbing.implementation.CTBNHillClimbingIndividual;
import es.upm.fi.cig.multictbnc.learning.structure.optimisation.scores.bn.BNBayesianScore;
import es.upm.fi.cig.multictbnc.learning.structure.optimisation.scores.bn.BNLogLikelihood;
import es.upm.fi.cig.multictbnc.learning.structure.optimisation.scores.bn.BNScoreFunction;
import es.upm.fi.cig.multictbnc.learning.structure.optimisation.scores.ctbn.CTBNBayesianScore;
import es.upm.fi.cig.multictbnc.learning.structure.optimisation.scores.ctbn.CTBNConditionalLogLikelihood;
import es.upm.fi.cig.multictbnc.learning.structure.optimisation.scores.ctbn.CTBNLogLikelihood;
import es.upm.fi.cig.multictbnc.learning.structure.optimisation.scores.ctbn.CTBNScoreFunction;
import es.upm.fi.cig.multictbnc.learning.structure.optimisation.tabusearch.BNTabuSearch;
import es.upm.fi.cig.multictbnc.learning.structure.optimisation.tabusearch.CTBNTabuSearchIndividual;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

/**
 * Builds the specified structure learning algorithms for Bayesian networks and continuous-time Bayesian networks.
 *
 * @author Carlos Villa Blanco
 */
public class StructureLearningAlgorithmFactory {

    private static final Logger logger = LogManager.getLogger(StructureLearningAlgorithmFactory.class);

    /**
     * Builds the specified structure learning algorithm for Bayesian networks. Current parameters: (1) scoreFunction:
     * name of the score function (score-based/hybrid algorithms) (2) penalisationFunction: name of the penalisation
     * function (score-based/hybrid algorithms) (3) numRestarts: number of restarts for the random-restart hill
     * climbing
     * (random-restart hill climbing) (3) tabuListSize: size of the tabu list (tabu search) (4) significancePC:
     * significance of the PC algorithm (constraint-based/hybrid algorithms)
     *
     * @param algorithm name of the structure learning algorithm
     * @param param     map containing the necessary parameters for the requested algorithm
     * @return structure learning algorithm for Bayesian networks
     */
    public static StructureLearningAlgorithm getAlgorithmBN(String algorithm, Map<String, String> param) {
        switch (algorithm) {
            case "PC":
            case "CTPC":
            case "MB-CTPC":
                double significancePC = Double.valueOf(param.get("significancePC"));
                return new PC(significancePC);
            case "Online-MB-CTPC":
                significancePC = Double.valueOf(param.get("significancePC"));
                return new HITONPC(significancePC);
            case "Random-restart hill climbing":
                BNScoreFunction score = getScoreFunctionBN(param);
                int numRestarts = Integer.valueOf(param.get("numRestarts"));
                return new RandomRestartHillClimbing(new BNHillClimbing(score), numRestarts);
            case "Tabu search":
                score = getScoreFunctionBN(param);
                int tabuListSize = Integer.valueOf(param.get("tabuListSize"));
                return new FirstChoiceHillClimbing(new BNTabuSearch(score, tabuListSize, 1));
            case "Hybrid algorithm":
                score = getScoreFunctionBN(param);
                significancePC = Double.valueOf(param.get("significancePC"));
                return new BNHybridStructureLearningAlgorithm(score, significancePC);
            case "Hill climbing":
                score = getScoreFunctionBN(param);
                return new FirstChoiceHillClimbing(new BNHillClimbing(score));
            default:
                logger.warn("The structure learning algorithm {} is not implemented. First-choice Hill climbing algorithm will be used instead",
                        algorithm);
                score = getScoreFunctionBN(param);
                return new FirstChoiceHillClimbing(new BNHillClimbing(score));
        }
    }

    /**
     * Builds the specified structure learning algorithm for continuous-time Bayesian networks.
     * <p>
     * (1) scoreFunction: name of the score function (score-based/hybrid algorithms) (2) penalisationFunction: name of
     * the penalisation function (score-based/hybrid algorithms) (3) numRestarts: number of restarts for the
     * random-restart hill climbing (Random-restart hill climbing) (3) tabuListSize: size of the tabu list (Tabu
     * search)
     * (4) sigTimeTransitionHyp: significance of the PC algorithm (constraint-based/hybrid algorithms) (5)
     * sigStateToStateTransitionHyp: significance of the PC algorithm (constraint-based/hybrid algorithms) (6)
     * maxSizeSepSet: (Hybrid algorithm)
     *
     * @param algorithm name of the structure learning algorithm
     * @param param     map containing the necessary parameters for the requested algorithm
     * @return structure learning algorithm for continuous-time Bayesian networks
     */
    public static StructureLearningAlgorithm getAlgorithmCTBN(String algorithm, Map<String, String> param) {
        switch (algorithm) {
            case "CTPC":
                double sigTimeTransitionHyp = Double.valueOf(param.get("sigTimeTransitionHyp"));
                double sigStateToStateTransitionHyp = Double.valueOf(param.get("sigStateToStateTransitionHyp"));
                return new CTPC(sigTimeTransitionHyp, sigStateToStateTransitionHyp);
            case "MB-CTPC":
                sigTimeTransitionHyp = Double.valueOf(param.get("sigTimeTransitionHyp"));
                sigStateToStateTransitionHyp = Double.valueOf(param.get("sigStateToStateTransitionHyp"));
                return new MarkovBlanketCTPC(sigTimeTransitionHyp, sigStateToStateTransitionHyp);
            case "Online-MB-CTPC":
                sigTimeTransitionHyp = Double.valueOf(param.get("sigTimeTransitionHyp"));
                sigStateToStateTransitionHyp = Double.valueOf(param.get("sigStateToStateTransitionHyp"));
                return new OnlineMarkovBlanketCTPC(sigTimeTransitionHyp, sigStateToStateTransitionHyp);
            case "Random-restart hill climbing":
                CTBNScoreFunction score = getScoreFunctionCTBN(param);
                int numRestarts = Integer.valueOf(param.get("numRestarts"));
                if (score.isDecomposable())
                    return new RandomRestartHillClimbing(new CTBNHillClimbingIndividual(score), numRestarts);
                return new RandomRestartHillClimbing(new CTBNHillClimbing(score), numRestarts);
            case "Tabu search":
                score = getScoreFunctionCTBN(param);
                int tabuListSize = Integer.valueOf(param.get("tabuListSize"));
                if (score.isDecomposable())
                    return new FirstChoiceHillClimbing(new CTBNTabuSearchIndividual(score, tabuListSize, 1));
            case "Hybrid algorithm":
                score = getScoreFunctionCTBN(param);
                int maxSizeSepSet = Integer.valueOf(param.get("maxSizeSepSet"));
                sigTimeTransitionHyp = Double.valueOf(param.get("sigTimeTransitionHyp"));
                sigStateToStateTransitionHyp = Double.valueOf(param.get("sigStateToStateTransitionHyp"));
                if (score.isDecomposable())
                    return new CTBNHybridStructureLearningAlgorithm(score, maxSizeSepSet, sigTimeTransitionHyp,
                            sigStateToStateTransitionHyp);
            case "Hill climbing":
                score = getScoreFunctionCTBN(param);
                if (score.isDecomposable())
                    return new FirstChoiceHillClimbing(new CTBNHillClimbingIndividual(score));
                return new FirstChoiceHillClimbing(new CTBNHillClimbing(score));
            default:
                logger.warn("The structure learning algorithm {} is not implemented. First-choice Hill Climbing algorithm will be used instead",
                        algorithm);
                score = getScoreFunctionCTBN(param);
                if (score.isDecomposable())
                    return new FirstChoiceHillClimbing(new CTBNHillClimbingIndividual(score));
                return new FirstChoiceHillClimbing(new CTBNHillClimbing(score));
        }
    }

    /**
     * Returns the name of available optimisation methods.
     *
     * @return optimisation methods
     */
    public static List<String> getAvailableLearningMethods() {
        return List.of("Hill climbing", "Random-restart hill climbing", "Tabu search", "CTPC", "MB-CTPC",
                "Hybrid algorithm");
    }

    private static BNScoreFunction getScoreFunctionBN(Map<String, String> args) {
        String scoreFunction = args.get("scoreFunction");
        String penalisationFunction = args.getOrDefault("penalisationFunction", "No");
        return getScoreFunctionBN(scoreFunction, penalisationFunction);
    }

    /**
     * Returns a {@code BNScoreFunction} for the specified score function for Bayesian networks.
     *
     * @param scoreFunction        name of the score function
     * @param penalisationFunction name of the penalisation function
     * @return a {@code BNScoreFunction}
     */
    private static BNScoreFunction getScoreFunctionBN(String scoreFunction, String penalisationFunction) {
        switch (scoreFunction) {
            case ("Bayesian Dirichlet equivalent"):
                return new BNBayesianScore();
            case ("Conditional log-likelihood"):
            case ("Log-likelihood"):
                return new BNLogLikelihood(penalisationFunction);
            default:
                logger.warn("The score function {} is not implemented. The log-likelihood will be used instead",
                        scoreFunction);
                return new BNLogLikelihood(penalisationFunction);
        }
    }

    /**
     * Returns a {@code CTBNScoreFunction} for the specified score function for continuous-time Bayesian networks.
     *
     * @param args parameters for the score function
     * @return a {@code CTBNScoreFunction}
     */
    private static CTBNScoreFunction getScoreFunctionCTBN(Map<String, String> args) {
        String scoreFunction = args.get("scoreFunction");
        String penalisationFunction = args.getOrDefault("penalisationFunction", "No");
        return getScoreFunctionCTBN(scoreFunction, penalisationFunction);
    }

    /**
     * Returns a {@code CTBNScoreFunction} for the specified score function for continuous-time Bayesian networks.
     *
     * @param scoreFunction        name of the score function
     * @param penalisationFunction name of the penalisation function
     * @return a {@code CTBNScoreFunction}
     */
    private static CTBNScoreFunction getScoreFunctionCTBN(String scoreFunction, String penalisationFunction) {
        switch (scoreFunction) {
            case ("Conditional log-likelihood"):
                return new CTBNConditionalLogLikelihood(penalisationFunction);
            case ("Bayesian Dirichlet equivalent"):
                return new CTBNBayesianScore();
            case ("Log-likelihood"):
                return new CTBNLogLikelihood(penalisationFunction);
            default:
                logger.warn("The score function {} is not implemented. The log-likelihood will be used instead",
                        scoreFunction);
                return new CTBNLogLikelihood(penalisationFunction);
        }
    }

}