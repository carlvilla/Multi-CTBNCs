package es.upm.fi.cig.multictbnc.learning.structure.optimisation.tabusearch;

import es.upm.fi.cig.multictbnc.learning.structure.optimisation.hillclimbing.HillClimbingSolution;
import es.upm.fi.cig.multictbnc.learning.structure.optimisation.hillclimbing.implementation.BNHillClimbing;
import es.upm.fi.cig.multictbnc.learning.structure.optimisation.scores.bn.BNScoreFunction;
import es.upm.fi.cig.multictbnc.models.BN;
import es.upm.fi.cig.multictbnc.models.PGM;
import es.upm.fi.cig.multictbnc.nodes.Node;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Implements the tabu search algorithm for Bayesian networks.
 *
 * @author Carlos Villa Blanco
 */
public class BNTabuSearch extends BNHillClimbing {
	private static final Logger logger = LogManager.getLogger(BNTabuSearch.class);
	int tabuListSize;
	private int[][][] tabuList;

	/**
	 * Initialises the tabu search algorithm by proving a score function and a tabu list size.
	 *
	 * @param scoreFunction score function
	 * @param tabuListSize  tabu list size
	 */
	public BNTabuSearch(BNScoreFunction scoreFunction, int tabuListSize) {
		super(scoreFunction);
		this.tabuListSize = tabuListSize;
	}

	@Override
	public HillClimbingSolution findStructure(PGM<? extends Node> pgm) {
		logger.info("Learning {} using Tabu Search with list size of {}", pgm.getType(), tabuListSize);
		int numNodes = pgm.getNumNodes();
		this.tabuList = new int[numNodes][numNodes][3];
		return super.findStructure(pgm);
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
					if (operation.equals("addition")) {
						if (tempAdjacencyMatrix[i][j] || this.tabuList[i][j][1] > 0)
							// Skip if there is already an arc or the operation is not allowed by tabu list
							continue;
						// Arc is added
						tempAdjacencyMatrix[i][j] = true;
						this.increaseNumEdgesTested();
					} else if (operation.equals("deletion")) {
						if (!tempAdjacencyMatrix[i][j] || this.tabuList[i][j][0] > 0)
							// If there is no arc, the operation cannot be performed
							continue;
						// Arc is removed
						tempAdjacencyMatrix[i][j] = false;
						this.increaseNumEdgesTested();
					} else {
						if (!tempAdjacencyMatrix[i][j] || this.tabuList[i][j][2] > 0)
							// If there is no arc, the operation cannot be performed
							continue;
						// Arc is reversed
						tempAdjacencyMatrix[i][j] = false;
						tempAdjacencyMatrix[j][i] = true;
						this.increaseNumEdgesTested();
					}
					if (bn.isStructureLegal(tempAdjacencyMatrix)) {
						logger.trace("Studying new {} structure: {}", bn.getType(), tempAdjacencyMatrix);
						// Retrieve score BN with the modified adjacency matrix
						double score = computeScore(bn, tempAdjacencyMatrix);
						if (scores[idxOperation] < score) {
							scores[idxOperation] = score;
							adjacencyMatrices[idxOperation] = tempAdjacencyMatrix;
							bestSolution.setArcModified(i, j, idxOperation);
						}
					}
				}
		}
	}

	@Override
	protected boolean isScoreImproved(HillClimbingSolution solution, boolean[][][] adjacencyMatrices,
									  int idxBestOperation, double iterationBestScore) {
		if (iterationBestScore > solution.getScore()) {
			logger.debug("Score improved! From {} to {}", solution.getScore(), iterationBestScore);
			logger.debug("New structure: {}", (Object) adjacencyMatrices[idxBestOperation]);
			solution.setAdjacencyMatrix(adjacencyMatrices[idxBestOperation]);
			solution.setScore(iterationBestScore);
			// Reduce the number of iterations for prohibited operations
			reduceExpirationPoint();
			// Add to the tabu list the last operation
			int idxParentNode = solution.getModifiedArcs()[idxBestOperation][0];
			int idxChildNode = solution.getModifiedArcs()[idxBestOperation][1];
			// If the best operation was reversing an arc
			if (idxBestOperation == 2)
				this.tabuList[idxChildNode][idxParentNode][idxBestOperation] = this.tabuListSize;
			else
				this.tabuList[idxParentNode][idxChildNode][idxBestOperation] = this.tabuListSize;
			return true;
		}
		return false;
	}

	private void reduceExpirationPoint() {
		for (int i = 0; i < this.tabuList.length; i++) {
			for (int j = 0; j < this.tabuList.length; j++) {
				for (int k = 0; k < this.tabuList[0][0].length; k++) {
					if (this.tabuList[i][j][k] > 0)
						this.tabuList[i][j][k]--;
				}
			}
		}
	}

}