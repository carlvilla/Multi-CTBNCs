package com.cig.mctbnc.learning.structure.constraints.CTBNC;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cig.mctbnc.learning.structure.constraints.AbstractStructureConstraints;
import com.cig.mctbnc.nodes.Node;
import com.cig.mctbnc.nodes.NodeIndexer;

/**
 * Specify the structure restrictions of a CTBNC. As it is used for the learning
 * of MCTBNCs, it is assumed the possible existence of more than one class
 * variable.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class CTBNC extends AbstractStructureConstraints {
	static Logger logger = LogManager.getLogger(CTBNC.class);

	@Override
	public boolean isStructureLegal(boolean[][] adjacencyMatrix, NodeIndexer<? extends Node> nodeIndexer) {
		int numNodes = adjacencyMatrix.length;
		for (int i = 0; i < numNodes; i++) {
			for (int j = 0; j < numNodes; j++) {
				if (adjacencyMatrix[i][j]) { 
					Node nodeI = nodeIndexer.getNodeByIndex(i);
					Node nodeJ = nodeIndexer.getNodeByIndex(j);
					// No arcs from features to class variables
					if (!nodeI.isClassVariable() && nodeJ.isClassVariable()) {
						logger.trace("Illegal structure - A feature cannot be a parent of a class variable");
						return false;
					}
				}
			}
		}
		return true;
	}

}
