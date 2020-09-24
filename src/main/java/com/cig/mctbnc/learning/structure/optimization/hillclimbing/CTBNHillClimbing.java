package com.cig.mctbnc.learning.structure.optimization.hillclimbing;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cig.mctbnc.learning.structure.CTBNStructureLearningAlgorithm;
import com.cig.mctbnc.learning.structure.optimization.StructureScoreFunctions;
import com.cig.mctbnc.models.CTBN;
import com.cig.mctbnc.nodes.Node;

/**
 * Implements hill climbing algorithm for CTBNs.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class CTBNHillClimbing extends HillClimbing implements CTBNStructureLearningAlgorithm {
	static Logger logger = LogManager.getLogger(CTBNHillClimbing.class);

	public CTBNHillClimbing(String scoreFunction) {
		super(scoreFunction);
	}

	public boolean[][] findStructure() {
		// Store adjacency matrix of the best structure found
		boolean[][] bestAdjacencyMatrix = initialAdjacencyMatrix.clone();
		int numNodes = bestAdjacencyMatrix.length;
		// As a CTBN can have cycles, the parent set of each node is optimized
		// individually
		for (int indexNode = 0; indexNode < numNodes; indexNode++) {
			Node node = pgm.getNodeByIndex(indexNode);
			// As this code is used to build a MCTBNC, there can be class variables. These
			// cannot have parents
			if (!node.isClassVariable()) {
				logger.info("Finding best parent set for node {}", node.getName());
				// Set as initial best score the one obtained with the initial structure
				double bestScore = setStructure(indexNode, initialAdjacencyMatrix);
				// Try to improve the current structure
				boolean improvement = false;
				do {
					// Store adjacency matrix of the current iteration
					boolean[][] iterationAdjacencyMatrix = bestAdjacencyMatrix.clone();
					HillClimbingSolution bestNeighbor = findBestNeighbor(indexNode, iterationAdjacencyMatrix);
					improvement = bestNeighbor.getScore() > bestScore;
					if (improvement) {
						bestScore = bestNeighbor.getScore();
						bestAdjacencyMatrix = bestNeighbor.getAdjacencyMatrix();
					}
				} while (improvement);
				logger.debug("New structure: {}", (Object) bestAdjacencyMatrix);
			}
		}
		logger.debug("Best structure found: {}", (Object) bestAdjacencyMatrix);
		return bestAdjacencyMatrix;
	}

	/**
	 * Find the best neighbor for a CTBN node. It returns the score of the neighbor,
	 * while the obtained adjacency matrix is stored in the parameter
	 * 'adjacencyMatrix'.
	 * 
	 * @param indexNode
	 * @param adjacencyMatrix
	 * @return
	 */
	private HillClimbingSolution findBestNeighbor(int indexNode, boolean[][] adjacencyMatrix) {
		HillClimbingSolution solution = new HillClimbingSolution();
		int numNodes = adjacencyMatrix.length;
		// Find the best neighbor structure at node 'indexNode'
		for (int parentIndex = 0; parentIndex < numNodes; parentIndex++) {
			if (indexNode != parentIndex && !adjacencyMatrix[parentIndex][indexNode]) {
				// Define a temporal adjacency matrix to try a new structure
				boolean[][] tempAdjacencyMatrix = new boolean[numNodes][numNodes];
				for (int r = 0; r < numNodes; r++)
					tempAdjacencyMatrix[r] = adjacencyMatrix[r].clone();
				// Set node 'parentIndex' as parent of node 'indexNode' (or remove arc if
				// initial structure is supplied)
				tempAdjacencyMatrix[parentIndex][indexNode] = !tempAdjacencyMatrix[parentIndex][indexNode];
				// Check if the structure is legal
				if (pgm.isStructureLegal(tempAdjacencyMatrix)) {
					// Set structure and obtain the local log-likelihood at the node 'indexNode'
					double obtainedScore = setStructure(indexNode, tempAdjacencyMatrix);
					if (obtainedScore > solution.getScore()) {
						// Set the obtained score and adjacency matrix as the best ones so far
						solution.setAdjacencyMatrix(tempAdjacencyMatrix);
						solution.setScore(obtainedScore);
					}
				}
			}
		}
		return solution;
	}

	/**
	 * Establish for the indicated node (in the CTBN) the parent set defined in an
	 * adjacency matrix and return the score for the new structure.
	 * 
	 * @param indexNode
	 * @param adjacencyMatrix
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private double setStructure(int indexNode, boolean[][] adjacencyMatrix) {
		// Establish the parent set of the node
		((CTBN) pgm).setStructure(indexNode, adjacencyMatrix);
		// Obtain the local log-likelihood at the node
		double score;
		if (scoreFunction.equals("Log-likelihood")) {
			score = StructureScoreFunctions.logLikelihoodScore(((CTBN) pgm), indexNode,
					structureConstraints.getPenalizationFunction());
		} else {
			score = StructureScoreFunctions.conditionalLogLikelihoodScore(((CTBN) pgm), indexNode,
					structureConstraints.getPenalizationFunction());
		}
		return score;
	}

}
