package com.cig.mctbnc.learning.structure.optimization.hillclimbing;

import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cig.mctbnc.learning.structure.CTBNStructureLearningAlgorithm;
import com.cig.mctbnc.learning.structure.optimization.CTBNScoreFunction;
import com.cig.mctbnc.models.CTBN;
import com.cig.mctbnc.nodes.Node;
import com.cig.mctbnc.util.Util;
import com.google.common.cache.Cache;

/**
 * Implements hill climbing algorithm for CTBNs.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class CTBNHillClimbing extends HillClimbing implements CTBNStructureLearningAlgorithm {
	CTBNScoreFunction scoreFunction;
	static Logger logger = LogManager.getLogger(CTBNHillClimbing.class);

	/**
	 * @param scoreFunction
	 */
	public CTBNHillClimbing(CTBNScoreFunction scoreFunction) {
		this.scoreFunction = scoreFunction;
	}

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
			// A cache is used to avoid recomputing the scores of structures
			Cache<Integer, Double> cache = Util.createCache(50);
			// Set as initial best score the one obtained with the initial structure
			double bestScore = setStructure(indexNode, initialAdjacencyMatrix);
			// Try to improve the current structure
			boolean improvement = false;
			do {
				// Store adjacency matrix of the current iteration
				boolean[][] iterationAdjacencyMatrix = bestAdjacencyMatrix.clone();
				HillClimbingSolution bestNeighbor = findBestNeighbor(indexNode, iterationAdjacencyMatrix, cache);
				improvement = bestNeighbor.getScore() > bestScore;
				if (improvement) {
					bestScore = bestNeighbor.getScore();
					bestAdjacencyMatrix = bestNeighbor.getAdjacencyMatrix();
				}
			} while (improvement);
			logger.debug("New structure: {}", (Object) bestAdjacencyMatrix);
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
	private HillClimbingSolution findBestNeighbor(int indexNode, boolean[][] adjacencyMatrix,
			Cache<Integer, Double> cache) {
		HillClimbingSolution solution = new HillClimbingSolution();
		int numNodes = adjacencyMatrix.length;
		// Find the best neighbor structure at node 'indexNode'
		for (int parentIndex = 0; parentIndex < numNodes; parentIndex++)
			if (indexNode != parentIndex) {// && !adjacencyMatrix[parentIndex][indexNode]) {
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
					double obtainedScore = computeScore(indexNode, tempAdjacencyMatrix, cache);
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
	private double computeScore(int indexNode, boolean[][] adjacencyMatrix, Cache<Integer, Double> cache) {
		double obtainedScore = 0;
		try {
			// If the structure was already evaluated, its score is obtained from the cache.
			// Otherwise, the score is saved. the adjacency matrix hashcode is used as key.
			int hashCodeAdjacencyMatrix = Arrays.deepHashCode(adjacencyMatrix);
			obtainedScore = cache.get(hashCodeAdjacencyMatrix, new Callable<Double>() {
				@Override
				public Double call() {
					// Set structure and obtain local score at the node 'indexNode'
					return setStructure(indexNode, adjacencyMatrix);
				}
			});
		} catch (ExecutionException e) {
			logger.error("An error occured using the cache");
		}
		return obtainedScore;
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
		// Clone model to avoid inconsistencies between threads
		CTBN ctbn = new CTBN(((CTBN) pgm));
		// Establish the parent set of the node 'indexNode'
		ctbn.setStructure(indexNode, adjacencyMatrix);
		// Obtain local score at the node 'indexNode'
		return scoreFunction.compute(ctbn, indexNode);
	}

}
