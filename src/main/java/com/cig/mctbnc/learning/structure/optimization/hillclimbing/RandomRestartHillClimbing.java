package com.cig.mctbnc.learning.structure.optimization.hillclimbing;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cig.mctbnc.learning.structure.optimization.hillclimbing.implementation.HillClimbingImplementation;

/**
 * Implements random-restart hill climbing.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class RandomRestartHillClimbing extends HillClimbing {
	int numRestarts;
	static Logger logger = LogManager.getLogger(RandomRestartHillClimbing.class);

	/**
	 * Constructs a {@code RandomRestartHillClimbing} by receiving the
	 * implementation of the hill climbing algorithm (for a Bayesian network,
	 * continuous time Bayesian network...).
	 * 
	 * @param hcImplementation
	 * @param numRestarts
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
			logger.warn("No number of random restarts provided. Five random restarts will be performed.");
		}
		for (int restart = 0; restart < this.numRestarts; restart++) {
			logger.info("Random-restart: {}", restart);
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
			// Set model with randomly defined structure
			this.pgm.setStructure(initialStructure);
			// Find structure
			HillClimbingSolution hcSolution = this.hcImplementation.findStructure(this.pgm);
			// Check if new structure is better than current one
			if (bestSolution == null || hcSolution.getScore() > bestSolution.getScore())
				bestSolution = hcSolution;
		}
		return bestSolution.getAdjacencyMatrix();
	}

	@Override
	public String getIdentifier() {
		return "Random-restart hill climbing";
	}

	@Override
	public Map<String, String> getParametersAlgorithm() {
		Map<String, String> parametersAlgorithm = new HashMap<String, String>(hcImplementation.getInfoScoreFunction());
		parametersAlgorithm.put("numRestarts", String.valueOf(numRestarts));
		return parametersAlgorithm;
	}

}
