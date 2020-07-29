package com.cig.mctbnc.learning.structure;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.learning.parameters.ParameterLearningAlgorithm;
import com.cig.mctbnc.models.PGM;
import com.cig.mctbnc.nodes.Node;
import com.cig.mctbnc.util.Util;

public class HillClimbing implements StructureLearningAlgorithm {
	PGM pgm;
	List<Node> nodes;
	Dataset trainingDataset;
	ParameterLearningAlgorithm parameterLearning;
	boolean[][] initialAdjacencyMatrix;
	static Logger logger = LogManager.getLogger(HillClimbing.class);
	
	@Override
	public void learn(PGM pgm, Dataset trainingDataset, ParameterLearningAlgorithm bnParameterLearning) {
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
		// Obtain best neighbor
		boolean[][] bestStructure = findStructure();
		pgm.setStructure(bestStructure);
	}

	/**
	 * Performs greedy Hill climbing to find a better structure from the initial
	 * one.
	 * 
	 * @return Adjacency matrix of the found structure.
	 */
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
			if (iterationBestScore > bestScore) {
				logger.debug("Score improved! From {} to {}", bestScore, iterationBestScore);
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
	 * @param operation
	 *            Possible operations to perform over the adjacency matrix. These
	 *            are addition, deletion or reversal of arcs.
	 */
	private void findBestNeighbor(boolean[][] bestStructure, double[] scores, boolean[][][] adjacencyMatrices,
			String operation) {

		// Define the index of the operation. It will be used to store the score and
		// adjacency matrix
		// in the correct position of the arrays.
		int idxOperation = operation == "addition" ? 0 : operation == "deletion" ? 1 : 2;
		int numNodes = bestStructure.length;
		for (int i = 0; i < numNodes; i++) {
			for (int j = 0; j < numNodes; j++) {
				if (i != j) {
					logger.trace("Studying new structure", pgm.getType());
					// Copy current best neighbor
					boolean[][] tempAdjacencyMatrix = new boolean[numNodes][numNodes];
					for (int r = 0; r < numNodes; r++) {
						tempAdjacencyMatrix[r] = bestStructure[r].clone();
					}

					if (operation == "addition") {
						// Arc is added
						tempAdjacencyMatrix[i][j] = true;
					} else if (operation == "deletion") {
						// Arc is removed
						tempAdjacencyMatrix[i][j] = false;
					} else {
						// Arc is reversed
						tempAdjacencyMatrix[i][j] = false;
						tempAdjacencyMatrix[j][i] = true;
					}

					if (pgm.isStructureLegal(tempAdjacencyMatrix)) {
						// Define Bayesian network with the modified adjacency matrix
						pgm.setStructure(tempAdjacencyMatrix);
						double obtainedScore = StructureScoreFunctions.penalizedLogLikelihoodScore(pgm);

						if (scores[idxOperation] < obtainedScore) {
							scores[idxOperation] = obtainedScore;
							adjacencyMatrices[idxOperation] = tempAdjacencyMatrix;
						}
					}
				}
			}
		}
	}
}
