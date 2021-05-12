package es.upm.fi.cig.mctbnc.models;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.upm.fi.cig.mctbnc.classification.Classifier;
import es.upm.fi.cig.mctbnc.classification.Prediction;
import es.upm.fi.cig.mctbnc.data.representation.Dataset;
import es.upm.fi.cig.mctbnc.data.representation.Sequence;
import es.upm.fi.cig.mctbnc.data.representation.State;
import es.upm.fi.cig.mctbnc.exceptions.ErroneousSequenceException;
import es.upm.fi.cig.mctbnc.learning.BNLearningAlgorithms;
import es.upm.fi.cig.mctbnc.learning.CTBNLearningAlgorithms;
import es.upm.fi.cig.mctbnc.learning.structure.constraints.StructureConstraints;
import es.upm.fi.cig.mctbnc.learning.structure.constraints.BN.DAG;
import es.upm.fi.cig.mctbnc.learning.structure.constraints.CTBNC.CTBNC;
import es.upm.fi.cig.mctbnc.nodes.CIMNode;
import es.upm.fi.cig.mctbnc.nodes.CPTNode;
import es.upm.fi.cig.mctbnc.nodes.DiscreteNode;
import es.upm.fi.cig.mctbnc.nodes.Node;
import es.upm.fi.cig.mctbnc.nodes.NodeIndexer;
import es.upm.fi.cig.mctbnc.util.ProbabilityUtil;
import es.upm.fi.cig.mctbnc.util.Util;

/**
 * 
 * Implements the multi-dimensional continuous time Bayesian network classifier
 * (MCTBNC).
 * 
 * @author Carlos Villa Blanco
 * 
 * @param <NodeTypeBN>   type of the nodes of the BN (class subgraph)
 * @param <NodeTypeCTBN> type of the nodes of the CTBN (feature subgraph)
 */
