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
		this.nameVariables = new ArrayList<String>();
		for (Node node : nodes)
			this.nameVariables.add(node.getName());
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
		this.nodeIndexer = new NodeIndexer<NodeType>(this.nodes);
	}

	@Override
	public void removeAllNodes() {
		this.nodes = new ArrayList<NodeType>();
		this.nodeIndexer = null;
	}

	@Override
	public void setStructure(boolean[][] adjacencyMatrix) {
		// Current edges are removed
		for (Node node : this.nodes)
			node.removeAllEdges();
		for (int i = 0; i < adjacencyMatrix.length; i++) {
			Node node = this.nodeIndexer.getNodeByIndex(i);
			for (int j = 0; j < adjacencyMatrix.length; j++)
				if (adjacencyMatrix[i][j]) {
					Node childNode = this.nodeIndexer.getNodeByIndex(j);
					node.setChild(childNode);
				}
		}
		// Learn the parameters of the model
		learnParameters();
	}

	@Override
	public void setStructureModifiedNodes(boolean[][] newAdjacencyMatrix) {
		if (areParametersEstimated()) {
			boolean[][] currentAdjacencyMatrix = getAdjacencyMatrix();
			// Get nodes whose parents changed
			List<Integer> modifiedNodes = getModifiedNodes(currentAdjacencyMatrix, newAdjacencyMatrix);
			// Modify structure of the model
			for (int idx : modifiedNodes) {
				Node node = this.nodeIndexer.getNodeByIndex(idx);
				// Current edges of selected nodes are removed
				node.removeParents();
				// Establish parents of selected nodes
				for (int i = 0; i < newAdjacencyMatrix.length; i++) {
					if (newAdjacencyMatrix[i][idx])
						node.setParent(this.nodeIndexer.getNodeByIndex(i));
				}
			}
			// Learn the parameters of the modified nodes
			learnParameters(modifiedNodes);
		} else {
			// It is necessary that all nodes have their parameters estimated
			setStructure(newAdjacencyMatrix);
		}
	}

	/**
	 * Return those nodes which have different parents in the provided adjacency
	 * matrices.
	 * 
	 * @param adjacencyMatrix1
	 * @param adjacencyMatrix2
	 * @param modifiedNodes
	 */
	private List<Integer> getModifiedNodes(boolean[][] adjacencyMatrix1, boolean[][] adjacencyMatrix2) {
		List<Integer> modifiedNodes = new ArrayList<Integer>();
		for (int j = 0; j < getNumNodes(); j++) {
			for (int i = 0; i < getNumNodes(); i++) {
				if (adjacencyMatrix1[j][i] != adjacencyMatrix2[j][i])
					modifiedNodes.add(i);
			}
		}
		return modifiedNodes;
	}

	@Override
	public void learnParameters() {
		// Learn the sufficient statistics and parameters for each node
		this.parameterLearningAlg.learn(this.nodes, this.dataset);
	}

	@Override
	public void learnParameters(List<Integer> idxsNodes) {
		// Learn the sufficient statistics and parameters for each node
		for (int idx : idxsNodes) {
			Node node = this.nodeIndexer.getNodeByIndex(idx);
			this.parameterLearningAlg.learn(node, this.dataset);
		}
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
		this.nodeFactory = new NodeFactory<NodeType>(this.nodeClass);
		// Clear the entire model
		removeAllNodes();
		// Create nodes using the dataset
		List<NodeType> nodes = new ArrayList<NodeType>();
		for (String nameVariable : this.nameVariables) {
			NodeType node = this.nodeFactory.createNode(nameVariable, dataset);
			nodes.add(node);
		}
		addNodes(nodes);
	}

	@Override
	public void learn() {
		if (this.dataset != null) {
			// Depending on the class of model to learn, there could be a unique structure
			// (naive Bayes or empty graph) or the initial one has to be optimized
			if (this.structureConstraints.uniqueStructure()) {
				// One possible structure. It is set in the PGM and the parameters learned
				this.structureConstraints.initializeStructure(this);
				learnParameters();
			} else
				// Learn structure and parameters with the specified algorithms
				this.structureLearningAlg.learn(this);
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
	@Override
	public boolean[][] getAdjacencyMatrix() {
		int numNodes = getNumNodes();
		boolean[][] adjacencyMatrix = new boolean[numNodes][numNodes];
		for (Node node : this.nodes) {
			List<Node> children = node.getChildren();
			for (Node childNode : children) {
				int indexNode = this.nodeIndexer.getIndexNodeByName(node.getName());
				int indexChildNode = this.nodeIndexer.getIndexNodeByName(childNode.getName());
				adjacencyMatrix[indexNode][indexChildNode] = true;
			}
		}
		return adjacencyMatrix;
	}

	@Override
	public List<NodeType> getNodes() {
		return this.nodes;
	}

	@Override
	public NodeType getNodeByIndex(int index) {
		return this.nodeIndexer.getNodeByIndex(index);
	}

	@Override
	public NodeType getNodeByName(String nameVariable) {
		return this.nodes.stream().filter(node -> node.getName().equals(nameVariable)).findFirst().orElse(null);
	}

	@Override
	public List<NodeType> getNodesByNames(List<String> nameVariables) {
		return this.nodes.stream().filter(node -> nameVariables.contains(node.getName())).collect(Collectors.toList());
	}

	/**
	 * Return the node indexer of the model.
	 * 
	 * @return node indexer
	 */
	@Override
	public NodeIndexer<NodeType> getNodeIndexer() {
		return this.nodeIndexer;
	}

	@Override
	public List<NodeType> getNodesClassVariables() {
		return this.nodes.stream().filter(node -> node.isClassVariable()).collect(Collectors.toList());
	}

	@Override
	public List<NodeType> getNodesFeatures() {
		return this.nodes.stream().filter(node -> !node.isClassVariable()).collect(Collectors.toList());
	}

	@Override
	public int getNumNodes() {
		return this.nodes.size();
	}

	/**
	 * Return the names of the variables of the PGM.
	 * 
	 * @return names of the variables
	 */
	public List<String> getNameVariables() {
		return this.nameVariables;
	}

	/**
	 * Return the algorithm that is used to learn the parameters of the PGM.
	 * 
	 * @return parameter learning algorithm
	 */
	public ParameterLearningAlgorithm getParameterLearningAlg() {
		return this.parameterLearningAlg;
	}

	/**
	 * Return the algorithm that is used to learn the structure of the PGM.
	 * 
	 * @return structure learning algorithm
	 */
	public StructureLearningAlgorithm getStructureLearningAlg() {
		return this.structureLearningAlg;
	}

	/**
	 * Return the constraints that the PGM needs to meet.
	 * 
	 * @return structure constraints
	 * 
	 */
	public StructureConstraints getStructureConstraints() {
		return this.structureConstraints;
	}

	@Override
	public boolean areParametersEstimated() {
		for (Node node : this.nodes) {
			if (!node.areParametersEstimated())
				return false;
		}
		return true;
	}

	/**
	 * Return the type of the nodes.
	 * 
	 * @return type of the nodes
	 */
	public Class<NodeType> getNodeClass() {
		return this.nodeClass;
	}

	/**
	 * Return the dataset used to learn the PGM.
	 * 
	 * @return dataset
	 */
	public Dataset getDataset() {
		return this.dataset;
	}

	/**
	 * Determine if the structure is legal.
	 * 
	 * @param adjacencyMatrix
	 * @return boolean that determines if the structure is valid
	 */
	@Override
	public boolean isStructureLegal(boolean[][] adjacencyMatrix) {
		return this.structureConstraints.isStructureLegal(adjacencyMatrix, getNodeIndexer());
	}

	/**
	 * Display the PGM using GraphStream.
	 * 
	 * @param graph
	 */
	@Override
	public void display() {
		// Create GraphStram graph
		Graph graph = new SingleGraph("PGM");
		addNodes(graph, this.nodes);
		addEdges(graph, this.nodes);
		// Define style of the graph
		graph.setAttribute("ui.stylesheet", "url(src/main/resources/css/graph-style.css);");
		// Define viewer
		FxViewer viewer = new FxViewer(graph, FxViewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
		FxViewPanel panel = (FxViewPanel) viewer.addDefaultView(false, new FxGraphRenderer());
		// Create stage and scene to visualize the graph
		Stage stage = new Stage();
		stage.setTitle(getType());
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
	private void addNodes(Graph graph, List<NodeType> nodes) {
		// Variables used to determine position of the node in the graph
		int numClassVariables = 0;
		int numFeatures = 0;
		for (int i = 0; i < nodes.size(); i++) {
			// Retrieve node from the model
			Node node = nodes.get(i);
			String nameNode = node.getName();
			// Create a node for the graph
			org.graphstream.graph.Node nodeGraph = graph.addNode(nameNode);
			nodeGraph.setAttribute("ui.label", nameNode);
			// Display node differently depending on if it is a class variable or not
			if (node.isClassVariable()) {
				nodeGraph.setAttribute("y", 1);
				nodeGraph.setAttribute("x", numClassVariables);
				numClassVariables++;
			} else {
				nodeGraph.setAttribute("y", 0);
				nodeGraph.setAttribute("x", numFeatures);
				numFeatures++;
			}
		}
	}

	/**
	 * Add edges to a graphstream graph.
	 * 
	 * @param graph
	 * @param nodes
	 */
	private void addEdges(Graph graph, List<NodeType> nodes) {
		for (Node node : nodes) {
			String nameNode = node.getName();
			for (Node child : node.getChildren()) {
				String nameChild = child.getName();
				graph.addEdge(nameNode + nameChild, nameNode, nameChild, true);
			}
		}
	}

}
