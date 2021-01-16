package com.cig.mctbnc.learning.structure.optimization.scores.ctbn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.cig.mctbnc.data.representation.Dataset;
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
		return 0;
	}

	/**
	 * Compute the (penalized) conditional log-likelihood score at a given node of a
	 * discrete continuous time Bayesian network. This is based on the idea of
	 * optimizing the conditional log-likelihood proposed by Friedman et al., 1997
	 * and used for the first time to learn the structure of a CTBNC by Codecasa and
	 * Stella, 2015.
	 * 
	 * @param ctbn                 continuous time Bayesian network
	 * @param nodeIndex            index of the node
	 * @param penalizationFunction penalization function
	 * @return (penalized) conditional log-likelihood score
	 */
	public double compute(CTBN<? extends Node> ctbn, int nodeIndex) {
		// Obtain node to evaluate
		CIMNode node = (CIMNode) ctbn.getNodes().get(nodeIndex);
		// Store the conditional log-likelihood score
		double cll = 0.0;

		System.out.println("-----------");
		System.out.println("Nodo: " + node.getName());
		System.out.println(Arrays.toString(node.getParents().stream().map(nodop -> nodop.getName()).toArray()));

		// The node has no class variables as parents. The worst score is given to this
		// structure.
//		if (!hasClassVariablesAsParent(node))
//			return Double.NEGATIVE_INFINITY;

		// Obtain possible states of the class variables that are parents of the node
		// List<String> nameCVs = nameClassVariablesParents(node);

		// Parameters of class variables are in the BN, their nodes are retrieved
		BN<CPTNode> bnClassSubgraph = (BN<CPTNode>) ctbn.getBnClassSubgraph();
		List<CPTNode> nodesCVs = bnClassSubgraph.getLearnedNodes();
		// Name class variables
		List<String> nameCVs = bnClassSubgraph.getNameVariables();

		// Es necesario calcular las probabilidades de las variables clase y la
		// likelihood para calcular el denominador. El problema es que para la
		// probabilidad a priori se consideran todas las variables clase y para los
		// demás términos solo las variables clase padres de features.

		// Class probability term
		Map<State, Double> norm = new HashMap<State, Double>();

		double cpt = 0;
		for (CPTNode nodeCV : nodesCVs) {
			List<State> statesParentsCV = nodeCV.getStatesParents();
			for (State stateParentsCV : statesParentsCV) {
				for (State stateCV : nodeCV.getStates()) {
					State query = new State(stateParentsCV.getEvents());
					query.addEvents(stateCV.getEvents());
					double nx = nodeCV.getSufficientStatistics().getNx().get(query);
					double ox = nodeCV.getCPT().get(query);
					if (nx > 0) {
						double prob = nx * Math.log(ox);
						cpt += prob;
						norm.put(query, prob);
					}
				}
			}
		}

		cll += cpt;

		// Posterior probability of the sequences given the class configuration (only
		// relevant those that are parents of the node)
		double pps = 0.0;
		// Sufficient statistics of the node
		CTBNSufficientStatistics ss = node.getSufficientStatistics();
		// States the feature node and its parents (features and class variables) take
		Set<State> statesNodeAndParents = node.getQx().keySet();
		for (State fromState : statesNodeAndParents) {
			double qx = node.getQx().get(fromState);
			double nx = ss.getMx().get(fromState);
			double tx = ss.getTx().get(fromState);
			if (qx > 0)
				pps += nx * Math.log(qx) - qx * tx;
			// Maps with probabilities (ox_) and number of occurrences (nx_) of transitions
			// from "fromState" to any other possible state of the feature node
			Map<State, Double> ox_ = node.getOxy().get(fromState);
			Map<State, Double> nx_ = node.getSufficientStatistics().getMxy().get(fromState);
			// Iterate over all states of the feature node (except its state in "fromState")
			for (State toState : ox_.keySet()) {
				double oxx = ox_.get(toState);
				double nxx = nx_.get(toState);
				if (oxx > 0)
					pps += nxx * Math.log(oxx);
			}
		}
		cll += pps;

		// Marginal log-likelihood

		// Obtain possible states of the class variables
		List<State> statesCVs = ctbn.getDataset().getPossibleStatesVariables(nameCVs);

		double mll = 0.0;

		for (State stateCVs : statesCVs) {

			double mllStateCVs = 1;

			for (CPTNode nodeCV : nodesCVs) {

				List<String> nameParentsCV = nodeCV.getNameParents();

				State query = new State();
				query.addEvent(nodeCV.getName(), stateCVs.getValueVariable(nodeCV.getName()));

				String[] valuesParents = stateCVs.getValueVariables(nameParentsCV);

				for (int i = 0; i < nameParentsCV.size(); i++)
					query.addEvent(nameParentsCV.get(i), valuesParents[i]);

				mllStateCVs *= nodeCV.getCPT().get(query);

			}

			for (State fromState : statesNodeAndParents) {

				List<Node> CVParents = node.getParents().stream().filter(nodeCVAux -> nodeCVAux.isClassVariable())
						.collect(Collectors.toList());

				boolean exit = true;

				for (Node parentOfNode : CVParents) {
					if (!fromState.getValueVariable(parentOfNode.getName())
							.equals(stateCVs.getValueVariable(parentOfNode.getName()))) {
						break;
					}
					exit = false;
				}

				if (exit)
					continue;

				double qx = node.getQx().get(fromState);
				double mx = ss.getMx().get(fromState);
				double tx = ss.getTx().get(fromState);

				mllStateCVs *= Math.pow(qx, mx) * Math.exp(-qx * tx);

				// Maps with probabilities (ox_) and number of occurrences (nx_) of transitions
				// from "fromState" to any other possible state of the feature node
				Map<State, Double> ox_ = node.getOxy().get(fromState);
				Map<State, Double> mx_ = node.getSufficientStatistics().getMxy().get(fromState);
				// Iterate over all states of the feature node (except its state in "fromState")
				for (State toState : ox_.keySet()) {
					double oxx = ox_.get(toState);
					double mxx = mx_.get(toState);
					if (oxx > 0) {
						mllStateCVs *= Math.pow(oxx, mxx);

					}
				}

			}

			mll += mllStateCVs;
		}

		mll = Math.log(mll);

		cll -= mll;

		// Compute unnormalized posteriors given each possible state of the class
		// variables
//		double[] uPs = new double[statesCVs.size()];
//		for (int i = 0; i < statesCVs.size(); i++) {
//			// Class probability term
//			uPs[i] = logPriorProbabilityClass(nodesCVs, statesCVs.get(i), ctbn.getDataset());
//			// Posterior probability of the sequences given the class configuration
//			uPs[i] += logPosteriorProbabilitySequence(node, statesCVs.get(i));
//			// Add the class probability and posterior probability
//			cll += uPs[i];
//		}

//		System.out.println("Without denominator: " + cll);

//		// Prior probability of the sequences (Denominator term)
//		// The log-sum-exp trick is used to avoid underflows
//		int idxLargestUP = Util.getIndexLargestValue(uPs);
//		double largestUP = uPs[idxLargestUP];
//		// Sum the exponential value of each unnormalized posterior minus the largest
//		// unnormalized posterior
//		double sum = Arrays.stream(uPs).map(uP -> Math.exp(uP - largestUP)).sum();
//		// Obtain the prior probability (normalizing constant)
//		double nc = largestUP + Math.log(sum);
//		cll -= nc;
//
//		// Apply the specified penalization function (if available)
		if (penalizationFunctionMap.containsKey(penalizationFunction)) {
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

		System.out.println(cll);

		return cll;
	}

	/**
	 * Estimate prior probability of a class configuration (stateCVs). This method
	 * will receive the class configuration of those class variables that are
	 * parents of a certain feature (observed class variables). However, it is
	 * necessary to also extract the parents of those class variables (unobserved
	 * class variables) to marginalize them out.
	 * 
	 * @param nodesCVs nodes of the studied class variables
	 * @param stateCVs state (class configuration) of the studied class variables
	 * @param dataset
	 * @return
	 */
	private double logPriorProbabilityClass(List<CPTNode> nodesCVs, State stateCVs, Dataset dataset) {
		System.out.println("Prior prob. class variables");

		double lp = 0.0;
		// Iterate over class variables nodes
		for (CPTNode node : nodesCVs) {
			// Extract value of the node from the studied class configuration
			String valueNode = stateCVs.getValueVariable(node.getName());
			// State containing the state of class variables in "stateCVs" and their parents
			State query = new State();
			// Add class of the node to the state
			query.addEvent(node.getName(), valueNode);
			// Obtain the parents of the class variable
			List<Node> parents = node.getParents();
			// Class variables that are parents of "nodeCV" but not of the feature
			List<Node> parentsUnobserved = new ArrayList<Node>();
			for (Node parent : parents) {
				String valueParent = stateCVs.getValueVariable(parent.getName());
				if (valueParent != null)
					// The parent is also parent of the feature
					query.addEvent(parent.getName(), valueParent);
				else
					// Class variable has a parent that is not parent of the studied feature
					parentsUnobserved.add(parent);
			}
			// To obtain probability of class configuration is necessary to marginalize out
			// their parents, which are not observed
			if (parentsUnobserved.size() > 0) {
				// Class variable has parents that are not included in "stateClassVariables"
				// (they are not observed). Therefore, to obtain the probability of the class
				// configuration is necessary to marginalize them out
				List<String> nameParentsUnobserved = parentsUnobserved.stream().map(parent -> parent.getName())
						.collect(Collectors.toList());
				// Extract all class configurations from unobserved class variables
				List<State> states = dataset.getPossibleStatesVariables(nameParentsUnobserved);
				for (State state : states) {
					// Add to class configuration of unobserved class variables the state of
					// observed class variables
					state.addEvents(query.getEvents());
					double nx = node.getSufficientStatistics().getNx().get(state);
					double ox = node.getCPT().get(state);
					if (ox > 0) {
						lp += nx * Math.log(ox);

						System.out.println("+ " + nx + " * Math.log(" + ox + ")");
					}
				}
			} else {
				// All parents of the class variables are observed
				double nx = node.getSufficientStatistics().getNx().get(query);
				double ox = node.getCPT().get(query);
				if (ox > 0) {
					lp += nx * Math.log(ox);

					System.out.println("+ " + nx + " * Math.log(" + ox + ")");
				}
			}
		}
		return lp;
	}

	/**
	 * Estimate posterior probability of the sequences, considering only a certain
	 * feature node, given a class configuration of the class variables that are
	 * parent of that node.
	 * 
	 * @param node     studied feature node
	 * @param stateCVs state (class configuration) of the class variables that are
	 *                 parents of "node"
	 * @return
	 */
	private double logPosteriorProbabilitySequence(CIMNode node, State stateCVs) {
		System.out.println("Posterior prob. sequence");

		// Sufficient statistics of the node
		CTBNSufficientStatistics ss = node.getSufficientStatistics();
		// Names of the class variables that are parents of the feature node
		List<String> namesCVs = stateCVs.getNameVariables();
		// States the feature node and its parents (features and class variables) take
		Set<State> statesNodeAndParents = node.getQx().keySet();
		double lpp = 0;
		// To compute the probability of the transitions of the node for all the
		// sequences given a class configuration, it is necessary to marginalize out
		// those features that are parents of the node (unobserved variables)
		for (State fromState : statesNodeAndParents) {
			// Check that "fromState" contains the studied class configuration
			boolean containsCC = namesCVs.stream()
					.allMatch(nameCV -> fromState.getValueVariable(nameCV).equals(stateCVs.getValueVariable(nameCV)));
			if (containsCC) {
				double qx = node.getQx().get(fromState);
				double nx = ss.getMx().get(fromState);
				double tx = ss.getTx().get(fromState);
				if (qx > 0) {
					lpp += nx * Math.log(qx) - qx * tx;

					System.out.println("+ " + nx + " * Math.log(" + qx + ") - " + qx + " * " + tx);

				}
				// Maps with probabilities (ox_) and number of occurrences (nx_) of transitions
				// from "fromState" to any other possible state of the feature node
				Map<State, Double> ox_ = node.getOxy().get(fromState);
				Map<State, Double> nx_ = node.getSufficientStatistics().getMxy().get(fromState);
				// Iterate over all states of the feature node (except its state in "fromState")
				for (State toState : ox_.keySet()) {
					double oxx = ox_.get(toState);
					double nxx = nx_.get(toState);
					if (oxx > 0) {
						lpp += nxx * Math.log(oxx);

						System.out.println("+ " + nxx + " * Math.log(" + oxx + ")");

					}
				}
			}
		}
		return lpp;

	}

	/**
	 * Check if a node has a class variable as parent.
	 * 
	 * @param node
	 * @return
	 */
	private boolean hasClassVariablesAsParent(Node node) {
		return node.getParents().stream().anyMatch(parent -> parent.isClassVariable());
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

}
