package com.cig.mctbnc.learning.structure.optimization.scores.ctbn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.cig.mctbnc.data.representation.State;
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
	 * conditional log-likelihood proposed by Friedman et al., 1997 and used for the
	 * first time to learn the structure of a CTBNC by Codecasa and Stella, 2015.
	 * 
	 * @param ctbn continuous time Bayesian network
	 */
	public double compute(CTBN<? extends Node> ctbn) {
		// Store the conditional log-likelihood score
		double cll = 0.0;

//		System.out.println("-----------");
//		System.out.println(ctbn);

		// Parameters of class variables are in the BN, their nodes are retrieved
		BN<CPTNode> bnClassSubgraph = (BN<CPTNode>) ctbn.getBnClassSubgraph();
		List<CPTNode> nodesCVs = bnClassSubgraph.getLearnedNodes();
		// All possible class configurations
		List<List<State>> statesEachCV = nodesCVs.stream().map(nodeCV -> ((DiscreteNode) nodeCV).getStates())
				.collect(Collectors.toList());
		List<State> statesCVs = Util.cartesianProduct(statesEachCV);
		// Store for each class configuration the sums of their log prior probability
		// and the log posterior probability of the sequence given the classes
		Map<State, Double> uPs = new HashMap<State, Double>();
		// Class probability term
		cll += logPriorProbabilityClassVariables(nodesCVs, statesCVs, uPs);
		// Posterior probability of the sequences given the class configuration (only
		// relevant class variables that are parents of the node)
		cll += logPosteriorProbabilitySequences(ctbn, statesCVs, uPs);
		// Prior probability of the sequences (Denominator term)
		cll -= logPriorProbabilitySequence(uPs);
		// Apply the specified penalization function (if available)
		if (penalizationFunctionMap.containsKey(penalizationFunction)) {
			for (CIMNode node : (List<CIMNode>) ctbn.getNodes()) {
				// Overfitting is avoid by penalizing the complexity of the network
				// Number of possible transitions
				int numStates = node.getStates().size();
				int numTransitions = (numStates - 1) * numStates;
				// Number of states of the parents
				int numStatesParents = node.getNumStateParents();
				// Complexity of the network
				int networkComplexity = numTransitions * numStatesParents;
				// Sample size (number of sequences)
				int sampleSize = ctbn.getDataset().getNumDataPoints();
				// Non-negative penalization
				double penalization = penalizationFunctionMap.get(penalizationFunction).applyAsDouble(sampleSize);
				cll -= (double) networkComplexity * penalization;

			}
		}

		//System.out.println(cll);

		return cll;
	}

	private double logPriorProbabilityClassVariables(List<CPTNode> nodesCVs, List<State> statesCVs,
			Map<State, Double> uPs) {
		double lpp = 0;
		// Save the log prior probability for each possible class configuration
		Map<State, Double> lpps = new HashMap<State, Double>();
		// Obtain log prior probabilities of every class variable
		for (CPTNode nodeCV : nodesCVs) {
			List<State> statesParentsCV = nodeCV.getStatesParents();
			for (State stateParentsCV : statesParentsCV)
				for (State stateCV : nodeCV.getStates()) {
					State query = new State(stateParentsCV.getEvents());
					query.addEvents(stateCV.getEvents());
					double nx = nodeCV.getSufficientStatistics().getNx().get(query);
					double ox = nodeCV.getCPT().get(query);
					double prob = 0;
					if (nx > 0) {
						prob = nx * Math.log(ox);
						lpp += prob;
					}
					lpps.put(query, prob);
				}
		}
		// Obtain log prior probability of every class configuration (necessary to
		// compute the denominator term)
		for (State stateCVs : statesCVs) {
			// log probability class configuration
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
				lpcc += lpps.get(query);
			}
			uPs.put(stateCVs, lpcc);
		}
		return lpp;
	}

	private double logPosteriorProbabilitySequences(CTBN<? extends Node> ctbn, List<State> statesCVs,
			Map<State, Double> uPs) {
		double pps = 0.0;
		for (CIMNode node : (List<CIMNode>) ctbn.getNodes()) {
			// Sufficient statistics of the node
			CTBNSufficientStatistics ss = node.getSufficientStatistics();
			// Class variables that are parents of the node
			List<String> nameCVsParentsNode = nameClassVariablesParents(node);
			// Obtain log posterior probability of the sequences given their class
			// configuration
			for (State stateCVs : statesCVs) {
				double ppsCC = 0;
				// All configurations between the node and its parents (feature and class
				// variables)
				Set<State> statesNodeAndParents = node.getQx().keySet();
				for (State stateNodeAndParents : statesNodeAndParents) {
					State fromState = new State(stateNodeAndParents);
					// Get states where class variables that are parents of the node have the states
					// in the currently evaluated class configuration
					for (String nameCVParentNode : nameCVsParentsNode) {
						// If the value of any of the class variable is not equal to the studied one,
						// continue with the next configuration of the node and parents
						if (!fromState.getValueVariable(nameCVParentNode)
								.equals(stateCVs.getValueVariable(nameCVParentNode)))
							continue;
					}
					double qx = node.getQx().get(fromState);
					double nx = ss.getMx().get(fromState);
					double tx = ss.getTx().get(fromState);
					if (qx > 0)
						ppsCC += nx * Math.log(qx) - qx * tx;
					// Maps with probabilities (ox_) and number of occurrences (nx_) of transitions
					// from "fromState" to any other possible state of the feature node
					Map<State, Double> ox_ = node.getOxy().get(fromState);
					Map<State, Double> nx_ = node.getSufficientStatistics().getMxy().get(fromState);
					// Iterate over all states of the feature node (except its state in "fromState")
					for (State toState : ox_.keySet()) {
						double oxx = ox_.get(toState);
						double nxx = nx_.get(toState);
						if (oxx > 0)
							ppsCC += nxx * Math.log(oxx);
					}
				}
				pps += ppsCC;
				uPs.merge(stateCVs, ppsCC, Double::sum);
			}
		}
		return pps;
	}

	private double logPriorProbabilitySequence(Map<State, Double> uPs) {
		// The log-sum-exp trick is used to avoid underflows
		Entry<State, Double> entryLargestUP = Util.getEntryLargestValue(uPs);
		double largestUP = entryLargestUP.getValue();
		// Sum the exponential value of each unnormalized posterior minus the largest
		// unnormalized posterior
		double sum = uPs.values().stream().mapToDouble(uP -> Math.exp(uP - largestUP)).sum();
		// Obtain the prior probability (normalizing constant)
		double nc = largestUP + Math.log(sum);
		return nc;
	}

	/**
	 * Return the name of the class variables that are parent of the given node.
	 * 
	 * @param node
	 * @return
	 */
	private List<String> nameClassVariablesParents(Node node) {
		Stream<Node> CVParents = node.getParents().stream().filter(parent -> parent.isClassVariable());
		return CVParents.map(CVParent -> CVParent.getName()).collect(Collectors.toCollection(ArrayList::new));
	}

	public double compute(CTBN<? extends Node> ctbn, int nodeIndex) {
		return 0;
	}

}
