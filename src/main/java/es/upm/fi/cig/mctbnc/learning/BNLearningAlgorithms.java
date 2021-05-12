package es.upm.fi.cig.mctbnc.learning;

import es.upm.fi.cig.mctbnc.learning.parameters.ParameterLearningAlgorithm;
import es.upm.fi.cig.mctbnc.learning.parameters.bn.BNParameterLearningAlgorithm;
import es.upm.fi.cig.mctbnc.learning.structure.StructureLearningAlgorithm;

/**
 * Stores the parameter and structure learning algorithms for a Bayesian
 * network.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class BNLearningAlgorithms {
	BNParameterLearningAlgorithm parameterLearningAlg;
	StructureLearningAlgorithm structureLearningAlg;

	/**
	 * Receives the learning algorithms for the parameters and the structure.
	 * 
	 * @param parameterLearningAlg parameter learning algorithm
	 * @param structureLearningAlg structure learning algorithm
	 */
	public BNLearningAlgorithms(BNParameterLearningAlgorithm parameterLearningAlg,
			StructureLearningAlgorithm structureLearningAlg) {
		this.parameterLearningAlg = parameterLearningAlg;
		this.structureLearningAlg = structureLearningAlg;
	}

	/**
	 * Returns the parameter learning algorithm for a BN.
	 * 
	 * @return parameter learning algorithm
	 */
	public ParameterLearningAlgorithm getParameterLearningAlgorithm() {
		return this.parameterLearningAlg;
	}

	/**
	 * Returns the structure learning algorithm for a BN.
	 * 
	 * @return structure learning algorithm
	 */
	public StructureLearningAlgorithm getStructureLearningAlgorithm() {
		return this.structureLearningAlg;
	}

}
