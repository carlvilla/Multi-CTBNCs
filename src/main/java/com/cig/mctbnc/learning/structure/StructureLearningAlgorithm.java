package com.cig.mctbnc.learning.structure;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.learning.parameters.ParameterLearningAlgorithm;
import com.cig.mctbnc.models.PGM;
import com.cig.mctbnc.nodes.Node;

public interface StructureLearningAlgorithm {

	/**
	 * Learn the structure of a certain PGM
	 * 
	 * @param pgm
	 * @param trainingDataset
	 * @param bnParameterLearning
	 */
	public void learn(PGM<? extends Node> pgm, Dataset trainingDataset, ParameterLearningAlgorithm bnParameterLearning);
	
}
