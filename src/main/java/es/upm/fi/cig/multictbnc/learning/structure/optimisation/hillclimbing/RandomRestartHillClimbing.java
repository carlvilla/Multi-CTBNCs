package es.upm.fi.cig.multictbnc.learning.structure.optimisation.hillclimbing;

import es.upm.fi.cig.multictbnc.learning.structure.optimisation.hillclimbing.implementation.HillClimbingImplementation;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implements random-restart hill climbing.
 *
 * @author Carlos Villa Blanco
 */
public class RandomRestartHillClimbing extends HillClimbing {
	private static final Logger logger = LogManager.getLogger(RandomRestartHillClimbing.class);
	int numRestarts;

	/**
	 * Constructs a {@code RandomRestartHillClimbing} by receiving the implementation of the hill climbing algorithm
	 * (for a Bayesian network, continuous-time Bayesian network...) and the number of restarts.
	 *
	 * @param hcImplementation implementation of the hill climbing algorithm
	 * @param numRestarts      number of restarts
	 */
	public RandomRestartHillClimbing(HillClimbingImplementation hcImplementation, int numRestarts) {
		this.hcImplementation = hcImplementation;
		this.numRestarts = numRestarts;
	}

	@Override
	public boolean[][] findStructure() {
		HillClimbingSolution bestSolution = null;
		boolean[][] initialStructure = this.pgm.getAdjacencyMatrix().clone();
		int numNodes = initialStructure.length;
		if (this.numRestarts == 0) {
			this.numRestarts = 5;
			logger.warn("No number of random restarts provided. Five random restarts will be performed");
		}
		for (int restart = 0; restart < this.numRestarts; restart++) {
			logger.info("Random-restart: {}", restart);
			do {
				// Randomly define the initial structure
				for (int i = 0; i < numNodes; i++) {
					for (int j = 0; j < numNodes; j++)
						if (i != j && !this.pgm.getNodeByIndex(i).isClassVariable()) {
							double random = Math.random();
							if (random > 0.5)
								initialStructure[j][i] = true;
							else
								initialStructure[j][i] = false;
						}
				}
			} while (!this.pgm.isStructureLegal(initialStructure));
			// Set model with randomly defined structure
			this.pgm.setStructure(initialStructure);
			this.pgm.learnParameters();
			// Find structure
			HillClimbingSolution hcSolution = this.hcImplementation.findStructure(this.pgm);
			// Check if the new structure is better than the current one
			if (bestSolution == null || hcSolution.getScore() > bestSolution.getScore())
				bestSolution = hcSolution;
		}
		return bestSolution.getAdjacencyMatrix();
	}

	@Override
	public boolean[][] findStructure(int idxNode) {
		throw new NotImplementedException("Feature not yet implemented");
	}

	@Override
	public boolean[][] findStructure(List<Integer> idxNodes) {
		throw new NotImplementedException("Feature not yet implemented");
	}

	@Override
	public String getIdentifier() {
		return "Random-restart hill climbing";
	}

	@Override
	public Map<String, String> getParametersAlgorithm() {
		Map<String, String> paramAlgorithm = new HashMap<>(this.hcImplementation.getInfoScoreFunction());
		paramAlgorithm.put("numRestarts", String.valueOf(this.numRestarts));
		return paramAlgorithm;
	}

}