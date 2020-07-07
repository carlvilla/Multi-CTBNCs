package main.java.com.cig.mctbnc.learning.structure;

import main.java.com.cig.mctbnc.learning.parameters.BNParameterLearning;
import main.java.com.cig.mctbnc.models.BN;
import main.java.com.cig.mctbnc.models.Dataset;

public interface BNStructureLearning {

	/**
	 * Learn the structure of a Bayesian network.
	 * @param bn
	 * @param bnParameterLearning
	 * @param trainingDataset
	 */
	public void learn(BN bn, BNParameterLearning bnParameterLearning, Dataset trainingDataset);
}
