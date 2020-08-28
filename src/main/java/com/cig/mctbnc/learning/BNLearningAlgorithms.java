package com.cig.mctbnc.learning;

import com.cig.mctbnc.learning.parameters.ParameterLearningAlgorithm;
import com.cig.mctbnc.learning.parameters.bn.BNParameterLearningAlgorithm;
import com.cig.mctbnc.learning.structure.BNStructureLearningAlgorithm;

/**
 * Store the parameter and structure learning algorithms for a Bayesian network.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class BNLearningAlgorithms {
	BNParameterLearningAlgorithm parameterLearningAlg;
	BNStructureLearningAlgorithm structureLearningAlg;

	/**
	 * Receive the learning algorithms for the parameters and the structure.
	 * 
	 * @param parameterLearningAlg
	 * @param structureLearningAlg
	 */
	public BNLearningAlgorithms(BNParameterLearningAlgorithm parameterLearningAlg,
			BNStructureLearningAlgorithm structureLearningAlg) {
		this.parameterLearningAlg = parameterLearningAlg;
		this.structureLearningAlg = structureLearningAlg;
	}

	/**
	 * Return the parameter learning algorithm for a BN.
	 * 
	 * @return parameter learning algorithm
	 */
	public ParameterLearningAlgorithm getParameterLearningAlgorithm() {
		return parameterLearningAlg;
	}

	/**
	 * Return the structure learning algorithm for a BN.
	 * 
	 * @return structure learning algorithm
	 */
	public BNStructureLearningAlgorithm getStructureLearningAlgorithm() {
		return structureLearningAlg;
	}

}
