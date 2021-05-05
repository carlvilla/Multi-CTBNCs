package es.upm.fi.cig.mctbnc.learning.structure.optimization.hillclimbing;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.upm.fi.cig.mctbnc.learning.structure.StructureLearningAlgorithm;
import es.upm.fi.cig.mctbnc.learning.structure.optimization.hillclimbing.implementation.HillClimbingImplementation;
import es.upm.fi.cig.mctbnc.models.PGM;
import es.upm.fi.cig.mctbnc.nodes.Node;

/**
 * Implements common attributes and methods for hill climbing algorithms.
 * 
 * @author Carlos Villa Blanco
 *
 */
public abstract class HillClimbing implements StructureLearningAlgorithm {
	PGM<? extends Node> pgm;
	// Implementation of the hill climbing
	HillClimbingImplementation hcImplementation;
	static Logger logger = LogManager.getLogger(HillClimbing.class);

	@Override
	public void learn(PGM<? extends Node> pgm) {
		logger.info("Learning {} using Hill Climbing", pgm.getType());
		// Define model
		this.pgm = pgm;
		// Optimize structure. Obtain a better one than the initial
		boolean[][] structureFound = findStructure();
		pgm.setStructure(structureFound);
	}

	/**
	 * Performs greedy Hill climbing to find a better structure than the initial
	 * one.
	 * 
	 * @return found adjacency matrix
	 */
	public abstract boolean[][] findStructure();

	@Override
	public String getIdentifier() {
		return "Hill climbing";
	}

	@Override
	public Map<String, String> getParametersAlgorithm() {
		return hcImplementation.getInfoScoreFunction();
	}

}
