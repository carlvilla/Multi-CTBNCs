package com.cig.mctbnc.models;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.learning.parameters.BNParameterLearning;
import com.cig.mctbnc.learning.parameters.CTBNParameterLearning;
import com.cig.mctbnc.learning.structure.BNStructureLearning;
import com.cig.mctbnc.learning.structure.CTBNCStructureLearning;
import com.cig.mctbnc.nodes.DiscreteNode;
import com.cig.mctbnc.nodes.Node;
import com.cig.mctbnc.view.Main;

/**
 * @author Carlos Villa Blanco <carlos.villa@upm.es>
 *
 *         Implements the Multi-dimensional Continuous Time Bayesian Network
 *         Classifier. 
 * @param <T>
 *            Type of the nodes (discrete or continuous)
 * 
 */
public class MCTBNC<T extends Node> extends AbstractPGM {

	private List<Node> features;
	private List<Node> classVariables;
	static Logger logger = LogManager.getLogger(MCTBNC.class);

	// The subgraph formed by class variables is a Bayesian network
	private BN<T> bn;
	private CTBNC<T> ctbn;

	private CTBNParameterLearning ctbnParameterLearningAlgorithm;
	private CTBNCStructureLearning ctbnStructureLearningAlgorithm;

	public MCTBNC(Dataset trainingDataset, CTBNParameterLearning ctbnParameterLearningAlgorithm,
			CTBNCStructureLearning ctbnStructureLearningAlgorithm, BNParameterLearning bnParameterLearningAlgorithm,
			BNStructureLearning bnStructureLearningAlgorithm) {
		// Define nodes of the MCTBNC
		features = new ArrayList<Node>();
		classVariables = new ArrayList<Node>();

		for (String nameFeature : trainingDataset.getNameFeatures()) {
			int index = trainingDataset.getIndexVariable(nameFeature);
			features.add(new DiscreteNode(index, nameFeature, trainingDataset.getStatesVariable(nameFeature)));
		}

		for (String nameClassVariable : trainingDataset.getNameClassVariables()) {
			int index = trainingDataset.getIndexVariable(nameClassVariable);
			classVariables.add(
					new DiscreteNode(index, nameClassVariable, trainingDataset.getStatesVariable(nameClassVariable)));
		}

		// Define algorithms to estimate the MCTBNC
		this.ctbnParameterLearningAlgorithm = ctbnParameterLearningAlgorithm;
		this.ctbnStructureLearningAlgorithm = ctbnStructureLearningAlgorithm;

		// Class subgraph is defined with a Bayesian network
		bn = new BN<T>(classVariables, trainingDataset, bnParameterLearningAlgorithm, bnStructureLearningAlgorithm);

		// Feature and bridge subgraphs are defined with a continuous time Bayesian
		// network
		List<Node> allVariables = Stream.concat(features.stream(), classVariables.stream())
				.collect(Collectors.toList());
		ctbn = new CTBNC<T>(allVariables);
	}

	public void display() {
		// Graph graph = new SingleGraph("MCTBNC");
		// List<Node> allNodes = Stream.concat(classVariables.stream(),
		// features.stream()).collect(Collectors.toList());
		// addNodes(graph, allNodes);
		// addEdges(graph, allNodes);
		// graph.display();
		bn.display();
	}

	/**
	 * Obtain the node (feature or class variable) with certain index.
	 * 
	 * @param index
	 * @return
	 */
	public Node getNodeByIndex(int index) {
		Node selectedNode = features.stream().filter(node -> node.getIndex() == index).findAny().orElse(null);

		if (selectedNode == null) {
			selectedNode = classVariables.stream().filter(node -> node.getIndex() == index).findAny().orElse(null);
		}

		return selectedNode;
	}
	
	/**
	 * Return the list of nodes for the class variables.
	 * @return
	 */
	public List<Node> getNodesClassVariables(){
		return classVariables;
	}
	
	/**
	 * Return the list of nodes for the features.
	 * @return
	 */
	public List<Node> getNodesFeatures(){
		return features;
	}

	@Override
	public void learn() {
		// Learn structure

		// Learn structure and parameters of class subgraph (Bayesian network)
		logger.info("Defining structure and parameters of the class subgraph (Bayesian network)");
		bn.learn();

		// Learn structure and parameters of feature and bridge subgraph. These are
		// modeled by
		// a continuous time Bayesian network classifier where the restriction that the
		// class
		// variable does not depend on the states of the features is extended to more
		// class variables
		logger.info(
				"Defining structure and parameters of the feature and bridge subgraphs (Continuous time Bayesian network)");
		ctbn.learn();

		// Join class subgraph with feature and bridge subgraphs

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
	public void sample() {
		// TODO Auto-generated method stub

	}

	@Override
	public T[][] predict() {
		// TODO Auto-generated method stub
		return null;
	}

}
