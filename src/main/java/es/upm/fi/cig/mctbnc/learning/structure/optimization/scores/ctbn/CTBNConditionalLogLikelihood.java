package es.upm.fi.cig.mctbnc.learning.structure.optimization.scores.ctbn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.DoubleUnaryOperator;

import es.upm.fi.cig.mctbnc.data.representation.State;
import es.upm.fi.cig.mctbnc.learning.parameters.bn.BNSufficientStatistics;
import es.upm.fi.cig.mctbnc.learning.parameters.ctbn.CTBNSufficientStatistics;
import es.upm.fi.cig.mctbnc.learning.structure.optimization.scores.AbstractLikelihood;
import es.upm.fi.cig.mctbnc.models.BN;
import es.upm.fi.cig.mctbnc.models.CTBN;
import es.upm.fi.cig.mctbnc.nodes.CIMNode;
import es.upm.fi.cig.mctbnc.nodes.CPTNode;
import es.upm.fi.cig.mctbnc.nodes.DiscreteNode;
import es.upm.fi.cig.mctbnc.nodes.Node;
import es.upm.fi.cig.mctbnc.util.Util;

/**
 * Implements the conditional log-likelihood score for MCTBNCs with nodes that
 * have CPTs and CIMs to define its bridge and feature subgraph (represented by
 * a CTBN). This score is used to perform a discriminative training.
 */
public class CTBNConditionalLogLikelihood extends AbstractLikelihood implements CTBNScoreFunction {
	// Store class configurations
	List<State> statesCVs;

	/**
	 * Receives the name of the penalization function for the structure complexity.
	 * 
	 * @param penalizationFunction name of the penalization function
	 */
	public CTBNConditionalLogLikelihood(String penalizationFunction) {
		super(penalizationFunction);
	}

	/**
	 * Computes the (penalized) conditional log-likelihood score of a discrete
	 * continuous time Bayesian network. This is based on the idea of optimizing the
	 * conditional log-likelihood proposed by Friedman et al., 1997.
	 * 
	 * @param ctbn a continuous time Bayesian network
	 */
	@Override
	@SuppressWarnings("unchecked")
	public double compute(CTBN<? extends Node> ctbn) {
		// Store the conditional log-likelihood score
		double cll = 0.0;
		// Parameters of class variables are in the BN
		BN<CPTNode> bn = (BN<CPTNode>) ctbn.getBnClassSubgraph();
		// Class probability term
		cll += logPriorProbabilityClassVariables(bn);
		// Log-likelihood of the sequences
		cll += logLikelihoodSequences(ctbn);
		// Log-marginal-likelihood of sequences (denominator/normalizing term)
		cll -= logMarginalLikelihoodSequences(bn, ctbn);
		// Apply the specified penalization function (if available)
		cll -= getPenalization(ctbn);
		return cll;
	}

	@Override
	@SuppressWarnings("unchecked")
	public double compute(CTBN<? extends Node> ctbn, int nodeIndex) {
		// Store the conditional log-likelihood score
		double cll = 0.0;
		// Parameters of class variables are in the BN
		BN<CPTNode> bn = (BN<CPTNode>) ctbn.getBnClassSubgraph();
		// Class probability term
		cll += logPriorProbabilityClassVariables(bn);
		// Log-likelihood of the sequences
		cll += logLikelihoodSequences(ctbn, nodeIndex);
		// Log-marginal-likelihood of sequences (denominator/normalizing term)
		cll -= logMarginalLikelihoodSequences(bn, ctbn, nodeIndex);
		// Apply the specified penalization function (if available)
		cll -= getPenalization(ctbn, nodeIndex);
		return cll;
	}

	@Override
	public boolean isDecomposable() {
		return false;
	}

	@Override
	public String getIdentifier() {
		return "Conditional log-likelihood";
	}

	@Override
	public String getPenalization() {
		return this.penalizationFunction;
	}

	/**
	 * Returns {@code State} objects with all class configurations. They are saved
	 * as a global variable to avoid recomputing them.
	 * 
	 * @param nodesCVs nodes of the class variables
	 * @return states with all class configurations
	 */
	private List<State> getClassConfigurations(List<CPTNode> nodesCVs) {
		if (!checkValidityCCs(nodesCVs)) {
			List<List<State>> statesEachCV = new ArrayList<List<State>>();
			for (CPTNode nodeCV : nodesCVs) {
				List<State> statesNode = new ArrayList<State>();
				for (String valueState : nodeCV.getStates())
					statesNode.add(new State(Map.of(nodeCV.getName(), valueState)));
				statesEachCV.add(statesNode);
			}
			this.statesCVs = Util.cartesianProduct(statesEachCV);
		}
		return this.statesCVs;
	}

