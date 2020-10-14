package com.cig.mctbnc.learning.structure.optimization.hillclimbing;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.learning.parameters.ParameterLearningAlgorithm;
import com.cig.mctbnc.learning.structure.StructureLearningAlgorithm;
import com.cig.mctbnc.learning.structure.constraints.StructureConstraints;
import com.cig.mctbnc.learning.structure.optimization.CTBNScoreFunction;
import com.cig.mctbnc.models.PGM;
import com.cig.mctbnc.nodes.Node;

/**
 * Implements common attributes and methods for hill climbing algorithms.
 * 
 * @author Carlos Villa Blanco
 *
 */
public abstract class HillClimbing implements StructureLearningAlgorithm {
	PGM<? extends Node> pgm;
	List<? extends Node> nodes;
	Dataset trainingDataset;
	ParameterLearningAlgorithm parameterLearning;
	StructureConstraints structureConstraints;
	boolean[][] initialAdjacencyMatrix;
	static Logger logger = LogManager.getLogger(HillClimbing.class);

	@Override
	public void learn(PGM<? extends Node> pgm, Dataset trainingDataset, ParameterLearningAlgorithm bnParameterLearning,
			StructureConstraints structureConstraints) {
		logger.info("Learning {} using Hill Climbing", pgm.getType());
		// Define model
		this.pgm = pgm;
		// Define nodes of the PGM
		this.nodes = pgm.getNodes();
		// Define parameter learning algorithm
		this.parameterLearning = bnParameterLearning;
		// Define the structure constraints
		this.structureConstraints = structureConstraints;
		// Get initial structure
		this.initialAdjacencyMatrix = pgm.getAdjacencyMatrix();
		// Define training dataset
		this.trainingDataset = trainingDataset;
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

}
