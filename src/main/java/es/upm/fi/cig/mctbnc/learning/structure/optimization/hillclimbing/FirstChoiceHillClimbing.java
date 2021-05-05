package es.upm.fi.cig.mctbnc.learning.structure.optimization.hillclimbing;

import es.upm.fi.cig.mctbnc.learning.structure.optimization.hillclimbing.implementation.HillClimbingImplementation;

/**
 * Implements first-choice Hill Climbing.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class FirstChoiceHillClimbing extends HillClimbing {

	/**
	 * Constructs a {@code FirstChoiceHillClimbing} by receiving the implementation
	 * of the hill climbing algorithm (for a Bayesian network, continuous time
	 * Bayesian network...).
	 * 
	 * @param hcImplementation
	 */
	public FirstChoiceHillClimbing(HillClimbingImplementation hcImplementation) {
		this.hcImplementation = hcImplementation;
	}

	@Override
	public boolean[][] findStructure() {
		HillClimbingSolution hcSolution = this.hcImplementation.findStructure(this.pgm);
		return hcSolution.getAdjacencyMatrix();
	}

}