public class MCTBNC<NodeTypeBN extends Node, NodeTypeCTBN extends Node> extends AbstractPGM<Node>
		implements Classifier {
	// The subgraph formed by class variables is a Bayesian network
	private BN<NodeTypeBN> bn;
	private CTBN<NodeTypeCTBN> ctbn;
	// Classes of the nodes
	private Class<NodeTypeBN> bnNodeClass;
	private Class<NodeTypeCTBN> ctbnNodeClass;
	// Algorithms used to learn the class subgraph (BN)
	private BNLearningAlgorithms bnLearningAlgs;
	// Algorithms used to learn the feature and bridge subgraphs (CTBN)
	private CTBNLearningAlgorithms ctbnLearningAlgs;
	// Initial structure (Empty by default)
	private String initialStructure = "Empty";
	private static Logger logger = LogManager.getLogger(MCTBNC.class);

	/**
	 * Receives learning algorithms for BNs and CTBNs to generate a MCTBNC.
	 * 
	 * @param bnLearningAlgs   parameter and structure learning algorithms for
	 *                         Bayesian networks
	 * @param ctbnLearningAlgs parameter and structure learning algorithms for
	 *                         continuous time Bayesian networks
	 * @param bnNodeClass      Bayesian network node type
	 * @param ctbnNodeClass    continuous time Bayesian network node type
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

	/**
	 * Receives a Bayesian network and a continuous time Bayesian network that
	 * represent the class subgraph and feature/bridge subgraph of a MCTBNC,
	 * respectively.
	 * 
	 * @param bn   Bayesian network
	 * @param ctbn continuous time Bayesian network
	 */
	public MCTBNC(BN<NodeTypeBN> bn, CTBN<NodeTypeCTBN> ctbn) {
		this.bn = bn;
		this.ctbn = ctbn;
	}

	@Override
	public void learn(Dataset dataset) {
		logger.info("Learning model with {} training samples", dataset.getNumDataPoints());
		// Remove previous instantiation of the model (if any)
		removeAllNodes();
		// Save dataset used for training
		this.dataset = dataset;
		// Extract name variables
		List<String> nameClassVariables = dataset.getNameClassVariables();
		List<String> nameVariables = dataset.getNameVariables();
		// Measure execution time
		Instant start = Instant.now();
		// ------------------ Class subgraph ------------------
		// Learn structure and parameters of class subgraph (Bayesian network)
		logger.info("Defining structure and parameters of the class subgraph (Bayesian network)");
		this.bn = new BN<NodeTypeBN>(dataset, nameClassVariables, this.bnLearningAlgs, getStructureConstraintsBN(),
				this.bnNodeClass);
		this.bn.learn();
		logger.info("Class subgraph established!");
		// ----------- Feature and bridge subgraphs -----------
		// Learn structure and parameters of feature and bridge subgraph. These are
		// modeled by a continuous time Bayesian network classifier where the
		// restriction that the class variable does not depend on the states of the
		// features is extended to more class variables
		logger.info("Defining structure and parameters of the feature and bridge subgraphs (Continuous time "
				+ "Bayesian network)");
		this.ctbn = new CTBN<NodeTypeCTBN>(dataset, nameVariables, this.ctbnLearningAlgs, getStructureConstraintsCTBN(),
				this.bn, this.ctbnNodeClass);
		setInitialStructure(this.ctbn);
		this.ctbn.learn();
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
		for (String nameVariable : this.dataset.getNameVariables()) {
			// Each variable node is obtained from the BN (if it exists) and from the CTBN
			Node nodeInBN = this.bn.getNodeByName(nameVariable);
			Node nodeInCTBN = this.ctbn.getNodeByName(nameVariable);
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
				// Set node as class variable
				nodeInBN.isClassVariable(true);
				nodes.add(nodeInBN);
			} else
				// It is a feature node
				nodes.add(nodeInCTBN);
		}
		// The nodes are added to the MCTBNC
		addNodes(nodes);
	}

	/**
	 * Establishes the approach that will be used to define the initial structure of
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

	/**
	 * Samples a sequence given its duration.
	 * 
	 * @param duration duration of the sequence
	 * @return sampled sequence
	 */
	public Sequence sample(int duration) {
		// Forward sampling Bayesian network (class subgraph). Sample the state
		// of the class variables
		State sampledCVs = sampleClassVariables();
		// Sample a sequence given the state of the class variables
		Sequence sequence = sampleSequence(sampledCVs, duration);
		return sequence;
	}

	/**
	 * Returns learning algorithms for class subgraph (Bayesian network).
	 * 
	 * @return learning algorithms for class subgraph (Bayesian network)
	 */
	public BNLearningAlgorithms getLearningAlgsBN() {
		return this.bnLearningAlgs;
	}

	/**
	 * Returns learning algorithms for bridge and features subgraphs (continuous
	 * time Bayesian network).
	 * 
	 * @return learning algorithms for bridge and features subgraphs (continuous
	 *         time Bayesian network)
	 */
	public CTBNLearningAlgorithms getLearningAlgsCTBN() {
		return this.ctbnLearningAlgs;
	}

	/**
	 * Returns the type of the class variable nodes.
	 * 
	 * @return type of the class variable nodes
	 */
	public Class<NodeTypeBN> getTypeNodeClassVariable() {
		return this.bnNodeClass;
	}

	/**
	 * Returns the type of the feature nodes.
	 * 
	 * @return type of the feature nodes
	 */
	public Class<NodeTypeCTBN> getTypeNodeFeature() {
		return this.ctbnNodeClass;
	}

	/**
	 * Returns the structure constraints for the BN.
	 * 
	 * @return a {@code StructureConstraint}
	 */
	public StructureConstraints getStructureConstraintsBN() {
		return new DAG();
	}

	/**
	 * Returns the structure constraints for the CTBN.
	 * 
	 * @return a {@code StructureConstraint}
	 */
	public StructureConstraints getStructureConstraintsCTBN() {
		return new CTBNC();
	}

	@Override
	public String getType() {
		if (getNodesBN().size() == 1)
			return "Continuous time Bayesian network classifier";
		return "Multidimensional continuous time Bayesian network classifier";
	}

	@Override
	public String getModelIdentifier() {
		return "MCTBNC";
	}

	/**
	 * Performs classification over the sequences of a dataset according to the
	 * maximum a posteriori probability, i.e., classes that obtain the highest
	 * posterior probability given each sequence are predicted.
	 * 
	 * @param dataset               dataset from which the sequences to predict are
	 *                              extracted
	 * @param estimateProbabilities true to estimate the probabilities of the class
	 *                              configurations, false otherwise
	 * @return array of Prediction object (one per sequence) that contain the
	 *         predicted classes and, if requested, the probabilities of all
	 *         possible class configurations
	 */
	@Override
	public Prediction[] predict(Dataset dataset, boolean estimateProbabilities) {
		logger.info("Performing prediction over {} sequences", dataset.getNumDataPoints());
		// Measure execution time
		Instant start = Instant.now();
		int numSequences = dataset.getNumDataPoints();
		Prediction[] predictions = new Prediction[numSequences];
		// Obtain class configurations
		List<State> statesCVs = getClassConfigurations(this.bn.getNodes());
		// Make predictions on all the sequences
		for (int i = 0; i < numSequences; i++) {
			logger.trace("Performing prediction over sequence {}/{}", i, dataset.getNumDataPoints());
			Sequence evidenceSequence = dataset.getSequences().get(i);
			predictions[i] = predict(evidenceSequence, statesCVs, estimateProbabilities);
		}
		Instant end = Instant.now();
		logger.info("Sequences predicted in {}", Duration.between(start, end));
		return predictions;
	}

	@Override
	public boolean areParametersEstimated() {
		return this.bn.areParametersEstimated() && this.ctbn.areParametersEstimated();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("--Structure multi-dimensional continuous time Bayesian network classifier--\n");
		for (Node node : this.nodes) {
			if (node.getParents().isEmpty())
				sb.append("{}");
			else
				for (Node parent : node.getParents())
					sb.append("(" + parent.getName() + ")");
			sb.append(" => (" + node.getName() + ") \n");
		}
		return sb.toString();
	}

	/**
	 * Samples the state of the class variables.
	 */
	private State sampleClassVariables() {
		// Define topological order
		List<Node> classVariables = this.bn.getTopologicalOrdering();
		// Store the sampled state of the nodes
		State sampledState = new State();
		for (int i = 0; i < classVariables.size(); i++) {
			// Extract CPT node
			CPTNode cptNode = (CPTNode) classVariables.get(i);
			// Sample state of the node given the states of the currently sampled nodes
			State state = cptNode.sampleState();
			// Save sampled state of the node
			sampledState.addEvents(state.getEvents());
		}
		return sampledState;
	}

	/**
	 * Samples the transitions of a sequence given its duration and the state of the
	 * class variables.
	 * 
	 * @param sampledCVs
	 */
	@SuppressWarnings("unchecked")
	private Sequence sampleSequence(State sampledCVs, int duration) {
		// Features whose transitions will be sampled
		LinkedList<CIMNode> features = new LinkedList<CIMNode>((List<CIMNode>) this.ctbn.getNodes());
		// List with the time when observations occur
		List<Double> timestamps = new ArrayList<Double>();
		// List of states with the observations (which form transitions) of the sequence
		List<State> observations = new ArrayList<State>();
		// Sample initial observation of the sequence
		State currentObservation = sampleInitialStateSequence(sampledCVs);
		// Add the initial observation and time when it occurred to the results lists
		observations.add(currentObservation);
		double currentTime = 0.0;
		timestamps.add(currentTime);
		// Keep the times when each of the nodes will change their state
		TreeMap<Double, CIMNode> transitionTimes = new TreeMap<Double, CIMNode>();
		// Generate transitions until the time of the current observation surpasses the
		// duration of the sequence
		while (currentTime < duration) {
			// Create an object that contains the previous observations
			State previousObservation = new State(currentObservation.getEvents());
			// Obtain the time when each of the features change their states. It is only
			// considered those features whose next transition time is unknown
			while (!features.isEmpty()) {
				// Get first feature of the list
				CIMNode featureToSample = features.pollFirst();
				Util.setStateNodeAndParents(featureToSample, previousObservation);
				// Get the time that the variable will stay in its current state
				double sampledTime = featureToSample.sampleTimeState();
				// Add the times to the list ordered from lowest to highest
				transitionTimes.put(sampledTime, featureToSample);
			}
			// Get Entry with the next node whose state will change and the time this occurs
			Entry<Double, CIMNode> nextTransition = transitionTimes.pollFirstEntry();
			// Update current time
			currentTime += nextTransition.getKey();
			// Get the next node whose state will change
			CIMNode changingNode = nextTransition.getValue();
			Util.setStateNodeAndParents(changingNode, previousObservation);
			// Sample the next state of the node
			State nextState = changingNode.sampleNextState();
			// Get current observation using the previous one and the new state of the node
			currentObservation = new State(previousObservation.getEvents());
			currentObservation.modifyEventValue(changingNode.getName(), nextState.getValues()[0]);
			// Save the observation
			observations.add(currentObservation);
			// Save the time when the observation (transition) occurred
			timestamps.add(currentTime);
			// Add the feature to the list to sample the time its next transition will occur
			features.add(changingNode);
		}
		// Create sequence with the sampled transitions and state of the class variables
		Sequence sequence = null;
		try {
			sequence = new Sequence(sampledCVs, observations, "t", timestamps);
		} catch (ErroneousSequenceException e) {
			logger.warn(e.getMessage());
		}
		return sequence;
	}

	/**
	 * Samples the initial state of the sequence (first observation). Although,
	 * theoretically, it should be sampled from a multi-dimensional Bayesian network
	 * classifier, a uniform distribution is used for simplicity.
	 * 
	 * @param sampledCVs
	 * @return State object with the initial observation of a sequence
	 */
	private State sampleInitialStateSequence(State sampledCVs) {
		State evidence = new State();
		for (int i = 0; i < this.ctbn.getNumNodes(); i++) {
			CIMNode node = (CIMNode) this.ctbn.getNodes().get(i);
			// Sample state
			int idxSampledState = (int) (Math.random() * node.getNumStates());
			// Set state feature node
			node.setState(idxSampledState);
			// Add state of the feature node
			evidence.addEvent(node.getName(), node.getState());
		}
		// Add the states of the class variables to the first observation
		evidence.addEvents(sampledCVs.getEvents());
		// Add the initial observation and the time when it occurred
		State initialObservation = new State(evidence.getEvents());
		return initialObservation;
	}

	/**
	 * Returns State objects with all class configurations.
	 * 
	 * @param nodesCVs
	 * @return states with all class configurations
	 */
	private List<State> getClassConfigurations(List<NodeTypeBN> nodesCVs) {
		List<List<State>> statesEachCV = new ArrayList<List<State>>();
		for (Node nodeCV : nodesCVs) {
			List<State> statesNode = new ArrayList<State>();
			for (String valueState : ((DiscreteNode) nodeCV).getStates())
				statesNode.add(new State(Map.of(nodeCV.getName(), valueState)));
			statesEachCV.add(statesNode);
		}
		return Util.cartesianProduct(statesEachCV);
	}

	/**
	 * Performs classification over a sequence according to the maximum a posteriori
	 * probability.
	 * 
	 * @param sequence             sequence whose class variables are predicted
	 * @param statesClassVariables possible class configurations
	 * @return Prediction object that contains the predicted classes and, if
	 *         requested, the probabilities of all possible class configurations
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
		// The state is clone to avoid that different predictions use the same objects
		State predictedCC = new State(statesClassVariables.get(idxBestCC));
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
	 * Establishes the initial structure of a CTBN depending on the global variable
	 * "initialStructure".
	 * 
	 * @param ctbn continuous time Bayesian network whose initial structure is
	 *             defined
	 */
	private void setInitialStructure(CTBN<NodeTypeCTBN> ctbn) {
		logger.info("Initial structure: {}", this.initialStructure);
		boolean[][] initialAdjMatrix = new boolean[ctbn.getNumNodes()][ctbn.getNumNodes()];
		if (this.initialStructure.equals("Naive Bayes")) {
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

	protected List<NodeTypeBN> getNodesBN() {
		List<NodeTypeBN> nodesBN = this.bn.getNodes();
		// Define nodes of the BN as class variables
		for (Node node : nodesBN)
			node.isClassVariable(true);
		return nodesBN;
	}

	private List<NodeTypeCTBN> getNodesCTBN() {
		return this.ctbn.getNodes();
	}

}
