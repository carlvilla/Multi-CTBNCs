package es.upm.fi.cig.multictbnc.learning.structure.optimisation.hillclimbing;

/**
 * Class used to contain the solution given by the hill climbing algorithms. This includes the adjacency matrix of the
 * found structure and its score.
 *
 * @author Carlos Villa Blanco
 */
public class HillClimbingSolution {
	private boolean[][] adjacencyMatrix;
	private double score = Double.NEGATIVE_INFINITY;
	private final int[][] lastArcsModified = new int[3][2];

	/**
	 * Returns the adjacency matrix of the structure.
	 *
	 * @return adjacency matrix of the structure
	 */
	public boolean[][] getAdjacencyMatrix() {
		return this.adjacencyMatrix;
	}

	/**
	 * Returns the last arc modified.
	 *
	 * @return {@code int[][]} with the first dimension referring to the index of the operation applied to the arc and
	 * the second dimension being of size two and representing the modified arc. The first position is for the parent
	 * node index and the second for the child.
	 */
	public int[][] getModifiedArcs() {
		return this.lastArcsModified;
	}

	/**
	 * Returns the score of the structure found.
	 *
	 * @return score of the found structure
	 */
	public double getScore() {
		return this.score;
	}

	/**
	 * Sets the adjacency matrix of the structure.
	 *
	 * @param adjacencyMatrix adjacency matrix of the structure
	 */
	public void setAdjacencyMatrix(boolean[][] adjacencyMatrix) {
		this.adjacencyMatrix = adjacencyMatrix;
	}

	/**
	 * Define the last arc that was modified.
	 *
	 * @param idxParent    index of the parent node
	 * @param idxChild     index of the child node
	 * @param idxOperation index of the operation performed
	 */
	public void setArcModified(int idxParent, int idxChild, int idxOperation) {
		this.lastArcsModified[idxOperation][0] = idxParent;
		this.lastArcsModified[idxOperation][1] = idxChild;
	}

	/**
	 * Sets the score of the structure found.
	 *
	 * @param score score of the structure found
	 */
	public void setScore(double score) {
		this.score = score;
	}

}