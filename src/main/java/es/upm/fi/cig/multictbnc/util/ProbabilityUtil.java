package es.upm.fi.cig.multictbnc.util;

import es.upm.fi.cig.multictbnc.data.representation.Sequence;
import es.upm.fi.cig.multictbnc.data.representation.State;
import es.upm.fi.cig.multictbnc.nodes.CIMNode;
import es.upm.fi.cig.multictbnc.nodes.CPTNode;
import es.upm.fi.cig.multictbnc.nodes.DiscreteNode;
import es.upm.fi.cig.multictbnc.nodes.Node;

import java.util.Arrays;
import java.util.List;

/**
 * Utility class with methods related to the estimation of probabilities.
 *
 * @author Carlos Villa Blanco
 */
public final class ProbabilityUtil {

	private ProbabilityUtil() {
	}

	/**
	 * Returns a probability between 0 or 0.3, or between 0.7 and 1.
	 *
	 * @return a probability
	 */
	public static double extremeProbability() {
		if (Math.random() < 0.5) {
			return Math.random() * 0.3;
		}
		return 0.7 + Math.random() * (1 - 0.7);
	}

	/**
	 * Computes the log-likelihood of a sequence, also known as temporal likelihood (Stella and Amer 2012), given the
	 * state of the class variables. This is done by using the CTBN.
	 *
	 * @param <NodeTypeCTBN>      type of the nodes of the continuous-time Bayesian network
	 * @param sequence            sequence evaluated
	 * @param nodesCTBN           nodes of a continuous-time Bayesian network
	 * @param stateClassVariables class configuration
	 * @return log-likelihood of the sequence given the class configuration
	 */
	public static <NodeTypeCTBN> double logLikelihoodSequence(Sequence sequence, List<NodeTypeCTBN> nodesCTBN,
															  State stateClassVariables) {
		// Initialise likelihood
		double ll = 0;
		// Iterate over all the observations of the sequence
		for (int j = 1; j < sequence.getNumObservations(); j++) {
			// Time difference between 'j' and 'j-1' observations
			double currentTimePoint = sequence.getTimeValue(j - 1);
			double nextTimePoint = sequence.getTimeValue(j);
			double deltaTime = nextTimePoint - currentTimePoint;
			for (NodeTypeCTBN node : nodesCTBN) {
				// Obtain node of CTBN
				CIMNode nodeCTBN = (CIMNode) node;
				// Check that the node is from a feature variable
				if (!nodeCTBN.isClassVariable()) {
					// Obtain the current state of the feature node
					String currentValue = sequence.getValueVariable(j - 1, nodeCTBN.getName());
					// Set current state in feature node
					nodeCTBN.setState(currentValue);
					// Set current state in parents of feature node
					for (Node nodeParent : nodeCTBN.getParents()) {
						// Obtain node from model (class or feature variable)
						DiscreteNode nodeParentCTBN = (DiscreteNode) nodeParent;
						String nameParent = nodeParentCTBN.getName();
						String currentValueParent;
						// Check if the parent is a class or a feature variable to retrieve its state
						if (nodeParentCTBN.isClassVariable())
							currentValueParent = stateClassVariables.getValueVariable(nameParent);
						else
							currentValueParent = sequence.getValueFeatureVariable(j - 1, nameParent);
						nodeParentCTBN.setState(currentValueParent);
					}
					int idxState = nodeCTBN.getIdxState();
					int idxStateParents = nodeCTBN.getIdxStateParents();
					// Obtain instantaneous probability of the feature leaving its current state
					// while its parents are in a certain state. NOTE: new feature states, which
					// were not considered during model training, could be found in the test dataset
					double qx = nodeCTBN.getQx(idxStateParents, idxState);
					// Probability of the feature staying in a certain state (while its parent have
					// a particular state) for an amount of time 'deltaTime' is exponentially
					// distributed with parameter 'qx'
					ll += -qx * deltaTime;
					// Get the value of the node for the following observation
					String nextValue = sequence.getValueVariable(j, nodeCTBN.getName());
					// If the feature transitions to another state, get the probability that this
					// occurs given the state of the parents
					if (!currentValue.equals(nextValue)) {
						// Set next state in feature node
						nodeCTBN.setState(nextValue);
						int idxToState = nodeCTBN.getIdxState();
						// Probability that the feature transitions from one state to another while its
						// parents have a certain state. NOTE: the probability would be zero if either
						// the departure or arrival state was not considered during model training
						Double oxy = nodeCTBN.getOxy(idxStateParents, idxState, idxToState);
						// Probability of the feature transition
						double qxy = 1 - Math.exp((-(oxy * qx)) * 0.00001);
						ll += qxy > 0 ? Math.log(qxy) : 0;
					}
				}
			}
		}
		return ll;
	}

	/**
	 * Computes the logarithm of the prior probability of the class variables taking certain values. Their probability
	 * is computed by using the Bayesian network.
	 *
	 * @param <NodeTypeBN> type of the nodes of the Bayesian network
	 * @param nodesBN      nodes of a Bayesian network
	 * @param stateCVs     class configuration
	 * @return logarithm of the prior probability of the class variables
	 */
	public static <NodeTypeBN extends Node> double logPriorProbabilityClassVariables(List<NodeTypeBN> nodesBN,
																					 State stateCVs) {
		double lpriorProbability = 0.0;
		for (NodeTypeBN node : nodesBN) {
			// Obtain class variable node from the BN
			CPTNode nodeBN = (CPTNode) node;
			// Set the state of the node and its parents
			Util.setStateNodeAndParents(nodeBN, stateCVs);
			int idxState = nodeBN.getIdxState();
			int idxStateParents = nodeBN.getIdxStateParents();
			// Probability of the class variable and its parents having a certain state
			double Ox = nodeBN.getCP(idxStateParents, idxState);
			lpriorProbability += Ox > 0 ? Math.log(Ox) : 0;
		}
		return lpriorProbability;
	}

	/**
	 * Computes the marginal log-likelihood of a sequence given the unnormalised log-a-posteriori probability for each
	 * class configuration.
	 *
	 * @param laps unnormalised log-a-posteriori probabilities
	 * @return marginal log-likelihood of a sequence
	 */
	public static double marginalLogLikelihoodSequence(double[] laps) {
		// The log-sum-exp trick is used to avoid underflows
		int idxLargestLap = Util.getIndexLargestValue(laps);
		double largestLap = laps[idxLargestLap];
		// Sum the exponential value of each log-a-posteriori probability minus the
		// largest log-a-posteriori
		double sum = Arrays.stream(laps).map(lap -> Math.exp(lap - largestLap)).sum();
		// Estimate marginal log-likelihood
		return largestLap + Math.log(sum);
	}

}