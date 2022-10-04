package es.upm.fi.cig.multictbnc.learning.structure.constraintlearning.PC;

import es.upm.fi.cig.multictbnc.exceptions.ErroneousValueException;
import es.upm.fi.cig.multictbnc.learning.parameters.ctbn.CTBNSufficientStatistics;
import es.upm.fi.cig.multictbnc.models.CTBN;
import es.upm.fi.cig.multictbnc.models.PGM;
import es.upm.fi.cig.multictbnc.nodes.CIMNode;
import es.upm.fi.cig.multictbnc.nodes.Node;
import es.upm.fi.cig.multictbnc.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of the MB-CTPC algorithm.
 *
 * @author Carlos Villa Blanco
 */
public class MarkovBlanketCTPC extends CTPC {
	private static final Logger logger = LogManager.getLogger(MarkovBlanketCTPC.class);
	int numCondIndTestsPerformed = 0;

	/**
	 * Initialises the MB-CTPC algorithm by providing the significances to be used.
	 *
	 * @param sigTimeTransitionHypothesis         significance level used for the null time to transition hypothesis
	 * @param sigStateToStateTransitionHypothesis significance level used for the null state-to-state transition
	 *                                            hypothesis
	 */
	public MarkovBlanketCTPC(double sigTimeTransitionHypothesis, double sigStateToStateTransitionHypothesis) {
		super(sigTimeTransitionHypothesis, sigStateToStateTransitionHypothesis);
	}

	@Override
	public void learn(PGM<? extends Node> pgm) {
		logger.info(
				"Learning {} using the Markov blanket-based continuous-time PC algorithm (MB-CTPC) and significances" +
						" " + "alpha_1 = {} and alpha_2 = {}", pgm.getType(), this.sigTimeTransitionHyp,
				this.sigStateToStateTransitionHyp);
		// Obtain complete directed graph
		boolean[][] adjacencyMatrix = buildCompleteStructure(pgm);
		// Return indexes feature variables
		List<Integer> idxFeatureVariables = getIdxFeatureVariables(pgm);
		List<Integer> idxClassVariables = getIdxClassVariables(pgm);
		// List of caches
		Map<Integer, Map<Long, List<Object>>> caches = new ConcurrentHashMap<>();
		initialiseCaches(caches, idxFeatureVariables);
		// Define bridge subgraph testing the conditional independence between feature
		// and class variables considering only class variables in the separating sets
		logger.info("Defining bridge subgraph considering only class variable in the separating sets");
		idxFeatureVariables.parallelStream().forEach(idxFeatureVariable -> {
			@SuppressWarnings("unchecked") CTBN<CIMNode> ctbn = new CTBN<>((CTBN<CIMNode>) pgm,
					((CTBN<CIMNode>) pgm).getDataset());
			// Parents of feature variables are being defined in parallel. Class variables
			// are ignored
			try {
				learnPreliminaryBridgeSubgraph(ctbn, idxFeatureVariable, adjacencyMatrix,
						caches.get(idxFeatureVariable));
			} catch (ErroneousValueException erroneousValueException) {
				logger.error("[Variable {} ] {}", ctbn.getNameVariables().get(idxFeatureVariable),
						erroneousValueException.getMessage());
			}
		});
		logger.info("Remove unlikely dependencies from the feature subgraph");
		idxFeatureVariables.parallelStream().forEach(idxFeatureVariable -> {
			// Ignore unnecessary tests of the feature subgraph
			removeUnnecessaryEdges(pgm, idxFeatureVariable, idxFeatureVariables, idxClassVariables, adjacencyMatrix);
		});
		// Definition of the feature and bridge subgraph
		logger.info(
				"Define feature subgraph and further remove dependencies in the bridge subgraph considering features" +
						" " + "in the separating set");
		idxFeatureVariables.parallelStream().forEach(idxFeatureVariable -> {
			@SuppressWarnings("unchecked") CTBN<CIMNode> ctbn = new CTBN<>((CTBN<CIMNode>) pgm,
					((CTBN<CIMNode>) pgm).getDataset());
			// Parents of feature variables are being defined in parallel. Class variables
			// are ignored
			try {
				learnFinalBridgeAndFeatureSubgraphs(ctbn, idxFeatureVariable, adjacencyMatrix,
						caches.get(idxFeatureVariable));
			} catch (ErroneousValueException erroneousValueException) {
				logger.error("[Variable {}] {}", ctbn.getNameVariables().get(idxFeatureVariable),
						erroneousValueException.getMessage());
			}
		});
		pgm.setStructure(idxFeatureVariables, adjacencyMatrix);
		pgm.learnParameters(idxFeatureVariables);
		logger.info("Bridge and feature subgraph learnt performing {} conditional independence tests",
				this.numCondIndTestsPerformed);
		// Reset number of performed conditional independence tests
		this.numCondIndTestsPerformed = 0;
	}

