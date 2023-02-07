package es.upm.fi.cig.multictbnc.learning.structure.constraintlearning.PC;

import es.upm.fi.cig.multictbnc.exceptions.ErroneousValueException;
import es.upm.fi.cig.multictbnc.learning.parameters.ctbn.CTBNSufficientStatistics;
import es.upm.fi.cig.multictbnc.learning.structure.StructureLearningAlgorithm;
import es.upm.fi.cig.multictbnc.models.CTBN;
import es.upm.fi.cig.multictbnc.models.PGM;
import es.upm.fi.cig.multictbnc.nodes.CIMNode;
import es.upm.fi.cig.multictbnc.nodes.Node;
import es.upm.fi.cig.multictbnc.util.Util;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.commons.math3.distribution.FDistribution;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * Implementation of the CTPC algorithm for Multi-CTBNCs.
 *
 * @author Carlos Villa Blanco
 */
public class CTPC implements StructureLearningAlgorithm {
	private static final Logger logger = LogManager.getLogger(CTPC.class);
	// Significances
	double sigTimeTransitionHyp;
	double sigStateToStateTransitionHyp;
	int numCondIndTestsPerformed = 0;

	/**
	 * Initialises the CTPC algorithm by providing the significance levels to be used.
	 *
	 * @param sigTimeTransitionHypothesis         significance level used for the null time to transition hypothesis
	 * @param sigStateToStateTransitionHypothesis significance level used for the null state-to-state transition
	 *                                            hypothesis
	 */
	public CTPC(double sigTimeTransitionHypothesis, double sigStateToStateTransitionHypothesis) {
		this.sigTimeTransitionHyp = sigTimeTransitionHypothesis;
		this.sigStateToStateTransitionHyp = sigStateToStateTransitionHypothesis;
	}

	@Override
	public String getIdentifier() {
		return "CTPC";
	}

	@Override
	public Map<String, String> getParametersAlgorithm() {
		Map<String, String> parametersAlgorithm = new HashMap<>();
		parametersAlgorithm.put("sigTimeTransitionHyp", String.valueOf(this.sigTimeTransitionHyp));
		parametersAlgorithm.put("sigStateToStateTransitionHyp", String.valueOf(this.sigStateToStateTransitionHyp));
		return parametersAlgorithm;
	}

	@Override
	public void learn(PGM<? extends Node> pgm, List<Integer> idxNodes) {
		// TODO
	}

	@Override
	public void learn(PGM<? extends Node> pgm) throws ErroneousValueException {
		if (this.sigTimeTransitionHyp > 1 || this.sigTimeTransitionHyp < 0 || this.sigStateToStateTransitionHyp > 1 ||
				this.sigStateToStateTransitionHyp < 0) {
			throw new ErroneousValueException(
					"The significances must be in range [0, 1]. Values provided: " + this.sigTimeTransitionHyp +
							" (time to transitions) and " + this.sigStateToStateTransitionHyp +
							" (state to state transitions)");
		}
		logger.info("Learning {} using CTPC algorithm and significances sig_waitingtime = {} and sig_transition = {}",
				pgm.getType(), this.sigTimeTransitionHyp, this.sigStateToStateTransitionHyp);
		// Obtain complete directed graph
		boolean[][] adjacencyMatrix = buildCompleteStructure(pgm);
		// Retrieve indexes of feature variables and iterate over them
		List<Integer> idxFeatureVariables = getIdxFeatureVariables(pgm);
		idxFeatureVariables.parallelStream().forEach(idxFeatureVariable -> {
			// Clone the model to avoid race conditions
			@SuppressWarnings("unchecked") CTBN<CIMNode> ctbn = new CTBN<>((CTBN<CIMNode>) pgm,
					((CTBN<CIMNode>) pgm).getDataset());
			// Find the parent set of each node in parallel
			try {
				learnParentSetNode(ctbn, idxFeatureVariable, adjacencyMatrix);
			} catch (ErroneousValueException erroneousValueException) {
				logger.error("[Variable " + ctbn.getNameVariables().get(idxFeatureVariable) + "] " +
						erroneousValueException.getMessage());
			}
		});
		pgm.setStructure(idxFeatureVariables, adjacencyMatrix);
		pgm.learnParameters(idxFeatureVariables);
		logger.info("Bridge and feature subgraph learnt performing {} conditional independence tests",
				this.numCondIndTestsPerformed);
		// Reset the number of performed conditional independence tests
		this.numCondIndTestsPerformed = 0;
	}

