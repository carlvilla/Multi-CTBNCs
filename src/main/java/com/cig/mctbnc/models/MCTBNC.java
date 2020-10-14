package com.cig.mctbnc.models;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cig.mctbnc.classification.Classifier;
import com.cig.mctbnc.classification.Prediction;
import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.data.representation.Sequence;
import com.cig.mctbnc.data.representation.State;
import com.cig.mctbnc.learning.BNLearningAlgorithms;
import com.cig.mctbnc.learning.CTBNLearningAlgorithms;
import com.cig.mctbnc.learning.structure.constraints.StructureConstraints;
import com.cig.mctbnc.learning.structure.constraints.BN.DAG;
import com.cig.mctbnc.learning.structure.constraints.CTBNC.CTBNC;
import com.cig.mctbnc.nodes.Node;
import com.cig.mctbnc.nodes.NodeIndexer;
import com.cig.mctbnc.util.ProbabilityUtil;
import com.cig.mctbnc.util.Util;

/**
 * 
 * Implements the multi-dimensional continuous time Bayesian network classifier
 * (MCTBNC).
 * 
 * @author Carlos Villa Blanco
 * 
 * @param <NodeTypeBN>   Type of the nodes of the Bayesian network (discrete or
 *                       continuous)
 * @param <NodeTypeCTBN> Type of the nodes of the continuous time Bayesian
 *                       network (discrete or continuous)
 */
