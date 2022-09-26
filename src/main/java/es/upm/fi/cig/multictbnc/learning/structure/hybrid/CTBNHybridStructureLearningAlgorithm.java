package es.upm.fi.cig.multictbnc.learning.structure.hybrid;

import es.upm.fi.cig.multictbnc.exceptions.ErroneousValueException;
import es.upm.fi.cig.multictbnc.learning.structure.StructureLearningAlgorithm;
import es.upm.fi.cig.multictbnc.learning.structure.hybrid.PC.CTPCHybridAlgorithm;
import es.upm.fi.cig.multictbnc.learning.structure.hybrid.hillclimbing.CTBNHillClimbingHybridAlgorithm;
import es.upm.fi.cig.multictbnc.learning.structure.optimisation.hillclimbing.HillClimbingSolution;
import es.upm.fi.cig.multictbnc.learning.structure.optimisation.scores.ctbn.CTBNScoreFunction;
import es.upm.fi.cig.multictbnc.models.PGM;
import es.upm.fi.cig.multictbnc.nodes.Node;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implements the hybrid structure learning algorithm for continuous-time Bayesian networks. This class was designed to
 * learn the bridge and feature subgraphs of a Multi-CTBNC.
 *
 * @author Carlos Villa Blanco
 */
public class CTBNHybridStructureLearningAlgorithm implements StructureLearningAlgorithm {
	private static final Logger logger = LogManager.getLogger(CTBNHybridStructureLearningAlgorithm.class);
	int maxSizeSepSet;
	CTBNScoreFunction scoreFunction;
	double sigTimeTransitionHyp;
	double sigStateToStateTransitionHyp;

	/**
	 * Initialises the hybrid structure learning algorithm receiving significance values, a score function and the
	 * maximum size of the separating sets.
	 *
	 * @param scoreFunction                       score function for the maximisation phase
	 * @param maxSizeSepSet                       maximum separating size for the restriction phase
	 * @param sigTimeTransitionHypothesis         significance level used for the null time to transition hypothesis in
	 *                                            the restriction phase
	 * @param sigStateToStateTransitionHypothesis significance level used for the null state-to-state transition
	 *                                            hypothesis in the restriction phase
	 */
	public CTBNHybridStructureLearningAlgorithm(CTBNScoreFunction scoreFunction, int maxSizeSepSet,
												double sigTimeTransitionHypothesis,
												double sigStateToStateTransitionHypothesis) {
		this.maxSizeSepSet = maxSizeSepSet;
		this.scoreFunction = scoreFunction;
		this.sigTimeTransitionHyp = sigTimeTransitionHypothesis;
		this.sigStateToStateTransitionHyp = sigStateToStateTransitionHypothesis;
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
		parametersAlgorithm.put("sigTimeTransitionHyp", String.valueOf(this.sigTimeTransitionHyp));
		parametersAlgorithm.put("sigStateToStateTransitionHyp", String.valueOf(this.sigStateToStateTransitionHyp));
		parametersAlgorithm.put("maxSizeSepSet", String.valueOf(this.maxSizeSepSet));
		return parametersAlgorithm;
	}

	@Override
	public void learn(PGM<? extends Node> pgm, List<Integer> idxNodes) {
		// TODO
	}

	@Override
	public void learn(PGM<? extends Node> pgm) throws ErroneousValueException {
		if (this.sigTimeTransitionHyp > 1 || this.sigTimeTransitionHyp < 0 || this.sigStateToStateTransitionHyp > 1 ||
				this.sigStateToStateTransitionHyp < 0) {
			throw new ErroneousValueException(
					"The significances must be in range [0, 1]. Values provided: " + this.sigTimeTransitionHyp +
							" (time to transitions) and " + this.sigStateToStateTransitionHyp +
							" (state to state transitions)");
		}
		// Retrieve indexes of feature variables and iterate over them
		List<Integer> idxFeatureVariables = getIdxFeatureVariables(pgm);
		boolean[][] adjacencyMatrix = restrictionPhase(pgm, idxFeatureVariables);
		pgm.setStructure(idxFeatureVariables, adjacencyMatrix);
		adjacencyMatrix = maximisationPhase(pgm, idxFeatureVariables, adjacencyMatrix);
		pgm.setStructure(adjacencyMatrix);
		pgm.learnParameters();
	}

	@Override
	public void learn(PGM<? extends Node> pgm, int idxNode) {
		// TODO

	}

	private List<Integer> getIdxFeatureVariables(PGM<? extends Node> pgm) {
		List<Integer> idxClassVariables = new ArrayList<>();
		for (int idxNode : pgm.getIndexNodes())
			if (!pgm.getNodeByIndex(idxNode).isClassVariable())
				idxClassVariables.add(idxNode);
		return idxClassVariables;
	}

	private boolean[][] maximisationPhase(PGM<? extends Node> pgm, List<Integer> idxFeatureVariables,
										  boolean[][] adjacencyMatrix) {
		logger.info("Performing the score-based phase to learn the structure of the bridge and feature subgraphs");
		CTBNHillClimbingHybridAlgorithm hillClimbing = new CTBNHillClimbingHybridAlgorithm(this.scoreFunction,
				adjacencyMatrix);
		HillClimbingSolution hs = hillClimbing.findStructure(pgm, idxFeatureVariables);
		return hs.getAdjacencyMatrix();
	}

	private boolean[][] restrictionPhase(PGM<? extends Node> pgm, List<Integer> idxFeatureVariables) {
		logger.info(
				"Performing the constraint-based phase to learn the structure of the bridge and feature subgraphs " +
						"using separating sets of maximum size {}", this.maxSizeSepSet);
		CTPCHybridAlgorithm ctpc = new CTPCHybridAlgorithm(this.maxSizeSepSet, this.sigTimeTransitionHyp,
				this.sigStateToStateTransitionHyp);
		// Find a structure using up to "maxSizeSepSet" nodes in the separating set
		return ctpc.learnInitialStructure(pgm, idxFeatureVariables);
	}

}