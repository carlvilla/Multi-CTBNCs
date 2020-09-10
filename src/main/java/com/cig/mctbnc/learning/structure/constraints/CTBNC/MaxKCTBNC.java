package com.cig.mctbnc.learning.structure.constraints.CTBNC;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cig.mctbnc.learning.structure.constraints.AbstractStructureConstraints;
import com.cig.mctbnc.models.MCTBNC;
import com.cig.mctbnc.nodes.Node;
import com.cig.mctbnc.nodes.NodeIndexer;

/**
 * Implements the structure constrains of a Max-k continuous time Bayesian
 * network classifier, i.e., a CTBNC where the number of parents of the feature
 * nodes is bounded by a positive number (Codecasa and Stella, 2014).
 * 
 * @author Carlos Villa Blanco
 *
 */
public class MaxKCTBNC extends AbstractStructureConstraints {
	int maxK;
	static Logger logger = LogManager.getLogger(MaxKCTBNC.class);

	/**
	 * Receives the maximum number of parents the nodes can have. If a positive
	 * number is not given, a default value (1 parent) is used.
	 * 
	 * @param maxK
	 */
	public MaxKCTBNC(int maxK) {
		if (maxK < 1) {
			logger.warn("Illegal max. number of parents - The maximum number of parents must be 1 or more");
			maxK = 1;
		}
		logger.warn("Max. number of parents: {}", maxK);
		this.maxK = maxK;
	}

	@Override
	public boolean isStructureLegal(boolean[][] adjacencyMatrix, NodeIndexer<? extends Node> nodeIndexer) {
		// Nodes can only have k parents.
		int numNodes = adjacencyMatrix.length;
		for (int j = 0; j < numNodes; j++) {
			int numParentsJ = 0;
			for (int i = 0; i < numNodes; i++) {
				if (i != j) {
					numParentsJ += adjacencyMatrix[i][j] ? 1 : 0;
					if (numParentsJ > maxK)
						return false;
				}
			}
		}
		return true;
	}
}
