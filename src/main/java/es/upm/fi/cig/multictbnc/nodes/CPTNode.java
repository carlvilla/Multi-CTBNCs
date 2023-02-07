package es.upm.fi.cig.multictbnc.nodes;

import es.upm.fi.cig.multictbnc.data.representation.State;
import es.upm.fi.cig.multictbnc.learning.parameters.SufficientStatistics;
import es.upm.fi.cig.multictbnc.learning.parameters.bn.BNSufficientStatistics;
import es.upm.fi.cig.multictbnc.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Extends the DiscreteNode class to store a CPT and the sufficient statistics for a BN.
 *
 * @author Carlos Villa Blanco
 */
public class CPTNode extends DiscreteStateNode {
	private static final Logger logger = LogManager.getLogger(CPTNode.class);
	double[][] CPT;
	BNSufficientStatistics sufficientStatistics;

	/**
	 * Constructor that receives the name of the variable and its possible states.
	 *
	 * @param name   name of the node
	 * @param states list of strings representing the states that the variable related to the node can take
	 */
	public CPTNode(String name, List<String> states) {
		super(name, states);
	}

	/**
	 * Constructor that receives the name of the variable, a list of strings with its possible states and if it is a
	 * class variable.
	 *
	 * @param name            name of the node
	 * @param states          list of strings representing the states that the variable related to the node can take
	 * @param isClassVariable true if the node represent a class variable, false otherwise
	 */
	public CPTNode(String name, List<String> states, boolean isClassVariable) {
		super(name, states, isClassVariable);
	}

	/**
	 * Constructor that receives a {@code CPTNode} and clones it. Everything is cloned except the sufficient
	 * statistics.
	 *
	 * @param node node to clone
	 */
	public CPTNode(CPTNode node) {
		super(node.getName(), new ArrayList<>(node.getStates()), node.isClassVariable());
		setCPT(Util.clone2DArray(node.getCPT()));
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
	 * Returns the conditional probability table (CPT) of the node.
	 *
	 * @return the conditional probability table of the node
	 */
	public double[][] getCPT() {
		return this.CPT;
	}

	/**
	 * Returns the conditional probability of a state of the node given the state of the parents.
	 *
	 * @param idxStateParents index of the parents' state
	 * @param idxStateNode    index of the node state
	 * @return conditional probability of a state of the node given the state of the parent
	 */
	public double getCP(int idxStateParents, int idxStateNode) {
		try {
			return this.CPT[idxStateParents][idxStateNode];
		} catch (IndexOutOfBoundsException iobe) {
			// One of the index states was never seen during prediction. The model will not
			// be retrained, so 0 is reported.
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

	@Override
	public double estimateLogLikelihood() {
		double llScore = 0.0;
		// Iterate over all states of the node (from where a transition begins)
		for (int idxState = 0; idxState < getNumStates(); idxState++) {
			// Iterate over all states of the node's parents
			for (int idxStateParents = 0; idxStateParents < getNumStatesParents(); idxStateParents++) {
				// Number of times the studied variable and its parents take a certain state
				double Nijk = getSufficientStatistics().getNx()[idxStateParents][idxState];
				// The zero count problem occurs if the class variable never takes the evaluated
				// state given the parents. This case is ignored to avoid NaNs
				if (Nijk != 0)
					llScore += Nijk * Math.log(getCP(idxStateParents, idxState));
			}
		}
		return llScore;
	}

	/**
	 * Samples the state of the node given evidence using forward sampling. First it is sampled from a uniform *
	 * distribution, then it is iterated over the state of the nodes and accumulated the probability of each of them. *
	 * Once this accumulated probability is more than the sampled value from the uniform distribution, the current *
	 * state under study is returned.
	 *
	 * @param percentageNoisyStates value from 0 to 1 representing the probability of randomly sampling the state of
	 *                                the
	 *                              variable
	 * @return sampled state of the node. Null is returned if the state could not be sampled
	 */
	public State sampleState(double percentageNoisyStates) {
		// Check if noise is added to the sampled data
		if (percentageNoisyStates > 0) {
			// Draw noise from Gaussian distribution with mean = 0 and sigma = 1
			Random rdn = new Random();
			double noise = rdn.nextGaussian();
			// If noise (absolute value) is greater than 1.95 (~5%), draw state index from uniform
			// distribution
			if (Math.abs(noise) > 1.95) {
				//this.logger.info("Adding noise to class variable {}", getName());
				// Define random state
				int idxState = rdn.nextInt(getNumStates());
				setState(idxState);
				return new State(getName(), getState());
			}
		}
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
		String sb = discreteNodeDescription + "\n" + "--CPT--\n" + Arrays.deepToString(this.CPT);
		return sb;
	}

}