	@Override
	public void learn(PGM<? extends Node> pgm, int idxNode) {
		// TODO
	}

	/**
	 * Defines a node, which is being studied as a possible parent node, and a separating set as parents of an
	 * evaluated
	 * node. This is done to compute the parameters and sufficient statistics.
	 *
	 * @param pgm           probabilistic graphical model that contains the nodes
	 * @param node          evaluated node
	 * @param sepSet        indexes of the nodes in the separating set
	 * @param idxParentNode index of the node being studied as a possible parent node
	 */
	protected void addSepSetAndNodeAsParents(PGM<? extends Node> pgm, CIMNode node, List<Integer> sepSet,
											 int idxParentNode) {
		// Remove previous parents of the node
		node.removeParents();
		// The indexes of the parent node and the separating set nodes are added to a
		// temporal list that is sorted. This is necessary since parameters and
		// sufficient statistics are cached to avoid recalculations and the index for
		// the current state of the node parents depends on the order in which these
		// nodes were added as parents.
		List<Integer> sortedListNodes = new ArrayList<>(sepSet);
		sortedListNodes.add(idxParentNode);
		Collections.sort(sortedListNodes);
		for (int idxNode : sortedListNodes)
			pgm.getNodeByIndex(idxNode).setChild(node);
	}

	/**
	 * Defines a separating set as parents of an evaluated node. This is done to compute the parameters and sufficient
	 * statistics.
	 *
	 * @param pgm    probabilistic graphical model that contains the nodes
	 * @param node   evaluated node
	 * @param sepSet indexes of the nodes in the separating set
	 */
	protected void addSepSetAsParents(PGM<? extends Node> pgm, CIMNode node, List<Integer> sepSet) {
		// Remove previous parents of the node
		node.removeParents();
		for (int idxNodeSubset : sepSet)
			pgm.getNodeByIndex(idxNodeSubset).setChild(node);
	}

	/**
	 * Returns the adjacency matrix of a PGM with a complete structure.
	 *
	 * @param pgm probabilistic graphical model
	 * @return adjacency matrix
	 */
	protected boolean[][] buildCompleteStructure(PGM<? extends Node> pgm) {
		boolean[][] adjacencyMatrix = new boolean[pgm.getNumNodes()][pgm.getNumNodes()];
		for (int i = 0; i < pgm.getNumNodes(); i++) {
			for (int j = 0; j < pgm.getNumNodes(); j++) {
				if (i != j && !pgm.getNodeByIndex(j).isClassVariable())
					adjacencyMatrix[i][j] = true;
			}
		}
		return adjacencyMatrix;
	}

	/**
	 * Returns the indexes of the feature nodes in a PGM.
	 *
	 * @param pgm a probabilistic graphical model
	 * @return indexes of the feature nodes in a PGM
	 */
	protected List<Integer> getIdxFeatureVariables(PGM<? extends Node> pgm) {
		List<Integer> idxClassVariables = new ArrayList<>();
		for (int idxNode : pgm.getIndexNodes())
			if (!pgm.getNodeByIndex(idxNode).isClassVariable())
				idxClassVariables.add(idxNode);
		return idxClassVariables;
	}

	/**
	 * Returns the indexes of a node's parents.
	 *
	 * @param idxNode         node index
	 * @param adjacencyMatrix adjacency matrix
	 * @return indexes of a node's parents
	 */
	protected List<Integer> getIdxParentsNode(int idxNode, boolean[][] adjacencyMatrix) {
		List<Integer> currentParents = new ArrayList<>();
		for (int idxParentNode = 0; idxParentNode < adjacencyMatrix.length; idxParentNode++) {
			if (idxParentNode != idxNode && adjacencyMatrix[idxParentNode][idxNode]) {
				currentParents.add(idxParentNode);
			}
		}
		return currentParents;
	}