public class MCTBNC<NodeTypeBN extends Node, NodeTypeCTBN extends Node> extends AbstractPGM<Node>
		implements Classifier {
	// The subgraph formed by class variables is a Bayesian network
	private BN<NodeTypeBN> bn;
	private CTBN<NodeTypeCTBN> ctbn;
	// Classes of the nodes
	Class<NodeTypeBN> bnNodeClass;
	Class<NodeTypeCTBN> ctbnNodeClass;
	// Algorithms used to learn the class subgraph (BN)
	BNLearningAlgorithms bnLearningAlgs;
	// Algorithms used to learn the feature and bridge subgraphs (CTBN)
	CTBNLearningAlgorithms ctbnLearningAlgs;
	// Initial structure (Empty by default)
	String initialStructure = "Empty";
	static Logger logger = LogManager.getLogger(MCTBNC.class);

	/**
	 * Constructor for MCTBNC.
	 * 
	 * @param bnLearningAlgs   algorithms used to learn a BN
	 * @param ctbnLearningAlgs algorithms used to learn a CTBN
	 * @param bnNodeClass      type of the BN nodes
	 * @param ctbnNodeClass    type of the CTBN nodes
	 */
	public MCTBNC(BNLearningAlgorithms bnLearningAlgs, CTBNLearningAlgorithms ctbnLearningAlgs,
			Class<NodeTypeBN> bnNodeClass, Class<NodeTypeCTBN> ctbnNodeClass) {
		// Save the class type of the nodes
		this.bnNodeClass = bnNodeClass;
		this.ctbnNodeClass = ctbnNodeClass;
		// Save algorithms used for the learning of the MCTBNC
		this.bnLearningAlgs = bnLearningAlgs;
		this.ctbnLearningAlgs = ctbnLearningAlgs;
	}

	@Override
	public void learn(Dataset dataset) {
		logger.info("Learning model with {} training samples", dataset.getNumDataPoints());
		// Remove previous instantiation of the model (if any)
		removeAllNodes();
		// Save dataset used for training
		this.dataset = dataset;
		// Measure execution time
		Instant start = Instant.now();
		// ------------------ Class subgraph ------------------
		// Learn structure and parameters of class subgraph (Bayesian network)
		logger.info("Defining structure and parameters of the class subgraph (Bayesian network)");
		List<String> nameClassVariables = dataset.getNameClassVariables();
		bn = new BN<NodeTypeBN>(dataset, nameClassVariables, bnLearningAlgs, getStructureConstraintsBN(), bnNodeClass);
		bn.learn();
		logger.info("Class subgraph established!");
		// ----------- Feature and bridge subgraphs -----------
		// Learn structure and parameters of feature and bridge subgraph. These are
		// modeled by a continuous time Bayesian network classifier where the
		// restriction that the class variable does not depend on the states of the
		// features is extended to more class variables
		logger.info("Defining structure and parameters of the feature and bridge subgraphs (Continuous time "
				+ "Bayesian network)");
		List<String> nameVariables = dataset.getNameVariables();
		ctbn = new CTBN<NodeTypeCTBN>(dataset, nameVariables, ctbnLearningAlgs, getStructureConstraintsCTBN(), bn,
				ctbnNodeClass);
		setInitialStructure(ctbn);
		ctbn.learn();
		logger.info("Feature and bridge subgraphs established!");
		// ------------------ Join subgraphs ------------------
		// Join class subgraph with feature and bridge subgraphs
		setStructure();
		Instant end = Instant.now();
		logger.info("MCTBNC model learnt in {} seconds", Duration.between(start, end));
	}

	/**
	 * Defines the nodes of the MCTBNC by using the nodes obtained from the BN and
	 * the CTBN.
	 */
	private void setStructure() {
		// Obtain nodes that will be added to the model
		List<Node> nodes = new ArrayList<Node>();
		// It is important to respect the original order of the variables to generate a
		// correct adjacency matrix
		for (String nameVariable : dataset.getNameVariables()) {
			// Each variable node is obtained from the BN (if it exists) and from the CTBN
			Node nodeInBN = bn.getNodeByName(nameVariable);
			Node nodeInCTBN = ctbn.getNodeByName(nameVariable);
			if (nodeInBN != null) {
				// Arcs between nodes of the BN and CTBN are created, while those from class
				// variables nodes of the CTBN are removed. A temporary list is used
				// to avoid a concurrent modification exception
				List<Node> tempList = new ArrayList<Node>(nodeInCTBN.getChildren());
				for (Node child : tempList) {
					// CTBN class variables nodes are removed from being parents. These nodes are
					// not necessary anymore
					child.removeParent(nodeInCTBN);
					// BN class variables nodes are added as parents for the CTBN feature nodes
					child.setParent(nodeInBN);
				}
				nodes.add(nodeInBN);
			} else
				// It is a feature node
				nodes.add(nodeInCTBN);
		}
		// The nodes are added to the MCTBNC
		addNodes(nodes);
	}

	/**
	 * Return the structure constraints for the BN.
	 * 
	 * @return StructureConstraint object
	 */
	public StructureConstraints getStructureConstraintsBN() {
		return new DAG();
	}

	/**
	 * Return the structure constraints for the CTBN.
	 * 
	 * @return StructureConstraint object
	 */
	public StructureConstraints getStructureConstraintsCTBN() {
		return new CTBNC();
	}

	/**
	 * Establish the approach that will be used to define the initial structure of
	 * the MCTBNC. For now it is possible to define an empty structure or a naive
	 * Bayes.
	 * 
	 * @param initialStructure initial structure that will be used (Empty (default)
	 *                         or naive Bayes)
	 */
	public void setIntialStructure(String initialStructure) {
		this.initialStructure = initialStructure;
	}

	@Override
	public void setStructure(boolean[][] adjacencyMatrix) {
		for (int i = 0; i < adjacencyMatrix.length; i++) {
			for (int j = 0; j < adjacencyMatrix.length; j++) {
				if (adjacencyMatrix[i][j]) {
					// Add arc from node i to j
					Node nodeParent = getNodeByIndex(i);
					Node nodeChildren = getNodeByIndex(j);
					nodeParent.setChild(nodeChildren);
					nodeChildren.setParent(nodeParent);
				}
			}
		}
	}

	@Override
	public String getType() {
		return "Multidimensional continuous time Bayesian network classifier";
	}

	/**
	 * Perform classification over the sequences of a dataset according to the
	 * maximum a posteriori probability, i.e., classes that obtain the highest
	 * posterior probability given each sequence are predicted.
	 * 
	 * @param dataset               dataset from which the sequences to predict are
	 *                              extracted
	 * @param estimateProbabilities determines if the probabilities of the classes
	 *                              are estimated
	 * @return array of Prediction object (one per sequence) that contain the
	 *         predicted classes and, if requested, the probabilities of all
	 *         possible classes
	 */
	@Override
	public Prediction[] predict(Dataset dataset, boolean estimateProbabilities) {
		logger.info("Performing prediction over {} sequences", dataset.getNumDataPoints());
		int numSequences = dataset.getNumDataPoints();
		Prediction[] predictions = new Prediction[numSequences];
		// Obtain the name of the class variables
		List<String> nameClassVariables = dataset.getNameClassVariables();
		// Obtation all possible states of the class variables
		List<State> statesClassVariables = dataset.getPossibleStatesVariables(nameClassVariables);
		// Make predictions on all the sequences
		for (int i = 0; i < numSequences; i++) {
			logger.trace("Performing prediction over sequence {}/{}", i, dataset.getNumDataPoints());
			Sequence evidenceSequence = dataset.getSequences().get(i);
			predictions[i] = predict(evidenceSequence, statesClassVariables, estimateProbabilities);
		}
		return predictions;
	}

	/**
	 * Perform classification over a sequence according to the maximum a posteriori
	 * probability.
	 * 
	 * @param sequence             sequence whose class variables are predicted
	 * @param statesClassVariables possible class configurations
	 * @return Prediction object that contains the predicted classes and, if
	 *         requested, the probabilities of all possible classes
	 */
	private Prediction predict(Sequence sequence, List<State> statesClassVariables, boolean estimateProbabilities) {
		// Obtain nodes of the Bayesian network and continuous time Bayesian network
		List<NodeTypeBN> nodesBN = getNodesBN();
		List<NodeTypeCTBN> nodesCTBN = getNodesCTBN();
		// Estimate unnormalized log-a-posteriori probabilities of class configurations
		double[] laps = new double[statesClassVariables.size()];
		for (int i = 0; i < statesClassVariables.size(); i++) {
			// Get class configuration 'i' of the class variables
			State stateClassVariables = statesClassVariables.get(i);
			// Compute the log-a-posteriori probability of current classes 'i'
			double lap = 0;
			// Estimate the log-prior probability of the classes 'i'
			lap += ProbabilityUtil.logPriorProbabilityClassVariables(nodesBN, stateClassVariables);
			// Estimate log-likelihood of the sequence given the classes 'i'
			lap += ProbabilityUtil.logLikelihoodSequence(sequence, nodesCTBN, stateClassVariables);
			// Store log-a-posteriori probability of class variables taking classes 'i'
			laps[i] = lap;
		}
		// Define a Prediction object to save the result
		Prediction prediction = new Prediction();
		// Retrieve class configuration which obtain largest a-posterior probability
		int idxBestCC = Util.getIndexLargestValue(laps);
		State predictedCC = statesClassVariables.get(idxBestCC);
		// Save the predicted classes
		prediction.setPredictedClasses(predictedCC);
		if (estimateProbabilities) {
			// If the class probabilities are requested, the marginal log-likelihood
			// (normalizing constant) is estimated
			double mll = ProbabilityUtil.marginalLogLikelihoodSequence(laps);
			// Save probabilities of each class configuration
			for (int k = 0; k < statesClassVariables.size(); k++) {
				// Obtain class probability (normalized a-posteriori probability)
				double ap = Math.exp(laps[k] - mll);
				prediction.setProbability(statesClassVariables.get(k), ap);
			}
			prediction.setProbabilityPrediction(prediction.getProbabilities().get(predictedCC));
		}
		return prediction;
	}

	/**
	 * Establish the initial structure of a CTBN depending on the global variable
	 * "initialStructure".
	 * 
	 * @param ctbn
	 */
	private void setInitialStructure(CTBN<NodeTypeCTBN> ctbn) {
		logger.info("Initial structure: {}", initialStructure);
		boolean[][] initialAdjMatrix = new boolean[ctbn.getNumNodes()][ctbn.getNumNodes()];
		if (initialStructure.equals("Naive Bayes")) {
			// Obtain names class variables
			List<String> nameCVs = this.dataset.getNameClassVariables();
			// Obtain names features
			List<String> nameFs = this.dataset.getNameFeatures();
			// Get the node indexer of the CTBN
			NodeIndexer<NodeTypeCTBN> nodeIndexer = ctbn.getNodeIndexer();
			// Get the indexes of the class variables and features in the ctbn
			int[] idxCVs = nameCVs.stream().mapToInt(nameCV -> nodeIndexer.getIndexNodeByName(nameCV)).toArray();
			int[] idxFs = nameFs.stream().mapToInt(nameF -> nodeIndexer.getIndexNodeByName(nameF)).toArray();
			// Each class variables is defined as parent of all features
			for (int idxCV : idxCVs)
				for (int idxF : idxFs)
					initialAdjMatrix[idxCV][idxF] = true;
		} else
			// An empty structure will be used
			return;
		ctbn.setStructure(initialAdjMatrix);
	}

	private List<NodeTypeBN> getNodesBN() {
		List<NodeTypeBN> nodesBN = bn.getNodes();
		// Define nodes of the BN as class variables
		for (Node node : nodesBN) {
			node.isClassVariable(true);
		}
		return nodesBN;
	}

	private List<NodeTypeCTBN> getNodesCTBN() {
		return ctbn.getNodes();
	}

}
