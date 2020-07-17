package com.cig.mctbnc.learning.structure;

import java.util.List;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.models.PGM;
import com.cig.mctbnc.nodes.Node;

public class MCTBNCStructureHillClimbing implements MCTBNCStructureLearning {
	
	private boolean[][] adjacencyMatrix;
	private Dataset trainingDataset;
	
	/**
	 * Initialize the adjacency matrix.
	 * @param nodes
	 */
	public void initializeAdjacencyMatrix(List<Node> nodes) {
		int numNodes = nodes.size();
		adjacencyMatrix = new boolean[numNodes][numNodes];
	}
	
	public boolean[][] getAdjacencyMatrix() {
		return adjacencyMatrix;
	}

	@Override
	public void getBestNeighbor() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void learn() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double evaluateStructure(PGM model) {
		// TODO Auto-generated method stub
		return 0;
	}

}