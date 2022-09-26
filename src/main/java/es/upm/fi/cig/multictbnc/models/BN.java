package es.upm.fi.cig.multictbnc.models;

import es.upm.fi.cig.multictbnc.data.representation.Dataset;
import es.upm.fi.cig.multictbnc.learning.BNLearningAlgorithms;
import es.upm.fi.cig.multictbnc.learning.structure.constraints.StructureConstraints;
import es.upm.fi.cig.multictbnc.nodes.Node;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Implements a Bayesian network (BN).
 *
 * @param <NodeType> type of the nodes of the BN, e.g., nodes with conditional probability table (CPTNode)
 * @author Carlos Villa Blanco
 */
public class BN<NodeType extends Node> extends AbstractPGM<NodeType> {
	/**
	 * Initialises a Bayesian network by receiving a list of nodes.
	 *
	 * @param nodes list of nodes
	 */
	public BN(List<NodeType> nodes) {
		super(nodes);
	}

	/**
	 * Initialises a Bayesian network by receiving a list of nodes and a dataset. This constructor was thought to be
	 * used by {@code MultiCTBNC}.
	 *
	 * @param nodes   list of nodes
	 * @param dataset dataset used to learn the Bayesian network
	 */
	public BN(List<NodeType> nodes, Dataset dataset) {
		super(nodes, dataset);
	}

	/**
	 * Initialises a Bayesian network by receiving a dataset, a list of variables to use and the algorithms for
	 * parameter and structure learning. This constructor was thought to be used by {@code MultiCTBNC}.
	 *
	 * @param dataset              dataset used to learn the Bayesian network
	 * @param nameVariables        name of the variables
	 * @param bnLearningAlgs       parameter and structure learning algorithms
	 * @param structureConstraints structure constrains to take into account during the learning of the Bayesian
	 *                             network
	 * @param nodeClass            type of the BN nodes
	 */
	public BN(Dataset dataset, List<String> nameVariables, BNLearningAlgorithms bnLearningAlgs,
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
		initialiseModel(dataset);
	}

	/**
	 * Constructor to clone a Bayesian network.
	 *
	 * @param bn             Bayesian network to clone
	 * @param cloneStructure {@code true} if the structure of the {@code BN} should be clone, {@code false} to define
	 *                       the model nodes from a dataset contained in the {@code BN}, if any, without copying the
	 *                       structure.
	 */
	public BN(BN<NodeType> bn, boolean cloneStructure) {
		this.nameVariables = bn.getNameVariables();
		this.nodeClass = bn.getNodeClass();
		this.setParameterLearningAlgorithm(bn.getParameterLearningAlg());
		this.setStructureLearningAlgorithm(bn.getStructureLearningAlg());
		setStructureConstraints(bn.getStructureConstraints());
		if (!cloneStructure && bn.getDataset() != null)
			initialiseModel(bn.getDataset());
		else if (cloneStructure && bn.getNodes() != null)
			this.addNodes(bn.getNodes(), true);
	}

	/**
	 * Returns the nodes with the learnt parameters. This can be, for example, a list of CPTNode objects that store
	 * conditional probability tables.
	 *
	 * @return nodes with learnt parameters
	 */
	public List<NodeType> getLearntNodes() {
		return this.nodes;
	}

	/**
	 * Obtains the topological ordering of the nodes with the Kahn's algorithm.
	 *
	 * @return sorted nodes
	 */
	public List<Node> getTopologicalOrdering() {
		List<Node> sortedNodes = new ArrayList<>();
		LinkedList<Node> nodesWithoutParents = this.nodes.stream().filter(node -> !node.hasParents()).collect(
				Collectors.toCollection(LinkedList::new));
		// Create a map with the nodes and their incoming arcs.
		Map<Node, Integer> indegree = IntStream.range(0, getNumNodes()).boxed().collect(
				Collectors.toMap(i -> this.nodes.get(i), i -> this.nodes.get(i).getNumParents()));
		// Iterate over nodes without parents
		while (!nodesWithoutParents.isEmpty()) {
			Node node = nodesWithoutParents.poll();
			sortedNodes.add(node);
			// Iterate over the children of the node (feature variables are ignored)
			List<Node> childrenNodes = node.getChildren().stream().filter(child -> child.isClassVariable()).collect(
					Collectors.toList());
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
		for (Node node : this.nodes) {
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

	@Override
	public String getModelIdentifier() {
		return "BN";
	}

}