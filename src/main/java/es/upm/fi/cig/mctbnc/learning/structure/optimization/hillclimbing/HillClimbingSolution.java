package es.upm.fi.cig.mctbnc.learning.structure.optimization.hillclimbing;

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
	 * Sets the adjacency matrix of the structure.
	 * 
	 * @param adjacencyMatrix adjacency matrix of the structure
	 */
	public void setAdjacencyMatrix(boolean[][] adjacencyMatrix) {
		this.adjacencyMatrix = adjacencyMatrix;
	}

	/**
	 * Sets the score of the found structure.
	 * 
	 * @param score score of the found structure
	 */
	public void setScore(double score) {
		this.score = score;
	}

	/**
	 * Returns the adjacency matrix of the structure.
	 * 
	 * @return adjacency matrix of the structure
	 */
	public boolean[][] getAdjacencyMatrix() {
		return this.adjacencyMatrix;
	}

	/**
	 * Returns the score of the found structure.
	 * 
	 * @return score of the found structure
	 */
	public double getScore() {
		return this.score;
	}

}
