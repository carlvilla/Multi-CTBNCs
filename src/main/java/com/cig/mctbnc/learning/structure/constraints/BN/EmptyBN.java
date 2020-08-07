package com.cig.mctbnc.learning.structure.constraints.BN;

import com.cig.mctbnc.learning.structure.constraints.StructureConstraints;
import com.cig.mctbnc.nodes.Node;
import com.cig.mctbnc.nodes.NodeIndexer;

/**
 * It only allows the creation of empty BNs. This is necessary for the
 * construction of models such as MCTNBCs or MCTTANBCs.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class EmptyBN implements StructureConstraints {

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