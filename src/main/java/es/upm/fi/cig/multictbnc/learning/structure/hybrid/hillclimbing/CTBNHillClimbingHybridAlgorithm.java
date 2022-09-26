package es.upm.fi.cig.multictbnc.learning.structure.hybrid.hillclimbing;

import es.upm.fi.cig.multictbnc.learning.structure.optimisation.hillclimbing.HillClimbingSolution;
import es.upm.fi.cig.multictbnc.learning.structure.optimisation.hillclimbing.implementation.CTBNHillClimbingIndividual;
import es.upm.fi.cig.multictbnc.learning.structure.optimisation.scores.ctbn.CTBNScoreFunction;
import es.upm.fi.cig.multictbnc.models.CTBN;
import es.upm.fi.cig.multictbnc.nodes.Node;

import java.util.Map;

/**
 * Implements the maximisation phase (hill climbing algorithm) of the hybrid structure learning algorithm for
 * continuous-time Bayesian networks.
 *
 * @author Carlos Villa Blanco
 */
public class CTBNHillClimbingHybridAlgorithm extends CTBNHillClimbingIndividual {
	boolean[][] constraintAdjacencyMatrix;

	/**
	 * Initialises the algorithm by proving a score function and an initial adjacency matrix for the continuous-time
	 * Bayesian network.
	 *
	 * @param scoreFunction          score function
	 * @param initialAdjacencyMatrix adjacency matrix of the initial structure
	 */
	public CTBNHillClimbingHybridAlgorithm(CTBNScoreFunction scoreFunction, boolean[][] initialAdjacencyMatrix) {
		super(scoreFunction);
		this.constraintAdjacencyMatrix = initialAdjacencyMatrix;
	}

	@Override
	protected HillClimbingSolution findBestNeighbor(CTBN<? extends Node> ctbn, int idxNode,
													boolean[][] adjacencyMatrix,
													Map<Long, Double> cache) {
		HillClimbingSolution solution = new HillClimbingSolution();
		int numNodes = adjacencyMatrix.length;
		// Find the best neighbour structure at node 'idxNode'
		for (int parentIndex = 0; parentIndex < numNodes; parentIndex++)
			if (idxNode != parentIndex) {
				// Define a temporal adjacency matrix to try a new structure
				boolean[][] tempAdjacencyMatrix = new boolean[numNodes][numNodes];
				for (int r = 0; r < numNodes; r++)
					tempAdjacencyMatrix[r] = adjacencyMatrix[r].clone();
				// If there is an arc, try to remove it
				if (tempAdjacencyMatrix[parentIndex][idxNode])
					tempAdjacencyMatrix[parentIndex][idxNode] = false;
					// If there is no arc, but the constraint algorithm left it, try to add it
				else if (this.constraintAdjacencyMatrix[parentIndex][idxNode])
					tempAdjacencyMatrix[parentIndex][idxNode] = true;
				else
					continue;
				// Check if the structure is legal
				if (ctbn.isStructureLegal(tempAdjacencyMatrix)) {
					// Obtain score at the node 'idxNode'
					double obtainedScore = computeScore(ctbn, idxNode, tempAdjacencyMatrix, cache);
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