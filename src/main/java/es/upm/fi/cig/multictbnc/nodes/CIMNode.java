package es.upm.fi.cig.multictbnc.nodes;

import es.upm.fi.cig.multictbnc.data.representation.State;
import es.upm.fi.cig.multictbnc.learning.parameters.SufficientStatistics;
import es.upm.fi.cig.multictbnc.learning.parameters.ctbn.CTBNSufficientStatistics;
import es.upm.fi.cig.multictbnc.util.Util;
import org.apache.commons.math3.distribution.NormalDistribution;

import java.util.List;
import java.util.Random;

/**
 * Extends the DiscreteNode class to store a CIM and the sufficient statistics for a CTBN.
 *
 * @author Carlos Villa Blanco
 */
public class CIMNode extends DiscreteStateNode {
	CTBNSufficientStatistics sufficientStatistics;
	// The conditional intensity matrix can be summarised by two types of parameters
	// (Nodelman et al., 2012):
	// (1) intensity of the variable leaving a certain state while its parents take
	// a certain instantiation
	private double[][] Qx;
	// (2) probability of the variable leaving a certain state for another one while
	// its parents take a certain instantiation
	private double[][][] Oxy;

	/**
	 * Constructs a CIMNode given its name and possible states.
	 *
	 * @param name   name of the node
	 * @param states list of strings representing the states that the variable related to the node can take
	 */
	public CIMNode(String name, List<String> states) {
		super(name, states);
	}

	/**
	 * Initialises a CIMNode given its name, possible states and if it is a class variable.
	 *
	 * @param name            name of the node
	 * @param states          list of strings representing the states that the variable related to the node can take
	 * @param isClassVariable true if the node represent a class variable, false otherwise
	 */
	public CIMNode(String name, List<String> states, boolean isClassVariable) {
		super(name, states, isClassVariable);
	}

	/**
	 * Constructor to clone a CIM node. The parameters and sufficient statistics are not cloned.
	 *
	 * @param node a {@code CIMNode}
	 */
	public CIMNode(CIMNode node) {
		super(node.getName(), node.getStates(), node.isClassVariable());
		setParameters(Util.clone2DArray(node.getQx()), Util.clone3DArray(node.getOxy()));
	}

	/**
	 * Return matrix with the probabilities of the variable leaving a certain state for another one while their parents
	 * take a certain instantiation
	 *
	 * @return probability matrix
	 */
	public double[][][] getOxy() {
		return Oxy;
	}

	/**
	 * Returns the probability of the variable leaving a state for a certain one given the state of its parents
	 *
	 * @param idxStateParents  index of the state of the node's parents
	 * @param idxFromStateNode leaving state index
	 * @param idxToStateNode   incoming state index
	 * @return parameter Oxy
	 */
	public double getOxy(int idxStateParents, int idxFromStateNode, int idxToStateNode) {
		try {
			return this.Oxy[idxStateParents][idxFromStateNode][idxToStateNode];
		} catch (IndexOutOfBoundsException iobe) {
			// One of the index states was never seen during prediction. The model will not
			// be retrained, so 0 is reported
			return 0;
		}
	}

	/**
	 * Returns the intensity of the variable leaving a certain state given the state of its parents
	 *
	 * @param idxStateParents index of the state of the node's parents
	 * @param idxStateNode    leaving state index
	 * @return parameter Qx
	 */
	public double getQx(int idxStateParents, int idxStateNode) {
		try {
			return this.Qx[idxStateParents][idxStateNode];
		} catch (IndexOutOfBoundsException iobe) {
			// One of the index states was never seen during prediction. The model will not
			// be retrained, so 0 is reported
			return 0;
		}
	}

	/**
	 * Return matrix with the intensities of the variables leaving a certain state while their parents take a certain
	 * instantiation.
	 *
	 * @return intensity matrix
	 */
	public double[][] getQx() {
		return Qx;
	}

	/**
	 * Gets the sufficient statistics of a CIM node.
	 *
	 * @return sufficient statistics.
	 */
	public CTBNSufficientStatistics getSufficientStatistics() {
		return this.sufficientStatistics;
	}

