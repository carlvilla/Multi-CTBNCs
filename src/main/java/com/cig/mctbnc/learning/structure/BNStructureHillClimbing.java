package main.java.com.cig.mctbnc.learning.structure;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Vector;

import main.java.com.cig.mctbnc.models.Dataset;
import main.java.com.cig.mctbnc.models.DiscreteNode;
import main.java.com.cig.mctbnc.models.Node;

public class BNStructureHillClimbing implements BNStructureLearning {

	List<Node> nodes;
	Dataset trainingDataset;

	public BNStructureHillClimbing(List<Node> nodes, Dataset trainingDataset) {
		this.nodes = nodes;
		this.trainingDataset = trainingDataset;
	}

	@Override
	public void learn(List<Node> nodes, Dataset trainingDataset) {
		int numNodes = nodes.size();

		// Learned structure
		boolean[][] initialAdjacencyMatrix = new boolean[numNodes][numNodes];

		for (Node node : nodes) {
			List<Node> children = node.getChildren();
			for (Node childNode : children) {
				int indexNode = node.getIndex();
				int indexChildNode = childNode.getIndex();

				initialAdjacencyMatrix[indexNode][indexChildNode] = true;
			}
		}

		// Local structure
		boolean[][] localAdjMatrix = new boolean[numNodes][numNodes];

		boolean[][] newStructure = getBestNeighbor(boolean[][] adjacencyMatrix);
		
		double scoreNew = 0;
		double scoreBest = 0;

		// Updated the best structure
		if (bestInd == null || scoreNew > scoreBest || scoreNew == scoreBest && Math.random() > 0.5) {
			bestInd = newInd;
		}
		
	}

	public boolean[][] getBestNeighbor(boolean[][] adjacencyMatrix) {
		
		// Add, remove and reverse arcs 
		for (int i = 0; i < adjacencyMatrix.length; i++) {
			for (int j = 0; j < adjacencyMatrix.length;j++) {
				if(i != j) {
					boolean[][] tempAdjacencyMatrix = adjacencyMatrix.clone(); 
					tempAdjacencyMatrix[i][j] = !tempAdjacencyMatrix[i][j];
					
					if(structureIsLegal(tempAdjacencyMatrix)) {
						score(tempAdjacencyMatrix);
					} 
					// It may be possible to reverse an arc and make the
					// structure legal
					else if(adjacencyMatrix[i][j] != adjacencyMatrix[j][i]) {
						tempAdjacencyMatrix[j][i] = !tempAdjacencyMatrix[j][i];
						if(structureIsLegal(tempAdjacencyMatrix)) {
							score(tempAdjacencyMatrix);
						} 
					}
					
				}
			}
		}
		
		

		
			
	
		
		

		for (int i = 0; i < initialAdjacencyMatrix.length; i++) {
			if (i == indexNode)
				continue;

			// Generate a new structure
			boolean[][] newAdjacencyMatrix = initialAdjacencyMatrix.clone();

			// Establish a node as the parent of the studied one.
			newAdjacencyMatrix[i][indexNode] = !newAdjacencyMatrix[i][indexNode];
			
			// Operations: Add arc, remove arc or reverse arc.

			

			// String key = CTBNCHillClimbingIndividual.getKey(newAdjMatrix,
			// this.nodeIndex);
			// if( cache != null && cache.contains(key)) // It is checked that the this
			// structure was not checked before.
			// newInd = cache.get( key);
			// else {

			// if( cache != null)
			// cache.put(key, newInd);
			// }

			ICTClassifier<Double, CTDiscreteNode> clonedModel = (ICTClassifier<Double, CTDiscreteNode>) this.model
					.clone();
			clonedModel.setStructure(newAdjMatrix);

			newInd = this.elementsFactory.newInstance(clonedModel, this.nodeIndex);

			scoreNew = newInd.evaluate();
			scoreBest = bestInd.evaluate();

			// Updated the best structure
			if (bestInd == null || scoreNew > scoreBest || scoreNew == scoreBest && Math.random() > 0.5) {
				bestInd = newInd;
			}

			// Update the best element / C�DIGO REPETIDO??

		}

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
