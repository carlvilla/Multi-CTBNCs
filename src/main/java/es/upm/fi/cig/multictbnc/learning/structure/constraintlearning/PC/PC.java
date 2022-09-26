package es.upm.fi.cig.multictbnc.learning.structure.constraintlearning.PC;

import es.upm.fi.cig.multictbnc.exceptions.ErroneousValueException;
import es.upm.fi.cig.multictbnc.learning.parameters.bn.BNSufficientStatistics;
import es.upm.fi.cig.multictbnc.learning.structure.StructureLearningAlgorithm;
import es.upm.fi.cig.multictbnc.models.PGM;
import es.upm.fi.cig.multictbnc.nodes.CPTNode;
import es.upm.fi.cig.multictbnc.nodes.DiscreteNode;
import es.upm.fi.cig.multictbnc.nodes.Node;
import es.upm.fi.cig.multictbnc.util.Util;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * Implementation of the PC algorithm discrete-state Bayesian networks.
 *
 * @author Carlos Villa Blanco
 */
public class PC implements StructureLearningAlgorithm {
	private static final Logger logger = LogManager.getLogger(PC.class);
	double significance;
	int numCondIndTestsPerformed = 0;

	/**
	 * Constructor that initialises the PC algorithm by proving the significance level used.
	 *
	 * @param significance significance level
	 */
	public PC(double significance) {
		this.significance = significance;
	}

	/**
	 * Orient the remaining undirected edges of the Bayesian network.
	 *
	 * @param adjacencyMatrix current adjacency matrix
	 */
	public void orientRemainingUndirectedEdges(boolean[][] adjacencyMatrix) {
		for (int idxNodeX = 0; idxNodeX < adjacencyMatrix.length; idxNodeX++) {
			for (int idxNodeY : findChildrenNode(idxNodeX, adjacencyMatrix)) {
				for (int idxNodeZ : findChildrenNode(idxNodeY, adjacencyMatrix)) {
					if (idxNodeX != idxNodeZ) {
						// If X -> Y - Z, then X -> Y -> Z (avoid new v-structure)
						if (haveDirectedEdge(idxNodeX, idxNodeY, adjacencyMatrix) &&
								haveUndirectedEdge(idxNodeY, idxNodeZ, adjacencyMatrix)) {
							adjacencyMatrix[idxNodeZ][idxNodeY] = false;
						}
						// If X - Z and X -> Y -> Z, then X -> Z (avoid cycles)
						else if (haveDirectedEdge(idxNodeX, idxNodeY, adjacencyMatrix) &&
								haveDirectedEdge(idxNodeY, idxNodeZ, adjacencyMatrix) &&
								haveUndirectedEdge(idxNodeX, idxNodeZ, adjacencyMatrix)) {
							adjacencyMatrix[idxNodeZ][idxNodeX] = false;
						}
						// If X - Z, X - Y -> Z, X - W -> Z, and X and W not adjacent, then X -> Z
						// (avoid new v-structure or cycle)
						else {
							if (haveUndirectedEdge(idxNodeX, idxNodeZ, adjacencyMatrix) &&
									haveUndirectedEdge(idxNodeX, idxNodeY, adjacencyMatrix) &&
									haveDirectedEdge(idxNodeY, idxNodeZ, adjacencyMatrix)) {
								for (int idxNodeW : findChildrenNode(idxNodeX, adjacencyMatrix)) {
									if (idxNodeY != idxNodeW && !areAdjacent(idxNodeY, idxNodeW, adjacencyMatrix) &&
											haveUndirectedEdge(idxNodeX, idxNodeW, adjacencyMatrix)) {
										adjacencyMatrix[idxNodeZ][idxNodeX] = false;
									}
								}
							}

						}
					}
				}
			}
		}
	}

	@Override
	public String getIdentifier() {
		return "PC";
	}

	@Override
	public Map<String, String> getParametersAlgorithm() {
		Map<String, String> parametersAlgorithm = new HashMap<>();
		parametersAlgorithm.put("significancePC", String.valueOf(this.significance));
		return parametersAlgorithm;
	}

	@Override
	public void learn(PGM<? extends Node> pgm, List<Integer> idxNodes) {
		// TODO
	}

