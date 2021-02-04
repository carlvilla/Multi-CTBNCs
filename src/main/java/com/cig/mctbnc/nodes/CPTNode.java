package com.cig.mctbnc.nodes;

import java.util.List;

import com.cig.mctbnc.data.representation.State;
import com.cig.mctbnc.learning.parameters.SufficientStatistics;
import com.cig.mctbnc.learning.parameters.bn.BNSufficientStatistics;

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
	 * @param nameVariable
	 * @param statesVariable
	 */
	public CPTNode(String name, List<String> states) {
		super(name, states);
	}

	/**
	 * Constructor that receives the name of the variable, a list of strings with
	 * its possible states and if it is a class variable.
	 * 
	 * @param name
	 * @param states
	 * @param isClassVariable
	 * 
	 */
	public CPTNode(String name, List<String> states, boolean isClassVariable) {
		super(name, states, isClassVariable);
	}

	/**
	 * Establish the sufficient statistics of a CPT node.
	 * 
	 * @param CPT
	 */
	@Override
	public void setSufficientStatistics(SufficientStatistics sufficientStatistics) {
		this.sufficientStatistics = (BNSufficientStatistics) sufficientStatistics;

	}

	/**
	 * Establish the CPT of the node.
	 * 
	 * @param CPT
	 */
	public void setCPT(double[][] CPT) {
		this.CPT = CPT;
	}

	/**
	 * Return the conditional probability of a state of the node given the state of
	 * the parents.
	 * 
	 * @param idxStateParents index of the parents state
	 * @param idxStateNode    index of the node state
	 * @return conditional probability of a state of the node given the state of the
	 *         parent
	 */
	public double getCP(int idxStateParents, int idxStateNode) {
		try {
			return CPT[idxStateParents][idxStateNode];
		} catch (IndexOutOfBoundsException e) {
			// One of the index state was never seen during prediction. The model will not
			// be retrain, so 0 is reported.
			return 0;
		}
	}

	/**
	 * Return sufficient statistics of the node.
	 * 
	 * @return sufficient statistics
	 */
	public BNSufficientStatistics getSufficientStatistics() {
		return sufficientStatistics;
	}

	/**
	 * Sample the state of the node given an evidence using forward sampling. First
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
		return !(CPT == null);
	}

	@Override
	public String toString() {
		String discreteNodeDescription = super.toString();
		StringBuilder sb = new StringBuilder();
		sb.append(discreteNodeDescription + "\n");
		sb.append("--CPT--\n");
		sb.append(CPT);
		return sb.toString();
	}

}
