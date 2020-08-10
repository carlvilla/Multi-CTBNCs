package com.cig.mctbnc.learning.structure.constraints;

import java.util.List;

import com.cig.mctbnc.nodes.Node;
import com.cig.mctbnc.nodes.NodeIndexer;

/**
 * Interface used to define classes that specify structure constrains for PGMs.
 * 
 * @author Carlos Villa Blanco
 *
 */
public interface StructureConstraints {

	/**
	 * Determine if the structure of a PGM is legal.
	 * 
	 * @param adjacencyMatrix adjacency matrix
	 * @param nodeIndexer     node indexer that allows to access information about
	 *                        the nodes
	 * @return boolean that determines if the structure is legal
	 */
	public boolean isStructureLegal(boolean[][] adjacencyMatrix, NodeIndexer<? extends Node> nodeIndexer);

	/**
	 * The structure of the PGM is initialized. This method is necessary for models
	 * such as naive Bayes.
	 * 
	 * @param nodes
	 */
	public void initializeStructure(List<? extends Node> nodes);

}