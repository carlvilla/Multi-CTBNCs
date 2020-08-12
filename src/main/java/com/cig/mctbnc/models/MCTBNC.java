package com.cig.mctbnc.models;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;

import com.cig.mctbnc.classification.Classifier;
import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.data.representation.Observation;
import com.cig.mctbnc.data.representation.Sequence;
import com.cig.mctbnc.data.representation.State;
import com.cig.mctbnc.learning.parameters.ParameterLearningAlgorithm;
import com.cig.mctbnc.learning.structure.StructureLearningAlgorithm;
import com.cig.mctbnc.learning.structure.constraints.MCTBNC.StructureConstraintsMCTBNC;
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
	public MCTBNC(Dataset dataset, ParameterLearningAlgorithm ctbnParameterLearningAlgorithm,
			StructureLearningAlgorithm ctbnStructureLearningAlgorithm,
			ParameterLearningAlgorithm bnParameterLearningAlgorithm,
			StructureLearningAlgorithm bnStructureLearningAlgorithm, Class<NodeTypeBN> bnNodeClass,
			Class<NodeTypeCTBN> ctbnNodeClass, StructureConstraintsMCTBNC structureConstraintsMCTBNC) {
		// Set dataset
		this.dataset = dataset;

		// Define class subgraph with a Bayesian network
		bn = new BN<NodeTypeBN>(dataset, dataset.getNameClassVariables(), bnParameterLearningAlgorithm,
				bnStructureLearningAlgorithm, structureConstraintsMCTBNC.getStructureConstraintsBN(), bnNodeClass);

		// Define feature and bridge subgraphs with a continuous time Bayesian network
		ctbn = new CTBN<NodeTypeCTBN>(dataset, dataset.getNameVariables(), ctbnParameterLearningAlgorithm,
				ctbnStructureLearningAlgorithm, structureConstraintsMCTBNC.getStructureConstraintsCTBN(),
				ctbnNodeClass);
	}

	public void display() {
		Graph graph = new SingleGraph("MCTBNC");
		addNodes(graph, nodes);
		addEdges(graph, nodes);
		graph.display();
	}

	@Override
	public void learn() {
		logger.info("Learning MCTBNC model");
		// Measure execution time
		Instant start = Instant.now();
		// Learn structure and parameters of class subgraph (Bayesian network)
		logger.info("Defining structure and parameters of the class subgraph (Bayesian network)");
		bn.learn();
		logger.info("Class subgraph established!");
		// Learn structure and parameters of feature and bridge subgraph. These are
		// modeled by a continuous time Bayesian network classifier where the
		// restriction that the class variable does not depend on the states of the
		// features is extended to more class variables
		logger.info("Defining structure and parameters of the feature and bridge subgraphs (Continuous time "
				+ "Bayesian network)");
		ctbn.learn();
		logger.info("Feature and bridge subgraphs established!");
		// Join class subgraph with feature and bridge subgraphs
		// Get nodes BN (class variables). Define them as class variables
		List<NodeTypeBN> nodesBN = getNodesBN();
		List<NodeTypeCTBN> nodesCTBN = getNodesCTBN();
		setStructure(nodesBN, nodesCTBN);
		Instant end = Instant.now();
		logger.info("MCTBNC model learnt in {} seconds", Duration.between(start, end));
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

	/**
	 * Defines the nodes of the MCTBNC by using the nodes obtained from the BN and
	 * the CTBN.
	 * 
	 * @param nodesBN
	 * @param nodesCTBN
	 */
	private void setStructure(List<NodeTypeBN> nodesBN, List<NodeTypeCTBN> nodesCTBN) {
		List<Node> nodes = new ArrayList<Node>();
		// It is important to respect the original order of the variables to generate a
		// correct adjacency matrix
		for (String nameVariable : dataset.getNameVariables()) {
			// It is obtained the node of each variable from the BN (if it exists) and from
			// the CTBN
			Node nodeInBN = nodesBN.stream().filter(nodeBN -> nodeBN.getName().equals(nameVariable)).findFirst()
					.orElse(null);
			Node nodeInCTBN = nodesCTBN.stream().filter(nodeCTBN -> nodeCTBN.getName().equals(nameVariable)).findFirst()
					.orElse(null);
			if (nodeInBN != null) {
				// It is a class variable node. The children obtained by the CTBN are added to
				// the node of the BN
				for (Node child : nodeInCTBN.getChildren())
					nodeInBN.setChild(child);
				nodes.add(nodeInBN);
			} else {
				// It is a feature node
				nodes.add(nodeInCTBN);
			}
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

	@Override
	public String getType() {
		return "Multidimensional Continuous time Bayesian network";
	}

	@Override
	public boolean isStructureLegal(boolean[][] adjacencyMatrix) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Performs classification over the sequences in a dataset according to the
	 * maximum aposteriori rule, i.e., the class variable values which obtain the
	 * largest log-likelihood are returned.
	 * 
	 * @param dataset dataset from which the sequences to predict are extracted
	 * @return bidimensional string array with the predictions of the class
	 *         variables for all the sequences
	 */
	public String[][] predict(Dataset dataset) {
		logger.info("Performing prediction over {} sequences", dataset.getNumDataPoints());
		int numSequences = dataset.getNumDataPoints();
		int numClassVariables = this.dataset.getNumClassVariables();
		String[][] predictions = new String[numSequences][numClassVariables];
		// Make predictions on all the sequences
		for (int i = 0; i < numSequences; i++) {
			logger.trace("Performing prediction over sequence {}/{}", i, dataset.getNumDataPoints());
			Sequence evidenceSequence = dataset.getSequences().get(i);
			predictions[i] = predict(evidenceSequence);
		}
		return predictions;
	}

	/**
	 * Performs classification over a sequence according to the maximum aposteriori
	 * rule, i.e., the class variable values which obtain the largest log-likelihood
	 * are returned.
	 * 
	 * @param sequence sequence whose class variables are predicted
	 * @return array of strings with the predicted states of the class variables
	 */
	public String[] predict(Sequence sequence) {
		// Obtain the name of the class variables
		List<String> nameClassVariables = dataset.getNameClassVariables();
		// Obtation all possible states of the class variables
		List<State> statesClassVariables = dataset.getStatesVariables(nameClassVariables);
		// Obtain graphs of the Bayesian network and continuous time Bayesian network
		List<NodeTypeBN> nodesBN = getNodesBN();
		List<NodeTypeCTBN> nodesCTBN = getNodesCTBN();
		// Get observations of the sequence
		List<Observation> observations = sequence.getObservations();
		// Posterior probability of each state combination of the class variables
		double[] posteriorProbability = new double[statesClassVariables.size()];
		for (int i = 0; i < statesClassVariables.size(); i++) {
			// Get states 'i' of the class variables
			State stateClassVariables = statesClassVariables.get(i);
			// Compute log-likelihood with current states of the class variables
			double ll = 0;
			for (int j = 0; j < observations.size(); j++) {
				// Get observation 'j' of the sequence to predict
				Observation observation = observations.get(j);

				// Estimate class probability
				for (NodeTypeBN node : nodesBN) {
					// Obtain class variable node from the BN
					CPTNode nodeBN = (CPTNode) node;
					// Obtain parents of the node
					List<Node> parentNodes = nodeBN.getParents();
					// Define State object for node and parents having certain values
					State query = new State();
					// Add value of the class variable
					query.addEvent(nodeBN.getName(), stateClassVariables.getValueNode(nodeBN.getName()));
					// Add values of the parents
					for (Node parentNode : parentNodes) {
						String nameParent = parentNode.getName();
						query.addEvent(nameParent, stateClassVariables.getValueNode(nameParent));
					}
					// Probability of the class variable and its parents having a certain state
					Double Ox = nodeBN.getCPT().get(query);
					ll += Ox != null && Ox > 0 ? Math.log(nodeBN.getCPT().get(query)) : 0;
				}

				// Estimate posterior probability
				// Obtain time difference between observation 'j' and 'j-1'
				double timeObservation = observation.getTimeValue();
				double deltaTime = j != 0 ? timeObservation - observations.get(j - 1).getTimeValue() : timeObservation;
				for (NodeTypeCTBN node : nodesCTBN) {
					// Obtain node of CTBN
					CIMNode nodeCTBN = (CIMNode) node;
					// Check that the node is from a feature
					if (!nodeCTBN.isClassVariable()) {
						// Obtain parents of the feature node
						List<Node> parentNodes = nodeCTBN.getParents();
						// Define State object for the node and parents (if any) having certain state
						State fromState = new State();
						// Add value of the feature to the State object
						fromState.addEvent(nodeCTBN.getName(), observation.getValueVariable(nodeCTBN.getName()));
						// Add values of the parents to the State object
						for (Node parentNode : parentNodes) {
							String nameParent = parentNode.getName();
							// Check if the parent is a class variable or a feature to retrieve its state
							if (nameClassVariables.contains(nameParent)) {
								fromState.addEvent(nameParent, stateClassVariables.getValueNode(nameParent));
							} else {
								fromState.addEvent(nameParent, observation.getValueVariable(nameParent));
							}
						}
						// Obtain the instantaneous probability of the feature leaving its current
						// state while its parents are in a certain state. NOTE: It is possible that
						// some states of the features were not considered during model training
						Double qx = nodeCTBN.getQx().get(fromState);
						qx = qx != null ? qx : 0;
						// Probability of the feature staying in a certain state (while its parent have
						// a particular state) for an amount of time 'deltaTime' is exponentially
						// distributed with parameter 'qx'. The log-likelihood is updated
						ll += -qx * deltaTime;

						// Get following observation (if any) and if the feature transition to another
						// state, get the probability that this occurs given the state of the parents
						if (i + 1 < observations.size()) {
							// Get value of the feature in the following observation
							String nextValue = observations.get(i + 1).getValueVariable(nodeCTBN.getName());
							// Check if the variable changed its state
							if (!observation.getValueVariable(nodeCTBN.getName()).equals(nextValue)) {
								// Define State object with next feature value to get the correct parameter
								State toState = new State();
								toState.addEvent(nodeCTBN.getName(), nextValue);
								// Probability that the feature transitions from one state to another while its
								// parents have a certain state. NOTE: the probability would be zero if either
								// the departure or arrival state was not considered during model training
								Map<State, Double> Oxx = nodeCTBN.getOxx().get(fromState);
								if (Oxx != null) {
									// Obtain the probability and update the log-likelihood
									Double qxy = Oxx.get(toState);
									ll += qxy != null && qxy > 0 ? Math.log(qxy) : 0;
								}
							}
						}
					}
				}
			}
			// Store log likelihood obtained when class variables take states 'i'
			posteriorProbability[i] = ll;
		}
		// Retrieve states of class variables which obtain the largest log-likelihood
		int indexBestStateClassVariables = Util.getIndexLargestValue(posteriorProbability);
		return statesClassVariables.get(indexBestStateClassVariables).getValueNodes(nameClassVariables);
	}

}
