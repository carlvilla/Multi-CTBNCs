package es.upm.fi.cig.multictbnc.learning.structure.constraints.BN;

import es.upm.fi.cig.multictbnc.learning.structure.constraints.AbstractStructureConstraints;
import es.upm.fi.cig.multictbnc.models.PGM;
import es.upm.fi.cig.multictbnc.nodes.Node;
import es.upm.fi.cig.multictbnc.nodes.NodeIndexer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * It only allows the creation of empty BNs. This is necessary for the construction of models such as Multi-CTNBCs.
 *
 * @author Carlos Villa Blanco
 */
public class EmptyBN extends AbstractStructureConstraints {
	private static final Logger logger = LogManager.getLogger(EmptyBN.class);

	@Override
	public void initialiseStructure(PGM<? extends Node> pgm) {
		// As it is a disconnected Bayesian network, it is not established any arc.
		logger.info("Estimating parameters of an empty Bayesian network");
	}

	@Override
	public boolean uniqueStructure() {
		return true;
	}

	@Override
	public boolean isStructureLegal(boolean[][] adjacencyMatrix, NodeIndexer<? extends Node> nodeIndexer) {
		int numNodes = adjacencyMatrix.length;
		for (int i = 0; i < numNodes; i++) {
			for (int j = 0; j < numNodes; j++) {
				if (adjacencyMatrix[i][j]) {
					return false;
				}
			}
		}
		return true;
	}
}