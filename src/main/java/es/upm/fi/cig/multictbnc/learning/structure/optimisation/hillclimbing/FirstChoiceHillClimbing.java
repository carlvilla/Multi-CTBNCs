package es.upm.fi.cig.multictbnc.learning.structure.optimisation.hillclimbing;

import es.upm.fi.cig.multictbnc.learning.structure.optimisation.hillclimbing.implementation.HillClimbingImplementation;

import java.util.List;

/**
 * Implements first-choice Hill Climbing.
 *
 * @author Carlos Villa Blanco
 */
public class FirstChoiceHillClimbing extends HillClimbing {

	/**
	 * Constructs a {@code FirstChoiceHillClimbing} by receiving the implementation of the hill climbing algorithm (for
	 * a Bayesian network, continuous-time Bayesian network...).
	 *
	 * @param hcImplementation implementation of the hill climbing algorithm
	 */
	public FirstChoiceHillClimbing(HillClimbingImplementation hcImplementation) {
		this.hcImplementation = hcImplementation;
	}

	@Override
	public boolean[][] findStructure() {
		HillClimbingSolution hcSolution = this.hcImplementation.findStructure(this.pgm);
		return hcSolution.getAdjacencyMatrix();
	}

	@Override
	public boolean[][] findStructure(int idxNode) {
		HillClimbingSolution hcSolution = this.hcImplementation.findStructure(this.pgm, idxNode);
		return hcSolution.getAdjacencyMatrix();
	}

	@Override
	public boolean[][] findStructure(List<Integer> idxNodes) {
		HillClimbingSolution hcSolution = this.hcImplementation.findStructure(this.pgm, idxNodes);
		return hcSolution.getAdjacencyMatrix();
	}

}