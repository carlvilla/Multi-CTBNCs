package es.upm.fi.cig.multictbnc.learning.structure.constraints.CTBNC;

import es.upm.fi.cig.multictbnc.learning.structure.constraints.AbstractStructureConstraints;
import es.upm.fi.cig.multictbnc.models.PGM;
import es.upm.fi.cig.multictbnc.nodes.Node;
import es.upm.fi.cig.multictbnc.nodes.NodeIndexer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Defines the structure of a continuous-time Naive Bayes classifier. As it is used for the construction of
 * Multi-CTBNCs, the existence of more than one class variable is allowed.
 *
 * @author Carlos Villa Blanco
 */
public class NaiveBayes extends AbstractStructureConstraints {
	private static final Logger logger = LogManager.getLogger(NaiveBayes.class);

	@Override
	public void initialiseStructure(PGM<? extends Node> pgm) {
		logger.info("Estimating parameters of a naive Bayes structure");
		// There is only one structure for a naive Bayes
		for (int i = 0; i < pgm.getNumNodes(); i++) {
			Node nodeI = pgm.getNodes().get(i);
			for (int j = 0; j < pgm.getNumNodes(); j++) {
				Node nodeJ = pgm.getNodes().get(j);
				if (i != j && nodeI.isClassVariable() && !nodeJ.isClassVariable()) {
					nodeI.setChild(nodeJ);
				}
			}
		}
	}

	@Override
	public boolean uniqueStructure() {
		return true;
	}

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
						// No arcs from feature to class variables, between feature variables or between
						// class variables
						if (!nodeI.isClassVariable() && nodeJ.isClassVariable() ||
								!nodeI.isClassVariable() && !nodeJ.isClassVariable() ||
								nodeI.isClassVariable() && nodeJ.isClassVariable())
							return false;
				}
			}
		}
		return true;
	}

}