	private List<Integer> getIdxClassVariables(PGM<? extends Node> pgm) {
		List<Integer> idxClassVariables = new ArrayList<>();
		for (int idxNode : pgm.getIndexNodes())
			if (pgm.getNodeByIndex(idxNode).isClassVariable())
				idxClassVariables.add(idxNode);
		return idxClassVariables;
	}

	private List<Integer> getParentClassVariable(int idxFeatureXi, List<Integer> idxClassVariables,
												 boolean[][] adjacencyMatrix) {
		List<Integer> idxParentClassVariables = new ArrayList<>();
		for (int idxClassVariable : idxClassVariables)
			if (adjacencyMatrix[idxClassVariable][idxFeatureXi])
				idxParentClassVariables.add(idxClassVariable);
		return idxParentClassVariables;
	}

	private void initialiseCaches(Map<Integer, Map<Long, List<Object>>> caches, List<Integer> idxFeatureVariables) {
		idxFeatureVariables.stream().forEach(idxFeatureVariable -> {
			Map<Long, List<Object>> cache = new HashMap<>();
			// The cache of each feature is saved to use in later computations
			caches.put(idxFeatureVariable, cache);
		});
	}

	private boolean isClassVariableParentsIntersectionEmpty(List<Integer> idxParentClassVariablesXi,
															List<Integer> idxParentClassVariablesXj) {
		for (int idxParentClassVariableXi : idxParentClassVariablesXi)
			if (idxParentClassVariablesXj.contains(idxParentClassVariableXi))
				return false;
		return true;
	}

	private boolean isSubstractionClassVariableParentsEmpty(List<Integer> idxParentClassVariablesXi,
															List<Integer> idxParentClassVariablesXj) {
		for (int idxParentClassVariableXi : idxParentClassVariablesXi)
			if (!idxParentClassVariablesXj.contains(idxParentClassVariableXi))
				return false;
		return true;
	}

