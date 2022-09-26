package es.upm.fi.cig.multictbnc.learning.structure.hybrid.hillclimbing;

import es.upm.fi.cig.multictbnc.learning.structure.optimisation.hillclimbing.HillClimbingSolution;
import es.upm.fi.cig.multictbnc.learning.structure.optimisation.hillclimbing.implementation.BNHillClimbing;
import es.upm.fi.cig.multictbnc.learning.structure.optimisation.scores.bn.BNScoreFunction;
import es.upm.fi.cig.multictbnc.models.BN;
import es.upm.fi.cig.multictbnc.nodes.Node;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Implements the maximisation phase (hill climbing algorithm) of the hybrid structure learning algorithm for Bayesian
 * networks.
 *
 * @author Carlos Villa Blanco
 */
public class BNHillClimbingHybridAlgorithm extends BNHillClimbing {
	private static final Logger logger = LogManager.getLogger(BNHillClimbingHybridAlgorithm.class);
	boolean[][] skeletonAdjacencyMatrix;

	/**
	 * Initialises the algorithm by proving a score function and a skeleton of the Bayesian network.
	 *
	 * @param scoreFunction           score function
	 * @param skeletonAdjacencyMatrix adjacency matrix of the Bayesian network skeleton
	 */
	public BNHillClimbingHybridAlgorithm(BNScoreFunction scoreFunction, boolean[][] skeletonAdjacencyMatrix) {
		super(scoreFunction);
		this.skeletonAdjacencyMatrix = skeletonAdjacencyMatrix;
	}

	@Override
	protected void findBestNeighbor(BN<? extends Node> bn, HillClimbingSolution bestSolution, double[] scores,
									boolean[][][] adjacencyMatrices, String operation) {
		// Define the index of the operation. It will be used to store the score and
		// adjacency matrix
		// in the correct position of the arrays.
		int idxOperation = operation.equals("addition") ? 0 : operation.equals("deletion") ? 1 : 2;
		boolean[][] bestStructure = bestSolution.getAdjacencyMatrix();
		int numNodes = bestStructure.length;
		for (int i = 0; i < numNodes; i++) {
			for (int j = 0; j < numNodes; j++)
				if (i != j) {
					// Copy current best neighbour
					boolean[][] tempAdjacencyMatrix = new boolean[numNodes][numNodes];
					for (int r = 0; r < numNodes; r++)
						tempAdjacencyMatrix[r] = bestStructure[r].clone();
					// Only add the arc if the constraint algorithm found it
					if (operation == "addition" && this.skeletonAdjacencyMatrix[i][j]) {
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
						// Only reverse the arc if the constraint algorithm found the reversed arc
						if (this.skeletonAdjacencyMatrix[j][i]) {
							if (!tempAdjacencyMatrix[i][j])
								// If there is no arc, the operation cannot be performed
								continue;
							// Arc is reversed
							tempAdjacencyMatrix[i][j] = false;
							tempAdjacencyMatrix[j][i] = true;
						}
					}
					if (bn.isStructureLegal(tempAdjacencyMatrix)) {
						logger.trace("Studying new {} structure: {}", bn.getType(), tempAdjacencyMatrix);
						// Retrieve score BN with the modified adjacency matrix
						double score = computeScore(bn, tempAdjacencyMatrix);
						if (scores[idxOperation] < score) {
							scores[idxOperation] = score;
							adjacencyMatrices[idxOperation] = tempAdjacencyMatrix;
						}
					}
				}
		}
	}

}