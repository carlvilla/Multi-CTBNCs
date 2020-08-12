package com.cig.mctbnc.learning.structure;

/**
 * Class used to contain the solution given by the hill climbing algorithms.
 * This includes the adjacency matrix of the found structure and its score.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class HillClimbingSolution {
	private boolean[][] adjacencyMatrix;
	private double score;

	/**
	 * Constructor that receive the adjacency matrix and score obtained from the
	 * execution of a hill climbing algorithm.
	 * 
	 * @param adjacencyMatrix
	 * @param score
	 */
	public HillClimbingSolution(boolean[][] adjacencyMatrix, double score) {
		this.adjacencyMatrix = adjacencyMatrix;
		this.score = score;
	}

	/**
	 * Return the adjacency matrix.
	 * 
	 * @return adjacency matrix
	 */
	public boolean[][] getAdjacencyMatrix() {
		return adjacencyMatrix;
	}

	/**
	 * Return the score.
	 * 
	 * @return score
	 */
	public double getScore() {
		return score;
	}

}