	@Override
	public void learn(PGM<? extends Node> pgm) throws ErroneousValueException {
		// Check the number of variables to decide how to proceed
		if (pgm.getNumNodes() > 1) {
			logger.info("Learning {} using the PC algorithm and significance = {}", pgm.getType(), this.significance);
			// Obtain complete undirected graph
			boolean[][] adjacencyMatrix = buildCompleteStructure(pgm);
			// Retrieve edges of the PGM
			List<List<Integer>> edges = getEdgesPGM(pgm);
			// Find skeleton and separation sets
			Map<Integer, List<Integer>> sepSet = learnSkeleton(pgm, adjacencyMatrix, edges);
			// Orient the colliders (v-structures) using the separation sets
			findVStructures(adjacencyMatrix, sepSet);
			// If there are undirected edges, i.e., we have a PDAG, we need to define the
			// direction of those. However, we need to be careful not to introduce
			// v-structures
			List<List<Integer>> undirectedEdges = getUndirectedEdges(edges, adjacencyMatrix);
			while (!undirectedEdges.isEmpty()) {
				// Orient remaining undirected edges
				orientRemainingUndirectedEdges(adjacencyMatrix);
				// A partially directed acyclic graph may have been found
				undirectedEdges = getUndirectedEdges(edges, adjacencyMatrix);
				if (!undirectedEdges.isEmpty())
					undirectedEdgeToDirected(undirectedEdges.get(0), adjacencyMatrix);
			}
			// Set found structure and learn parameters
			pgm.setStructure(adjacencyMatrix);
			logger.info("Class subgraph learnt performing {} conditional independence tests",
					this.numCondIndTestsPerformed);
			// Reset the number of performed conditional independence tests
			this.numCondIndTestsPerformed = 0;
		}
		pgm.learnParameters();
	}

