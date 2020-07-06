package main.java.com.cig.mctbnc.learning.structure;

import java.util.List;

import main.java.com.cig.mctbnc.models.Node;

public class MCTBNCStructureHillClimbing {
	
	private int[][] adjacencyMatrix;
	
	public MCTBNCStructureHillClimbing(List<Node> nodes) {
		int numNodes = nodes.size();
		adjacencyMatrix = new int[numNodes][numNodes];
	}
	
	public int[][] getAdjacencyMatrix() {
		return adjacencyMatrix;
	}

}
