package es.upm.fi.cig.multictbnc.learning.structure.constraints.CTBNC;

import es.upm.fi.cig.multictbnc.learning.structure.constraints.AbstractStructureConstraints;
import es.upm.fi.cig.multictbnc.nodes.Node;
import es.upm.fi.cig.multictbnc.nodes.NodeIndexer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Implements the structure constraints of a Max-k continuous-time Bayesian network classifier, i.e., a CTBNC where the
 * number of parents of the feature nodes is bounded by a positive number (Codecasa and Stella, 2014).
 *
 * @author Carlos Villa Blanco
 */
public class MaxKCTBNC extends AbstractStructureConstraints {
	private static final Logger logger = LogManager.getLogger(MaxKCTBNC.class);
	int maxK;

	/**
	 * Receives the maximum number of parents the nodes can have. If a positive number is not given, one parent is
	 * used.
	 *
	 * @param maxK maximum number of parents the nodes of feature variables can have (without including nodes of class
	 *             variables)
	 */
	public MaxKCTBNC(int maxK) {
		if (maxK < 1) {
			logger.warn("Illegal maximum number of parents. The maximum number of parents must be 1 or more");
			this.maxK = 1;
		}
		logger.trace("Max. number of parents: {}", maxK);
		this.maxK = maxK;
	}

	@Override
	public boolean isStructureLegal(boolean[][] adjacencyMatrix, NodeIndexer<? extends Node> nodeIndexer) {
		// Nodes can only have k parents (excluding class variables)
		int numNodes = adjacencyMatrix.length;
		for (int j = 0; j < numNodes; j++) {
			int numParentsJ = 0;
			for (int i = 0; i < numNodes; i++) {
				if (i != j && !nodeIndexer.getNodeByIndex(i).isClassVariable()) {
					numParentsJ += adjacencyMatrix[i][j] ? 1 : 0;
					if (numParentsJ > this.maxK)
						return false;
				}
			}
		}
		return true;
	}
}