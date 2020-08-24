package com.cig.mctbnc.models;

import java.util.List;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.learning.parameters.ParameterLearningAlgorithm;
import com.cig.mctbnc.learning.structure.StructureLearningAlgorithm;
import com.cig.mctbnc.learning.structure.constraints.StructureConstraints;
import com.cig.mctbnc.nodes.Node;

/**
 * 
 * @author Carlos Villa Blanco
 *
 * @param <NodeType> Type of nodes that will be learned, e.g., nodes with
 *                   conditional probability table (CPTNode)
 */
public class BN<NodeType extends Node> extends AbstractPGM<NodeType> {
	/**
	 * Initialize a Bayesian network by receiving a list of nodes and a dataset.
	 * This constructor was thought to be used by the MCTBNC.
	 * 
	 * @param nodes
	 * @param dataset
	 */
	public BN(List<NodeType> nodes, Dataset dataset) {
		super(nodes);
		this.dataset = dataset;
	}

	/**
	 * Initialize a Bayesian network by receiving a dataset, a list of variables to
	 * use and the algorithms for parameter and structure learning. This constructor
	 * was thought to be used by the MCTBNC.
	 * 
	 * @param nameVariables
	 * @param dataset
	 * @param parameterLearningAlg
	 * @param structureLearningAlg
	 * @param structureConstraints
	 * @param nodeClass
	 */
	public BN(List<String> nameVariables, ParameterLearningAlgorithm parameterLearningAlg,
			StructureLearningAlgorithm structureLearningAlg, StructureConstraints structureConstraints,
			Class<NodeType> nodeClass) {
		// Set variables to use
		this.nameVariables = nameVariables;
		// Set node type
		this.nodeClass = nodeClass;
		// Set necessary algorithms to learn the model
		setParameterLearningAlgorithm(parameterLearningAlg);
		setStructureLearningAlgorithm(structureLearningAlg);
		setStructureConstraints(structureConstraints);
	}

	/**
	 * Return the nodes with the learned parameters. This can be, for example, a
	 * list of CPTNode objects that store conditional probability tables.
	 * 
	 * @return nodes with learned parameters
	 */
	public List<NodeType> getLearnedNodes() {
		return (List<NodeType>) nodes;
	}

	@Override
	public void display() {
		Graph graph = new SingleGraph("BN");
		addNodes(graph, nodes);
		addEdges(graph, nodes);
		graph.display();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("--Structure Bayesian network--\n");
		for (Node node : nodes) {
			if (node.getChildren().isEmpty())
				sb.append("(" + node.getName() + ")");
			else {
				sb.append("(" + node.getName() + ") => ");
				for (Node child : node.getChildren()) {
					sb.append(String.join(", ", "(" + child.getName() + ")"));
				}
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	@Override
	public String getType() {
		return "Bayesian network";
	}

}
