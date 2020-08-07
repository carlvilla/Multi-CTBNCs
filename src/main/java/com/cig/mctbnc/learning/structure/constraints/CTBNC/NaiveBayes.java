package com.cig.mctbnc.learning.structure.constraints.CTBNC;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cig.mctbnc.learning.structure.constraints.StructureConstraints;
import com.cig.mctbnc.nodes.Node;
import com.cig.mctbnc.nodes.NodeIndexer;

/**
 * Define the structure of a continuous time Naive Bayes classifier. As it is
 * used for the construction of MCTBNC, it is allowed the existance of more than
 * one class variable.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class NaiveBayes implements StructureConstraints {
	static Logger logger = LogManager.getLogger(NaiveBayes.class);

	@Override
	public boolean isStructureLegal(boolean[][] adjacencyMatrix, NodeIndexer<? extends Node> nodeIndexer) {
		// First it s check that the structure is from a correct CTBNC
		StructureConstraints generalCTBNC = new GeneralCTBNC();
		boolean isLegal = generalCTBNC.isStructureLegal(adjacencyMatrix, nodeIndexer);
		// We avoid the following checks if the structure is not a general CTBNC
		if (!isLegal)
			return false;
		// There can only be arcs from the class variables to the features
		int numNodes = adjacencyMatrix.length;
		for (int i = 0; i < numNodes; i++) {
			for (int j = 0; j < numNodes; j++) {
				if (i != j && !adjacencyMatrix[i][j]) {
					Node nodeI = nodeIndexer.getNodeByIndex(i);
					Node nodeJ = nodeIndexer.getNodeByIndex(j);
					if (nodeI.isClassVariable() && !nodeJ.isClassVariable())
						isLegal = isLegal && false;
				}
			}
		}
		return isLegal;
	}

}