	/**
	 * Learns the parent set of a node.
	 *
	 * @param pgm             probabilistic graphical model that contains the node
	 * @param idxNode         index of the node whose parent set is being learnt
	 * @param adjacencyMatrix current adjacency matrix
	 * @throws ErroneousValueException if a provided parameter is erroneous for the requested task
	 */
	protected void learnParentSetNode(PGM<? extends Node> pgm, int idxNode, boolean[][] adjacencyMatrix)
			throws ErroneousValueException {
		// Cache used to avoid the recomputation of parameters and sufficient statistics
		Map<Long, List<Object>> cache = new HashMap<>();
		// Retrieve node
		CIMNode node = (CIMNode) pgm.getNodeByIndex(idxNode);
		logger.debug("Define parents of node {}", node.getName());
		// Retrieve parents of the feature variable
		List<Integer> currentParents = getIdxParentsNode(idxNode, adjacencyMatrix);
		// Iterate over possible sizes of the separating sets
		for (int sizeSepSets = 0; sizeSepSets <= currentParents.size() - 1; sizeSepSets++) {
			logger.trace("Evaluating separating sets of size = {} for {}", sizeSepSets, node.getName());
			// Iterate over the parents of the variable to decide if it should be removed
			parentNodeLoop:
			for (int idxParentNode = 0; idxParentNode < adjacencyMatrix.length; idxParentNode++) {
				// Only evaluate conditional independence test with variables that are
				// considered potential parents
				if (adjacencyMatrix[idxParentNode][idxNode]) {
					// Retrieve parent node
					CIMNode parentNode = (CIMNode) pgm.getNodeByIndex(idxParentNode);
					// Retrieve possible separating sets and iterate over them
					List<List<Integer>> possibleSepSets = Util.getSubsets(currentParents, sizeSepSets, idxParentNode);
					for (List<Integer> idxSepSet : possibleSepSets) {
						// Extract names of variables in the subset
						List<String> nameNodesSepSet = pgm.getNamesNodesByIndex(idxSepSet);
						logger.trace("Test conditional independence between {} and {} given separating set {}",
								node.getName(), parentNode.getName(), nameNodesSepSet);
						// Increase the number of conditional independence tests performed
						this.numCondIndTestsPerformed++;
						// Set nodes of separating set as parents of the node
						addSepSetAsParents(pgm, node, idxSepSet);
						// Retrieve parameters and sufficient statistics given separating set
						retrieveParametersAndSuffStatistics(pgm, idxNode, cache, idxSepSet);
						double[][] qxSepSet = node.getQx();
						CTBNSufficientStatistics suffStatSepSet = node.getSufficientStatistics();
						// Set nodes of separating set and 'idxParentNode' as parents of the node
						addSepSetAndNodeAsParents(pgm, node, idxSepSet, idxParentNode);
						// Retrieve parameters and sufficient statistics given 'idxParentNode' and the
						// separating set
						retrieveParametersAndSuffStatistics(pgm, idxNode, cache, idxSepSet, idxParentNode);
						double[][] qxSepSetAndParent = node.getQx();
						CTBNSufficientStatistics suffStatSepSetAndParent = node.getSufficientStatistics();
						// Test null time to transition hypothesis for the given separating set
						boolean isNullTimeToTransitionHypAccepted = testNullTimeToTransitionHypForGivenSepSet(node,
								parentNode, nameNodesSepSet, qxSepSet, suffStatSepSet, qxSepSetAndParent,
								suffStatSepSetAndParent);
						// If the hypothesis is rejected, evaluate the following separating set
						if (!isNullTimeToTransitionHypAccepted)
							continue;
						// The null time to transition hypothesis was not rejected
						if (node.getNumStates() > 2) {
							// Test null state-to-state transition hypothesis
							boolean isNullStateToStateTransitionHypAccepted =
									testNullStateToStateTransitionHypForGivenSepSet(
									node, parentNode, nameNodesSepSet, suffStatSepSet, suffStatSepSetAndParent);
							// If the hypothesis is rejected, evaluate the following separating set
							if (!isNullStateToStateTransitionHypAccepted)
								continue;
						}
						// If the hypothesis tests are not rejected, nodes are conditionally independent
						logger.trace("Removing arc {} <- {}, given separating set {}", node.getName(),
								parentNode.getName(), nameNodesSepSet);
						// Conditional independence between the variables was detected
						adjacencyMatrix[idxParentNode][idxNode] = false;
						currentParents.remove(Integer.valueOf(idxParentNode));
						continue parentNodeLoop;
					}
				}
			}
		}
		logger.info("Parents of node {} were learnt", node.getName());
	}

