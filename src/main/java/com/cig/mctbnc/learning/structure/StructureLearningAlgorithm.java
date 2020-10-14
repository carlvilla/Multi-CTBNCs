package com.cig.mctbnc.learning.structure;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.learning.parameters.ParameterLearningAlgorithm;
import com.cig.mctbnc.learning.structure.constraints.StructureConstraints;
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
	 * @param trainingDataset
	 * @param bnParameterLearning
	 * @param structureConstraints define constraints on the structure of the PGM
	 */
	public void learn(PGM<? extends Node> pgm, Dataset trainingDataset, ParameterLearningAlgorithm bnParameterLearning,
			StructureConstraints structureConstraints);

}
