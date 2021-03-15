package com.cig.mctbnc.learning.structure;

import com.cig.mctbnc.models.PGM;
import com.cig.mctbnc.nodes.Node;

/**
 * 
 * Interface used to define algorithms for learning the structure of PGMs.
 * 
 * @author Carlos Villa Blanco
 *
 */
public interface StructureLearningAlgorithm {

	/**
	 * Learn the structure of a certain PGM
	 * 
	 * @param pgm
	 */
	public void learn(PGM<? extends Node> pgm);

}
