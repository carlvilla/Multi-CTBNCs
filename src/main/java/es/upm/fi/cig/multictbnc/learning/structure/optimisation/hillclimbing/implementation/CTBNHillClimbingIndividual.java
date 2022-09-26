package es.upm.fi.cig.multictbnc.learning.structure.optimisation.hillclimbing.implementation;

import es.upm.fi.cig.multictbnc.learning.structure.optimisation.hillclimbing.HillClimbingSolution;
import es.upm.fi.cig.multictbnc.learning.structure.optimisation.scores.ctbn.CTBNScoreFunction;
import es.upm.fi.cig.multictbnc.models.CTBN;
import es.upm.fi.cig.multictbnc.models.PGM;
import es.upm.fi.cig.multictbnc.nodes.Node;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * Implements hill climbing algorithm for CTBNs. This class is used with scores that can be optimised by finding the
 * best parent set for each node individually.
 *
 * @author Carlos Villa Blanco
 */
public class CTBNHillClimbingIndividual implements HillClimbingImplementation {
	private static final Logger logger = LogManager.getLogger(CTBNHillClimbingIndividual.class);
	CTBNScoreFunction scoreFunction;
	private int numEdgesTested = 0;

	/**
	 * Constructor that receives the score function to optimise.
	 *
	 * @param scoreFunction score function for continuous-time Bayesian networks
	 */
	public CTBNHillClimbingIndividual(CTBNScoreFunction scoreFunction) {
		this.scoreFunction = scoreFunction;
	}

	/**
	 * Returns the number of edges that have been evaluated so far.
	 *
	 * @return number of edges that have been evaluated so far
	 */
	public int getNumEdgesTested() {
		return numEdgesTested;
	}

	@Override
	public HillClimbingSolution findStructure(PGM<? extends Node> pgm) {
		// Retrieve the model, initial structure and number of nodes
		CTBN<? extends Node> ctbn = (CTBN<? extends Node>) pgm;
		boolean[][] bestAdjacencyMatrix = pgm.getAdjacencyMatrix().clone();
		int numNodes = bestAdjacencyMatrix.length;
		// Retrieve nodes that are not class variables
		List<Integer> idxFeatureNodes = getIdxFeatureVariables(pgm);
		// As a CTBN can have cycles, the parent set of each feature node is optimised
		// individually.
		idxFeatureNodes.stream().parallel().forEach(indexFeatureNode -> {
			// Optimise the parent set of the node
			boolean[][] bestAdjacencyMatrixNode = findStructureNode(ctbn, indexFeatureNode, bestAdjacencyMatrix);
			// Save in the final adjacency matrix the parent set of the node
			for (int indexParentNode = 0; indexParentNode < numNodes; indexParentNode++)
				bestAdjacencyMatrix[indexParentNode][indexFeatureNode] =
						bestAdjacencyMatrixNode[indexParentNode][indexFeatureNode];
		});
		logger.debug("Best structure found: {}", (Object) bestAdjacencyMatrix);
		logger.info("Number of edges tested to learn the bridge and feature subgraphs: {}", this.getNumEdgesTested());
		HillClimbingSolution solution = new HillClimbingSolution();
		solution.setAdjacencyMatrix(bestAdjacencyMatrix);
		// Reset the number of edges tested
		this.resetNumEdgesTested();
		return solution;
	}

	@Override
	public HillClimbingSolution findStructure(PGM<? extends Node> pgm, int idxNode) {
		// Retrieve the model, initial structure and number of nodes
		CTBN<? extends Node> ctbn = (CTBN<? extends Node>) pgm;
		boolean[][] bestAdjacencyMatrix = pgm.getAdjacencyMatrix().clone();
		int numNodes = bestAdjacencyMatrix.length;
		// As a CTBN can have cycles, the parent set of the node is optimised
		// individually.
		boolean[][] bestAdjacencyMatrixNode = findStructureNode(ctbn, idxNode, bestAdjacencyMatrix);
		// Save in the final adjacency matrix the parent set of the node
		for (int indexParentNode = 0; indexParentNode < numNodes; indexParentNode++)
			bestAdjacencyMatrix[indexParentNode][idxNode] = bestAdjacencyMatrixNode[indexParentNode][idxNode];
		logger.debug("Best structure found: {}", (Object) bestAdjacencyMatrix);
		HillClimbingSolution solution = new HillClimbingSolution();
		solution.setAdjacencyMatrix(bestAdjacencyMatrix);
		return solution;
	}

	@Override
	public HillClimbingSolution findStructure(PGM<? extends Node> pgm, List<Integer> idxNodes) {
		// Retrieve the model, initial structure and number of nodes
		CTBN<? extends Node> ctbn = (CTBN<? extends Node>) pgm;
		boolean[][] bestAdjacencyMatrix = pgm.getAdjacencyMatrix().clone();
		int numNodes = bestAdjacencyMatrix.length;
		// As a CTBN can have cycles, the parent set of each node is optimised
		// individually.
		idxNodes.stream().parallel().forEach(idxNode -> {
			// Optimise the parent set of the node
			boolean[][] bestAdjacencyMatrixNode = findStructureNode(ctbn, idxNode, bestAdjacencyMatrix);
			// Save in the final adjacency matrix the parent set of the node
			for (int indexParentNode = 0; indexParentNode < numNodes; indexParentNode++)
				bestAdjacencyMatrix[indexParentNode][idxNode] = bestAdjacencyMatrixNode[indexParentNode][idxNode];
		});
		logger.debug("Best structure found: {}", (Object) bestAdjacencyMatrix);
		HillClimbingSolution solution = new HillClimbingSolution();
		solution.setAdjacencyMatrix(bestAdjacencyMatrix);
		return solution;
	}

