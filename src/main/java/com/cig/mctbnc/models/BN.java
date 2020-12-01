package com.cig.mctbnc.models;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.learning.BNLearningAlgorithms;
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
	 * Initialize a Bayesian network by receiving a list of nodes.
	 * 
	 * @param nodes
	 */
	public BN(List<NodeType> nodes) {
		super(nodes);
	}

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
	 * @param trainingDataset
	 * @param nameVariables
	 * @param dataset
	 * @param bnLearningAlgs
	 * @param structureConstraints
	 * @param nodeClass
	 */
	public BN(Dataset trainingDataset, List<String> nameVariables, BNLearningAlgorithms bnLearningAlgs,
			StructureConstraints structureConstraints, Class<NodeType> nodeClass) {
		// Set variables to use
		this.nameVariables = nameVariables;
		// Set node type
		this.nodeClass = nodeClass;
		// Set necessary algorithms to learn the model
		setParameterLearningAlgorithm(bnLearningAlgs.getParameterLearningAlgorithm());
		setStructureLearningAlgorithm(bnLearningAlgs.getStructureLearningAlgorithm());
		setStructureConstraints(structureConstraints);
		// Set the training dataset
		setTrainingDataset(trainingDataset);
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

	/**
	 * Obtain the topological ordering of the nodes with the Kahn's algorithm.
	 * 
	 * @return sorted nodes
	 */
	public List<Node> getTopologicalOrdering() {
		List<Node> sortedNodes = new ArrayList<Node>();
		LinkedList<Node> nodesWithoutParents = nodes.stream().filter(node -> !node.hasParents())
				.collect(Collectors.toCollection(LinkedList::new));
		// Create a map with the nodes and their incoming arcs.
		Map<Node, Integer> indegree = IntStream.range(0, getNumNodes()).boxed()
				.collect(Collectors.toMap(i -> nodes.get(i), i -> nodes.get(i).getNumParents()));
		// Iterate over nodes without parents
		while (!nodesWithoutParents.isEmpty()) {
			Node node = nodesWithoutParents.poll();
			sortedNodes.add(node);
			// Iterate over the children of the node (features are ignored)
			List<Node> childrenNodes = node.getChildren().stream().filter(child -> child.isClassVariable())
					.collect(Collectors.toList());
			for (Node childNode : childrenNodes) {
				// Discard the arc between the parent and the child
				indegree.put(childNode, indegree.get(childNode) - 1);
				// If the child has no more incoming arcs, it is added as a node without parents
				if (indegree.get(childNode) == 0)
					nodesWithoutParents.add(childNode);
			}
		}
		return sortedNodes;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("--Structure Bayesian network--\n");
		for (Node node : nodes) {
			if (node.getParents().isEmpty())
				sb.append("{}");
			else	
				for (Node parent : node.getParents()) {
					sb.append("(" + parent.getName() + ")");
				}
			sb.append(" => (" + node.getName() + ") \n");
		}		
		return sb.toString();
	}

	@Override
	public String getType() {
		return "Bayesian network";
	}

}
