package es.upm.fi.cig.mctbnc.learning;

import es.upm.fi.cig.mctbnc.learning.parameters.ctbn.CTBNParameterLearningAlgorithm;
import es.upm.fi.cig.mctbnc.learning.structure.StructureLearningAlgorithm;

/**
 * Stores the parameter and structure learning algorithms for a Continuous time
 * Bayesian network.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class CTBNLearningAlgorithms {
	CTBNParameterLearningAlgorithm parameterLearningAlg;
	StructureLearningAlgorithm structureLearningAlg;

	/**
	 * Receives the learning algorithms for the parameters and the structure.
	 * 
	 * @param parameterLearningAlg parameter learning algorithm
	 * @param structureLearningAlg structure learning algorithm
	 */
	public CTBNLearningAlgorithms(CTBNParameterLearningAlgorithm parameterLearningAlg,
			StructureLearningAlgorithm structureLearningAlg) {
		this.parameterLearningAlg = parameterLearningAlg;
		this.structureLearningAlg = structureLearningAlg;
	}

	/**
	 * Returns the parameter learning algorithm for a CTBN.
	 * 
	 * @return parameter learning algorithm
	 */
	public CTBNParameterLearningAlgorithm getParameterLearningAlgorithm() {
		return this.parameterLearningAlg;
	}

	/**
	 * Returns the structure learning algorithm for a CTBN.
	 * 
	 * @return structure learning algorithm
	 */
	public StructureLearningAlgorithm getStructureLearningAlgorithm() {
		return this.structureLearningAlg;
	}

}