	/**
	 * Checks if the saved class configurations are still valid.
	 * 
	 * @param nodesCVs nodes of the class variables
	 * @return true if the currently saved class configurations are still valid,
	 *         false otherwise
	 */
	private boolean checkValidityCCs(List<CPTNode> nodesCVs) {
		// Check if the class configurations were previously obtained
		if (this.statesCVs == null)
			return false;
		// Check if it contains the currently studied class variables
		for (CPTNode nodeCV : nodesCVs) {
			if (this.statesCVs.get(0).getValueVariable(nodeCV.getName()) == null)
				return false;
		}
		return true;
	}

	/**
	 * Computes the log prior probability of the class variables.
	 * 
	 * @param bnClassSubgraph Bayesian network with the class variables (class
	 *                        subgraph)
	 * @return log prior probability of the class variables
	 */
	private double logPriorProbabilityClassVariables(BN<CPTNode> bnClassSubgraph) {
		// Retrieve nodes BN
		List<CPTNode> nodesCVs = bnClassSubgraph.getLearnedNodes();
		double lpp = 0;
		// Obtain log prior probabilities of every class variable
		for (CPTNode nodeCV : nodesCVs) {
			// Retrieve sufficient statistics of the node
			BNSufficientStatistics ss = nodeCV.getSufficientStatistics();
			// Iterate over the states of the class variable and its parents
			for (int idxStateParents = 0; idxStateParents < nodeCV.getNumStatesParents(); idxStateParents++) {
				for (int idxState = 0; idxState < nodeCV.getNumStates(); idxState++) {
					// Counts for class variable taking the state given the states of parents
					double nx = ss.getNx()[idxStateParents][idxState];
					// Probability class variable takes the state given the states of parents
					double ox = nodeCV.getCP(idxStateParents, idxState);
					// States that never occurred are ignored to avoid NaNs
					if (nx > 0) {
						double prob = nx * Math.log(ox);
						lpp += prob;
					}
				}
			}
		}
		return lpp;
	}

	/**
	 * Computes the log-likelihood of the sequences at each feature node. Only
	 * features and class variables that are parents of each node are relevant.
	 * 
	 * @param ctbn a continuous time Bayesian network
	 * @return log-likelihood of the sequences at each feature node
	 */
	@SuppressWarnings("unchecked")
	private double logLikelihoodSequences(CTBN<? extends Node> ctbn) {
		double lls = 0.0;
		// Compute log-likelihood of the sequences given the class variables
		for (CIMNode node : (List<CIMNode>) ctbn.getNodes()) {
			// Sufficient statistics of the node
			CTBNSufficientStatistics ss = node.getSufficientStatistics();
			int numNodeStates = node.getNumStates();
			int numParentsStates = node.getNumStatesParents();
			// Iterate over all states of the node (from where a transition begins)
			for (int idxFromState = 0; idxFromState < numNodeStates; idxFromState++) {
				// Iterate over all states of the node's parents
				for (int idxStateParents = 0; idxStateParents < numParentsStates; idxStateParents++) {
					// Retrieve parameters and sufficient statistics for evaluated states
					double qx = node.getQx(idxStateParents, idxFromState);
					// Cases where there are no transitions are ignored to avoid NaNs
					if (qx > 0) {
						double mx = ss.getMx()[idxStateParents][idxFromState];
						double tx = ss.getTx()[idxStateParents][idxFromState];
						lls += mx * Math.log(qx) - qx * tx;
						// Iterate over all states of the feature node (except "idxFromState")
						for (int idxToState = 0; idxToState < numNodeStates; idxToState++) {
							if (idxToState != idxFromState) {
								// Retrieve parameters and sufficient statistics for evaluated states
								double oxx = node.getOxy(idxStateParents, idxFromState, idxToState);
								// Cases without transitions between the states are ignored to avoid NaNs
								if (oxx > 0) {
									double mxy = ss.getMxy()[idxStateParents][idxFromState][idxToState];
									lls += mxy * Math.log(oxx);
								}
							}
						}
					}
				}
			}
		}
		return lls;
	}

