package com.cig.mctbnc.learning.structure.constraints;

import com.cig.mctbnc.models.PGM;
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
	 * @param pgm probabilistic graphical model
	 */
	public void initializeStructure(PGM<? extends Node> pgm);

	/**
	 * Determine if there is only one possible structure. This is the case of models
	 * such as naive Bayes, for which it is not necessary to find a structure.
	 * 
	 * @return boolean that determines if there is only one possible structure
	 */
	public boolean uniqueStructure();

	/**
	 * Establishes the penalization function to apply over the structure complexity.
	 * 
	 * @param penalizationFunction String with the name of the penalization function
	 */
	public void setPenalizationFunction(String penalizationFunction);

	/**
	 * Return the name of the penalization function that is applied.
	 * 
	 * @return penalizationFunction name of the penalization function
	 */
	public String getPenalizationFunction();

}
