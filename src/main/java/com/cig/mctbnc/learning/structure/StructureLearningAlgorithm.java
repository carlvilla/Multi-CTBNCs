package com.cig.mctbnc.learning.structure;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.learning.parameters.ParameterLearningAlgorithm;
import com.cig.mctbnc.models.PGM;

public interface StructureLearningAlgorithm {

	/**
	 * Learn the structure of a certain PGM
	 * 
	 * @param pgm
	 * @param trainingDataset
	 * @param bnParameterLearning
	 */
	public void learn(PGM pgm, Dataset trainingDataset, ParameterLearningAlgorithm bnParameterLearning);
	
}