	/**
	 * Computes the log-marginal-likelihoods of the sequences (denominator term).
	 * 
	 * @param nodesCVs
	 * @param ctbn
	 * @param statesCVs
	 * @return log-marginal-likelihood
	 */
	private double logMarginalLikelihoodSequences(BN<CPTNode> bn, CTBN<? extends Node> ctbn) {
		// Retrieve nodes BN
		List<CPTNode> nodesCVs = bn.getLearnedNodes();
		// Retrieve all class configurations
		List<State> statesCVs = getClassConfigurations(nodesCVs);
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
	 * Computes the log prior probability of every class configuration and stored
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
				String stateCV = stateCVs.getValueVariable(nodeCV.getName());
				nodeCV.setState(stateCV);
				for (Node nodeParent : nodeCV.getParents()) {
					String stateParentCV = stateCVs.getValueVariable(nodeParent.getName());
					((DiscreteNode) nodeParent).setState(stateParentCV);
				}
				int idxState = nodeCV.getIdxState();
				int idxStateParents = nodeCV.getIdxStateParents();
				double ox = nodeCV.getCP(idxStateParents, idxState);
				if (ox > 0)
					lpcc += Math.log(ox);
			}
			uPs.put(stateCVs, lpcc);
		}
	}

	/**
	 * Computes the log-likelihood of the sequences at the feature nodes given each
	 * possible class configuration and stored them in Map 'uPs'.
	 * 
	 * @param ctbn
	 * @param statesCVs
	 * @param uPs
	 */
	@SuppressWarnings("unchecked")
	private void logLikelihoodSequencesGivenClassConfigurations(CTBN<? extends Node> ctbn, List<State> statesCVs,
			Map<State, Double> uPs) {
		for (State stateCVs : statesCVs) {
			// Log-likelihood sequences for given class configuration
			double llsCC = 0;
			for (CIMNode node : (List<CIMNode>) ctbn.getNodes()) {
				// Sufficient statistics of the node
				CTBNSufficientStatistics ss = node.getSufficientStatistics();
				// Number of states of the node and its parents
				int numNodeStates = node.getNumStates();
				int numParentsStates = node.getNumStatesParents();
				// Iterate over all states of the node (from where a transition begins)
				for (int idxFromState = 0; idxFromState < numNodeStates; idxFromState++) {
					// Iterate over all states of the node's parents
					stateParents: for (int idxStateParents = 0; idxStateParents < numParentsStates; idxStateParents++) {
						node.setState(idxFromState);
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
						// Retrieve parameters and sufficient statistics for evaluated states
						double qx = node.getQx(idxStateParents, idxFromState);
						// Cases where there are no transitions are ignored to avoid NaNs
						if (qx > 0) {
							double mx = ss.getMx()[idxStateParents][idxFromState];
							double tx = ss.getTx()[idxStateParents][idxFromState];
							llsCC += (mx * Math.log(qx)) + (-qx * tx);
							// Iterate over all states of the feature node (except "idxFromState")
							for (int idxToState = 0; idxToState < numNodeStates; idxToState++) {
								if (idxToState != idxFromState) {
									// Retrieve parameters and sufficient statistics for evaluated states
									double oxx = node.getOxy(idxStateParents, idxFromState, idxToState);
									// Cases without transitions between the states are ignored to avoid NaNs
									if (oxx > 0) {
										double mxy = ss.getMxy()[idxStateParents][idxFromState][idxToState];
										llsCC += (mxy * Math.log(oxx));
									}
								}
							}
						}
					}
				}
			}
			// Save log-likelihood sequences for the evaluated class configuration
			uPs.merge(stateCVs, llsCC, Double::sum);
		}
	}

	/**
	 * Defines the total penalization to apply to the given model.
	 * 
	 * @param ctbn
	 * @return penalization to apply to the given model
	 */
	@SuppressWarnings("unchecked")
	private double getPenalization(CTBN<? extends Node> ctbn) {
		double totalPenalization = 0.0;
		DoubleUnaryOperator penalizationFunction = this.penalizationFunctionMap.get(this.penalizationFunction);
		if (penalizationFunction != null) {
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
				double penalization = penalizationFunction.applyAsDouble(sampleSize);
				totalPenalization += networkComplexity * penalization;
			}
		}
		return totalPenalization;
	}

	/**
	 * Computes the log-likelihood of the sequences at specified feature node. Only
	 * features and class variables that are parents of each node are relevant.
	 * 
	 * @param ctbn
	 * @param statesCVs
	 * @return log-likelihood of the sequences at specified feature node
	 */
	private double logLikelihoodSequences(CTBN<? extends Node> ctbn, int nodeIndex) {
		double lls = 0.0;
		// Compute log-likelihood of the sequences given the class variables
		CIMNode node = (CIMNode) ctbn.getNodes().get(nodeIndex);
		// Sufficient statistics of the node
		CTBNSufficientStatistics ss = node.getSufficientStatistics();
		int numNodeStates = node.getNumStates();
		int numParentsStates = node.getNumStatesParents();
		// Iterate over all states of the node (from where a transition begins)
		for (int idxFromState = 0; idxFromState < numNodeStates; idxFromState++) {
			// Iterate over all states of the node's parents
			for (int idxStateParents = 0; idxStateParents < numParentsStates; idxStateParents++) {
				// Retrieve parameters and sufficient statistics for evaluated states
				double qx = node.getQx(idxStateParents, idxFromState);
				// Cases where there are no transitions are ignored to avoid NaNs
				if (qx > 0) {
					double mx = ss.getMx()[idxStateParents][idxFromState];
					double tx = ss.getTx()[idxStateParents][idxFromState];
					lls += mx * Math.log(qx) - qx * tx;
					// Iterate over all states of the feature node (except "idxFromState")
					for (int idxToState = 0; idxToState < numNodeStates; idxToState++) {
						if (idxToState != idxFromState) {
							// Retrieve parameters and sufficient statistics for evaluated states
							double oxx = node.getOxy(idxStateParents, idxFromState, idxToState);
							// Cases without transitions between the states are ignored to avoid NaNs
							if (oxx > 0) {
								double mxy = ss.getMxy()[idxStateParents][idxFromState][idxToState];
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
	 * Computes the log-marginal-likelihoods of the sequences (denominator term) at
	 * the specified node.
	 * 
	 * @param nodesCVs
	 * @param ctbn
	 * @param statesCVs
	 * @return log-marginal-likelihoods of the sequences at the specified node
	 */
	private double logMarginalLikelihoodSequences(BN<CPTNode> bn, CTBN<? extends Node> ctbn, int nodeIndex) {
		// Retrieve nodes BN
		List<CPTNode> nodesCVs = bn.getLearnedNodes();
		// Retrieve all class configurations
		List<State> statesCVs = getClassConfigurations(nodesCVs);
		// Store for each class configuration the sums of their log prior probability
		// and the log likelihood of the sequence given the classes
		Map<State, Double> uPs = new HashMap<State, Double>();
		// Obtain log prior probability of every class configuration
		logPriorProbabilityClassConfigurations(nodesCVs, statesCVs, uPs);
		// Compute log likelihood of sequences given every class configuration
		logLikelihoodSequencesGivenClassConfigurations(ctbn, nodeIndex, statesCVs, uPs);
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
	 * Computes the log-likelihood of the sequences at the specified feature node
	 * given each possible class configuration and stored them in Map 'uPs'.
	 * 
	 * @param ctbn
	 * @param statesCVs
	 * @param uPs
	 */
	private void logLikelihoodSequencesGivenClassConfigurations(CTBN<? extends Node> ctbn, int nodeIndex,
			List<State> statesCVs, Map<State, Double> uPs) {
		for (State stateCVs : statesCVs) {
			// Log-likelihood sequences for given class configuration
			double llsCC = 0;
			CIMNode node = (CIMNode) ctbn.getNodes().get(nodeIndex);
			// Sufficient statistics of the node
			CTBNSufficientStatistics ss = node.getSufficientStatistics();
			// Number of states of the node and its parents
			int numNodeStates = node.getNumStates();
			int numParentsStates = node.getNumStatesParents();
			// Iterate over all states of the node (from where a transition begins)
			for (int idxFromState = 0; idxFromState < numNodeStates; idxFromState++) {
				// Iterate over all states of the node's parents
				stateParents: for (int idxStateParents = 0; idxStateParents < numParentsStates; idxStateParents++) {
					node.setState(idxFromState);
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
					// Retrieve parameters and sufficient statistics for evaluated states
					double qx = node.getQx(idxStateParents, idxFromState);
					// Cases where there are no transitions are ignored to avoid NaNs
					if (qx > 0) {
						double mx = ss.getMx()[idxStateParents][idxFromState];
						double tx = ss.getTx()[idxStateParents][idxFromState];
						llsCC += mx * Math.log(qx) - qx * tx;
						// Iterate over all states of the feature node (except "idxFromState")
						for (int idxToState = 0; idxToState < numNodeStates; idxToState++) {
							if (idxToState != idxFromState) {
								// Retrieve parameters and sufficient statistics for evaluated states
								double oxx = node.getOxy(idxStateParents, idxFromState, idxToState);
								// Cases without transitions between the states are ignored to avoid NaNs
								if (oxx > 0) {
									double mxy = ss.getMxy()[idxStateParents][idxFromState][idxToState];
									llsCC += (mxy * Math.log(oxx));
								}
							}
						}
					}
				}
			}
			// Save log-likelihood sequences for the evaluated class configuration
			uPs.merge(stateCVs, llsCC, Double::sum);
		}
	}

	/**
	 * Defines the total penalization to apply to the given model, considering only
	 * the specified feature node.
	 * 
	 * @param ctbn
	 * @return penalization to apply to the given model
	 */
	private double getPenalization(CTBN<? extends Node> ctbn, int nodeIndex) {
		double totalPenalization = 0.0;
		DoubleUnaryOperator penalizationFunction = this.penalizationFunctionMap.get(this.penalizationFunction);
		if (penalizationFunction != null) {
			CIMNode node = (CIMNode) ctbn.getNodes().get(nodeIndex);
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
			double penalization = penalizationFunction.applyAsDouble(sampleSize);
			totalPenalization += networkComplexity * penalization;
		}
		return totalPenalization;
	}
}
