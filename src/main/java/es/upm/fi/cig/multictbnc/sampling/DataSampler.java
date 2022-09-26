package es.upm.fi.cig.multictbnc.sampling;

import es.upm.fi.cig.multictbnc.data.representation.Dataset;
import es.upm.fi.cig.multictbnc.data.representation.Sequence;
import es.upm.fi.cig.multictbnc.data.writer.MultipleCSVWriter;
import es.upm.fi.cig.multictbnc.learning.structure.constraints.BN.DAG;
import es.upm.fi.cig.multictbnc.models.BN;
import es.upm.fi.cig.multictbnc.models.CTBN;
import es.upm.fi.cig.multictbnc.models.MultiCTBNC;
import es.upm.fi.cig.multictbnc.nodes.CIMNode;
import es.upm.fi.cig.multictbnc.nodes.CPTNode;
import es.upm.fi.cig.multictbnc.util.ProbabilityUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

/**
 * Implements methods for the generation and writing of datasets sampled from Multi-CTBNCs.
 *
 * @author Carlos Villa Blanco
 */
public class DataSampler {
	private static final Logger logger = LogManager.getLogger(DataSampler.class);
	private static boolean[][] previousAdjacencyMatrix;

	/**
	 * Sample a dataset from the provided model.
	 *
	 * @param multiCTBNC        model from which datasets are sampled
	 * @param numSequences      number of sequences of the dataset
	 * @param durationSequences duration of the sequences
	 * @param destinationPath   path where the dataset is saved
	 */
	public static void generateDataset(MultiCTBNC<CPTNode, CIMNode> multiCTBNC, int numSequences,
									   double durationSequences, String destinationPath) {
		if (multiCTBNC == null)
			// The selected experiment was not found
			return;
		// Sample sequences from the Multi-CTBNC
		List<Sequence> sequences = new ArrayList<>();
		IntStream.range(0, numSequences).forEach(indexSequence -> {
			sequences.add(multiCTBNC.sample(durationSequences));
		});
		// Create a dataset with the generated sequences
		Dataset dataset = new Dataset(sequences);
		// Save the dataset in the provided path
		MultipleCSVWriter.write(dataset, destinationPath);
		logger.info("Dataset generated!");
	}

	/**
	 * Generate uniformly distributed random conditional intensity tables for a continuous-time Bayesian network.
	 *
	 * @param ctbn         continuous-time Bayesian network
	 * @param minIntensity minimum value of the intensities
	 * @param maxIntensity maximum value of the intensities
	 */
	public static void generateRandomCIMs(CTBN<CIMNode> ctbn, double minIntensity, double maxIntensity) {
		// The initial distribution of a CTBN is a Bayesian network
		for (CIMNode node : ctbn.getNodes()) {
			logger.info("Generating CIM of node {}", node.getName());
			// Number of states of the node and its parents
			int numStates = node.getNumStates();
			int numStatesParents = node.getNumStatesParents();
			// Initialise parameters of the node
			double[][] Qx = new double[numStatesParents][numStates];
			double[][][] Oxx = new double[numStatesParents][numStates][numStates];
			// Iterate over states of the parents
			for (int idxStateParents = 0; idxStateParents < numStatesParents; idxStateParents++) {
				// Define intensity matrix for the instantiation of the parents
				double[][] im = new double[numStates][numStates];
				// Iterate over elements of the intensity matrix
				for (int idxFromState = 0; idxFromState < numStates; idxFromState++) {
					// Generate the intensity for the variable for leaving a certain state
					double intensityDiagonal = minIntensity + (maxIntensity - minIntensity) * Math.random();
					im[idxFromState][idxFromState] = intensityDiagonal;
					Qx[idxStateParents][idxFromState] = intensityDiagonal;
					// Generate the intensities of leaving the state for a certain one
					// Generate random numbers for the off-diagonal intensities and sum them
					double sum = 0;
					for (int idxToState = 0; idxToState < numStates; idxToState++) {
						if (idxToState != idxFromState) {
							im[idxFromState][idxToState] = Math.random();
							sum += im[idxFromState][idxToState];
						}
					}
					// Divide the random numbers of the off-diagonals by their sum and multiple each
					// by the intensity of the diagonal, so their sum is equal to the diagonal
					// intensity
					for (int idxToState = 0; idxToState < numStates; idxToState++) {
						if (idxToState != idxFromState) {
							double intensity = (im[idxFromState][idxToState] / sum) * intensityDiagonal;
							im[idxFromState][idxToState] = intensity;
							Oxx[idxStateParents][idxFromState][idxToState] =
									im[idxFromState][idxToState] / im[idxFromState][idxFromState];
						}
					}
				}
			}
			// Set parameters on the node
			node.setParameters(Qx, Oxx);
		}
	}

