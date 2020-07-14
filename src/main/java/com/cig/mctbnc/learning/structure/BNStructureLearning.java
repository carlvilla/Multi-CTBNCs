package com.cig.mctbnc.learning.structure;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.learning.parameters.BNParameterLearning;
import com.cig.mctbnc.models.BN;
import com.cig.mctbnc.nodes.Node;

public interface BNStructureLearning {

	/**
	 * Learn the structure of a Bayesian network.
	 * @param bn
	 * @param bnParameterLearning
	 * @param trainingDataset
	 */
	public void learn(BN<? extends Node> bn, BNParameterLearning bnParameterLearning, Dataset trainingDataset);
}
