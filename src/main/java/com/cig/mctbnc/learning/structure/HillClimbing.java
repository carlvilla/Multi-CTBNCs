package com.cig.mctbnc.learning.structure;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.learning.parameters.ParameterLearningAlgorithm;
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
	boolean[][] initialAdjacencyMatrix;
	String penalizationFunction;
	static Logger logger = LogManager.getLogger(HillClimbing.class);

	@Override
	public void learn(PGM<? extends Node> pgm, Dataset trainingDataset, ParameterLearningAlgorithm bnParameterLearning,
			String penalizationFunction) {
		logger.info("Learning {} using Hill Climbing", pgm.getType());
		// Define model
		this.pgm = pgm;
		// Define nodes of the PGM
		this.nodes = pgm.getNodes();
		// Define parameter learning algorithm
		this.parameterLearning = bnParameterLearning;
		// Get initial structure
		this.initialAdjacencyMatrix = pgm.getAdjacencyMatrix();
		// Define training dataset
		this.trainingDataset = trainingDataset;
		// Define if the structure complexity has to be penalized
		this.penalizationFunction = penalizationFunction;
		// Obtain best neighbor
		boolean[][] bestStructure = findStructure();
		pgm.setStructure(bestStructure);
	}

	/**
	 * Performs greedy Hill climbing to find a better structure than the initial
	 * one.
	 * 
	 * @return found adjacency matrix
	 */
	public abstract boolean[][] findStructure();
}
