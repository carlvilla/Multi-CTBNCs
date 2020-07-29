package com.cig.mctbnc.models;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.learning.parameters.ParameterLearningAlgorithm;
import com.cig.mctbnc.learning.structure.StructureLearningAlgorithm;
import com.cig.mctbnc.nodes.DiscreteNode;
import com.cig.mctbnc.nodes.Node;

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
	static Logger logger = LogManager.getLogger(MCTBNC.class);

	// The subgraph formed by class variables is a Bayesian network
	private BN<T> bn;
	private CTBNC<T> ctbnc;

	public MCTBNC(Dataset trainingDataset, ParameterLearningAlgorithm ctbnParameterLearningAlgorithm,
			StructureLearningAlgorithm ctbncStructureLearningAlgorithm,
			ParameterLearningAlgorithm bnParameterLearningAlgorithm,
			StructureLearningAlgorithm bnStructureLearningAlgorithm) {

		// Define nodes of the MCTBNC
		List<Node> nodes = new ArrayList<Node>();
		for (String nameFeature : trainingDataset.getNameFeatures()) {
			int index = trainingDataset.getIndexVariable(nameFeature);
			nodes.add(new DiscreteNode(index, nameFeature, false, trainingDataset.getStatesVariable(nameFeature)));
		}

		for (String nameClassVariable : trainingDataset.getNameClassVariables()) {
			int index = trainingDataset.getIndexVariable(nameClassVariable);
			nodes.add(new DiscreteNode(index, nameClassVariable, true,
					trainingDataset.getStatesVariable(nameClassVariable)));
		}
		
		// Add nodes to the model, creating an index for each of them when used with this model
		addNodes(nodes);

		// Define class subgraph with a Bayesian network
		bn = new BN<T>(getNodesClassVariables(), trainingDataset, bnParameterLearningAlgorithm,
				bnStructureLearningAlgorithm);

		// Define feature and bridge subgraphs with a continuous time Bayesian network
		ctbnc = new CTBNC<T>(getNodesFeatures(), trainingDataset, ctbnParameterLearningAlgorithm,
				ctbncStructureLearningAlgorithm);
	}

	public void display() {
		Graph graph = new SingleGraph("MCTBNC");
		addNodes(graph, nodes);
		addEdges(graph, nodes);
		graph.display();
	}

	@Override
	public void learn() {
		// Learn structure

		// Learn structure and parameters of class subgraph (Bayesian network)
		logger.info("Defining structure and parameters of the class subgraph (Bayesian network)");
		bn.learn();
		logger.info("Class subgraph established!");


		// Learn structure and parameters of feature and bridge subgraph. These are
		// modeled by
		// a continuous time Bayesian network classifier where the restriction that the
		// class
		// variable does not depend on the states of the features is extended to more
		// class variables
		logger.info("Defining structure and parameters of the feature and bridge subgraphs (Continuous time "
				+ "Bayesian network)");
		ctbnc.learn();
		logger.info("Feature and bridge subgraphs established!");

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
	public String getType() {
		return "Multidimensional Continuous time Bayesian network";
	}

	@Override
	public boolean isStructureLegal(boolean[][] adjacencyMatrix) {
		// TODO Auto-generated method stub
		return false;
	}

}
