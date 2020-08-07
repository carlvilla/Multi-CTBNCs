package com.cig.mctbnc.models;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.graphstream.graph.Graph;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.learning.parameters.ParameterLearningAlgorithm;
import com.cig.mctbnc.learning.structure.StructureLearningAlgorithm;
import com.cig.mctbnc.learning.structure.constraints.StructureConstraints;
import com.cig.mctbnc.nodes.Node;
import com.cig.mctbnc.nodes.NodeIndexer;

public abstract class AbstractPGM<NodeType extends Node> implements PGM<NodeType> {
	protected List<NodeType> nodes;
	protected NodeIndexer<NodeType> nodeIndexer;
	protected Dataset dataset;
	protected ParameterLearningAlgorithm parameterLearningAlg;
	protected StructureLearningAlgorithm structureLearningAlg;
	protected StructureConstraints structureConstraints;

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
	public void addNodes(Graph graph, List<NodeType> nodes) {
		for (Node node : nodes) {
			String nameNode = node.getName();
			graph.addNode(nameNode).addAttribute("ui.label", nameNode);
		}
	}

	@Override
	public void addEdges(Graph graph, List<NodeType> nodes) {
		for (Node node : nodes) {
			String nameNode = node.getName();
			for (Node child : node.getChildren()) {
				String nameChild = child.getName();
				graph.addEdge(nameNode + nameChild, nameNode, nameChild, true);
			}
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

	@Override
	public Node getNodeByIndex(int index) {
		return nodeIndexer.getNodeByIndex(index);
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
		// addNodes(graph, nodes);
		// addEdges(graph, nodes);
		// graph.display();
	}

}