	@Override
	public String getIdentifier() {
		return "Hill climbing";
	}

	@Override
	public Map<String, String> getInfoScoreFunction() {
		return Map.of("scoreFunction", this.scoreFunction.getIdentifier(), "penalisationFunction",
				this.scoreFunction.getNamePenalisationFunction());
	}

	@Override
	public Map<String, String> getParametersAlgorithm() {
		return getInfoScoreFunction();
	}

	/**
	 * Computes the score at a certain node given an adjacency matrix.
	 *
	 * @param ctbn            continuous-time Bayesian network that contains the node
	 * @param idxNode         node index
	 * @param adjacencyMatrix evaluated adjacency matrix
	 * @param cache           cache used to avoid recomputing scores
	 * @return resulting score
	 */
	protected double computeScore(CTBN<? extends Node> ctbn, int idxNode, boolean[][] adjacencyMatrix,
								  Map<Long, Double> cache) {
		long keyCache = getKeyCache(idxNode, adjacencyMatrix);
		Double localScore = cache.get(keyCache);
		if (localScore == null) {
			// Clone model to avoid inconsistencies between threads
			CTBN<? extends Node> tmpCtbn = new CTBN<>(ctbn, false);
			// Establish the parent set of the node 'idxNode'
			tmpCtbn.setStructure(idxNode, adjacencyMatrix);
			// Learn the parameters of the node
			tmpCtbn.learnParameters(idxNode);
			// Obtain local score at the node 'idxNode'
			localScore = this.scoreFunction.compute(tmpCtbn, idxNode);
			cache.put(keyCache, localScore);
		}
		return localScore;
	}

	/**
	 * Finds the best neighbour for a CTBN node.
	 *
	 * @param ctbn            continuous-time Bayesian network that contains the node
	 * @param idxNode         node index
	 * @param adjacencyMatrix current adjacency matrix
	 * @param cache           cache used to avoid recomputing scores
	 * @return a {@code HillClimbingSolution} with the adjacency matrix and score of the best neighbour
	 */
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
					}
				}
			}
		return solution;
	}

	/**
	 * Optimises the function score to find the parent set of a given node.
	 *
	 * @param ctbn            continuous-time Bayesian network that contains the node
	 * @param idxNode         index of the node
	 * @param adjacencyMatrix current adjacency matrix
	 * @return adjacency matrix that includes the best parent set of the node that was found
	 */
	protected boolean[][] findStructureNode(CTBN<? extends Node> ctbn, int idxNode, boolean[][] adjacencyMatrix) {
		Node node = ctbn.getNodeByIndex(idxNode);
		// Cache used to avoid the recomputation of the scores
		Map<Long, Double> cache = new HashMap<>();
		// Store best adjacency matrix found
		boolean[][] bestAdjacencyMatrix = adjacencyMatrix.clone();
		// As this code is used to build a Multi-CTBNC, there can be class variables.
		// These cannot have parents
		if (!node.isClassVariable()) {
			logger.info("Finding best parent set for node {} from a {} using Hill Climbing", node.getName(),
					ctbn.getType());
			// Set as initial best score the one obtained with the initial structure
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
				}
			} while (improvement);
			logger.debug("New structure: {}", (Object) bestAdjacencyMatrix);
			logger.trace("Score new structure: {}", bestScore);
		}
		return bestAdjacencyMatrix;
	}

	/**
	 * Increases the number of evaluated edges in one.
	 */
	protected void increaseNumEdgesTested() {
		this.numEdgesTested++;
	}

	/**
	 * Sets to zero the number of evaluated edges.
	 */
	protected void resetNumEdgesTested() {
		this.numEdgesTested = 0;
	}

	/**
	 * Computes the key used in the cache to store the local scores.
	 *
	 * @param idxParentNodes indexes of the parent nodes
	 * @return key
	 */
	private long computeKeyCache(List<Integer> idxParentNodes) {
		long keyCache = 0;
		for (int idx : idxParentNodes)
			keyCache += Math.pow(2, idx);
		return keyCache;
	}

	private List<Integer> getIdxFeatureVariables(PGM<? extends Node> pgm) {
		List<Integer> idxClassVariables = new ArrayList<>();
		for (int idxNode : pgm.getIndexNodes())
			if (!pgm.getNodeByIndex(idxNode).isClassVariable())
				idxClassVariables.add(idxNode);
		return idxClassVariables;
	}

	private long getKeyCache(int idxNode, boolean[][] adjacencyMatrix) {
		List<Integer> idxParentNodes = new ArrayList<>();
		for (int idxParentNode = 0; idxParentNode < adjacencyMatrix.length; idxParentNode++) {
			if (idxNode != idxParentNode && adjacencyMatrix[idxParentNode][idxNode])
				idxParentNodes.add(idxParentNode);
		}
		// The list is sorted to always obtain the same key given the same nodes
		Collections.sort(idxParentNodes);
		return computeKeyCache(idxParentNodes);
	}

}