	private void learnFinalBridgeAndFeatureSubgraphs(PGM<? extends Node> pgm, int idxNode, boolean[][] adjacencyMatrix,
													 Map<Long, List<Object>> cache) throws ErroneousValueException {
		// Retrieve node
		CIMNode node = (CIMNode) pgm.getNodeByIndex(idxNode);
		logger.debug("Define parents of node {}", node.getName());
		// Retrieve parents of the feature variable
		List<Integer> currentParents = getIdxParentsNode(idxNode, adjacencyMatrix);
		// Iterate over possible sizes of the separating sets
		for (int sizeSepSets = 0; sizeSepSets <= currentParents.size() - 1; sizeSepSets++) {
			logger.debug("Evaluating separating sets of size = {} for {}", sizeSepSets, node.getName());
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
						logger.debug("Test conditional independence between {} and {} given separating set {}",
								node.getName(), parentNode.getName(), nameNodesSepSet);
						this.numCondIndTestsPerformed++;
						// Set nodes of separating set as parents of the node
						addSepSetAsParents(pgm, node, idxSepSet);
						// Retrieve parameters and sufficient statistics given separating set
						retrieveParametersAndSuffStatistics(pgm, idxNode, cache, idxSepSet);
						double[][] qxSepSet = node.getQx();
						CTBNSufficientStatistics suffStatSepSet = node.getSufficientStatistics();
						// Set nodes of separating set and as 'idxParentNode' as parents of the node
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
						// If hypothesis is rejected, evaluate following separating set
						if (!isNullTimeToTransitionHypAccepted)
							continue;
						// The null time to transition hypothesis was not rejected
						if (node.getNumStates() > 2) {
							// Test null state-to-state transition hypothesis
							boolean isNullStateToStateTransitionHypAccepted =
									testNullStateToStateTransitionHypForGivenSepSet(
									node, parentNode, nameNodesSepSet, suffStatSepSet, suffStatSepSetAndParent);
							// If hypothesis is rejected, evaluate following separating set
							if (!isNullStateToStateTransitionHypAccepted)
								continue;
						}
						// If hypothesis tests are not rejected, nodes are conditionally independent
						logger.debug("Removing arc {} <- {}, given separating set {}", node.getName(),
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
	 * Study the conditional independence between the feature and class variables given separating sets that are either
	 * empty or formed by other class variables.
	 *
	 * @param ctbn            ctbn that contains the nodes
	 * @param idxNode         index of the node
	 * @param adjacencyMatrix adjacency matrix
	 * @param cache           cache to avoid recomputations
	 */
	private void learnPreliminaryBridgeSubgraph(CTBN<CIMNode> ctbn, int idxNode, boolean[][] adjacencyMatrix,
												Map<Long, List<Object>> cache) throws ErroneousValueException {
		// Retrieve node
		CIMNode node = ctbn.getNodeByIndex(idxNode);
		logger.debug("Define parents of node {}", node.getName());
		// Retrieve parent class variables of the feature variable
		List<Integer> idxCurrentClassVariableParents = getIdxClassVariables(ctbn);
		// Iterate over possible sizes of the separating sets
		for (int sizeSepSets = 0; sizeSepSets <= idxCurrentClassVariableParents.size() - 1; sizeSepSets++) {
			logger.trace("Evaluating separating sets of size = {} for {}", sizeSepSets, node.getName());
			// Iterate over the parents of the variable to decide if it should be removed
			parentNodeLoop:
			for (Iterator<Integer> itIdxParentNode = idxCurrentClassVariableParents.iterator();
				 itIdxParentNode.hasNext(); ) {
				int idxParentNode = itIdxParentNode.next();
				CIMNode parentNode = ctbn.getNodeByIndex(idxParentNode);
				// Only evaluate the conditional independence of features from class variables
				// on separating sets formed only by class variables
				if (parentNode.isClassVariable() && adjacencyMatrix[idxParentNode][idxNode]) {
					// Retrieve possible separating sets and iterate over them
					List<List<Integer>> possibleSepSets = Util.getSubsets(idxCurrentClassVariableParents, sizeSepSets,
							idxParentNode);
					// Iterate over the separating sets of size "sizeSepSets"
					for (List<Integer> idxSepSet : possibleSepSets) {
						// Extract names of variables in the separating set
						List<String> nameNodesSepSet = ctbn.getNamesNodesByIndex(idxSepSet);
						logger.debug("Test conditional independence between {} and {} given separating set {}",
								node.getName(), parentNode.getName(), nameNodesSepSet);
						// Increase number of conditional independence tests performed
						this.numCondIndTestsPerformed++;
						// Set nodes of separating set as parents of the node
						addSepSetAsParents(ctbn, node, idxSepSet);
						// Retrieve parameters and sufficient statistics given separating set
						retrieveParametersAndSuffStatistics(ctbn, idxNode, cache, idxSepSet);
						double[][] qxSepSet = node.getQx();
						CTBNSufficientStatistics suffStatSepSet = node.getSufficientStatistics();
						// Set nodes of separating set and 'idxParentNode' as parents of the node
						addSepSetAndNodeAsParents(ctbn, node, idxSepSet, idxParentNode);
						// Retrieve parameters and sufficient statistics given 'idxParentNode' and the
						// separating set
						retrieveParametersAndSuffStatistics(ctbn, idxNode, cache, idxSepSet, idxParentNode);
						double[][] qxSepSetAndParent = node.getQx();
						CTBNSufficientStatistics suffStatSepSetAndParent = node.getSufficientStatistics();
						// Test null time to transition hypothesis for the given separating set
						boolean isNullTimeToTransitionHypAccepted = testNullTimeToTransitionHypForGivenSepSet(node,
								parentNode, nameNodesSepSet, qxSepSet, suffStatSepSet, qxSepSetAndParent,
								suffStatSepSetAndParent);
						// If hypothesis is rejected, evaluate following separating set
						if (!isNullTimeToTransitionHypAccepted)
							continue;
						// The null time to transition hypothesis was not rejected
						if (node.getNumStates() > 2) {
							// Test null state-to-state transition hypothesis
							boolean isNullStateToStateTransitionHypAccepted =
									testNullStateToStateTransitionHypForGivenSepSet(
									node, parentNode, nameNodesSepSet, suffStatSepSet, suffStatSepSetAndParent);
							// If hypothesis is rejected, evaluate following separating set
							if (!isNullStateToStateTransitionHypAccepted)
								continue;
						}
						// If hypothesis tests are not rejected, nodes are conditionally independent
						logger.trace("Removing arc {} <- {}, given separating set {}", node.getName(),
								parentNode.getName(), nameNodesSepSet);
						// Conditional independence between the variables was detected
						adjacencyMatrix[idxParentNode][idxNode] = false;
						itIdxParentNode.remove();
						continue parentNodeLoop;
					}
				}
			}
		}
		logger.trace("Parents of node {} were learnt", node.getName());
	}

	private void removeUnnecessaryEdges(PGM<? extends Node> pgm, int idxFeatureXi, List<Integer> idxFeatures,
										List<Integer> idxClassVariables, boolean[][] adjacencyMatrix) {
		// Retrieve parent class variables of the feature variable
		List<Integer> parentClassVariablesXi = getParentClassVariable(idxFeatureXi, idxClassVariables,
				adjacencyMatrix);
		if (parentClassVariablesXi.isEmpty()) {
			logger.debug("Feature variable {} has no class variables as parents", idxFeatureXi);
			for (int idxFeatureXj : idxFeatures) {
				if (idxFeatureXi != idxFeatureXj) {
					logger.debug("Ignoring conditional independence tests of feature {} from feature {}",
							pgm.getNodeByIndex(idxFeatureXi).getName(), pgm.getNodeByIndex(idxFeatureXj).getName());
					adjacencyMatrix[idxFeatureXj][idxFeatureXi] = false;
				}
			}
			return;
		}
		// Evaluate if it is possible to remove dependencies between Xi and any other
		// feature given the bridge subgraph learnt
		for (int idxFeatureXj : idxFeatures) {
			if (idxFeatureXi != idxFeatureXj &&
					(adjacencyMatrix[idxFeatureXj][idxFeatureXi] || adjacencyMatrix[idxFeatureXi][idxFeatureXj])) {
				// Retrieve the class variables that are parents of Xj
				List<Integer> parentClassVariablesXj = getParentClassVariable(idxFeatureXj, idxClassVariables,
						adjacencyMatrix);
				// If one of the feature variables does not have class variables as parents, we
				// cannot perform the following assumptions
				if (parentClassVariablesXj.isEmpty())
					continue;
				// If intersection(Pa_y(X_i), Pa_y(X_j)) == {}, remove arcs between X_i and X_j
				if ((adjacencyMatrix[idxFeatureXi][idxFeatureXj] || adjacencyMatrix[idxFeatureXj][idxFeatureXi]) &&
						isClassVariableParentsIntersectionEmpty(parentClassVariablesXi, parentClassVariablesXj)) {
					logger.debug("Empty intersection between parent class variables of {} and {}",
							pgm.getNodeByIndex(idxFeatureXi).getName(), pgm.getNodeByIndex(idxFeatureXj).getName());

					logger.debug("Ignoring conditional independence tests of feature {} from feature {}",
							pgm.getNodeByIndex(idxFeatureXi).getName(), pgm.getNodeByIndex(idxFeatureXj).getName());
					logger.debug("Ignoring conditional independence tests of feature {} from feature {}",
							pgm.getNodeByIndex(idxFeatureXj).getName(), pgm.getNodeByIndex(idxFeatureXi).getName());

					adjacencyMatrix[idxFeatureXi][idxFeatureXj] = false;
					adjacencyMatrix[idxFeatureXj][idxFeatureXi] = false;
				}
				// Given that the intersection is not empty, if Pa_y(Xi) / Pa_y(Xj) != {},
				// then remove arc from Xj to Xi. Otherwise, there would be a flow of
				// information from Xj to Xi, and Xi would have at least the same parents as Xj
				else {
					if (adjacencyMatrix[idxFeatureXi][idxFeatureXj] &&
							!isSubstractionClassVariableParentsEmpty(parentClassVariablesXi, parentClassVariablesXj)) {
						logger.debug("Subtraction between parent class variables of {} and {} is not empty",
								pgm.getNodeByIndex(idxFeatureXi).getName(),
								pgm.getNodeByIndex(idxFeatureXj).getName());

						logger.debug("Ignoring conditional independence tests of feature {} from feature {}",
								pgm.getNodeByIndex(idxFeatureXj).getName(),
								pgm.getNodeByIndex(idxFeatureXi).getName());
						adjacencyMatrix[idxFeatureXi][idxFeatureXj] = false;
					}
					if (adjacencyMatrix[idxFeatureXj][idxFeatureXi] &&
							!isSubstractionClassVariableParentsEmpty(parentClassVariablesXj, parentClassVariablesXi)) {
						logger.debug("Subtraction between parent class variables of {} and {} is empty",
								pgm.getNodeByIndex(idxFeatureXj).getName(),
								pgm.getNodeByIndex(idxFeatureXi).getName());

						logger.debug("Ignoring conditional independence tests of feature {} from feature {}",
								pgm.getNodeByIndex(idxFeatureXi).getName(),
								pgm.getNodeByIndex(idxFeatureXj).getName());
						adjacencyMatrix[idxFeatureXj][idxFeatureXi] = false;
					}
				}
			}
		}
	}

}