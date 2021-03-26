package com.cig.mctbnc.learning.structure.optimization.hillclimbing.implementation;

import com.cig.mctbnc.learning.structure.optimization.hillclimbing.HillClimbingSolution;
import com.cig.mctbnc.models.PGM;
import com.cig.mctbnc.nodes.Node;

/**
 * Defines an interface for different implementations of the hill climbing
 * algorithm. These can be implementations for Bayesian networks, continuous
 * time Bayesian networks, decomposable score functions, etc.
 * 
 * @author Carlos Villa Blanco
 *
 */
public interface HillClimbingImplementation {

	/**
	 * Find a structure for a given model.
	 * 
	 * @param pgm
	 * @return solution given by the hill climbing algorithm
	 */
	public HillClimbingSolution findStructure(PGM<? extends Node> pgm);

}