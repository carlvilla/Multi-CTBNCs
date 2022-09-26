package es.upm.fi.cig.multictbnc.learning.structure;

import es.upm.fi.cig.multictbnc.models.PGM;
import es.upm.fi.cig.multictbnc.nodes.Node;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

/**
 * The structure is given. It is only necessary to learn its parameters.
 *
 * @author Carlos Villa Blanco
 */
public class ExpertKnowledge implements StructureLearningAlgorithm {
	private static final Logger logger = LogManager.getLogger(ExpertKnowledge.class);
	private boolean[][] adjacencyMatrix;

	/**
	 * Creates an object {@code ExpertKnowlege} that receives an adjacency matrix.
	 *
	 * @param adjacencyMatrix adjacency matrix given by expert knowledge
	 */
	public ExpertKnowledge(boolean[][] adjacencyMatrix) {
		this.adjacencyMatrix = adjacencyMatrix;
	}

	@Override
	public String getIdentifier() {
		return "Expert knowledge";
	}

	@Override
	public Map<String, String> getParametersAlgorithm() {
		return null;
	}

	@Override
	public void learn(PGM<? extends Node> pgm, List<Integer> idxNodes) {
		// TODO
	}

	@Override
	public void learn(PGM<? extends Node> pgm) {
		logger.info("Using predefined structure to learn a {}", pgm.getType());
		pgm.setStructure(adjacencyMatrix);
		pgm.learnParameters();
	}

	@Override
	public void learn(PGM<? extends Node> pgm, int idxNode) {
		// TODO
	}

}