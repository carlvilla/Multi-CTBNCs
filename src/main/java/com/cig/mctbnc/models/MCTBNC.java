package com.cig.mctbnc.models;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;

import com.cig.mctbnc.classification.Classifier;
import com.cig.mctbnc.classification.Prediction;
import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.data.representation.Observation;
import com.cig.mctbnc.data.representation.Sequence;
import com.cig.mctbnc.data.representation.State;
import com.cig.mctbnc.learning.parameters.ParameterLearningAlgorithm;
import com.cig.mctbnc.learning.structure.StructureLearningAlgorithm;
import com.cig.mctbnc.learning.structure.constraints.StructureConstraints;
import com.cig.mctbnc.learning.structure.constraints.BN.DAG;
import com.cig.mctbnc.learning.structure.constraints.CTBNC.CTBNC;
import com.cig.mctbnc.nodes.CIMNode;
import com.cig.mctbnc.nodes.CPTNode;
import com.cig.mctbnc.nodes.Node;
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
	// Algorithms used to learn the MCTBNC
	ParameterLearningAlgorithm ctbnParameterLearningAlgorithm;
	StructureLearningAlgorithm ctbnStructureLearningAlgorithm;
	ParameterLearningAlgorithm bnParameterLearningAlgorithm;
	StructureLearningAlgorithm bnStructureLearningAlgorithm;
	// Penalization function (None by default)
	String penalizationFunction = "No";
	static Logger logger = LogManager.getLogger(MCTBNC.class);

	/**
	 * 
	 * Constructor for MCTBNC.
	 * 
	 * @param dataset                        dataset used to learn the MCTBNC
	 * @param ctbnParameterLearningAlgorithm algorithm used to learn the parameters
	 *                                       of a CTBN
	 * @param ctbnStructureLearningAlgorithm algorithm used to learn the structure
	 *                                       of a CTBN
	 * @param bnParameterLearningAlgorithm   algorithm used to learn the parameters
	 *                                       of a BN
	 * @param bnStructureLearningAlgorithm   algorithm used to learn the structure
	 *                                       of a BN
	 * @param bnNodeClass                    type of the BN nodes
	 * @param ctbnNodeClass                  type of the CTBN nodes
	 */
	public MCTBNC(ParameterLearningAlgorithm ctbnParameterLearningAlgorithm,
			StructureLearningAlgorithm ctbnStructureLearningAlgorithm,
			ParameterLearningAlgorithm bnParameterLearningAlgorithm,
			StructureLearningAlgorithm bnStructureLearningAlgorithm, Class<NodeTypeBN> bnNodeClass,
			Class<NodeTypeCTBN> ctbnNodeClass) {
		// Save the class type of the nodes
		this.bnNodeClass = bnNodeClass;
		this.ctbnNodeClass = ctbnNodeClass;
		// Save algorithms used for the learning of the MCTBNC
		this.ctbnParameterLearningAlgorithm = ctbnParameterLearningAlgorithm;
		this.ctbnStructureLearningAlgorithm = ctbnStructureLearningAlgorithm;
		this.bnParameterLearningAlgorithm = bnParameterLearningAlgorithm;
		this.bnStructureLearningAlgorithm = bnStructureLearningAlgorithm;
	}

	@Override
	public void learn(Dataset dataset) {
		logger.info("Learning MCTBNC model");
		// Remove previous instantiation of the model (if any)
		removeAllNodes();
		// Save dataset used for training
		this.dataset = dataset;
		// Measure execution time
		Instant start = Instant.now();
		// ------------------ Class subgraph ------------------
		// Learn structure and parameters of class subgraph (Bayesian network)
		logger.info("Defining structure and parameters of the class subgraph (Bayesian network)");
		bn = new BN<NodeTypeBN>(dataset.getNameClassVariables(), bnParameterLearningAlgorithm,
				bnStructureLearningAlgorithm, getStructureConstraintsBN(), bnNodeClass);
		bn.setPenalizationFunction(penalizationFunction);
		bn.learn(dataset);
		logger.info("Class subgraph established!");
		// ----------- Feature and bridge subgraphs -----------
		// Learn structure and parameters of feature and bridge subgraph. These are
		// modeled by a continuous time Bayesian network classifier where the
		// restriction that the class variable does not depend on the states of the
		// features is extended to more class variables
		logger.info("Defining structure and parameters of the feature and bridge subgraphs (Continuous time "
				+ "Bayesian network)");
		ctbn = new CTBN<NodeTypeCTBN>(dataset.getNameVariables(), ctbnParameterLearningAlgorithm,
				ctbnStructureLearningAlgorithm, getStructureConstraintsCTBN(), ctbnNodeClass);
		ctbn.setPenalizationFunction(penalizationFunction);
		ctbn.learn(dataset);
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
	 * 
	 * @param nodesBN
	 * @param nodesCTBN
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

	/**
	 * Establish the penalization function used for the structure complexity of the
	 * BNs and CTBNs. By default it is not applied any penalization.
	 * 
	 * @param penalizationFunction name of the penalization function
	 */
	public void setPenalizationFunction(String penalizationFunction) {
		logger.info("Penalizing model structure with {} penalization function", penalizationFunction);
		this.penalizationFunction = penalizationFunction;
	}

	/**
	 * Perform classification over the sequences of a dataset according to the
	 * maximum a posteriori probability. Classes that obtain the highest posterior
	 * probability given each sequence are predicted.
	 * 
	 * @param dataset               dataset from which the sequences to predict are
	 *                              extracted
	 * @param estimateProbabilities determines if the probabilities of the classes
	 *                              are estimated
	 * @return array of Prediction object (one per sequence) that contain the
	 *         predicted classes and, if requested, the probabilities of all
	 *         possible classes
	 */
	public Prediction[] predict(Dataset dataset, boolean estimateProbabilities) {
		logger.info("Performing prediction over {} sequences", dataset.getNumDataPoints());
		int numSequences = dataset.getNumDataPoints();
		Prediction[] predictions = new Prediction[numSequences];
		// Make predictions on all the sequences
		for (int i = 0; i < numSequences; i++) {
			logger.trace("Performing prediction over sequence {}/{}", i, dataset.getNumDataPoints());
			Sequence evidenceSequence = dataset.getSequences().get(i);
			predictions[i] = predict(evidenceSequence, estimateProbabilities);
		}
		return predictions;
	}

	/**
	 * Perform classification over a sequence according to the maximum a posteriori
	 * probability. Classes that obtain the highest posterior probability given the
	 * sequence are predicted.
	 * 
	 * @param sequence              sequence whose class variables are predicted
	 * @param estimateProbabilities determines if the probabilities of the classes
	 *                              are estimated
	 * @return Prediction object that contains the predicted classes and, if
	 *         requested, the probabilities of all possible classes
	 */
	public Prediction predict(Sequence sequence, boolean estimateProbabilities) {
		// Obtain the name of the class variables
		List<String> nameClassVariables = dataset.getNameClassVariables();
		// Obtation all possible states of the class variables
		List<State> statesClassVariables = dataset.getStatesVariables(nameClassVariables);
		// Obtain nodes of the Bayesian network and continuous time Bayesian network
		List<NodeTypeBN> nodesBN = getNodesBN();
		List<NodeTypeCTBN> nodesCTBN = getNodesCTBN();
		// Estimate the log-a-posteriori probabilities (without normalizing constant)
		// of each combination of classes
		double[] laps = new double[statesClassVariables.size()];
		for (int i = 0; i < statesClassVariables.size(); i++) {
			// Get state/classes 'i' of the class variables
			State stateClassVariables = statesClassVariables.get(i);
			// Compute the log-a-posteriori probability of current classes 'i'
			double lap = 0;
			// Estimate the log-prior probability of the classes 'i'
			lap += logPriorProbabilityClassVariables(nodesBN, stateClassVariables);
			// Estimate log-likelihood of the sequence given the classes 'i'
			lap += logLikelihood(sequence, nodesCTBN, stateClassVariables);
			// Store log-a-posteriori probability of class variables taking classes 'i'
			laps[i] = lap;
		}
		// Define a Prediction object to save the result
		Prediction prediction = new Prediction();
		// Retrieve class configuration which obtain largest posterior probability
		int indexBestStateClassVariables = Util.getIndexLargestValue(laps);
		State predictedClasses = statesClassVariables.get(indexBestStateClassVariables);
		// Save the predicted classes
		prediction.setPredictedClasses(predictedClasses);
		// If requested, compute and save the a posteriori probabilities of the classes
		if (estimateProbabilities) {
			// Estimate the log-prior probability of the sequence (normalizing constant)
			double priorSequence = logPriorProbabilitySequence(sequence, nodesBN, nodesCTBN, statesClassVariables);
			// Normalize log-a-posteriori probabilities
			for (int i = 0; i < statesClassVariables.size(); i++) {
				// Normalize log-a-posteriori
				laps[i] = laps[i] - priorSequence;
				// Save a posteriori probability
				prediction.setProbability(statesClassVariables.get(i), Math.exp(laps[i]));
			}
			// Save a posteriori probability of prediction
			double ap = Math.exp(laps[indexBestStateClassVariables]);
			prediction.setProbabilityPrediction(ap);
		}
		return prediction;
	}

	/**
	 * Compute the logarithm of the prior probability of the class variables taking
	 * certain values. Their probability is computed by using the Bayesian network.
	 * 
	 * @param nodesBN
	 * @param stateClassVariables
	 * @return logarithm of the prior probability of the class variables
	 */
	private double logPriorProbabilityClassVariables(List<NodeTypeBN> nodesBN, State stateClassVariables) {
		double priorProbability = 0.0;
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
			priorProbability += Ox != null && Ox > 0 ? Math.log(Ox) : 0;
		}
		return priorProbability;
	}

	/**
	 * Compute the logarithm of the prior probability of a sequence.
	 * 
	 * @return logarithm of the prior probability of a sequence
	 */
	public double logPriorProbabilitySequence(Sequence sequence, List<NodeTypeBN> nodesBN, List<NodeTypeCTBN> nodesCTBN,
			List<State> statesClassVariables) {
		double lppSequence = 0.0;
		for (int i = 0; i < statesClassVariables.size(); i++) {
			State stateClassVariable = statesClassVariables.get(i);
			double lppClass = logPriorProbabilityClassVariables(nodesBN, stateClassVariable);
			double llSequence = logLikelihood(sequence, nodesCTBN, stateClassVariable);
			lppSequence += Math.exp(lppClass) * Math.exp(llSequence);
		}
		return Math.log(lppSequence);
	}

	/**
	 * Compute the logarithm of the likelihood of a sequence, also known as temporal
	 * likelihood (Stella and Amer 2012), given the state of the class variables.
	 * This is done by using the CTBN.
	 * 
	 * @param sequence
	 * @param nodesCTBN
	 * @param stateClassVariables
	 * @return
	 */
	private double logLikelihood(Sequence sequence, List<NodeTypeCTBN> nodesCTBN, State stateClassVariables) {
		// Names of the class variables
		List<String> nameClassVariables = stateClassVariables.getNameVariables();
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
						if (nameClassVariables.contains(nameParent)) {
							fromState.addEvent(nameParent, stateClassVariables.getValueVariable(nameParent));
						} else {
							fromState.addEvent(nameParent, currentObservation.getValueVariable(nameParent));
						}
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
						Map<State, Double> Oxx = nodeCTBN.getOxx().get(fromState);
						if (Oxx != null) {
							Double oxy = Oxx.get(toState);
							double qxy = 0;
							if (oxy != null)
								// Instantaneous probability of feature moving from "fromState" to "toState"
								qxy = oxy * qx;
							// Small positive number (Stella and Amer 2012)
							// double e = 0.000001;
							// ll += qxy > 0 ? Math.log(1 - Math.exp(-(qxy) * e)) : 0;
							ll += qxy > 0 ? Math.log(qxy) : 0;
						}
					} else {
						ll += Math.log(1);
					}
				}
			}
		}
		return ll;
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

	@Override
	public String getType() {
		return "Multidimensional Continuous time Bayesian network";
	}

	public void display() {
		Graph graph = new SingleGraph("MCTBNC");
		addNodes(graph, nodes);
		addEdges(graph, nodes);
		graph.display();
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
