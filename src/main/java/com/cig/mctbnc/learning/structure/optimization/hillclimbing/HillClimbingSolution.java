package com.cig.mctbnc.learning.structure.optimization.hillclimbing;

/**
 * Class used to contain the solution given by the hill climbing algorithms.
 * This includes the adjacency matrix of the found structure and its score.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class HillClimbingSolution {
	private boolean[][] adjacencyMatrix; 
	private double score = Double.NEGATIVE_INFINITY;;

	/**
	 * Set adjacency matrix.
	 * 
	 * @param adjacencyMatrix
	 */
	public void setAdjacencyMatrix(boolean[][] adjacencyMatrix) {
		this.adjacencyMatrix = adjacencyMatrix;
	}

	/**
	 * Set score.
	 * 
	 * @param score
	 */
	public void setScore(double score) {
		this.score = score;
	}

	/**
	 * Return the adjacency matrix.
	 * 
	 * @return adjacency matrix
	 */
	public boolean[][] getAdjacencyMatrix() {
		return this.adjacencyMatrix;
	}

	/**
	 * Return the score.
	 * 
	 * @return score
	 */
	public double getScore() {
		return this.score;
	}

}
