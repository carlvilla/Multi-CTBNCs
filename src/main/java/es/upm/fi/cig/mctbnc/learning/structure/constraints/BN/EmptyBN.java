package es.upm.fi.cig.mctbnc.learning.structure.constraints.BN;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.upm.fi.cig.mctbnc.learning.structure.constraints.AbstractStructureConstraints;
import es.upm.fi.cig.mctbnc.models.PGM;
import es.upm.fi.cig.mctbnc.nodes.Node;
import es.upm.fi.cig.mctbnc.nodes.NodeIndexer;

/**
 * It only allows the creation of empty BNs. This is necessary for the
 * construction of models such as MCTNBCs or MCTTANBCs.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class EmptyBN extends AbstractStructureConstraints {
	static Logger logger = LogManager.getLogger(EmptyBN.class);

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

	@Override
	public void initializeStructure(PGM<? extends Node> pgm) {
		// As it is a disconnected Bayesian network, it is not established any arc.
		logger.info("Estimating parameters of empty Bayesian network");
	}

	@Override
	public boolean uniqueStructure() {
		return true;
	}
}