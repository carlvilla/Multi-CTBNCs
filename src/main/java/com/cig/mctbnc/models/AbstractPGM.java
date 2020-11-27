package com.cig.mctbnc.models;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.fx_viewer.FxViewPanel;
import org.graphstream.ui.fx_viewer.FxViewer;
import org.graphstream.ui.javafx.FxGraphRenderer;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.learning.parameters.ParameterLearningAlgorithm;
import com.cig.mctbnc.learning.structure.StructureLearningAlgorithm;
import com.cig.mctbnc.learning.structure.constraints.StructureConstraints;
import com.cig.mctbnc.nodes.Node;
import com.cig.mctbnc.nodes.NodeFactory;
import com.cig.mctbnc.nodes.NodeIndexer;

import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Contains common attributes and methods for PGM.
 * 
 * @author Carlos Villa Blanco
 *
 * @param <NodeType>
 */
public abstract class AbstractPGM<NodeType extends Node> implements PGM<NodeType> {
	Dataset dataset;
	List<String> nameVariables;
	List<NodeType> nodes;
	NodeIndexer<NodeType> nodeIndexer;
	NodeFactory<NodeType> nodeFactory;
	Class<NodeType> nodeClass;
	ParameterLearningAlgorithm parameterLearningAlg;
	StructureLearningAlgorithm structureLearningAlg;
	StructureConstraints structureConstraints;
	static Logger logger = LogManager.getLogger(AbstractPGM.class);

	/**
	 * Common initialization for PGM.
	 * 
	 * @param nodes
	 */
	public AbstractPGM(List<NodeType> nodes) {
		addNodes(nodes);
	}

	/**
	 * Default constructor
	 */
	public AbstractPGM() {
	}

	@Override
	public void addNodes(List<NodeType> nodes) {
		if (this.nodes == null) {
			this.nodes = new ArrayList<NodeType>();
		}
		this.nodes.addAll(nodes);
		nodeIndexer = new NodeIndexer<NodeType>(this.nodes);
	}

	@Override
	public void removeAllNodes() {
		nodes = new ArrayList<NodeType>();
		nodeIndexer = null;
	}

	@Override
	public void setStructure(boolean[][] adjacencyMatrix) {
		// Current edges are removed
		for (Node node : this.nodes)
			node.removeAllEdges();
		for (int i = 0; i < adjacencyMatrix.length; i++) {
			Node node = nodeIndexer.getNodeByIndex(i);
			for (int j = 0; j < adjacencyMatrix.length; j++)
				if (adjacencyMatrix[i][j]) {
					Node childNode = nodeIndexer.getNodeByIndex(j);
					node.setChild(childNode);
				}
		}
		learnParameters();
	}

	@Override
	public void learnParameters() {
		// Learn the sufficient statistics and parameters for each node
		parameterLearningAlg.learn(nodes, dataset);
	}

	/**
	 * Establish the algorithm that will be used to learn the parameters of the PGM.
	 * 
	 * @param parameterLearningAlg
	 */
	public void setParameterLearningAlgorithm(ParameterLearningAlgorithm parameterLearningAlg) {
		this.parameterLearningAlg = parameterLearningAlg;
	}

	/**
	 * Establish the algorithm that will be used to learn the structure of the PGM.
	 * 
	 * @param structureLearningAlg
	 */
	public void setStructureLearningAlgorithm(StructureLearningAlgorithm structureLearningAlg) {
		this.structureLearningAlg = structureLearningAlg;
	}

	/**
	 * Establish the constraints that the PGM needs to meet.
	 * 
	 * @param structureConstraints
	 */
	public void setStructureConstraints(StructureConstraints structureConstraints) {
		this.structureConstraints = structureConstraints;
	}

	@Override
	public void setTrainingDataset(Dataset dataset) {
		// Save dataset used to learn the model
		this.dataset = dataset;
		// Use node factory to create nodes of the specified type
		nodeFactory = new NodeFactory<NodeType>(nodeClass);
		// Create nodes using the dataset
		List<NodeType> nodes = new ArrayList<NodeType>();
		for (String nameVariable : nameVariables) {
			NodeType node = nodeFactory.createNode(nameVariable, dataset);
			nodes.add(node);
		}
		addNodes(nodes);
	}

