package com.cig.mctbnc.learning.structure.optimization.scores.ctbn;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.cig.mctbnc.data.representation.State;
import com.cig.mctbnc.learning.parameters.bn.BNSufficientStatistics;
import com.cig.mctbnc.learning.parameters.ctbn.CTBNSufficientStatistics;
import com.cig.mctbnc.learning.structure.optimization.scores.AbstractLogLikelihood;
import com.cig.mctbnc.models.BN;
import com.cig.mctbnc.models.CTBN;
import com.cig.mctbnc.nodes.CIMNode;
import com.cig.mctbnc.nodes.CPTNode;
import com.cig.mctbnc.nodes.DiscreteNode;
import com.cig.mctbnc.nodes.Node;
import com.cig.mctbnc.util.Util;

/**
 * Implements the conditional log-likelihood score to perform a discriminative
 * training.
 */
public class CTBNConditionalLogLikelihood extends AbstractLogLikelihood implements CTBNScoreFunction {

	/**
	 * Receives the name of the penalization function used for the structure
	 * complexity.
	 * 
	 * @param penalizationFunction
	 */
	public CTBNConditionalLogLikelihood(String penalizationFunction) {
		super(penalizationFunction);
	}

	/**
	 * Compute the (penalized) conditional log-likelihood score of a discrete
	 * continuous time Bayesian network. This is based on the idea of optimizing the
	 * conditional log-likelihood proposed by Friedman et al., 1997.
	 * 
	 * @param ctbn continuous time Bayesian network
	 */
	public double compute(CTBN<? extends Node> ctbn) {
		// Store the conditional log-likelihood score
		double cll = 0.0;
		// Parameters of class variables are in the BN, their nodes are retrieved
		BN<CPTNode> bnClassSubgraph = (BN<CPTNode>) ctbn.getBnClassSubgraph();
		List<CPTNode> nodesCVs = bnClassSubgraph.getLearnedNodes();
		// All possible class configurations
		List<List<State>> statesEachCV = nodesCVs.stream().map(nodeCV -> ((DiscreteNode) nodeCV).getStates())
				.collect(Collectors.toList());
		List<State> statesCVs = Util.cartesianProduct(statesEachCV);
		// Class probability term
		cll += logPriorProbabilityClassVariables(nodesCVs, statesCVs);
		// Log-likelihood of the sequences given the class configuration
		cll += logLikelihoodSequences(ctbn, statesCVs);
		// Log-marginal-likelihood of sequences (denominator/normalizing term)
		cll -= logMarginalLikelihoodSequences(nodesCVs, ctbn, statesCVs);
		// Apply the specified penalization function (if available)
		cll -= getPenalization(ctbn);
		return cll;
	}

	/**
	 * Compute the log prior probability of the class variables.
	 * 
	 * @param nodesCVs
	 * @param statesCVs
	 * @return
	 */
	private double logPriorProbabilityClassVariables(List<CPTNode> nodesCVs, List<State> statesCVs) {
		double lpp = 0;
		// Save the log prior probability for each possible class configuration
		Map<State, Double> lpps = new HashMap<State, Double>();
		// Obtain log prior probabilities of every class variable
		for (CPTNode nodeCV : nodesCVs) {
			// Retrieve sufficient statistics of the node
			BNSufficientStatistics ss = nodeCV.getSufficientStatistics();
			// Iterate over the states of the class variable and its parents
			for (int idxStateParents = 0; idxStateParents < nodeCV.getNumStatesParents(); idxStateParents++) {
				for (int idxState = 0; idxState < nodeCV.getNumStates(); idxState++) {
					// Counts for class variable taking the state given the states of parents
					nodeCV.setState(idxState);
					nodeCV.setStateParents(idxStateParents);
					double nx = ss.getNx()[idxStateParents][idxState];
					// Probability class variable takes the state given the states of parents
					State state = nodeCV.getStateNodeAndParents();
					double ox = nodeCV.getCPT().get(state);
					double prob = 0;
					if (nx > 0) {
						prob = nx * Math.log(ox);
						lpp += prob;
					}
					lpps.put(state, prob);
				}
			}
		}
		return lpp;
	}

