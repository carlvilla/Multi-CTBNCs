package com.cig.mctbnc.models;

import java.util.ArrayList;
import java.util.List;

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

	@Override
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
	 * rule.
	 * 
	 * @param sequence
	 * @return array of strings with the predicted states of the class variables
	 */
	public String[] predict(Sequence sequence) {
		// Obtain graphs of the Bayesian network and continuous time Bayesian network
		List<NodeTypeBN> nodesBN = getNodesBN();
		List<NodeTypeCTBN> nodesCTBN = getNodesCTBN();
		// Obtain the name of the class variables
		List<String> nameClassVariables = dataset.getNameClassVariables();
		// Obtain the name of the features
		List<String> nameFeatures = dataset.getNameFeatures();
		// Obtation all possible states of the class variables
		List<State> statesClassVariables = dataset.getStatesVariables(nameClassVariables);

		// Get the observations of the sequence
		List<Observation> observations = sequence.getObservations();

		// Compute the posterior probability for each possible combination of states of
		// the class variables

		double[] posteriorProbability = new double[statesClassVariables.size()];
		for (int i = 0; i < statesClassVariables.size(); i++) {
			// Get states 'i' of the class variables
			State stateClassVariables = statesClassVariables.get(i);
			// Store log-likelihood obtained with the current states of the class variables
			double ll = 0;

			for (int j = 0; j < observations.size(); j++) {
				// Get observation 'j' of the sequence to predict
				Observation observation = observations.get(j);
				String[] valuesFeatures = observation.getValueVariables(nameFeatures);

				// Estimate class probability
				for (NodeTypeBN node : bn.getLearnedNodes()) {
					// Obtain node of BN
					CPTNode nodeBN = (CPTNode) node;
					// Obtain names of the parents of the node
					String[] nameParents = nodeBN.getParents().stream().map(nodeParent -> nodeParent.getName())
							.toArray(String[]::new);
					// Define State object for node and parents having certain values
					State query = new State();
					// Add value of the node
					query.addEvent(nodeBN.getName(), stateClassVariables.getValueNode(nodeBN.getName()));
					// Add values of the parents
					for (String nameParent : nameParents)
						query.addEvent(nameParent, stateClassVariables.getValueNode(nameParent));
					// Obtain probability of the node having certain state given its parents' state
					ll += Math.log(nodeBN.getCPT().get(query));
				}

				// Estimate posterior probability
				// Obtain time difference between the observation 'j' and 'j-1'
				double timeCurrentObservation = observation.getTimeValue();
				double deltaT = j != 0 ? timeCurrentObservation - observations.get(j - 1).getTimeValue()
						: timeCurrentObservation;
				for (NodeTypeCTBN node : ctbn.getNodes()) {
					// Obtain node of CTBN
					CIMNode nodeCTBN = (CIMNode) node;
					// Define State object for node and parents (if any) having certain values
					State fromState = new State();
					// Check if the node is from a feature
					if (!nodeCTBN.isClassVariable()) {
						// Obtain names of the parents of the node
						String[] nameParents = nodeCTBN.getParents().stream().map(nodeParent -> nodeParent.getName())
								.toArray(String[]::new);
						// Add value of the node
						fromState.addEvent(nodeCTBN.getName(), observation.getValueVariable(nodeCTBN.getName()));
						// Add values of the parents
						for (String nameParent : nameParents) {
							// Check if the parent is a class variable or a feature
							if (nameClassVariables.contains(nameParent)) {
								fromState.addEvent(nameParent, stateClassVariables.getValueNode(nameParent));
							} else {
								fromState.addEvent(nameParent, observation.getValueVariable(nameParent));
							}
						}

						// It is possible that the state of the feature was not considered during the
						// training of the model
						double qx = nodeCTBN.getQx().get(fromState) != null ? nodeCTBN.getQx().get(fromState) : 0;
						ll += -qx * deltaT;

					}

					// Get following observation (if there is any) if the the node will transition
					// to another state
					if (i + 1 < observations.size()) {

						String nextState = observations.get(i + 1).getValueVariable(nodeCTBN.getName());

						if (!observation.getValueVariable(nodeCTBN.getName()).equals(nextState)) {

							State toState = new State();
							toState.addEvent(nodeCTBN.getName(), nextState);
							// If there is a transition, obtain its probability

							// The probability of a transition would be zero if either the current state or
							// following state was not considered during the training of the model
							if (nodeCTBN.getOxx().get(fromState) != null
									&& nodeCTBN.getOxx().get(fromState).get(toState) != null) {
								double qxy = nodeCTBN.getOxx().get(fromState).get(toState);
								ll += Math.log(qxy);
							}
						}

					}
				}

			}

			// Store log likelihood obtained when class variables take states 'i'
			posteriorProbability[i] = ll;

		}

		int indexBestStateClassVariables = Util.getIndexLargestValue(posteriorProbability);
		return statesClassVariables.get(indexBestStateClassVariables).getValueNodes(nameClassVariables);
	}

}