	@Override
	public void learn() {
		if (dataset != null) {
			// Depending on the class of model to learn, there could be a unique structure
			// (naive Bayes or empty graph) or the initial one has to be optimized
			if (structureConstraints.uniqueStructure()) {
				// One possible structure. It is set in the PGM and the parameters learned
				structureConstraints.initializeStructure(this);
				learnParameters();
			} else
				// Learn structure and parameters with the specified algorithms
				structureLearningAlg.learn(this, dataset, parameterLearningAlg, structureConstraints);
		} else {
			logger.warn("Training dataset was not established");
		}
	}

	@Override
	public void learn(Dataset dataset) {
		setTrainingDataset(dataset);
		learn();
	}

	/**
	 * Return the adjacency matrix of the PGM by analyzing the children of each
	 * node.
	 * 
	 * @return bidimensional boolean array representing the adjacency matrix
	 */
	public boolean[][] getAdjacencyMatrix() {
		int numNodes = getNumNodes();
		boolean[][] adjacencyMatrix = new boolean[numNodes][numNodes];
		for (Node node : nodes) {
			List<Node> children = node.getChildren();
			for (Node childNode : children) {
				int indexNode = nodeIndexer.getIndexNodeByName(node.getName());
				int indexChildNode = nodeIndexer.getIndexNodeByName(childNode.getName());
				adjacencyMatrix[indexNode][indexChildNode] = true;
			}
		}
		return adjacencyMatrix;
	}

	@Override
	public List<NodeType> getNodes() {
		return nodes;
	}

	public NodeType getNodeByIndex(int index) {
		return nodeIndexer.getNodeByIndex(index);
	}

	@Override
	public NodeType getNodeByName(String nameVariable) {
		return nodes.stream().filter(node -> node.getName().equals(nameVariable)).findFirst().orElse(null);
	}

	@Override
	public List<NodeType> getNodesByNames(List<String> nameVariables) {
		return nodes.stream().filter(node -> nameVariables.contains(node.getName())).collect(Collectors.toList());
	}

	/**
	 * Return the node indexer of the model.
	 * 
	 * @return node indexer
	 */
	public NodeIndexer<NodeType> getNodeIndexer() {
		return nodeIndexer;
	}

	@Override
	public List<NodeType> getNodesClassVariables() {
		return nodes.stream().filter(node -> node.isClassVariable()).collect(Collectors.toList());
	}

	@Override
	public List<NodeType> getNodesFeatures() {
		return nodes.stream().filter(node -> !node.isClassVariable()).collect(Collectors.toList());
	}

	@Override
	public int getNumNodes() {
		return nodes.size();
	}

	/**
	 * Return the dataset used to learn the PGM.
	 * 
	 * @return dataset
	 */
	public Dataset getDataset() {
		return dataset;
	}

	/**
	 * Determine if the structure is legal.
	 * 
	 * @param adjacencyMatrix
	 * @return boolean that determines if the structure is valid
	 */
	public boolean isStructureLegal(boolean[][] adjacencyMatrix) {
		return structureConstraints.isStructureLegal(adjacencyMatrix, getNodeIndexer());
	}

	/**
	 * Display the PGM using GraphStream.
	 * 
	 * @param graph
	 */
	public void display() {
		// Create GraphStram graph
		Graph graph = new SingleGraph("PGM");
		addNodes(graph, nodes);
		addEdges(graph, nodes);
		// Define style of the graph
		graph.setAttribute("ui.stylesheet", "url(src/main/resources/css/graph-style.css);");
		// Define viewer
		FxViewer viewer = new FxViewer(graph, FxViewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
		viewer.enableAutoLayout();
		FxViewPanel panel = (FxViewPanel) viewer.addDefaultView(false, new FxGraphRenderer());

		// Create stage and scene to visualize the graph
		Stage stage = new Stage();
		Scene scene = new Scene(panel);
		stage.setScene(scene);
		stage.show();
	}

	/**
	 * Add nodes to a graphstream graph.
	 * 
	 * @param graph
	 * @param nodes
	 */
	public void addNodes(Graph graph, List<NodeType> nodes) {
		for (Node node : nodes) {
			String nameNode = node.getName();
			graph.addNode(nameNode).setAttribute("ui.label", nameNode);
		}
	}

	/**
	 * Add edges to a graphstream graph.
	 * 
	 * @param graph
	 * @param nodes
	 */
	public void addEdges(Graph graph, List<NodeType> nodes) {
		for (Node node : nodes) {
			String nameNode = node.getName();
			for (Node child : node.getChildren()) {
				String nameChild = child.getName();
				graph.addEdge(nameNode + nameChild, nameNode, nameChild, true);
			}
		}
	}

}
