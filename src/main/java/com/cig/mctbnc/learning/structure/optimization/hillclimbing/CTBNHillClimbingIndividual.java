package com.cig.mctbnc.learning.structure.optimization.hillclimbing;

import java.util.Arrays;
import java.util.stream.IntStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cig.mctbnc.learning.structure.CTBNStructureLearningAlgorithm;
import com.cig.mctbnc.learning.structure.optimization.scores.ctbn.CTBNScoreFunction;
import com.cig.mctbnc.models.CTBN;
import com.cig.mctbnc.nodes.Node;

/**
 * Implements hill climbing algorithm for CTBNs.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class CTBNHillClimbingIndividual extends HillClimbing implements CTBNStructureLearningAlgorithm {
	CTBNScoreFunction scoreFunction;
	static Logger logger = LogManager.getLogger(CTBNHillClimbingIndividual.class);

	/**
	 * @param scoreFunction
	 */
	public CTBNHillClimbingIndividual(CTBNScoreFunction scoreFunction) {
		this.scoreFunction = scoreFunction;
	}

	@Override
	public boolean[][] findStructure() {
		// Store adjacency matrix of the best structure found
		boolean[][] bestAdjacencyMatrix = initialAdjacencyMatrix.clone();
		int numNodes = bestAdjacencyMatrix.length;
		// As a CTBN can have cycles, the parent set of each node is optimized
		// individually.
		IntStream.range(0, numNodes).parallel().forEach(indexNode -> {
			// Optimize parent set of the node
			boolean[][] bestAdjacencyMatrixNode = findStructureNode(indexNode, bestAdjacencyMatrix);
			// Save in the final adjacency matrix the parent set of the node
			for (int indexParentNode = 0; indexParentNode < numNodes; indexParentNode++) {
				bestAdjacencyMatrix[indexParentNode][indexNode] = bestAdjacencyMatrixNode[indexParentNode][indexNode];
			}
		});
		logger.debug("Best structure found: {}", (Object) bestAdjacencyMatrix);
		return bestAdjacencyMatrix;
	}

	private boolean[][] findStructureNode(int indexNode, boolean[][] bestAdjacencyMatrix) {
		Node node = pgm.getNodeByIndex(indexNode);
		// As this code is used to build a MCTBNC, there can be class variables. These
		// cannot have parents
		if (!node.isClassVariable()) {
			logger.info("Finding best parent set for node {}", node.getName());
			// Set as initial best score the one obtained with the initial structure
			double bestScore = computeScore(indexNode, initialAdjacencyMatrix);
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
			logger.trace("Score new structure: {}", bestScore);
		}
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
		for (int parentIndex = 0; parentIndex < numNodes; parentIndex++)
			if (indexNode != parentIndex) {
				// Define a temporal adjacency matrix to try a new structure
				boolean[][] tempAdjacencyMatrix = new boolean[numNodes][numNodes];
				for (int r = 0; r < numNodes; r++)
					tempAdjacencyMatrix[r] = adjacencyMatrix[r].clone();
				// Set node 'parentIndex' as parent of node 'indexNode' (or remove arc if
				// initial structure is supplied)
				tempAdjacencyMatrix[parentIndex][indexNode] = !tempAdjacencyMatrix[parentIndex][indexNode];
				// Check if the structure is legal
				if (pgm.isStructureLegal(tempAdjacencyMatrix)) {
					// Obtain score at the node 'indexNode'
					double obtainedScore = computeScore(indexNode, tempAdjacencyMatrix);
					if (obtainedScore > solution.getScore()) {
						// Set the obtained score and adjacency matrix as the best ones so far
						solution.setAdjacencyMatrix(tempAdjacencyMatrix);
						solution.setScore(obtainedScore);
					}
				}
			}
		return solution;
	}

	/**
	 * Compute the score at a certain node given an adjacency matrix. A cache is
	 * used to avoid recomputing a score.
	 * 
	 * @param indexNode
	 * @param adjacencyMatrix
	 * @param cache
	 * @return
	 */
	private double computeScore(int indexNode, boolean[][] adjacencyMatrix) {
		// Clone model to avoid inconsistencies between threads
		CTBN ctbn = new CTBN(((CTBN) pgm));
		// Establish the parent set of the node 'indexNode'
		ctbn.setStructure(indexNode, adjacencyMatrix);
		// Obtain local score at the node 'indexNode'
		return scoreFunction.compute(ctbn, indexNode);
	}
}
