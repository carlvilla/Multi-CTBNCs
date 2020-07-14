package main.java.com.cig.mctbnc.models;

import java.util.ArrayList;
import java.util.List;

import main.java.com.cig.mctbnc.data.representation.Dataset;
import main.java.com.cig.mctbnc.learning.parameters.BNParameterLearning;
import main.java.com.cig.mctbnc.learning.parameters.CTBNParameterLearning;
import main.java.com.cig.mctbnc.learning.structure.BNStructureLearning;
import main.java.com.cig.mctbnc.learning.structure.CTBNStructureLearning;
import main.java.com.cig.mctbnc.nodes.DiscreteNode;
import main.java.com.cig.mctbnc.nodes.Node;

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
	private String initialMBNC;

	// The subgraph formed by class variables is a Bayesian network
	private BN<T> bn;

	private CTBNParameterLearning ctbnParameterLearningAlgorithm;
	private CTBNStructureLearning ctbnStructureLearningAlgorithm;

	public MCTBNC(Dataset trainingDataset, CTBNParameterLearning ctbnParameterLearningAlgorithm,
			CTBNStructureLearning ctbnStructureLearningAlgorithm, BNParameterLearning bnParameterLearningAlgorithm,
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

		// Define class subgraph
		bn = new BN<T>(classVariables, bnParameterLearningAlgorithm, bnStructureLearningAlgorithm, trainingDataset);

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
	 * Obtain the node (feature or class variable) with certain index
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

	@Override
	public void learn() {
		// Learn structure

		// Learn structure and parameters of class subgraph (Bayesian network)
		bn.learn();

		// Learn structure and parameters of feature and bridge subgraph (Continuous time Bayesian network)

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
