package es.upm.fi.cig.mctbnc.learning.structure.optimization.hillclimbing.implementation;

import java.util.Map;

import es.upm.fi.cig.mctbnc.learning.structure.optimization.hillclimbing.HillClimbingSolution;
import es.upm.fi.cig.mctbnc.models.PGM;
import es.upm.fi.cig.mctbnc.nodes.Node;

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
	 * Finds a structure for a given PGM.
	 * 
	 * @param pgm a probabilistic graphical model
	 * @return solution given by the hill climbing algorithm
	 */
	public HillClimbingSolution findStructure(PGM<? extends Node> pgm);

	/**
	 * Returns a {@code Map} with the name of the score function that is optimized
	 * and the name of the penalization function that is applied (if any).
	 * 
	 * @return a {@code Map} with the name of the score function that is optimized
	 *         and the name of the penalization function that is applied
	 */
	public abstract Map<String, String> getInfoScoreFunction();

}