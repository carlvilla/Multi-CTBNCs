package com.cig.mctbnc.learning.structure.optimization.hillclimbing;

import com.cig.mctbnc.learning.structure.optimization.hillclimbing.implementation.HillClimbingImplementation;

public class FirstChoiceHillClimbing extends HillClimbing {

	public FirstChoiceHillClimbing(HillClimbingImplementation hcImplementation) {
		this.hcImplementation = hcImplementation;
	}

	@Override
	public boolean[][] findStructure() {
		HillClimbingSolution hcSolution = hcImplementation.findStructure(pgm);
		return hcSolution.getAdjacencyMatrix();
	}

}
