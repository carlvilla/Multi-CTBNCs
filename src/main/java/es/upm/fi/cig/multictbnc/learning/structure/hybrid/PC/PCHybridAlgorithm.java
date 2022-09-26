package es.upm.fi.cig.multictbnc.learning.structure.hybrid.PC;

import es.upm.fi.cig.multictbnc.exceptions.ErroneousValueException;
import es.upm.fi.cig.multictbnc.learning.structure.constraintlearning.PC.PC;
import es.upm.fi.cig.multictbnc.models.PGM;
import es.upm.fi.cig.multictbnc.nodes.Node;

import java.util.List;

/**
 * Implements the restriction phase (PC algorithm) of the hybrid structure learning algorithm.
 *
 * @author Carlos Villa Blanco
 */
public class PCHybridAlgorithm extends PC {

	/**
	 * Initialises the algorithm by proving a significance level.
	 *
	 * @param significance significance level
	 */
	public PCHybridAlgorithm(double significance) {
		super(significance);
	}

	/**
	 * Learns the skeleton of a given PGM.
	 *
	 * @param pgm probabilistic graphical model
	 * @return adjacency matrix of the skeleton
	 * @throws ErroneousValueException if a provided parameter is erroneous for the requested task
	 */
	public boolean[][] learnSkeleton(PGM<? extends Node> pgm) throws ErroneousValueException {
		// Obtain complete undirected graph
		boolean[][] adjacencyMatrix = buildCompleteStructure(pgm);
		// Retrieve edges of the PGM
		List<List<Integer>> edges = getEdgesPGM(pgm);
		// Find skeleton
		learnSkeleton(pgm, adjacencyMatrix, edges);
		return adjacencyMatrix;
	}

}