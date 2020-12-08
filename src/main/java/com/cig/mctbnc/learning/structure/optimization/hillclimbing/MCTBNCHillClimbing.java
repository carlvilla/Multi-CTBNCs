package com.cig.mctbnc.learning.structure.optimization.hillclimbing;

import com.cig.mctbnc.learning.structure.optimization.MCTBNCScoreFunction;

/**
 * Implements hill climbing algorithm for MCTBNCs. This class allows learning
 * the class, bridge and feature subgraph of a MCTBNC simultaneously, instead of
 * learning a BN and CTBN separately.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class MCTBNCHillClimbing extends HillClimbing {
	MCTBNCScoreFunction scoreFunction;

	/**
	 * Constructor that receives the score function to optimize.
	 * 
	 * @param scoreFunction
	 */
	public MCTBNCHillClimbing(MCTBNCScoreFunction scoreFunction) {
		this.scoreFunction = scoreFunction;
	}

	@Override
	public boolean[][] findStructure() {
		// TODO Auto-generated method stub
		return null;
	}

}
