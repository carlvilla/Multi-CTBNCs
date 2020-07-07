package test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;

import org.junit.jupiter.api.Test;

class learnStructures {

	@Test
	void checkLegalGraphs() {
		
		//BNStructureHillClimbing bnStructureLearning = new BNStructureHillClimbing();
		
		int numNodes = 5;
		boolean[][] adjacencyMatrix = new boolean[numNodes][numNodes];
		adjacencyMatrix[0][1] = true;
		adjacencyMatrix[1][3] = true;
		adjacencyMatrix[3][0] = true;

		assertEquals(true, isCyclic(adjacencyMatrix));
		
		adjacencyMatrix = new boolean[numNodes][numNodes];
		adjacencyMatrix[0][1] = true;
		adjacencyMatrix[1][3] = true;
		adjacencyMatrix[3][4] = true;
		adjacencyMatrix[0][2] = true;
		
		assertEquals(false, isCyclic(adjacencyMatrix));
		
		adjacencyMatrix = new boolean[numNodes][numNodes];
		adjacencyMatrix[0][1] = true;
		adjacencyMatrix[1][3] = true;
		adjacencyMatrix[3][4] = true;
		adjacencyMatrix[0][2] = true;
		adjacencyMatrix[4][0] = true;
		
		assertEquals(true, isCyclic(adjacencyMatrix));
		
	}
	
	private boolean isCyclic(boolean[][] adjacencyMatrix) {

		int numNodes = adjacencyMatrix.length;

		// Indegrees of all nodes.
		int[] inDegree = new int[numNodes];

		for (int i = 0; i < numNodes; i++) {
			boolean[] children = adjacencyMatrix[i];
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
			boolean[] children = adjacencyMatrix[i];
			for (int j = 0; j < numNodes; j++)
				if (i != j && adjacencyMatrix[i][j] && --inDegree[j] == 0)
					q.add(j);
			countVisitedNodes++;
		}

		// If the number of visited nodes are different than the number 
		// of nodes in the graph, there are cycles
		return !(countVisitedNodes == numNodes);

	}
	
	
	
	@Test
	void prueba() {
		String[] array1 = new String[]{"Hola", "A", "B"};
		String[] array2 = new String[]{"Hola", "B", "C"};
		
		boolean a = Arrays.asList(array1).containsAll(Arrays.asList(array2));		
		System.out.println(a);
	}

}