	/**
	 * Compute the log-likelihood of sequences given the class variables. Only class
	 * variables that are parents of each node are relevant.
	 * 
	 * @param ctbn
	 * @param statesCVs
	 * @return
	 */
	private double logLikelihoodSequences(CTBN<? extends Node> ctbn, List<State> statesCVs) {
		double lls = 0.0;
		// Compute log-likelihood of the sequences given the class variables
		for (CIMNode node : (List<CIMNode>) ctbn.getNodes()) {
			// Sufficient statistics of the node
			CTBNSufficientStatistics ss = node.getSufficientStatistics();
			int numNodeStates = node.getNumStates();
			int numParentsStates = node.getNumStatesParents();
			// Iterate over all states of the node (from where a transition begins)
			for (int idxFromStateNode = 0; idxFromStateNode < numNodeStates; idxFromStateNode++) {
				// Iterate over all states of the node's parents
				for (int idxStateParents = 0; idxStateParents < numParentsStates; idxStateParents++) {
					node.setState(idxFromStateNode);
					node.setStateParents(idxStateParents);
					State fromState = node.getStateNodeAndParents();
					// Retrieve parameters and sufficient statistics for evaluated states
					double qx = node.getQx().get(fromState);
					double mx = ss.getMx()[idxStateParents][idxFromStateNode];
					double tx = ss.getTx()[idxStateParents][idxFromStateNode];
					if (qx > 0) {
						lls += mx * Math.log(qx) - qx * tx;
						// Maps with probabilities (ox_) and number of occurrences (nx_) of transitions
						// from "fromState" to any other possible state of the feature node
						Map<State, Double> ox_ = node.getOxy().get(fromState);
						// Iterate over all states of the feature node (except its state in "fromState")
						for (int idxToStateNode = 0; idxToStateNode < numNodeStates; idxToStateNode++) {
							if (idxToStateNode != idxFromStateNode) {
								node.setState(idxStateParents);
								State toState = node.indexToState(idxToStateNode);
								// Retrieve parameters and sufficient statistics for evaluated states
								double oxx = ox_.get(toState);
								double mxy = ss.getMxy()[idxStateParents][idxFromStateNode][idxToStateNode];
								if (oxx > 0)
									lls += mxy * Math.log(oxx);
							}
						}
					}
				}
			}
		}
		return lls;
	}

	/**
	 * Compute the log-marginal-likelihoods of the sequences (denominator term).
	 * 
	 * @param nodesCVs
	 * @param ctbn
	 * @param statesCVs
	 * @return
	 */
	private double logMarginalLikelihoodSequences(List<CPTNode> nodesCVs, CTBN<? extends Node> ctbn,
			List<State> statesCVs) {
		// Store for each class configuration the sums of their log prior probability
		// and the log likelihood of the sequence given the classes
		Map<State, Double> uPs = new HashMap<State, Double>();
		// Obtain log prior probability of every class configuration
		logPriorProbabilityClassConfigurations(nodesCVs, statesCVs, uPs);
		// Compute log likelihood of sequences given every class configuration
		logLikelihoodSequencesGivenClassConfigurations(ctbn, statesCVs, uPs);
		// The log-sum-exp trick is used to avoid underflows
		Entry<State, Double> entryLargestUP = Util.getEntryLargestValue(uPs);
		double largestUP = entryLargestUP.getValue();
		// Sum the exponential value of each unnormalized posterior minus the largest
		// unnormalized posterior
		double sum = uPs.values().stream().mapToDouble(uP -> Math.exp(uP - largestUP)).sum();
		// Obtain the prior probability (normalizing constant)
		return largestUP + Math.log(sum);
	}

	/**
	 * Compute the log prior probability of every class configuration and stored
	 * them in Map 'uPs'.
	 * 
	 * @param nodesCVs
	 * @param statesCVs
	 * @param uPs
	 */
	private void logPriorProbabilityClassConfigurations(List<CPTNode> nodesCVs, List<State> statesCVs,
			Map<State, Double> uPs) {
		for (State stateCVs : statesCVs) {
			// log prior probability class configuration
			double lpcc = 0;
			// Estimate the probabilities of each class variable of taking the states
			// defined in the class configuration
			for (CPTNode nodeCV : nodesCVs) {
				List<String> namesParentsCV = nodeCV.getNameParents();
				State query = new State();
				String stateCV = stateCVs.getValueVariable(nodeCV.getName());
				query.addEvent(nodeCV.getName(), stateCV);
				for (String nameParentCV : namesParentsCV) {
					String statesParentCV = stateCVs.getValueVariable(nameParentCV);
					query.addEvent(nameParentCV, statesParentCV);
				}
				lpcc += nodeCV.getCPT().get(query);
			}
			uPs.put(stateCVs, lpcc);
		}
	}

