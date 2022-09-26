package es.upm.fi.cig.multictbnc.learning.structure.optimisation.hillclimbing;

import es.upm.fi.cig.multictbnc.learning.structure.StructureLearningAlgorithm;
import es.upm.fi.cig.multictbnc.learning.structure.optimisation.hillclimbing.implementation.HillClimbingImplementation;
import es.upm.fi.cig.multictbnc.models.PGM;
import es.upm.fi.cig.multictbnc.nodes.Node;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implements common attributes and methods for hill climbing algorithms.
 *
 * @author Carlos Villa Blanco
 */
public abstract class HillClimbing implements StructureLearningAlgorithm {
	private static final Logger logger = LogManager.getLogger(HillClimbing.class);
	PGM<? extends Node> pgm;
	// Implementation of the hill climbing
	HillClimbingImplementation hcImplementation;

	/**
	 * Performs greedy Hill climbing to find a better structure than the initial one.
	 *
	 * @return found adjacency matrix
	 */
	public abstract boolean[][] findStructure();

	/**
	 * Performs greedy Hill climbing to find a better local structure for a given node.
	 *
	 * @param idxNode node index
	 * @return adjacency matrix found
	 */
	public abstract boolean[][] findStructure(int idxNode);

	/**
	 * Performs greedy Hill climbing to find a better local structure for some given nodes.
	 *
	 * @param idxNodes node indexes
	 * @return adjacency matrix found
	 */
	public abstract boolean[][] findStructure(List<Integer> idxNodes);

	@Override
	public String getIdentifier() {
		return hcImplementation.getIdentifier();
	}

	@Override
	public Map<String, String> getParametersAlgorithm() {
		return hcImplementation.getParametersAlgorithm();
	}

	@Override
	public void learn(PGM<? extends Node> pgm, List<Integer> idxNodes) {
		List<String> namesNodes = idxNodes.stream().map(idxNode -> pgm.getNodeByIndex(idxNode).getName()).collect(
				Collectors.toList());
		logger.info("Learning local structure of nodes {} from a {} using Hill Climbing", namesNodes, pgm.getType());
		// Define model
		this.pgm = pgm;
		// Optimise the structure. Obtain a better one than the initial
		boolean[][] structureFound = findStructure(idxNodes);
		pgm.setStructure(idxNodes, structureFound);
		pgm.learnParameters(idxNodes);
	}

	@Override
	public void learn(PGM<? extends Node> pgm) {
		// Define model
		this.pgm = pgm;
		// Optimise the structure. Obtain a better one than the initial
		boolean[][] structureFound = findStructure();
		pgm.setStructure(structureFound);
		pgm.learnParameters();
	}

	@Override
	public void learn(PGM<? extends Node> pgm, int idxNode) {
		// Define model
		this.pgm = pgm;
		// Optimise the structure. Obtain a better one than the initial
		boolean[][] structureFound = findStructure(idxNode);
		pgm.setStructure(idxNode, structureFound);
		pgm.learnParameters(idxNode);
	}

}