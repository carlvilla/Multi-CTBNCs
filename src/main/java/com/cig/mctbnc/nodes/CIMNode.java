package com.cig.mctbnc.nodes;

import java.util.List;

import com.cig.mctbnc.data.representation.State;
import com.cig.mctbnc.learning.parameters.SufficientStatistics;
import com.cig.mctbnc.learning.parameters.ctbn.CTBNSufficientStatistics;

/**
 * Extends the DiscreteNode class in order to store a CIM and the sufficient
 * statistics for a CTBN. Ø
 * 
 * @author Carlos Villa Blanco
 *
 */
public class CIMNode extends DiscreteNode {
	// The conditional intensity matrix can be summarized by two types of parameters
	// (Nodelman et al., 2012): (1) instantaneous probability of the variable
	// leaving a certain state while its parents take a certain value
	double[][] Qx;
	// (2) probability of the variable leaving a certain state for another one while
	// its parents take a certain value
	double[][][] Oxy;

	CTBNSufficientStatistics sufficientStatistics;

	/**
	 * Construct a CIMNode given its name and possible states.
	 * 
	 * @param name
	 * @param states
	 */
	public CIMNode(String name, List<State> states) {
		super(name, states);
	}

	/**
	 * Initialize a CIMNode given its name, possible states and if it is a class
	 * variable.
	 * 
	 * @param name
	 * @param states
	 * @param isClassVariable
	 */
	public CIMNode(String name, List<State> states, boolean isClassVariable) {
		super(name, states, isClassVariable);
	}

	/**
	 * Initialize a CIMNode node given a list of strings with its states. The order
	 * of parameters is changed with respect to the other constructor to avoid both
	 * of them having the same erasure.
	 * 
	 * @param name
	 * @param isClassVariable
	 * @param states
	 * 
	 */
	public CIMNode(String name, boolean isClassVariable, List<String> states) {
		super(name, isClassVariable, states);
	}

	@Override
	public void setSufficientStatistics(SufficientStatistics sufficientStatistics) {
		this.sufficientStatistics = (CTBNSufficientStatistics) sufficientStatistics;
	}

	/**
	 * Set the parameters of a node.
	 * 
	 * @param Qx
	 * @param Oxy
	 */
	public void setParameters(double[][] Qx, double[][][] Oxy) {
		this.Qx = Qx;
		this.Oxy = Oxy;
	}

	/**
	 * Sample the time that the node stays in its current state given the state of
	 * its parents.
	 * 
	 * @return sampled time
	 */
	public double sampleTimeState() {
		// Get indexes states node and parents
		int idxState = getIdxState();
		int idxStateParents = getIdxStateParents();
		// Intensity of the node of transitioning from the state specified in "evidence"
		// (parameter exponential distribution)
		double q = getQx(idxStateParents, idxState);
		// If the parameter is 0, the node is in an absorbing state that cannot be left
		if (q == 0)
			return Double.POSITIVE_INFINITY;
		// Sample from uniform distribution to obtain the probability of the time that
		// will be sampled
		double prob = Math.random();
		// Use the quantile function of the exponential distribution with parameter 'q'
		// to sample the time with the previous obtained probability
		return -Math.log(1 - prob) / q;
	}

	/**
	 * Sample the next state of the node given the current one and that of its
	 * parents. Returns null if not all the parents were instantiated.
	 * 
	 * @return sampled state
	 * 
	 */
	public State sampleNextState() {
		// Get indexes states node and parents
		int idxFromState = getIdxState();
		int idxStateParents = getIdxStateParents();
		// Sample from uniform distribution
		double probUniform = Math.random();
		// Accumulated probability
		double accProb = 0;
		// Sampled state
		State sampledState = null;
		for (int idxToState = 0; idxToState < getNumStates(); idxToState++) {
			if (idxToState != idxFromState) {
				// Probability of transitioning to "idxToState"
				accProb += getOxy(idxStateParents, idxFromState, idxToState);
				if (probUniform <= accProb) {
					// Generated state for the node
					setState(idxToState);
					sampledState = new State(getName(), getState());
					break;
				}
			}
		}
		return sampledState;
	}

	/**
	 * Get the sufficient statistics of a CIM node.
	 * 
	 * @return sufficient statistics.
	 */
	public CTBNSufficientStatistics getSufficientStatistics() {
		return sufficientStatistics;
	}

	/**
	 * Return the parameter containing the probabilities of the variable leaving
	 * every state for a different one.
	 * 
	 * @return parameter Qx
	 */
	public double getQx(int idxStateParents, int idxStateNode) {
		try {
			return Qx[idxStateParents][idxStateNode];
		} catch (IndexOutOfBoundsException e) {
			// One of the index state was never seen. Report value given by the
			// hyperparameters or 0
			double MxHP = getSufficientStatistics().getMxHyperparameter();
			double TxHP = getSufficientStatistics().getTxHyperparameter();
			if (MxHP == 0 || TxHP == 0)
				return 0;
			return MxHP / TxHP;
		}
	}

	/**
	 * Return the parameter containing the probabilities of the variable leaving a
	 * state for a certain one
	 * 
	 * @return parameter Oxy
	 */
	public double getOxy(int idxStateParents, int idxToStateNode, int idxFromStateNode) {
		try {
			return Oxy[idxStateParents][idxToStateNode][idxFromStateNode];
		} catch (IndexOutOfBoundsException e) {
			// One of the index state was never seen. Report value given by the
			// hyperparameters or 0
			double MxHP = getSufficientStatistics().getMxHyperparameter();
			double MxyHP = getSufficientStatistics().getMxyHyperparameter();
			if (MxHP == 0 || MxyHP == 0)
				return 0;
			return MxyHP / MxHP;
		}
	}

	@Override
	public boolean areParametersEstimated() {
		return !(Qx == null || Oxy == null);
	}

	@Override
	public String toString() {
		String discreteNodeDescription = super.toString();
		StringBuilder sb = new StringBuilder();
		sb.append(discreteNodeDescription + "\n");
		return sb.toString();
	}

}