	/**
	 * Compute the log-likelihood of the sequences given each possible class
	 * configuration and stored them in Map 'uPs'.
	 * 
	 * @param ctbn
	 * @param statesCVs
	 * @param uPs
	 */
	private void logLikelihoodSequencesGivenClassConfigurations(CTBN<? extends Node> ctbn, List<State> statesCVs,
			Map<State, Double> uPs) {
		for (State stateCVs : statesCVs) {
			for (CIMNode node : (List<CIMNode>) ctbn.getNodes()) {
				// Sufficient statistics of the node
				CTBNSufficientStatistics ss = node.getSufficientStatistics();
				// Log-likelihood sequences for given class configuration
				double llsCC = 0;
				// Number of states of the node and its parents
				int numNodeStates = node.getNumStates();
				int numParentsStates = node.getNumStatesParents();
				// Iterate over all states of the node (from where a transition begins)
				for (int idxFromStateNode = 0; idxFromStateNode < numNodeStates; idxFromStateNode++) {
					// Iterate over all states of the node's parents
					stateParents: for (int idxStateParents = 0; idxStateParents < numParentsStates; idxStateParents++) {
						node.setState(idxFromStateNode);
						node.setStateParents(idxStateParents);
						// Consider configurations where parent class variables take the states in the
						// currently evaluated class configuration
						for (Node parentNode : node.getParents()) {
							if (parentNode.isClassVariable()) {
								String stateCVParent = ((DiscreteNode) parentNode).getState();
								String stateCVinCC = stateCVs.getValueVariable(parentNode.getName());
								if (!stateCVParent.equals(stateCVinCC))
									continue stateParents;
							}
						}
						State fromState = node.getStateNodeAndParents();
						double qx = node.getQx().get(fromState);
						double mx = ss.getMx()[idxStateParents][idxFromStateNode];
						double tx = ss.getTx()[idxStateParents][idxFromStateNode];
						if (qx > 0) {
							llsCC += (mx * Math.log(qx)) + (-qx * tx);
							// Maps with probabilities (ox_) and number of occurrences (nx_) of transitions
							// from "fromState" to any other possible state of the feature node
							Map<State, Double> ox_ = node.getOxy().get(fromState);
							// Iterate over all states of the feature node (except its state in "fromState")
							for (int idxToStateNode = 0; idxToStateNode < numNodeStates; idxToStateNode++) {
								if (idxToStateNode != idxFromStateNode) {
									node.setState(idxStateParents);
									State toState = node.getStates().get(idxToStateNode);
									double oxx = ox_.get(toState);
									double mxy = ss.getMxy()[idxStateParents][idxFromStateNode][idxToStateNode];
									if (oxx > 0)
										llsCC += (mxy * Math.log(oxx));
								}
							}
						}
					}
				}
				// Save log-likelihood sequences for the evaluated class configuration
				uPs.merge(stateCVs, llsCC, Double::sum);
			}
		}
	}

	/**
	 * Define the total penalization to apply to the given model.
	 * 
	 * @param ctbn
	 * @return
	 */
	private double getPenalization(CTBN<? extends Node> ctbn) {
		double totalPenalization = 0.0;
		if (penalizationFunctionMap.containsKey(penalizationFunction)) {
			for (CIMNode node : (List<CIMNode>) ctbn.getNodes()) {
				// Overfitting is avoid by penalizing the complexity of the network
				// Number of possible transitions
				int numStates = node.getNumStates();
				int numTransitions = (numStates - 1) * numStates;
				// Number of states of the parents
				int numStatesParents = node.getNumStatesParents();
				// Complexity of the network
				int networkComplexity = numTransitions * numStatesParents;
				// Sample size (number of sequences)
				int sampleSize = ctbn.getDataset().getNumDataPoints();
				// Non-negative penalization
				double penalization = penalizationFunctionMap.get(penalizationFunction).applyAsDouble(sampleSize);
				totalPenalization += (double) networkComplexity * penalization;
			}
		}
		return totalPenalization;
	}

	public double compute(CTBN<? extends Node> ctbn, int nodeIndex) {
		return 0;
	}

}
