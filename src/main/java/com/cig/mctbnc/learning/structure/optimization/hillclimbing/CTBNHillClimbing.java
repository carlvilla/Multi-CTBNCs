package com.cig.mctbnc.learning.structure.optimization.hillclimbing;

import com.cig.mctbnc.learning.structure.CTBNStructureLearningAlgorithm;
import com.cig.mctbnc.learning.structure.optimization.scores.ctbn.CTBNScoreFunction;
import com.cig.mctbnc.models.CTBN;
import com.cig.mctbnc.nodes.Node;
import com.cig.mctbnc.util.Util;

/**
 * Implements hill climbing algorithm for CTBNs. This class is used with scores
 * that cannot be optimized by finding the best parent set for each node
 * individually. This is the case of, for example, the conditional
 * log-likelihood.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class CTBNHillClimbing extends HillClimbing implements CTBNStructureLearningAlgorithm {
	CTBNScoreFunction scoreFunction;

	/**
	 * Constructor that receives the score function to optimize.
	 * 
	 * @param scoreFunction
	 */
	public CTBNHillClimbing(CTBNScoreFunction scoreFunction) {
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
	 * @param operation         Possible operations to perform over the adjacency
	 *                          matrix. These are addition, deletion or reversal of
	 *                          arcs.
	 * @param cache
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
				if (checkModificationAdjacencyMatrix(i, j, idxOperation)) {
					// Copy current best neighbor
					boolean[][] tempAdjacencyMatrix = new boolean[bestStructure.length][];
					for (int r = 0; r < bestStructure.length; r++)
						tempAdjacencyMatrix[r] = bestStructure[r].clone();
					if (operation == "addition") {
						// An arc is added
						if (tempAdjacencyMatrix[i][j])
							// If there is already an arc, the operation is not performed
							continue;
						// Arc is added
						tempAdjacencyMatrix[i][j] = true;
					} else if (operation == "deletion") {
						// An arc is removed
						if (!tempAdjacencyMatrix[i][j])
							// If there is no arc, the operation cannot be performed
							continue;
						// Arc is removed
						tempAdjacencyMatrix[i][j] = false;
					} else {
						// An arc is reversed
						if (!tempAdjacencyMatrix[i][j] || pgm.getNodeByIndex(i).isClassVariable())
							// If there is no arc, the operation cannot be performed. An arc from a class
							// variable to a feature cannot be reversed
							continue;
						// Arc is reversed
						tempAdjacencyMatrix[i][j] = false;
						tempAdjacencyMatrix[j][i] = true;
					}
					if (pgm.isStructureLegal(tempAdjacencyMatrix)) {
						logger.trace("Studying new {} structure: {}", pgm.getType(), tempAdjacencyMatrix);
						// Retrieve score CTBN with the modified adjacency matrix
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
	 * Check if an arc between two nodes can be added. Arcs from any node to a class
	 * variable cannot be added, removed or reversed, and self-loops are also
	 * avoided.
	 * 
	 * @param i
	 * @param j
	 * @param idxOperation
	 * @return
	 */
	private boolean checkModificationAdjacencyMatrix(int i, int j, int idxOperation) {
		return i != j && !pgm.getNodeByIndex(j).isClassVariable();
	}

	/**
	 * Compute the score of a structure given an adjacency matrix.
	 * 
	 * @param adjacencyMatrix
	 * @param modifiedNodes
	 * @return
	 */
	private double computeScore(boolean[][] adjacencyMatrix) {
		// Set structure and obtain its score
		pgm.setStructureModifiedNodes(adjacencyMatrix);
		return scoreFunction.compute((CTBN<? extends Node>) pgm);
	}

}