	/**
	 * Retrieves the parameter and sufficient statistics of a node. The method searches in a given cache first in case
	 * they were already computed.
	 *
	 * @param pgm        probabilistic graphical model that contains the node
	 * @param idxNode    index of the node
	 * @param cache      cache with parameters and sufficient statistics of previous tests
	 * @param idxSepSet  indexes of the nodes in the separating set
	 * @param idxParents indexes of the parent nodes
	 */
	protected void retrieveParametersAndSuffStatistics(PGM<? extends Node> pgm, int idxNode,
													   Map<Long, List<Object>> cache, List<Integer> idxSepSet,
													   Integer... idxParents) {
		CIMNode node = (CIMNode) pgm.getNodeByIndex(idxNode);
		// Access cache to retrieve parameters and sufficient statistics
		long keyCache = computeKeyCache(idxSepSet, idxParents);
		List<Object> parameterAndSufStat = cache.get(keyCache);
		if (parameterAndSufStat == null) {
			// Retrieve sufficient statistics and learn parameters for the current node
			pgm.learnParameters(List.of(idxNode));
			// Parameters and sufficient statistics of the node given the separating set
			double[][] qx = node.getQx();
			CTBNSufficientStatistics suffStat = node.getSufficientStatistics();
			// Store parameters and sufficient statistics in the cache
			parameterAndSufStat = List.of(qx, suffStat);
			cache.put(keyCache, parameterAndSufStat);
		} else {
			double[][] qx = (double[][]) parameterAndSufStat.get(0);
			CTBNSufficientStatistics suffStat = (CTBNSufficientStatistics) parameterAndSufStat.get(1);
			node.setParameters(qx, null);
			node.setSufficientStatistics(suffStat);
		}
	}