	/**
	 * Generate uniformly distributed random conditional probability tables for a Bayesian network.
	 *
	 * @param bn               a Bayesian network
	 * @param forceExtremeProb true to force the probabilities to be extreme (0 to 0.3 or 0.7 to 1) if the size of the
	 *                         sample space of the class variables is 2, false otherwise
	 */
	public static void generateRandomCPTs(BN<CPTNode> bn, boolean forceExtremeProb) {
		// Iterate over all possible nodes to define their CPTs
		for (CPTNode node : bn.getNodes()) {
			logger.info("Generating CPT of node {}", node.getName());
			// Number of states node and parents
			int numStates = node.getNumStates();
			int numStatesParents = node.getNumStatesParents();
			// Initialise CPT
			double[][] CPT = new double[numStatesParents][numStates];
			// Iterate over states of the node and its parents
			for (int idxStateParents = 0; idxStateParents < numStatesParents; idxStateParents++) {
				if (numStates == 2) {
					// Obtain the probability of the node state given the parents from a uniform
					// distribution
					double prob = forceExtremeProb ? ProbabilityUtil.extremeProbability() : Math.random();
					for (int idxState = 0; idxState < numStates; idxState++) {
						CPT[idxStateParents][idxState] = prob;
						prob = 1 - prob;
					}
				} else {
					// Generate a random vector uniformly distributed over a numStates-dimensional
					// simplex (Rubinstein, 1982)
					double x[] = new double[numStates];
					double sum = 0.0;
					for (int idxState = 0; idxState < numStates; idxState++) {
						x[idxState] = -Math.log(1.0 - Math.random());
						sum += x[idxState];
					}
					for (int idxState = 0; idxState < numStates; idxState++)
						CPT[idxStateParents][idxState] = x[idxState] / sum;
				}
			}
			node.setCPT(CPT);
		}
	}

