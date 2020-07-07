package main.java.com.cig.mctbnc.models;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;

import main.java.com.cig.mctbnc.learning.parameters.BNParameterLearning;
import main.java.com.cig.mctbnc.learning.parameters.CTBNParameterLearning;
import main.java.com.cig.mctbnc.learning.structure.BNStructureLearning;
import main.java.com.cig.mctbnc.learning.structure.MCTBNCStructureLearning;

/**
 * @author Carlos Villa Blanco <carlos.villa@upm.es>
 *
 *         Implements the Multi-dimensional Continuous Time Bayesian Network
 *         Classifier.
 * @param <T> Type of the predicted classes
 * 
 */
public class MCTBNC<T> extends AbstractPGM{

	private List<Node> features;
	private List<Node> classVariables;
	private String initialMBNC;
	
	// The subgraph formed by class variables is a Bayesian network
	private BN bn;

	private BNParameterLearning BNParameterLearningAlgorithm;
	private CTBNParameterLearning CTBNparameterLearningAlgorithm;
	
	private BNStructureLearning bnStructureLearningAlgorithm;
	private MCTBNCStructureLearning structureLearningAlgorithm;

	public MCTBNC(Dataset trainingDataset, CTBNParameterLearning parameterLearningAlgorithm,
			MCTBNCStructureLearning structureLearningAlgorithm,
			BNStructureLearning bnStructureLearningAlgorithm,
			BNParameterLearning bnParameterLearningAlgorithm) {

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
		this.structureLearningAlgorithm = structureLearningAlgorithm;
		this.bnStructureLearningAlgorithm = bnStructureLearningAlgorithm;
		
		// Define class subgraph
		bn = new BN(features, bnStructureLearningAlgorithm, bnParameterLearningAlgorithm, trainingDataset);
		
		
	}

	public void display() {
		Graph graph = new SingleGraph("MCTBNC");
		List<Node> allNodes = Stream.concat(classVariables.stream(), features.stream()).collect(Collectors.toList());
		addNodes(graph, allNodes);
		addEdges(graph, allNodes);
		graph.display();
	}
	
	/**
	 * Obtain the node (feature or class variable) with certain index
	 * @param index
	 * @return
	 */
	public Node getNodeByIndex(int index) {		
		Node selectedNode = features.stream()
				  .filter(node -> node.getIndex() == index)
				  .findAny()
				  .orElse(null);
			
		if (selectedNode == null) {
			selectedNode = classVariables.stream()
					  .filter(node -> node.getIndex() == index)
					  .findAny()
					  .orElse(null);
		}
		
		return selectedNode;
	}

	@Override
	public void learn() {
		// Learn structure
		
		// Learn strucuture Bayesian network
		//bnStructureLearningAlgorithm.learn();
		
		
		// Learn parameters

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