	/**
	 * Evaluates null state-to-state transition hypothesis for a given node and parent node given a certain separating
	 * set. Returns {@code true} if the null hypothesis is not rejected, {@code false} otherwise.
	 *
	 * @param node                    evaluate node
	 * @param parentNode              parent node
	 * @param nameNodesSepSet         names of the nodes in the separating set
	 * @param suffStatSepSet          sufficient statistics of the node given the separating set
	 * @param suffStatSepSetAndParent sufficient statistics of the node given the parent and the separating set
	 * @return {@code true} if the null hypothesis is not rejected, {@code false} otherwise.
	 */
	protected boolean testNullStateToStateTransitionHypForGivenSepSet(CIMNode node, CIMNode parentNode,
																	  List<String> nameNodesSepSet,
																	  CTBNSufficientStatistics suffStatSepSet,
																	  CTBNSufficientStatistics suffStatSepSetAndParent) {
		// Iterate over all possible states of the node
		for (int idxState = 0; idxState < node.getNumStates(); idxState++) {
			// Iterate over all possible states of the subset and 'idxParent'
			for (int idxStateSepSetAndParent = 0; idxStateSepSetAndParent < node.getNumStatesParents();
				 idxStateSepSetAndParent++) {
				// Perform the hypothesis test
				boolean isNullStateToStateTransitionHypAccepted = testNullStateToStateTransitionHyp(node,
						nameNodesSepSet, suffStatSepSet, suffStatSepSetAndParent, idxState, idxStateSepSetAndParent);
				if (!isNullStateToStateTransitionHypAccepted) {
					// Reject hypothesis. Variables are not conditionally independent
					logger.debug("Nodes {} and {} are not conditionally independent given {} (using the transitions)",
							node.getName(), parentNode.getName(), nameNodesSepSet);
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Evaluate null time to transition hypothesis for a given node and parent given a certain separating set. Returns
	 * {@code true} if the null hypothesis is not rejected, {@code false} otherwise.
	 *
	 * @param node                    evaluated node
	 * @param parentNode              node's parent
	 * @param nameNodesSepSet         names of the nodes in the separating set
	 * @param qxSepSet                parameters of the node given the separating set
	 * @param suffStatSepSet          sufficient statistics of the node given the separating set
	 * @param qxSepSetAndParent       parameters of the node given the parent and the separating set
	 * @param suffStatSepSetAndParent sufficient statistics of the node given the parent and the separating set
	 * @return {@code true} if the null hypothesis is not rejected, {@code false} otherwise
	 * @throws ErroneousValueException if a provided parameter is erroneous for the requested task
	 */
	protected boolean testNullTimeToTransitionHypForGivenSepSet(CIMNode node, CIMNode parentNode,
																List<String> nameNodesSepSet, double[][] qxSepSet,
																CTBNSufficientStatistics suffStatSepSet,
																double[][] qxSepSetAndParent,
																CTBNSufficientStatistics suffStatSepSetAndParent)
			throws ErroneousValueException {
		// Iterate over all possible states of the node
		for (int idxState = 0; idxState < node.getNumStates(); idxState++) {
			// Iterate over all possible states of the subset and 'idxParent'
			for (int idxStateSepSetAndParent = 0; idxStateSepSetAndParent < node.getNumStatesParents();
				 idxStateSepSetAndParent++) {
				// Perform the hypothesis test
				boolean isNullTimeToTransitionHypAccepted = testNullTimeToTransitionHyp(node, nameNodesSepSet,
						qxSepSet,
						suffStatSepSet, qxSepSetAndParent, suffStatSepSetAndParent, idxState, idxStateSepSetAndParent);
				if (!isNullTimeToTransitionHypAccepted) {
					// Reject hypothesis. Variables are not conditionally independent
					logger.debug(
							"Nodes {} and {} are not conditionally independent given {} [{}] (using waiting times)",
							node.getName(), parentNode.getName(), nameNodesSepSet, idxStateSepSetAndParent);
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Computes the key used in the cache to store parameters and sufficient statistics for a given separating set and
	 * parents whose conditional independence to a node we are testing.
	 *
	 * @param idxSepSet  indexes of the nodes in the separating set
	 * @param idxParents indexes of the parent nodes
	 * @return key for the given separating set and parent nodes
	 */
	private long computeKeyCache(List<Integer> idxSepSet, Integer... idxParents) {
		long keyCache = 0;
		for (int idx : idxSepSet)
			keyCache += Math.pow(2, idx);
		for (int idx : idxParents)
			keyCache += Math.pow(2, idx);
		return keyCache;
	}

	/**
	 * Performs F test. Returns {@code true} if the null hypothesis is rejected.
	 *
	 * @param qxs  intensity for the state of a node given the state of a separating set
	 * @param qxys intensity for the state of a node given the state of its parents and a separating set
	 * @param df1  numerator degrees of freedom
	 * @param df2  denominator degrees of freedom.
	 * @return {@code true} if the null hypothesis is rejected
	 */
	private boolean performFTest(double qxs, double qxys, double df1, double df2) throws ErroneousValueException {
		// Estimate F value
		double fValue = qxs / qxys;
		// Retrieve critical values from F distribution
		FDistribution fDistribution;
		try {
			fDistribution = new FDistribution(df1, df2);
		} catch (NotStrictlyPositiveException nspe) {
			throw new ErroneousValueException(
					"Degrees of freedom provided to the F test (CTPC algorithm) are zero. This may be due to the " +
							"training dataset not containing certain transitions between states. Consider using the " +
							"Bayesian estimation to learn the parameters of the model");
		}
		// Obtain upper and lower critical values for the given significance
		double upperCriticalValue = fDistribution.inverseCumulativeProbability(1 - this.sigTimeTransitionHyp / 2);
		double lowerCriticalValue = fDistribution.inverseCumulativeProbability(this.sigTimeTransitionHyp / 2);
		return fValue <= lowerCriticalValue || fValue >= upperCriticalValue;
	}

	private boolean testNullStateToStateTransitionHyp(CIMNode node, List<String> nameNodesSepSet,
													  CTBNSufficientStatistics suffStatSepSet,
													  CTBNSufficientStatistics suffStatSepSetAndParent,
													  int idxStateNode, int idxStateSepSetAndParent) {
		// Set the state of the parents of the node
		node.setStateParents(idxStateSepSetAndParent);
		// Extract the state index of the given separating set
		int idxStateSepSet = node.getIdxStateParents(nameNodesSepSet);
		// Estimate degrees of freedom
		double mxxs = 0;
		double mxxys = 0;
		for (int idxToStateNode = 0; idxToStateNode < node.getNumStates(); idxToStateNode++) {
			mxxys += suffStatSepSetAndParent.getMxy()[idxStateSepSetAndParent][idxStateNode][idxToStateNode];
			mxxs += suffStatSepSet.getMxy()[idxStateSepSet][idxStateNode][idxToStateNode];
		}
		double K = Math.sqrt((mxxs / mxxys));
		double L = 1 / K;
		int df = node.getNumStates() - 1;
		double chiSquared = 0;
		for (int idxToStateNode = 0; idxToStateNode < node.getNumStates(); idxToStateNode++) {
			mxxys = suffStatSepSetAndParent.getMxy()[idxStateSepSetAndParent][idxStateNode][idxToStateNode];
			mxxs = suffStatSepSet.getMxy()[idxStateSepSet][idxStateNode][idxToStateNode];
			chiSquared += Math.pow(K * mxxys - L * mxxs, 2) / (mxxys + mxxs);
		}
		// Retrieve critical values from chi-squared distribution
		ChiSquaredDistribution chiSquaredDistribution = new ChiSquaredDistribution(df);
		// Obtain critical value for the given significance
		double criticalValue = chiSquaredDistribution.inverseCumulativeProbability(
				1 - this.sigStateToStateTransitionHyp);

		// True if the null hypothesis is not rejected
		return chiSquared < criticalValue;
	}

	/**
	 * Evaluate null time to transition hypothesis for a given state of the node and its parents. Returns {@code true}
	 * if the null hypothesis is not rejected, {@code false} otherwise.
	 *
	 * @param node                    evaluated node
	 * @param nameNodesSepSet         names of the nodes in the separating set
	 * @param qxSepSet                parameters of the node given the separating set
	 * @param suffStatSepSet          sufficient statistics of the node given the separating set
	 * @param qxSepSetAndParent       parameters of the node given the parents and the separating set
	 * @param suffStatSepSetAndParent sufficient statistics of the node given the parents and the separating set
	 * @param idxState                index of the node's state
	 * @param idxStateSepSetAndParent index of the state of the node's parents
	 * @return {@code true} if the null hypothesis is not rejected, {@code false} otherwise
	 */
	private boolean testNullTimeToTransitionHyp(CIMNode node, List<String> nameNodesSepSet, double[][] qxSepSet,
												CTBNSufficientStatistics suffStatSepSet, double[][] qxSepSetAndParent,
												CTBNSufficientStatistics suffStatSepSetAndParent, int idxState,
												int idxStateSepSetAndParent) throws ErroneousValueException {
		// Set the state of the parents of the node
		node.setStateParents(idxStateSepSetAndParent);
		// Extract the state index of the given separating set
		int idxStateSepSet = node.getIdxStateParents(nameNodesSepSet);
		// Extract the intensities exponential distribution
		double qxs = qxSepSet[idxStateSepSet][idxState];
		double qxys = qxSepSetAndParent[idxStateSepSetAndParent][idxState];
		// Estimate degrees of freedom
		double df1 = 0;
		double df2 = 0;
		for (int idxToStateNode = 0; idxToStateNode < node.getNumStates(); idxToStateNode++) {
			df1 += suffStatSepSetAndParent.getMxy()[idxStateSepSetAndParent][idxState][idxToStateNode];
			df2 += suffStatSepSet.getMxy()[idxStateSepSet][idxState][idxToStateNode];
		}
		return !performFTest(qxs, qxys, df1, df2);
	}

}