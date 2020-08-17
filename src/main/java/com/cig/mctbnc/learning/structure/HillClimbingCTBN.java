package com.cig.mctbnc.learning.structure;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cig.mctbnc.models.CTBN;
import com.cig.mctbnc.nodes.Node;

/**
 * Implements hill climbing algorithm for CTBNs.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class HillClimbingCTBN extends HillClimbing {
	static Logger logger = LogManager.getLogger(HillClimbingCTBN.class);

	public boolean[][] findStructure() {
		// Store adjacency matrix of the best structure found
		boolean[][] bestAdjacencyMatrix = initialAdjacencyMatrix.clone();
		int numNodes = bestAdjacencyMatrix.length;

		// A CTBN can have cycles, so the parent set of each node is optimized
		// individually
		for (int indexNode = 0; indexNode < numNodes; indexNode++) {
			Node node = pgm.getNodeByIndex(indexNode);
			// As this code is used to build a MCTBNC, there can be class variables. These
			// cannot have parents
			if (!node.isClassVariable()) {
				logger.info("Finding best parent set for node {}", node.getName());

				// Store score of the best neighbor structure for the node
				double bestScore = Double.NEGATIVE_INFINITY;
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
				for (int r = 0; r < numNodes; r++) {
					tempAdjacencyMatrix[r] = adjacencyMatrix[r].clone();
				}
				// Set the node 'parentIndex' as parent of the node 'indexNode'
				tempAdjacencyMatrix[parentIndex][indexNode] = true;
				((CTBN) pgm).setStructure(indexNode, tempAdjacencyMatrix);
				// Obtain the local log-likelihood of the node 'indexNode'
				double obtainedScore = StructureScoreFunctions.logLikelihoodScore(((CTBN) pgm), indexNode,
						structureConstraints.getPenalizationFunction());
				if (obtainedScore > solution.getScore()) {
					// Set the obtained score and adjacency matrix as the best ones so far
					solution.setAdjacencyMatrix(tempAdjacencyMatrix);
					solution.setScore(obtainedScore);
				}
			}
		}
		return solution;
	}

}