	/**
	 * Generates a Multi-CTBNC that can be used to sample data.
	 *
	 * @param numFeatureVariables            number of feature variables
	 * @param numClassVariables              number of class variables
	 * @param cardinalityFeatureVariables    cardinalities of the feature variables
	 * @param cardinalityClassVariables      cardinalities of the class variables
	 * @param probabilityEdgeClassSubgraph   probability of adding an edge in the class subgraph
	 * @param probabilityEdgeBridgeSubgraph  probability of adding an edge in the bridge subgraph
	 * @param probabilityEdgeFeatureSubgraph probability of adding an edge in the feature subgraph
	 * @param minIntensity                   minimum intensity
	 * @param maxIntensity                   maximum intensity
	 * @param maxNumParentsFeature           maximum number of feature variables that can be parents of another feature
	 *                                       variable
	 * @param differentStructurePerDataset   {@code true} to used the structure defined for a previous model, {@code
	 *                                       false} otherwise.
	 * @param forceExtremeProb               {@code true} to force the probabilities of the CPTs to be extreme (0 to
	 *                                                     0.3
	 *                                       or 0.7 to 1), {@code false} otherwise
	 * @param adjMatrix                      adjacency matrix used to define the structure of the model (null to define
	 *                                       the structure randomly)
	 * @return Multi-CTBNC generated
	 */
	protected static MultiCTBNC<CPTNode, CIMNode> generateModel(int numFeatureVariables, int numClassVariables,
																int cardinalityFeatureVariables,
																int cardinalityClassVariables,
																double probabilityEdgeClassSubgraph,
																double probabilityEdgeBridgeSubgraph,
																double probabilityEdgeFeatureSubgraph,
																int minIntensity,
																int maxIntensity, int maxNumParentsFeature,
																boolean differentStructurePerDataset,
																boolean forceExtremeProb, boolean[][] adjMatrix) {
		// Define class variables. Specify their names and sample spaces.
		BN<CPTNode> CS = initialiseClassSubgraph(numClassVariables, cardinalityClassVariables);
		// Define feature variables
		CTBN<CIMNode> FBS = initialiseFeatureAndBridgeSubgraphs(numFeatureVariables, cardinalityFeatureVariables, CS);
		// If no adjacency matrix is provided, a random matrix is generated
		if (adjMatrix == null) {
			adjMatrix = generateRandomAdjacencyMatrix(numFeatureVariables, numClassVariables,
					probabilityEdgeClassSubgraph, probabilityEdgeBridgeSubgraph, probabilityEdgeFeatureSubgraph,
					maxNumParentsFeature, differentStructurePerDataset);
		}
		// Definition of the initial Multi-CTBNC
		MultiCTBNC<CPTNode, CIMNode> multiCTBNC = new MultiCTBNC<>(CS, FBS);
		multiCTBNC.setStructure(adjMatrix);
		// Definition of the CPTs and CIMs
		generateRandomCPTs(multiCTBNC.getBN(), forceExtremeProb);
		generateRandomCIMs(multiCTBNC.getCTBN(), minIntensity, maxIntensity);
		return multiCTBNC;
	}

	private static CPTNode generateClassVariable(String name, List<String> states) {
		return new CPTNode(name, states, true);
	}

	private static CIMNode generateFeatureVariable(String name, List<String> states) {
		return new CIMNode(name, states, false);
	}

	private static boolean[][] generateRandomAdjacencyMatrix(int numFeatureVariables, int numClassVariables,
															 double probabilityEdgeClassSubgraph,
															 double probabilityEdgeBridgeSubgraph,
															 double probabilityEdgeFeatureSubgraph,
															 int maxNumParentsFeature,
															 boolean differentStructurePerDataset) {
		if (differentStructurePerDataset || previousAdjacencyMatrix == null) {
			// Randomly define the structure of the Multi-CTBNC
			boolean[][] adjacencyMatrix = new boolean[numClassVariables + numFeatureVariables][numClassVariables +
					numFeatureVariables];
			// Generate random class subgraph
			adjacencyMatrix = generateRandomClassSubgraph(numFeatureVariables, adjacencyMatrix,
					probabilityEdgeClassSubgraph);
			// Generate random bridge subgraph
			adjacencyMatrix = generateRandomBridgeSubgraph(numFeatureVariables, adjacencyMatrix,
					probabilityEdgeBridgeSubgraph);
			// Generate random feature subgraph
			adjacencyMatrix = generateRandomFeatureSubgraph(numFeatureVariables, maxNumParentsFeature, adjacencyMatrix,
					probabilityEdgeFeatureSubgraph);
			previousAdjacencyMatrix = adjacencyMatrix;

			return adjacencyMatrix;
		}
		return previousAdjacencyMatrix;
	}

