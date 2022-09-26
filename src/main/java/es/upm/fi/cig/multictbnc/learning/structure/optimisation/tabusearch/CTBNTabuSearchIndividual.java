package es.upm.fi.cig.multictbnc.learning.structure.optimisation.tabusearch;

import es.upm.fi.cig.multictbnc.learning.structure.optimisation.hillclimbing.HillClimbingSolution;
import es.upm.fi.cig.multictbnc.learning.structure.optimisation.hillclimbing.implementation.CTBNHillClimbingIndividual;
import es.upm.fi.cig.multictbnc.learning.structure.optimisation.scores.ctbn.CTBNScoreFunction;
import es.upm.fi.cig.multictbnc.models.CTBN;
import es.upm.fi.cig.multictbnc.nodes.Node;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Implements the tabu search algorithm for continuous-time Bayesian networks. It finds the parent set for a single
 * node, so it can be used to find the structure of a continuous-time Bayesian network in parallel.
 *
 * @author Carlos Villa Blanco
 */
public class CTBNTabuSearchIndividual extends CTBNHillClimbingIndividual {
	private static final Logger logger = LogManager.getLogger(CTBNTabuSearchIndividual.class);
	private int[][] tabuList;
	private int tabuListSize;

	/**
	 * Initialises the tabu search algorithm by proving a score function and a tabu list size.
	 *
	 * @param scoreFunction score function
	 * @param tabuListSize  tabu list size
	 */
	public CTBNTabuSearchIndividual(CTBNScoreFunction scoreFunction, int tabuListSize) {
		super(scoreFunction);
		this.tabuListSize = tabuListSize;
	}

	@Override
	public String getIdentifier() {
		return "Tabu search";
	}

	@Override
	public Map<String, String> getParametersAlgorithm() {
		Map<String, String> parametersAlgorithm = new HashMap<>(getInfoScoreFunction());
		parametersAlgorithm.put("tabuListSize", String.valueOf(this.tabuListSize));
		return parametersAlgorithm;
	}

	@Override
	protected HillClimbingSolution findBestNeighbor(CTBN<? extends Node> ctbn, int idxNode,
													boolean[][] adjacencyMatrix,
													Map<Long, Double> cache) {
		HillClimbingSolution solution = new HillClimbingSolution();
		int numNodes = adjacencyMatrix.length;
		// Find the best neighbour structure at node 'idxNode'
		for (int parentIndex = 0; parentIndex < numNodes; parentIndex++)
			if (idxNode != parentIndex && this.tabuList[parentIndex][idxNode] == 0) {
				// Define a temporal adjacency matrix to try a new structure
				boolean[][] tempAdjacencyMatrix = new boolean[numNodes][numNodes];
				for (int r = 0; r < numNodes; r++)
					tempAdjacencyMatrix[r] = adjacencyMatrix[r].clone();
				// Set node 'parentIndex' as parent of node 'idxNode' (or remove arc if
				// initial structure is supplied)
				tempAdjacencyMatrix[parentIndex][idxNode] = !tempAdjacencyMatrix[parentIndex][idxNode];
				this.increaseNumEdgesTested();
				// Check if the structure is legal
				if (ctbn.isStructureLegal(tempAdjacencyMatrix)) {
					// Obtain score at the node 'idxNode'
					double obtainedScore = computeScore(ctbn, idxNode, tempAdjacencyMatrix, cache);
					if (obtainedScore > solution.getScore()) {
						// Set the obtained score and adjacency matrix as the best ones so far
						solution.setAdjacencyMatrix(tempAdjacencyMatrix);
						solution.setScore(obtainedScore);
						solution.setArcModified(parentIndex, idxNode, 0);
					}
				}
			}
		return solution;
	}

	@Override
	protected boolean[][] findStructureNode(CTBN<? extends Node> ctbn, int idxNode, boolean[][] adjacencyMatrix) {
		Node node = ctbn.getNodeByIndex(idxNode);
		// Cache used to avoid the recomputation of the scores
		Map<Long, Double> cache = new HashMap<>();
		// Store best adjacency matrix found
		boolean[][] bestAdjacencyMatrix = adjacencyMatrix.clone();
		// Initialises tabu list
		this.tabuList = new int[bestAdjacencyMatrix.length][bestAdjacencyMatrix.length];
		// As this code is used to build a Multi-CTBNC, there can be class variables.
		// These cannot have parents
		if (!node.isClassVariable()) {
			logger.info("Finding best parent set for node {} from a {} using Tabu Search with list size of {}",
					node.getName(), ctbn.getType(), tabuListSize);
			// Set as the initial best score the one obtained with the initial structure
			double bestScore = computeScore(ctbn, idxNode, adjacencyMatrix, cache);
			// Try to improve the current structure
			boolean improvement;
			do {
				// Store adjacency matrix of the current iteration
				boolean[][] iterationAdjacencyMatrix = bestAdjacencyMatrix.clone();
				HillClimbingSolution bestNeighbor = findBestNeighbor(ctbn, idxNode, iterationAdjacencyMatrix, cache);
				improvement = bestNeighbor.getScore() > bestScore;
				if (improvement) {
					bestScore = bestNeighbor.getScore();
					bestAdjacencyMatrix = bestNeighbor.getAdjacencyMatrix();
					// Reduce the number of iterations for prohibited operations
					reduceExpirationPoint();
					// Add to the tabu list the last operation
					int idxParentNode = bestNeighbor.getModifiedArcs()[0][0];
					this.tabuList[idxParentNode][idxNode] = this.tabuListSize;
				}
			} while (improvement);
			logger.debug("New structure: {}", (Object) bestAdjacencyMatrix);
			logger.trace("Score new structure: {}", bestScore);
		}
		return bestAdjacencyMatrix;
	}

	private void reduceExpirationPoint() {
		for (int i = 0; i < this.tabuList.length; i++) {
			for (int j = 0; j < this.tabuList.length; j++) {
				if (this.tabuList[i][j] > 0)
					this.tabuList[i][j]--;
			}
		}
	}

}