package com.cig.mctbnc.util;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.cig.mctbnc.data.representation.Observation;
import com.cig.mctbnc.data.representation.Sequence;
import com.cig.mctbnc.data.representation.State;
import com.cig.mctbnc.nodes.CIMNode;
import com.cig.mctbnc.nodes.CPTNode;
import com.cig.mctbnc.nodes.Node;

/**
 * Utility class with methods related with the estimation of probabilities.
 * 
 * @author Carlos Villa Blanco
 *
 */
public final class ProbabilityUtil {

	private ProbabilityUtil() {
	}

	/**
	 * Compute the logarithm of the prior probability of the class variables taking
	 * certain values. Their probability is computed by using the Bayesian network.
	 * 
	 * @param nodesBN
	 * @param stateClassVariables
	 * @return logarithm of the prior probability of the class variables
	 */
	public static <NodeTypeBN extends Node> double logPriorProbabilityClassVariables(List<NodeTypeBN> nodesBN,
			State stateClassVariables) {
		double lpriorProbability = 0.0;
		for (NodeTypeBN node : nodesBN) {
			// Obtain class variable node from the BN
			CPTNode nodeBN = (CPTNode) node;
			// Obtain parents of the node
			List<Node> parentNodes = nodeBN.getParents();
			// Define State object for node and parents having certain values
			State query = new State();
			// Add value of the class variable
			query.addEvent(nodeBN.getName(), stateClassVariables.getValueVariable(nodeBN.getName()));
			// Add values of the parents
			for (Node parentNode : parentNodes) {
				String nameParent = parentNode.getName();
				query.addEvent(nameParent, stateClassVariables.getValueVariable(nameParent));
			}
			// Probability of the class variable and its parents having a certain state
			Double Ox = nodeBN.getCPT().get(query);
			lpriorProbability += Ox != null && Ox > 0 ? Math.log(Ox) : 0;
		}
		return lpriorProbability;
	}

	/**
	 * Compute the log-likelihood of a sequence, also known as temporal likelihood
	 * (Stella and Amer 2012), given the state of the class variables. This is done
	 * by using the CTBN.
	 * 
	 * @param sequence
	 * @param nodesCTBN
	 * @param stateClassVariables
	 * @return log-likelihood of the sequence given a class configuration
	 */
	public static <NodeTypeCTBN> double logLikelihoodSequence(Sequence sequence, List<NodeTypeCTBN> nodesCTBN,
			State stateClassVariables) {
		// Get observations of the sequence
		List<Observation> observations = sequence.getObservations();
		// Initialize likelihood
		double ll = 0;
		// Iterate over all the observations of the sequence
		for (int j = 1; j < observations.size(); j++) {
			// Get observations 'j-1' (current) and 'j' (next) of the sequence
			Observation currentObservation = observations.get(j - 1);
			Observation nextObservation = observations.get(j);
			// Time difference between 'j' and 'j-1' observations
			double currentTimePoint = currentObservation.getTimeValue();
			double nextTimePoint = nextObservation.getTimeValue();
			double deltaTime = nextTimePoint - currentTimePoint;
			for (NodeTypeCTBN node : nodesCTBN) {
				// Obtain node of CTBN
				CIMNode nodeCTBN = (CIMNode) node;
				// Check that the node is from a feature
				if (!nodeCTBN.isClassVariable()) {
					// Obtain current value of the feature node
					String currentValue = currentObservation.getValueVariable(nodeCTBN.getName());
					// Obtain parents of the feature node
					List<Node> parentNodes = nodeCTBN.getParents();
					// Define State object for the node and parents (if any) having certain state
					State fromState = new State();
					// Add value of the feature to the State object
					fromState.addEvent(nodeCTBN.getName(), currentValue);
					// Add values of the parents to the State object
					for (Node parentNode : parentNodes) {
						String nameParent = parentNode.getName();
						// Check if the parent is a class variable or a feature to retrieve its state
						if (parentNode.isClassVariable())
							fromState.addEvent(nameParent, stateClassVariables.getValueVariable(nameParent));
						else
							fromState.addEvent(nameParent, currentObservation.getValueVariable(nameParent));
					}
					// Obtain instantaneous probability of the feature leaving its current state
					// while its parents are in a certain state. NOTE: new feature states, which
					// were not considered during model training, could be found in the test dataset
					Double qx = nodeCTBN.getQx().get(fromState);
					qx = qx != null ? qx : 0;
					// Probability of the feature staying in a certain state (while its parent have
					// a particular state) for an amount of time 'deltaTime' is exponentially
					// distributed with parameter 'qx'
					ll += -qx * deltaTime;
					// Get value of the node for the following observation
					String nextValue = nextObservation.getValueVariable(nodeCTBN.getName());
					// If the feature transitions to another state, get the probability that this
					// occurs given the state of the parents
					if (!currentValue.equals(nextValue)) {
						// Define State object with next feature value to get the correct parameter
						State toState = new State();
						toState.addEvent(nodeCTBN.getName(), nextValue);
						// Probability that the feature transitions from one state to another while its
						// parents have a certain state. NOTE: the probability would be zero if either
						// the departure or arrival state was not considered during model training
						Map<State, Double> Oxx = nodeCTBN.getOxy().get(fromState);
						if (Oxx != null) {
							Double oxy = Oxx.get(toState);
							double qxy = 0;	
							if (oxy != null) {
								// Instantaneous probability of feature moving from "fromState" to "toState"
								qxy = oxy * qx;
							}
							ll += qxy > 0 ? Math.log(qxy) : 0;
						}
					}
				}
			}
		}
		return ll;
	}

	/**
	 * Compute the marginal log-likelihood of a sequence given the unnormalized
	 * log-a-posteriori probability for each class configuration.
	 * 
	 * @param laps unnormalized log-a-posteriori probabilities
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
		double mll = largestLap + Math.log(sum);
		return mll;
	}

	/**
	 * Return a probability between 0 or 0.3, or between 0.7 and 1.
	 * 
	 * @return probability
	 */
	public static double extremeProbability() {
		if (Math.random() < 0.5) {
			return Math.random() * 0.3;
		} else {
			return 0.7 + Math.random() * (1 - 0.7);
		}
	}

}
