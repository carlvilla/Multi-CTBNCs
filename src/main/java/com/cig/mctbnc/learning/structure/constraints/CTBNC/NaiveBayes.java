package com.cig.mctbnc.learning.structure.constraints.CTBNC;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cig.mctbnc.learning.structure.constraints.AbstractStructureConstraints;
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
public class NaiveBayes extends AbstractStructureConstraints {
	static Logger logger = LogManager.getLogger(NaiveBayes.class);

	@Override
	public boolean isStructureLegal(boolean[][] adjacencyMatrix, NodeIndexer<? extends Node> nodeIndexer) {
		// There can only be arcs from the class variables to the features
		int numNodes = adjacencyMatrix.length;
		for (int i = 0; i < numNodes; i++) {
			for (int j = 0; j < numNodes; j++) {
				// No self-loops
				if (i == j && adjacencyMatrix[i][j])
					return false;
				else if (i != j) {
					Node nodeI = nodeIndexer.getNodeByIndex(i);
					Node nodeJ = nodeIndexer.getNodeByIndex(j);
					// No missing arcs from class variables to features
					if (!adjacencyMatrix[i][j] && nodeI.isClassVariable() && !nodeJ.isClassVariable())
						return false;
					else if (adjacencyMatrix[i][j])
						// No arcs from features to class variables, between features or between class
						// variables
						if (!nodeI.isClassVariable() && nodeJ.isClassVariable()
								|| !nodeI.isClassVariable() && !nodeJ.isClassVariable()
								|| nodeI.isClassVariable() && nodeJ.isClassVariable())
							return false;
				}
			}
		}
		return true;
	}

	@Override
	public void initializeStructure(List<? extends Node> nodes) {
		// There is only one structure for a naive Bayes
		for (int i = 0; i < nodes.size(); i++) {
			Node nodeI = nodes.get(i);
			for (int j = 0; j < nodes.size(); j++) {
				Node nodeJ = nodes.get(j);
				if (i != j && nodeI.isClassVariable() && !nodeJ.isClassVariable()) {
					nodeI.setChild(nodeJ);
				}
			}
		}

	}

}