	private static boolean[][] generateRandomBridgeSubgraph(int idxFirstClassVariable, boolean[][] adjacencyMatrix,
															double probabilityEdgeBridgeSubgraph) {
		Random random = new Random();
		int idxLastClassVariable = adjacencyMatrix.length;
		// At least one edge is expected in the bridge subgraph
		boolean oneEdgeAdded = false;
		while (!oneEdgeAdded) {
			for (int i = idxFirstClassVariable; i < idxLastClassVariable; i++) {
				for (int j = 0; j < idxFirstClassVariable; j++) {
					boolean addEdge = random.nextFloat() < probabilityEdgeBridgeSubgraph;
					adjacencyMatrix[i][j] = addEdge;
					oneEdgeAdded = oneEdgeAdded || addEdge;
				}
			}
		}
		return adjacencyMatrix;
	}

	private static boolean[][] generateRandomClassSubgraph(int idxFirstClassVariable, boolean[][] adjacencyMatrix,
														   double probabilityEdgeClassSubgraph) {
		Random random = new Random();
		DAG dag = new DAG();
		for (int i = idxFirstClassVariable; i < adjacencyMatrix.length; i++) {
			for (int j = idxFirstClassVariable; j < adjacencyMatrix.length; j++) {
				if (i != j) {
					boolean addEdge = random.nextFloat() < probabilityEdgeClassSubgraph;
					adjacencyMatrix[i][j] = addEdge;
					if (addEdge && !dag.isStructureLegal(adjacencyMatrix, null)) {
						adjacencyMatrix[i][j] = false;
					}
				}
			}
		}
		return adjacencyMatrix;
	}

	private static boolean[][] generateRandomFeatureSubgraph(int numFeatureVariables, int maxNumParentsFeature,
															 boolean[][] adjacencyMatrix,
															 double probabilityEdgeFeatureSubgraph) {
		Random random = new Random();
		for (int i = 0; i < numFeatureVariables; i++) {
			for (int j = 0; j < numFeatureVariables; j++) {
				if (getNumParents(j, adjacencyMatrix) == maxNumParentsFeature)
					continue;
				if (i != j)
					adjacencyMatrix[i][j] = random.nextFloat() < probabilityEdgeFeatureSubgraph;
			}
		}
		return adjacencyMatrix;
	}

	private static int getNumParents(int idxChild, boolean[][] adjacencyMatrix) {
		int numParents = 0;
		for (int idxParent = 0; idxParent < adjacencyMatrix.length; idxParent++) {
			if (idxChild != idxParent && adjacencyMatrix[idxParent][idxChild]) {
				numParents++;
			}
		}
		return numParents;
	}

	private static BN<CPTNode> initialiseClassSubgraph(int numClassVariables, int cardinalityClassVariables) {
		List<CPTNode> classVariables = new ArrayList<>();
		for (int idxClassVariable = 0; idxClassVariable < numClassVariables; idxClassVariable++) {
			// Generate states of the class variables
			List<String> states = new ArrayList<>();
			for (int idxState = 0; idxState < cardinalityClassVariables; idxState++)
				states.add("C" + (idxClassVariable + 1) + "_" + idxState);
			CPTNode classVariable = generateClassVariable("C" + (idxClassVariable + 1), states);
			classVariables.add(classVariable);
		}
		return new BN<>(classVariables);
	}

	private static CTBN<CIMNode> initialiseFeatureAndBridgeSubgraphs(int numFeatureVariables,
																	 int cardinalityFeatureVariables, BN<CPTNode> CS) {
		List<CIMNode> featureVariables = new ArrayList<>();
		for (int idxFeatureVariable = 0; idxFeatureVariable < numFeatureVariables; idxFeatureVariable++) {
			// Generate states of the class variables
			List<String> states = new ArrayList<>();
			for (int idxState = 0; idxState < cardinalityFeatureVariables; idxState++)
				states.add("X" + (idxFeatureVariable + 1) + "_" + idxState);
			CIMNode featureVariable = generateFeatureVariable("X" + (idxFeatureVariable + 1), states);
			featureVariables.add(featureVariable);
		}
		return new CTBN<>(featureVariables, CS);
	}

}