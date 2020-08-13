package com.cig.mctbnc.learning.structure.constraints.CTBNC;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cig.mctbnc.learning.structure.constraints.AbstractStructureConstraints;
import com.cig.mctbnc.models.PGM;
import com.cig.mctbnc.nodes.Node;
import com.cig.mctbnc.nodes.NodeIndexer;

/**
 * Specify the general structure restrictions of a CTBNC. As it is used for the
 * learning of MCTBNCs, it is assumed the possible existence of more than one
 * class variable.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class CTBNC extends AbstractStructureConstraints {
	static Logger logger = LogManager.getLogger(CTBNC.class);

	@Override
	public boolean isStructureLegal(boolean[][] adjacencyMatrix, NodeIndexer<? extends Node> nodeIndexer) {
		int numNodes = adjacencyMatrix.length;
		// The class variables cannot have parents
		for (int i = 0; i < numNodes; i++) {
			for (int j = 0; j < numNodes; j++) { // If there is an arc
				if (adjacencyMatrix[i][j]) { // If the arc is from a feature to a class variable, the structure is
												// illegal
					Node nodeI = nodeIndexer.getNodeByIndex(i);
					Node nodeJ = nodeIndexer.getNodeByIndex(j);
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
