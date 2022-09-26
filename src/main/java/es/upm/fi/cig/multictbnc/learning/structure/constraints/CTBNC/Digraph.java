package es.upm.fi.cig.multictbnc.learning.structure.constraints.CTBNC;

import es.upm.fi.cig.multictbnc.learning.structure.constraints.AbstractStructureConstraints;
import es.upm.fi.cig.multictbnc.nodes.Node;
import es.upm.fi.cig.multictbnc.nodes.NodeIndexer;

/**
 * Specifies the structure restrictions of a CTBN.
 *
 * @author Carlos Villa Blanco
 */
public class Digraph extends AbstractStructureConstraints {

	@Override
	public boolean isStructureLegal(boolean[][] adjacencyMatrix, NodeIndexer<? extends Node> nodeIndexer) {
		return true;
	}

}