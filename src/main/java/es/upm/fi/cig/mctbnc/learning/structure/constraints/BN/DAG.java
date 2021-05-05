package es.upm.fi.cig.mctbnc.learning.structure.constraints.BN;

import java.util.LinkedList;
import java.util.Queue;

import es.upm.fi.cig.mctbnc.learning.structure.constraints.AbstractStructureConstraints;
import es.upm.fi.cig.mctbnc.nodes.Node;
import es.upm.fi.cig.mctbnc.nodes.NodeIndexer;

/**
 * Defines the restrictions of a general directed acyclic graph.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class DAG extends AbstractStructureConstraints {

	/**
	 * Check if the structure (given by an adjacencyMatrix) is legal for a Bayesian
	 * network without restrictions. The method determines if there are cycles in an
	 * adjacency matrix. Modified version of code in
	 * https://www.geeksforgeeks.org/detect-cycle-in-a-directed-graph-using-bfs/.
	 * 
	 * @param adjacencyMatrix adjacency matrix
	 * @return boolean that determines if the structure is valid
	 */
	@Override
	public boolean isStructureLegal(boolean[][] adjacencyMatrix, NodeIndexer<? extends Node> nodeIndexer) {
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
		// There are cycles if the number of visited nodes are different from the number
		// of nodes in the graph
		return countVisitedNodes == numNodes;
	}

}