	@Override
	public void learn(PGM<? extends Node> pgm, int idxNode) {
		// TODO
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
	 * Retrieves all possible edges between nodes of a PGM.
	 *
	 * @param pgm a probabilistic graphical model
	 * @return list of all possible edges
	 */
	protected List<List<Integer>> getEdgesPGM(PGM<? extends Node> pgm) {
		List<Integer> idxNodes = pgm.getIndexNodes();
		return Util.combination(idxNodes, 2);
	}

	/**
	 * Finds the skeleton and separation sets of the given PGM. The skeleton is saved in a given adjacency matrix,
	 * while
	 * the method returns the separation sets.
	 *
	 * @param pgm             probabilistic graphical model
	 * @param adjacencyMatrix adjacency matrix
	 * @param edges           list of all possible edges
	 * @return separating sets
	 * @throws ErroneousValueException if a provided parameter is erroneous for the requested task
	 */
	protected Map<Integer, List<Integer>> learnSkeleton(PGM<? extends Node> pgm, boolean[][] adjacencyMatrix,
														List<List<Integer>> edges) throws ErroneousValueException {
		int numNodes = pgm.getNumNodes();
		// Extract the adjacent nodes of all the nodes
		List<List<Integer>> adjacencies = new ArrayList<>();
		for (int idxNodeX = 0; idxNodeX < numNodes; idxNodeX++) {
			List<Integer> adjacentNodes = new ArrayList<>();
			for (int idxNodeY = 0; idxNodeY < numNodes; idxNodeY++) {
				if (adjacencyMatrix[idxNodeX][idxNodeY]) {
					adjacentNodes.add(idxNodeY);
				}
			}
			adjacencies.add(adjacentNodes);
		}
		Map<Integer, List<Integer>> sepSet = new HashMap<>();
		int l = -1;
		while (moreAdjacentNodesThan(adjacencies, l)) {
			l += 1;
			edgeLoop:
			for (Iterator<List<Integer>> iterEdge = edges.iterator(); iterEdge.hasNext(); ) {
				List<Integer> edge = iterEdge.next();
				int idxNodeX = edge.get(0);
				int idxNodeY = edge.get(1);
				CPTNode nodeX = (CPTNode) pgm.getNodeByIndex(idxNodeX);
				CPTNode nodeY = (CPTNode) pgm.getNodeByIndex(idxNodeY);
				if (adjacencies.get(idxNodeX).size() - 1 >= l) {
					// Retrieve all possible separating sets
					List<List<Integer>> possibleSepSets = Util.getSubsets(adjacencies.get(idxNodeX), l, idxNodeY);
					for (List<Integer> idxSepSet : possibleSepSets) {
						// Extract names of variables in the subset
						List<String> nameNodesSepSet = pgm.getNamesNodesByIndex(idxSepSet);
						logger.debug("Test conditional independence between {} and {} given separating set {}",
								nodeX.getName(), nodeY.getName(), nameNodesSepSet);
						numCondIndTestsPerformed++;
						// Remove previous parents of the nodes
						nodeX.removeParents();
						nodeY.removeParents();
						// Retrieve sufficient statistics of nodeX given separating set
						addParentsToNode(pgm, nodeX, idxSepSet);
						pgm.computeSufficientStatistics(List.of(idxNodeX));
						BNSufficientStatistics suffStatNodeXGivenSepSet = nodeX.getSufficientStatistics();
						// Retrieve sufficient statistics of nodeY given separating set
						addParentsToNode(pgm, nodeY, idxSepSet);
						pgm.computeSufficientStatistics(List.of(idxNodeY));
						BNSufficientStatistics suffStatNodeYGivenSepSet = nodeY.getSufficientStatistics();
						// Retrieve sufficient statistics of nodeX <- nodeY given separating set
						nodeX.setParent(nodeY);
						pgm.computeSufficientStatistics(List.of(idxNodeX));
						BNSufficientStatistics suffStatNodeXGivenNodeYSepSet = nodeX.getSufficientStatistics();
						// Test if nodeX and nodeY are conditionally independent given a separating set
						boolean areConditionalDependent = testConditionalDependence(nodeX, nodeY,
								suffStatNodeXGivenSepSet, suffStatNodeYGivenSepSet, suffStatNodeXGivenNodeYSepSet);
						if (!areConditionalDependent) {
							logger.debug("Nodes {} and {} are conditionally independent given {}", nodeX.getName(),
									nodeY.getName(), nameNodesSepSet);
							adjacencyMatrix[idxNodeX][idxNodeY] = false;
							adjacencyMatrix[idxNodeY][idxNodeX] = false;
							// Remove edge so it is not considered in future iterations
							iterEdge.remove();
							// Remove edges between nodeX and nodeY. Cast to Integer to not remove by index
							adjacencies.get(idxNodeX).remove((Integer) idxNodeY);
							adjacencies.get(idxNodeY).remove((Integer) idxNodeX);
							// Save the separation set that makes nodeX and nodeY conditionally independent
							int edgeKey = Util.szudzikFunction(idxNodeX, idxNodeY);
							sepSet.put(edgeKey, idxSepSet);
							continue edgeLoop;
						}
					}
				}
			}
		}
		return sepSet;
	}

	private void addParentsToNode(PGM<? extends Node> pgm, DiscreteNode node, List<Integer> subset) {
		for (int idxNodeSubset : subset)
			pgm.getNodeByIndex(idxNodeSubset).setChild(node);
	}

	private boolean areAdjacent(int idxNodeX, int idxNodeY, boolean[][] adjacencyMatrix) {
		return adjacencyMatrix[idxNodeX][idxNodeY] || adjacencyMatrix[idxNodeY][idxNodeX];
	}

	private List<Integer> findChildrenNode(int idxNode, boolean[][] adjacencyMatrix) {
		List<Integer> children = new ArrayList<>();
		for (int idxChild = 0; idxChild < adjacencyMatrix.length; idxChild++)
			if (idxNode != idxChild && adjacencyMatrix[idxNode][idxChild])
				children.add(idxChild);
		return children;
	}

	/**
	 * Finds v-structures in the skeleton.
	 *
	 * @param adjacencyMatrix adjacency matrix
	 * @param sepSet          separating set
	 */
	private void findVStructures(boolean[][] adjacencyMatrix, Map<Integer, List<Integer>> sepSet) {
		for (int idxNodeX = 0; idxNodeX < adjacencyMatrix.length; idxNodeX++) {
			for (int idxNodeY : findChildrenNode(idxNodeX, adjacencyMatrix)) {
				for (int idxNodeZ : findChildrenNode(idxNodeY, adjacencyMatrix)) {
					if (idxNodeX != idxNodeZ && isUnshieldedTriple(idxNodeX, idxNodeY, idxNodeZ, adjacencyMatrix)) {
						int keyEdge = 0;
						if (idxNodeX < idxNodeZ)
							keyEdge = Util.szudzikFunction(idxNodeX, idxNodeZ);
						else
							keyEdge = Util.szudzikFunction(idxNodeZ, idxNodeX);
						// If separation set between nodeX and nodeZ does not contain nodeY, a
						// v-structure was found
						if (sepSet.get(keyEdge) == null || !sepSet.get(keyEdge).contains(idxNodeY)) {
							adjacencyMatrix[idxNodeY][idxNodeX] = false;
							adjacencyMatrix[idxNodeY][idxNodeZ] = false;
						}
					}
				}
			}
		}
	}

	/**
	 * Return the number of samples for a given instantiation of the separation set.
	 *
	 * @param nodeX                         a {@code CPTNode}
	 * @param nodeY                         a {@code CPTNode}
	 * @param suffStatNodeXGivenNodeYSepSet sufficient statistics of nodeX given nodeY and the separating set
	 * @return number of samples for a given instantiation of the separation set
	 */
	private int getNumSamplesGivenSepSet(CPTNode nodeX, CPTNode nodeY,
										 BNSufficientStatistics suffStatNodeXGivenNodeYSepSet) {
		int numSamples = 0;
		for (int idxStateNodeY = 0; idxStateNodeY < nodeY.getNumStates(); idxStateNodeY++) {
			nodeY.setState(idxStateNodeY);
			for (int idxStateNodeX = 0; idxStateNodeX < nodeX.getNumStates(); idxStateNodeX++) {
				numSamples += suffStatNodeXGivenNodeYSepSet.getNx()[nodeX.getIdxStateParents()][idxStateNodeX];
			}
		}
		return numSamples;
	}

	private List<List<Integer>> getUndirectedEdges(List<List<Integer>> edges, boolean[][] adjacencyMatrix) {
		List<List<Integer>> undirectedEdges = new ArrayList<>();
		for (List<Integer> edge : edges)
			if (adjacencyMatrix[edge.get(0)][edge.get(1)] && adjacencyMatrix[edge.get(1)][edge.get(0)])
				undirectedEdges.add(edge);
		return undirectedEdges;
	}

	private boolean haveDirectedEdge(int idxNodeX, int idxNodeY, boolean[][] adjacencyMatrix) {
		return adjacencyMatrix[idxNodeX][idxNodeY] && !adjacencyMatrix[idxNodeY][idxNodeX];
	}

	private boolean haveUndirectedEdge(int idxNodeX, int idxNodeY, boolean[][] adjacencyMatrix) {
		return adjacencyMatrix[idxNodeX][idxNodeY] && adjacencyMatrix[idxNodeY][idxNodeX];
	}

	private boolean isUnshieldedTriple(int idxNodeX, int idxNodeY, int idxNodeZ, boolean[][] adjacencyMatrix) {
		return haveUndirectedEdge(idxNodeX, idxNodeY, adjacencyMatrix) &&
				haveUndirectedEdge(idxNodeY, idxNodeZ, adjacencyMatrix) &&
				!areAdjacent(idxNodeX, idxNodeZ, adjacencyMatrix);
	}

	private boolean moreAdjacentNodesThan(List<List<Integer>> adjacencies, int size) {
		for (int idxNode = 0; idxNode < adjacencies.size(); idxNode++)
			if (adjacencies.get(idxNode).size() - 1 > size)
				return true;
		return false;
	}

	private boolean testConditionalDependence(CPTNode nodeX, CPTNode nodeY,
											  BNSufficientStatistics suffStatNodeXGivenSepSet,
											  BNSufficientStatistics suffStatNodeYGivenSepSet,
											  BNSufficientStatistics suffStatNodeXGivenNodeYSepSet)
			throws ErroneousValueException {
		double chiSquared = 0;
		int df = 0;
		// Iterate over all possible states of the separating set
		for (int idxStateSepSet = 0; idxStateSepSet < nodeY.getNumStatesParents(); idxStateSepSet++) {
			// Establish the state of the separating set
			nodeY.setStateParents(idxStateSepSet);
			int numSamples = getNumSamplesGivenSepSet(nodeX, nodeY, suffStatNodeXGivenNodeYSepSet);
			for (int idxStateNodeY = 0; idxStateNodeY < nodeY.getNumStates(); idxStateNodeY++) {
				nodeY.setState(idxStateNodeY);
				double probNodeY = suffStatNodeYGivenSepSet.getNx()[idxStateSepSet][idxStateNodeY] / numSamples;
				for (int idxStateNodeX = 0; idxStateNodeX < nodeX.getNumStates(); idxStateNodeX++) {
					double probNodeX = suffStatNodeXGivenSepSet.getNx()[idxStateSepSet][idxStateNodeX] / numSamples;
					double observed = suffStatNodeXGivenNodeYSepSet.getNx()[nodeX.getIdxStateParents()][idxStateNodeX];
					double expected = (probNodeY * probNodeX) * numSamples;
					chiSquared += Math.pow(observed - expected, 2) / expected;
					df += (nodeX.getNumStates() - 1) * (nodeX.getNumStatesParents() - 1);
				}
			}
		}
		// Obtain critical value for the given significance
		double criticalValue = 0;
		try {
			// Retrieve critical values from the chi-squared distribution
			ChiSquaredDistribution chiSquaredDistribution = new ChiSquaredDistribution(df);
			criticalValue = chiSquaredDistribution.inverseCumulativeProbability(1 - this.significance);
		} catch (OutOfRangeException ore) {
			throw new ErroneousValueException(
					"The significance must be in range [0, 1]. Value provided: " + this.significance);
		} catch (NotStrictlyPositiveException nspe) {
			throw new ErroneousValueException(
					"Degree of freedom provided to the chi-squared test (PC algorithm) is zero. Check if variables " +
							nodeX.getName() + " or " + nodeY.getName() + " have only one possible state");
		}
		// If chiSquared >= criticalValue, the null hypothesis is rejected, and
		// variables are conditionally dependent
		return chiSquared >= criticalValue;
	}

	private void undirectedEdgeToDirected(List<Integer> edge, boolean[][] adjacencyMatrix) {
		adjacencyMatrix[edge.get(1)][edge.get(0)] = false;
	}

}