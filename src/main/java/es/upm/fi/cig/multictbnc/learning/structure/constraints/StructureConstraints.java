package es.upm.fi.cig.multictbnc.learning.structure.constraints;

import es.upm.fi.cig.multictbnc.models.PGM;
import es.upm.fi.cig.multictbnc.nodes.Node;
import es.upm.fi.cig.multictbnc.nodes.NodeIndexer;

/**
 * Interface used to define classes that specify structure constraints for PGMs.
 *
 * @author Carlos Villa Blanco
 */
public interface StructureConstraints {

	/**
	 * The structure of the PGM is initialised. This method is necessary for models such as naive Bayes.
	 *
	 * @param pgm probabilistic graphical model
	 */
	void initialiseStructure(PGM<? extends Node> pgm);

	/**
	 * Determines if the structure of a PGM is legal.
	 *
	 * @param adjacencyMatrix adjacency matrix
	 * @param nodeIndexer     node indexer that allows access to information about the nodes
	 * @return true if the structure is legal, false otherwise
	 */
	boolean isStructureLegal(boolean[][] adjacencyMatrix, NodeIndexer<? extends Node> nodeIndexer);

	/**
	 * Determines if there is only one possible structure. This is the case of models such as naive Bayes, for which it
	 * is not necessary to find a structure.
	 *
	 * @return true if there is only one possible structure, false otherwise
	 */
	boolean uniqueStructure();

}