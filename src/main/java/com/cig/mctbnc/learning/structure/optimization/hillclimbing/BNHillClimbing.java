package com.cig.mctbnc.learning.structure.optimization.hillclimbing;

import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import com.cig.mctbnc.learning.structure.BNStructureLearningAlgorithm;
import com.cig.mctbnc.learning.structure.optimization.scores.bn.BNScoreFunction;
import com.cig.mctbnc.models.BN;
import com.cig.mctbnc.nodes.Node;
import com.cig.mctbnc.util.Util;
import com.google.common.cache.Cache;

/**
 * Implements hill climbing algorithm for BNs.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class BNHillClimbing extends HillClimbing implements BNStructureLearningAlgorithm {
	BNScoreFunction scoreFunction;

	/**
	 * Constructor that receives the score function to optimize.
	 * 
	 * @param scoreFunction
	 */
	public BNHillClimbing(BNScoreFunction scoreFunction) {
		this.scoreFunction = scoreFunction;
	}

	@Override
	public boolean[][] findStructure() {
		boolean[][] bestStructure = initialAdjacencyMatrix.clone();
		double bestScore = Double.NEGATIVE_INFINITY;
		int numNodes = bestStructure.length;
		boolean improvement;
		do {
			improvement = false;
			// Store scores and respective structures for each operation (addition, deletion
			// and reversal)
			double[] scores = { Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY };
			boolean[][][] adjacencyMatrices = new boolean[3][numNodes][numNodes];
			// Structures generated by addition
			findBestNeighbor(bestStructure, scores, adjacencyMatrices, "addition");
			// Structures generated by deletion
			findBestNeighbor(bestStructure, scores, adjacencyMatrices, "deletion");
			// Structures generated by reversal
			findBestNeighbor(bestStructure, scores, adjacencyMatrices, "reversal");
			int idxBestOperation = Util.getIndexLargestValue(scores);
			double iterationBestScore = scores[idxBestOperation];
			logger.trace("Iteration best score {}", iterationBestScore);
			if (iterationBestScore > bestScore) {
				logger.debug("Score improved! From {} to {}", bestScore, iterationBestScore);
				logger.debug("New structure: {}", (Object) adjacencyMatrices[idxBestOperation]);
				bestScore = iterationBestScore;
				bestStructure = adjacencyMatrices[idxBestOperation];
				improvement = true;
			}
		} while (improvement);
		return bestStructure;
	}

	/**
	 * Find the best neighbor of the adjacency matrix "bestStructure" given an
	 * operation to perform.
	 * 
	 * @param bestStructure
	 * @param scores
	 * @param adjacencyMatrices
	 * @param cache
	 * @param operation         Possible operations to perform over the adjacency
	 *                          matrix. These are addition, deletion or reversal of
	 *                          arcs.
	 */
	private void findBestNeighbor(boolean[][] bestStructure, double[] scores, boolean[][][] adjacencyMatrices,
			String operation) {
		// Define the index of the operation. It will be used to store the score and
		// adjacency matrix
		// in the correct position of the arrays.
		int idxOperation = operation == "addition" ? 0 : operation == "deletion" ? 1 : 2;
		int numNodes = bestStructure.length;
		for (int i = 0; i < numNodes; i++) {
			for (int j = 0; j < numNodes; j++)
				if (i != j) {
					// Copy current best neighbor
					boolean[][] tempAdjacencyMatrix = new boolean[numNodes][numNodes];
					for (int r = 0; r < numNodes; r++)
						tempAdjacencyMatrix[r] = bestStructure[r].clone();
					if (operation == "addition") {
						if (tempAdjacencyMatrix[i][j])
							// If there is already an arc, the operation is not performed
							continue;
						// Arc is added
						tempAdjacencyMatrix[i][j] = true;
					} else if (operation == "deletion") {
						if (!tempAdjacencyMatrix[i][j])
							// If there is no arc, the operation cannot be performed
							continue;
						// Arc is removed
						tempAdjacencyMatrix[i][j] = false;
					} else {
						if (!tempAdjacencyMatrix[i][j])
							// If there is no arc, the operation cannot be performed
							continue;
						// Arc is reversed
						tempAdjacencyMatrix[i][j] = false;
						tempAdjacencyMatrix[j][i] = true;
					}
					if (pgm.isStructureLegal(tempAdjacencyMatrix)) {
						logger.trace("Studying new {} structure: {}", pgm.getType(), tempAdjacencyMatrix);
						// Retrieve score BN with the modified adjacency matrix
						double score = computeScore(tempAdjacencyMatrix);
						if (scores[idxOperation] < score) {
							scores[idxOperation] = score;
							adjacencyMatrices[idxOperation] = tempAdjacencyMatrix;
						}
					}
				}
		}
	}

	/**
	 * Compute the score of a structure given an adjacency matrix.
	 * 
	 * @param indexNode
	 * @param adjacencyMatrix
	 * @return
	 */
	private double computeScore(boolean[][] adjacencyMatrix) {
		// Establish the structure defined in an adjacency matrix and return its score
		pgm.setStructureModifiedNodes(adjacencyMatrix);
		return scoreFunction.compute((BN<? extends Node>) pgm);
	}

}