	/**
	 * Samples the next state of the node given the current one and that of its parents. Returns null if not all the *
	 * parents were instantiated.
	 *
	 * @param percentageNoisyTransitions value from 0 to 1 representing the probability of randomly sampling the next
	 *                                   state of the variable
	 * @return sampled state
	 */
	public State sampleNextState(double percentageNoisyTransitions) {
		// Get index current state node
		int idxFromState = getIdxState();
		// Check if noise is added to the sampled data
		if (percentageNoisyTransitions > 0) {
			// Draw noise from Gaussian distribution with mean = 0 and sigma = 1
			NormalDistribution normalDistribution = new NormalDistribution(0, 1);
			double zscore = normalDistribution.inverseCumulativeProbability(percentageNoisyTransitions / 2);
			double noise = normalDistribution.sample();
			// If noise (absolute value) is greater than the z-score (absolute value), draw state index from uniform
			// distribution
			if (Math.abs(noise) > Math.abs(zscore)) {
				// Define random state
				Random rdn = new Random();
				int idxToState = rdn.nextInt(getNumStates());
				while (getNumStates() > 1 && idxToState == idxFromState) {
					// Define random state different from the current one
					idxToState = rdn.nextInt(getNumStates());
				}
				setState(idxToState);
				return new State(getName(), getState());
			}
		}
		// Get index state parents
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
	 * Samples the time that the node stays in its current state given the state of its parents.
	 *
	 * @param stdDeviationGaussianNoiseWaitingTime standard deviation of a Gaussian distribution used to sample noise.
	 *                                             If zero, no noise is added
	 * @return sampled time
	 */
	public double sampleTimeState(double stdDeviationGaussianNoiseWaitingTime) {
		// Get indexes states node and parents
		int idxState = getIdxState();
		int idxStateParents = getIdxStateParents();
		// Intensity of the node of transitioning from the state specified in "evidence"
		// (parameter exponential distribution)
		double q = getQx(idxStateParents, idxState);
		// If the parameter is 0, the node is in an absorbing state that cannot be left
		if (q == 0)
			return Double.POSITIVE_INFINITY;
		// Sample from a uniform distribution to obtain the probability of the time that
		// will be sampled
		double prob = Math.random();
		// Use the quantile function of the exponential distribution with parameter 'q'
		// to sample the time with the previously obtained probability
		double time = -Math.log(1 - prob) / q;
		// Draw noise from Gaussian distribution with mean = 0 and sigma given by user
		Random rdn = new Random();
		double noise = rdn.nextGaussian() * stdDeviationGaussianNoiseWaitingTime;
		// If the noise is negative and makes the waiting time 0 or less, the noise in absolute value is used
		if ((time + noise) <= 0)
			noise = Math.abs(noise);
		time += noise;
		return time;
	}

	/**
	 * Sets the parameters of a node.
	 *
	 * @param Qx  intensity of the variable leaving a certain state while its parents take a certain instantiation
	 * @param Oxy probability of the variable leaving a certain state for another one while its parents take a certain
	 *            instantiation
	 */
	public void setParameters(double[][] Qx, double[][][] Oxy) {
		this.Qx = Qx;
		this.Oxy = Oxy;
	}

	@Override
	public boolean areParametersEstimated() {
		return !(this.Qx == null || this.Oxy == null);
	}

	@Override
	public double estimateLogLikelihood() {
		// Retrieve sufficient statistics of the node
		CTBNSufficientStatistics ss = getSufficientStatistics();
		// Store marginal log-likelihood
		double ll = 0.0;
		for (int idxStateParents = 0; idxStateParents < getNumStatesParents(); idxStateParents++) {
			for (int idxFromState = 0; idxFromState < getNumStates(); idxFromState++) {
				double qx = getQx(idxStateParents, idxFromState);
				// Cases where there are no transitions are ignored to avoid NaNs
				if (qx > 0) {
					double mx = ss.getMx()[idxStateParents][idxFromState];
					double tx = ss.getTx()[idxStateParents][idxFromState];
					// Log probability density function of the exponential distribution
					ll += mx * Math.log(qx) - qx * tx;
					for (int idxToState = 0; idxToState < getNumStates(); idxToState++) {
						if (idxToState != idxFromState) {
							// Probability of transitioning from "state" to "toState"
							double oxx = getOxy(idxStateParents, idxFromState, idxToState);
							// Cases without transitions between the states are ignored to avoid NaNs
							if (oxx != 0) {
								// Number of times the variable transitions from "idxFromState" to "idxToState"
								double mxx = ss.getMxy()[idxStateParents][idxFromState][idxToState];
								ll += mxx * Math.log(oxx);
							}
						}
					}
				}
			}
		}
		return ll;
	}

	@Override
	public void setSufficientStatistics(SufficientStatistics sufficientStatistics) {
		this.sufficientStatistics = (CTBNSufficientStatistics) sufficientStatistics;
	}

	@Override
	public String toString() {
		String discreteNodeDescription = super.toString();
		return discreteNodeDescription + "\n";
	}

}