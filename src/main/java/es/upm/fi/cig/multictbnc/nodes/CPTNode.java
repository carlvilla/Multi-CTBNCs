package es.upm.fi.cig.multictbnc.nodes;

import java.util.List;

import es.upm.fi.cig.multictbnc.data.representation.State;
import es.upm.fi.cig.multictbnc.learning.parameters.SufficientStatistics;
import es.upm.fi.cig.multictbnc.learning.parameters.bn.BNSufficientStatistics;

/**
 * Extends the DiscreteNode class to store a CPT and the sufficient statistics
 * for a BN.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class CPTNode extends DiscreteNode {
	double[][] CPT;
	BNSufficientStatistics sufficientStatistics;

	/**
	 * Constructor that receives the name of the variable and its possible states.
	 * 
	 * @param name   name of the node
	 * @param states list of strings representing the states that the variable
	 *               related to the node can take
	 */
	public CPTNode(String name, List<String> states) {
		super(name, states);
	}

	/**
	 * Constructor that receives the name of the variable, a list of strings with
	 * its possible states and if it is a class variable.
	 * 
	 * @param name            name of the node
	 * @param states          list of strings representing the states that the
	 *                        variable related to the node can take
	 * @param isClassVariable true if the node represent a class variable, false
	 *                        otherwise
	 * 
	 */
	public CPTNode(String name, List<String> states, boolean isClassVariable) {
		super(name, states, isClassVariable);
	}

	/**
	 * Establishes the sufficient statistics of a CPT node.
	 * 
	 * @param sufficientStatistics sufficient statistics of a CPT node
	 */
	@Override
	public void setSufficientStatistics(SufficientStatistics sufficientStatistics) {
		this.sufficientStatistics = (BNSufficientStatistics) sufficientStatistics;

	}

	/**
	 * Establishes the CPT of the node.
	 * 
	 * @param CPT CPT of the node
	 */
	public void setCPT(double[][] CPT) {
		this.CPT = CPT;
	}

	/**
	 * Returns the conditional probability of a state of the node given the state of
	 * the parents.
	 * 
	 * @param idxStateParents index of the parents state
	 * @param idxStateNode    index of the node state
	 * @return conditional probability of a state of the node given the state of the
	 *         parent
	 */
	public double getCP(int idxStateParents, int idxStateNode) {
		try {
			return this.CPT[idxStateParents][idxStateNode];
		} catch (IndexOutOfBoundsException e) {
			// One of the index state was never seen during prediction. The model will not
			// be retrain, so 0 is reported.
			return 0;
		}
	}

	/**
	 * Returns sufficient statistics of the node.
	 * 
	 * @return sufficient statistics
	 */
	public BNSufficientStatistics getSufficientStatistics() {
		return this.sufficientStatistics;
	}

	/**
	 * Samples the state of the node given an evidence using forward sampling. First
	 * it is sampled from a uniform distribution, then it is iterated over the state
	 * of the nodes and accumulated the probability of each of them. Once this
	 * accumulated probability is more than the sampled value from the uniform
	 * distribution, the current state under study is returned.
	 * 
	 * @return sampled state of the node. Null is returned if the state could not be
	 *         sampled
	 */
	public State sampleState() {
		// Sample from uniform distribution
		double probUniform = Math.random();
		// Accumulated probability
		double accProb = 0;
		// Iterate over all the states of the node and its parents
		for (int idxState = 0; idxState < getNumStates(); idxState++) {
			int idxStateParents = getIdxStateParents();
			// Retrieve probability given the state of the node and its parents
			accProb += getCP(idxStateParents, idxState);
			if (probUniform <= accProb) {
				// Generated state for the node
				setState(idxState);
				break;
			}
		}
		return new State(getName(), getState());
	}

	@Override
	public boolean areParametersEstimated() {
		return !(this.CPT == null);
	}

	@Override
	public String toString() {
		String discreteNodeDescription = super.toString();
		StringBuilder sb = new StringBuilder();
		sb.append(discreteNodeDescription + "\n");
		sb.append("--CPT--\n");
		sb.append(this.CPT);
		return sb.toString();
	}

}
