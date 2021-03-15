package com.cig.mctbnc.learning;

import com.cig.mctbnc.learning.parameters.ctbn.CTBNParameterLearningAlgorithm;
import com.cig.mctbnc.learning.structure.StructureLearningAlgorithm;

/**
 * Store the parameter and structure learning algorithms for a Continuous time
 * Bayesian network.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class CTBNLearningAlgorithms {
	CTBNParameterLearningAlgorithm parameterLearningAlg;
	StructureLearningAlgorithm structureLearningAlg;

	/**
	 * Receive the learning algorithms for the parameters and the structure.
	 * 
	 * @param parameterLearningAlg
	 * @param structureLearningAlg
	 */
	public CTBNLearningAlgorithms(CTBNParameterLearningAlgorithm parameterLearningAlg,
			StructureLearningAlgorithm structureLearningAlg) {
		this.parameterLearningAlg = parameterLearningAlg;
		this.structureLearningAlg = structureLearningAlg;
	}

	/**
	 * Return the parameter learning algorithm for a CTBN.
	 * 
	 * @return parameter learning algorithm
	 */
	public CTBNParameterLearningAlgorithm getParameterLearningAlgorithm() {
		return parameterLearningAlg;
	}

	/**
	 * Return the structure learning algorithm for a CTBN.
	 * 
	 * @return structure learning algorithm
	 */
	public StructureLearningAlgorithm getStructureLearningAlgorithm() {
		return structureLearningAlg;
	}

}
