package es.upm.fi.cig.multictbnc.learning.structure.hybrid;

import es.upm.fi.cig.multictbnc.exceptions.ErroneousValueException;
import es.upm.fi.cig.multictbnc.learning.structure.StructureLearningAlgorithm;
import es.upm.fi.cig.multictbnc.learning.structure.hybrid.PC.PCHybridAlgorithm;
import es.upm.fi.cig.multictbnc.learning.structure.hybrid.hillclimbing.BNHillClimbingHybridAlgorithm;
import es.upm.fi.cig.multictbnc.learning.structure.optimisation.hillclimbing.HillClimbingSolution;
import es.upm.fi.cig.multictbnc.learning.structure.optimisation.scores.bn.BNScoreFunction;
import es.upm.fi.cig.multictbnc.models.PGM;
import es.upm.fi.cig.multictbnc.nodes.Node;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implements the hybrid structure learning algorithm for Bayesian networks. This class was designed to learn the class
 * subgraph of a Multi-CTBNC.
 *
 * @author Carlos Villa Blanco
 */
public class BNHybridStructureLearningAlgorithm implements StructureLearningAlgorithm {
	private static final Logger logger = LogManager.getLogger(BNHybridStructureLearningAlgorithm.class);
	BNScoreFunction scoreFunction;
	double significance;

	/**
	 * Initialises the hybrid structure learning algorithm receiving a significance value and a score function.
	 *
	 * @param scoreFunction score function for the maximisation phase
	 * @param significance  significance level for the restriction phase
	 */
	public BNHybridStructureLearningAlgorithm(BNScoreFunction scoreFunction, double significance) {
		this.scoreFunction = scoreFunction;
		this.significance = significance;
	}

	@Override
	public String getIdentifier() {
		return "Hybrid algorithm";
	}

	@Override
	public Map<String, String> getParametersAlgorithm() {
		Map<String, String> parametersAlgorithm = new HashMap<>();
		parametersAlgorithm.put("scoreFunction", this.scoreFunction.getIdentifier());
		parametersAlgorithm.put("penalisationFunction", this.scoreFunction.getNamePenalisationFunction());
		parametersAlgorithm.put("significancePC", String.valueOf(this.significance));
		return parametersAlgorithm;
	}

	@Override
	public void learn(PGM<? extends Node> pgm, List<Integer> idxNodes) {
		// TODO
	}

	@Override
	public void learn(PGM<? extends Node> pgm) throws ErroneousValueException {
		boolean[][] adjacencyMatrix = restrictionPhase(pgm);
		adjacencyMatrix = maximisationPhase(pgm, adjacencyMatrix);
		pgm.setStructure(adjacencyMatrix);
		pgm.learnParameters();
	}

	@Override
	public void learn(PGM<? extends Node> pgm, int idxNode) {
		// TODO
	}

	private boolean[][] maximisationPhase(PGM<? extends Node> pgm, boolean[][] skeletonAdjacencyMatrix) {
		logger.info("Performing the score-based phase to learn the structure of the class subgraph");
		BNHillClimbingHybridAlgorithm hillClimbing = new BNHillClimbingHybridAlgorithm(this.scoreFunction,
				skeletonAdjacencyMatrix);
		HillClimbingSolution hs = hillClimbing.findStructure(pgm);
		return hs.getAdjacencyMatrix();
	}

	private boolean[][] restrictionPhase(PGM<? extends Node> pgm) throws ErroneousValueException {
		logger.info("Performing the constraint-based phase to learn the structure of the class subgraph");
		// Use PC to learn skeleton
		PCHybridAlgorithm pc = new PCHybridAlgorithm(this.significance);
		return pc.learnSkeleton(pgm);
	}

}