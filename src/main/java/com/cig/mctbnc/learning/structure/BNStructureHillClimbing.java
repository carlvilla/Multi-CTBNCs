package main.java.com.cig.mctbnc.learning.structure;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Vector;

import main.java.com.cig.mctbnc.learning.parameters.BNParameterLearning;
import main.java.com.cig.mctbnc.learning.parameters.CPTNode;
import main.java.com.cig.mctbnc.models.BN;
import main.java.com.cig.mctbnc.models.Dataset;
import main.java.com.cig.mctbnc.models.DiscreteNode;
import main.java.com.cig.mctbnc.models.Node;

public class BNStructureHillClimbing implements BNStructureLearning {

	List<Node> nodes;
	Dataset trainingDataset;
	BNParameterLearning bnParameterLearning;
	boolean[][] initialAdjacencyMatrix;

	@Override
	public void learn(BN bn, BNParameterLearning bnParameterLearning, Dataset trainingDataset) {
		System.out.println("Learning Bayesian network using Hill Climbing...");

		// Define nodes of the bayesian network
		this.nodes = bn.getNodes();
		
		// Define parameter learning algorithm
		this.bnParameterLearning = bnParameterLearning;

		// Get initial structure
		this.initialAdjacencyMatrix = bn.getAdjacencyMatrix();
		
		// Define training dataset
		this.trainingDataset = trainingDataset;
		
		getBestNeighbor();
	}

	public boolean[][] getBestNeighbor() {

		boolean[][] currentBestNeighbor = initialAdjacencyMatrix.clone();
		double currentBestScore = Double.NEGATIVE_INFINITY;

		// Add, remove and reverse arcs
		for (int i = 0; i < currentBestNeighbor.length; i++) {
			for (int j = 0; j < currentBestNeighbor.length; j++) {
				if (i != j) {
					boolean[][] tempAdjacencyMatrix = currentBestNeighbor.clone();
					double obtainedScore = 0;
					tempAdjacencyMatrix[i][j] = !tempAdjacencyMatrix[i][j];

					if (structureIsLegal(tempAdjacencyMatrix)) {
						// Define Bayesian network with the modified adjacency matrix
						BN<CPTNode> bn = new BN<CPTNode>(nodes, trainingDataset);
						bn.setParameterLearningAlgorithm(bnParameterLearning);
						bn.setStructure(tempAdjacencyMatrix);
						List<CPTNode> cptNodes = bn.getLearnedNodes();
						obtainedScore = StructureScoreFunctions.BNlogLikelihoodScore(cptNodes, trainingDataset);
					}
					// It may be possible to reverse an arc and make the
					// structure legal
					else if (currentBestNeighbor[i][j] != currentBestNeighbor[j][i]) {
						tempAdjacencyMatrix[j][i] = !tempAdjacencyMatrix[j][i];
						if (structureIsLegal(tempAdjacencyMatrix)) {
							// Define Bayesian network with the modified adjacency matrix
							BN<CPTNode> bn = new BN<CPTNode>(nodes, trainingDataset);
							bn.setParameterLearningAlgorithm(bnParameterLearning);
							bn.setStructure(tempAdjacencyMatrix);
							List<CPTNode> cptNodes = bn.getLearnedNodes();
							obtainedScore = StructureScoreFunctions.BNlogLikelihoodScore(cptNodes, trainingDataset);
						}
					}

					if (currentBestScore < obtainedScore)
						System.out.println("Score improved!");
						currentBestScore = obtainedScore;
				}
			}
		}
		return currentBestNeighbor;
	}

	/**
	 * Check if the structure (given by a adjacencyMatrix) is legal for a Bayesian
	 * network.
	 * 
	 * @param adjacencyMatrix
	 * @return
	 */
	private boolean structureIsLegal(boolean[][] adjacencyMatrix) {
		return !isCyclic(adjacencyMatrix);
	}

	/**
	 * Modified version of code in
	 * https://www.geeksforgeeks.org/detect-cycle-in-a-directed-graph-using-bfs/.
	 * 
	 * @param adjacencyMatrix
	 * @return
	 */
	private boolean isCyclic(boolean[][] adjacencyMatrix) {

		int numNodes = adjacencyMatrix.length;

		// Indegrees of all nodes.
		int[] inDegree = new int[numNodes];

		for (int i = 0; i < numNodes; i++) {
			for (int j = 0; j < numNodes; j++) {
				if (i != j && adjacencyMatrix[i][j]) {
					inDegree[j]++;
				}
			}
		}

		// Enqueue all nodes with indegree 0
		Queue<Integer> q = new LinkedList<Integer>();
		for (int i = 0; i < numNodes; i++)
			if (inDegree[i] == 0)
				q.add(i);

		// Initialize count of visited vertices
		int countVisitedNodes = 0;

		// One by one dequeue vertices from queue and enqueue
		// adjacents if indegree of adjacent becomes 0
		while (!q.isEmpty()) {

			// Extract node from queue
			int i = q.poll();

			// Iterate over all children nodes of dequeued node i and
			// decrease their in-degree by 1
			for (int j = 0; j < numNodes; j++)
				if (i != j && adjacencyMatrix[i][j] && --inDegree[j] == 0)
					q.add(j);
			countVisitedNodes++;
		}

		// If the number of visited nodes are different than the number
		// of nodes in the graph, there are cycles
		return !(countVisitedNodes == numNodes);

	}

}
