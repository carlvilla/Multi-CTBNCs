package es.upm.fi.cig.multictbnc.models;

import es.upm.fi.cig.multictbnc.data.representation.Dataset;
import es.upm.fi.cig.multictbnc.learning.CTBNLearningAlgorithms;
import es.upm.fi.cig.multictbnc.learning.structure.constraints.StructureConstraints;
import es.upm.fi.cig.multictbnc.nodes.Node;
import es.upm.fi.cig.multictbnc.nodes.NodeIndexer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implements a continuous-time Bayesian network (CTBN).
 *
 * @param <NodeType> type of the nodes of the CTBN (e.g. nodes that learn a CIM)
 * @author Carlos Villa Blanco
 */
public class CTBN<NodeType extends Node> extends AbstractPGM<NodeType> {
	// Reference to the class subgraph. Only necessary to compute the conditional LL
	BN<? extends Node> bnClassSubgraph;

	/**
	 * Initialises a continuous-time Bayesian network by receiving a list of nodes and a Bayesian network modelling the
	 * class subgraph of a Multi-CTBNC. This constructor is used when the structure of the model is provided and to
	 * build a {@code MultiCTBNC}.
	 *
	 * @param nodes           nodes that make up the model and define its structure
	 * @param bnClassSubgraph a Bayesian network modelling the class subgraph of a Multi-CTBNC (necessary to estimate
	 *                        the conditional log-likelihood)
	 */
	public CTBN(List<NodeType> nodes, BN<? extends Node> bnClassSubgraph) {
		super(nodes);
		this.nameVariables = nodes.stream().map(node -> node.getName()).collect(Collectors.toList());
		this.bnClassSubgraph = bnClassSubgraph;
	}

	/**
	 * Initialises a continuous-time Bayesian network by receiving a list of nodes, a Bayesian network modelling the
	 * class subgraph of a Multi-CTBNC and a training dataset. This constructor is used when the structure of the model
	 * is provided and to build a {@code MultiCTBNC}.
	 *
	 * @param nodes           nodes that make up the model and define its structure
	 * @param bnClassSubgraph a Bayesian network modelling the class subgraph of a Multi-CTBNC (necessary to estimate
	 *                        the conditional log-likelihood)
	 * @param dataset         dataset used to learn the continuous-time Bayesian network
	 */
	public CTBN(List<NodeType> nodes, BN<? extends Node> bnClassSubgraph, Dataset dataset) {
		super(nodes, dataset);
		this.nameVariables = nodes.stream().map(node -> node.getName()).collect(Collectors.toList());
		this.bnClassSubgraph = bnClassSubgraph;
	}

	/**
	 * Initialises a continuous-time Bayesian network given a dataset, the list of variables to use and the algorithms
	 * for parameter and structure learning. This constructor was thought to be used by a {@code MultiCTBNC}.
	 *
	 * @param dataset              dataset used to learn the continuous-time Bayesian network
	 * @param nameVariables        variables that makeup the model
	 * @param ctbnLearningAlgs     a {@code CTBNLearningAlgorithms} containing the algorithms for parameter and
	 *                             structure learning
	 * @param structureConstraints structure constraints to take into account during the learning of the
	 *                                continuous-time
	 *                             Bayesian network
	 * @param bnClassSubgraph      a Bayesian network modelling the class subgraph of a Multi-CTBNC (necessary to
	 *                             estimate the conditional log-likelihood)
	 * @param nodeClass            type of the CTBN nodes
	 */
	public CTBN(Dataset dataset, List<String> nameVariables, CTBNLearningAlgorithms ctbnLearningAlgs,
				StructureConstraints structureConstraints, BN<? extends Node> bnClassSubgraph,
				Class<NodeType> nodeClass) {
		// Set variables to use
		this.nameVariables = nameVariables;
		// Set node type
		this.nodeClass = nodeClass;
		// Set necessary algorithms to learn the model
		setParameterLearningAlgorithm(ctbnLearningAlgs.getParameterLearningAlgorithm());
		setStructureLearningAlgorithm(ctbnLearningAlgs.getStructureLearningAlgorithm());
		setStructureConstraints(structureConstraints);
		// Save the dataset used to learn the model
		initialiseModel(dataset);
		// Set the class subgraph
		this.bnClassSubgraph = bnClassSubgraph;
	}

	/**
	 * Initialises a continuous-time Bayesian network given a dataset, the list of variables to use and the algorithms
	 * for parameter and structure learning.
	 *
	 * @param dataset              dataset used to learn the continuous-time Bayesian network
	 * @param nameVariables        variables that makeup the model
	 * @param ctbnLearningAlgs     a {@code CTBNLearningAlgorithms} containing the algorithms for parameter and
	 *                             structure learning
	 * @param structureConstraints structure constrains to take into account during the learning of the continuous-time
	 *                             Bayesian network
	 * @param nodeClass            type of the CTBN nodes
	 */
	public CTBN(Dataset dataset, List<String> nameVariables, CTBNLearningAlgorithms ctbnLearningAlgs,
				StructureConstraints structureConstraints, Class<NodeType> nodeClass) {
		// Set variables to use
		this.nameVariables = nameVariables;
		// Set node type
		this.nodeClass = nodeClass;
		// Set necessary algorithms to learn the model
		setParameterLearningAlgorithm(ctbnLearningAlgs.getParameterLearningAlgorithm());
		setStructureLearningAlgorithm(ctbnLearningAlgs.getStructureLearningAlgorithm());
		setStructureConstraints(structureConstraints);
		// Save the dataset used to learn the model
		initialiseModel(dataset);
	}

	/**
	 * Constructor to clone a continuous-time Bayesian network.
	 *
	 * @param ctbn           continuous-time Bayesian network to clone
	 * @param cloneStructure {@code true} if the structure of the {@code CTBN} should be cloned, {@code false} to
	 *                                      define
	 *                       the model nodes from a dataset contained in the {@code CTBN}, if any, without copying the
	 *                       structure.
	 */
	public CTBN(CTBN<NodeType> ctbn, boolean cloneStructure) {
		// Set variables to use
		this.nameVariables = ctbn.getNameVariables();
		// Set node type
		this.nodeClass = ctbn.getNodeClass();
		// Set necessary algorithms to learn the model
		setParameterLearningAlgorithm(ctbn.getParameterLearningAlg());
		setStructureLearningAlgorithm(ctbn.getStructureLearningAlg());
		setStructureConstraints(ctbn.getStructureConstraints());
		if (!cloneStructure && ctbn.getDataset() != null)
			// Set dataset and create model nodes
			initialiseModel(ctbn.getDataset());
		else if (cloneStructure && ctbn.getNodes() != null)
			// Provided CTBN has nodes
			addNodes(ctbn.getNodes(), true);
		// Set the class subgraph
		this.bnClassSubgraph = ctbn.getBnClassSubgraph();
	}

	/**
	 * Constructor to clone a continuous-time Bayesian network. It clones the nodes of the CTBN and save the provided
	 * dataset.
	 *
	 * @param ctbn    continuous-time Bayesian network
	 * @param dataset a dataset
	 */
	public CTBN(CTBN<NodeType> ctbn, Dataset dataset) {
		// Set variables to use
		this.nameVariables = ctbn.getNameVariables();
		// Set node type
		this.nodeClass = ctbn.getNodeClass();
		// Set necessary algorithms to learn the model
		setParameterLearningAlgorithm(ctbn.getParameterLearningAlg());
		setStructureLearningAlgorithm(ctbn.getStructureLearningAlg());
		setStructureConstraints(ctbn.getStructureConstraints());
		// Clone nodes
		addNodes(ctbn.getNodes(), true);
		// Set the class subgraph
		this.bnClassSubgraph = ctbn.getBnClassSubgraph();
		// Set dataset
		setDataset(dataset);
	}

	/**
	 * Returns the class subgraph (Bayesian network). This is necessary when the structure is defined by optimising the
	 * conditional log-likelihood.
	 *
	 * @return Bayesian network modelling the class subgraph
	 */
	public BN<? extends Node> getBnClassSubgraph() {
		return this.bnClassSubgraph;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void addNodes(List<NodeType> nodes, boolean createNodes) {
		if (nodes != null && nodes.size() > 0) {
			if (this.nodes == null || this.nodes.isEmpty()) {
				this.nodeClass = (Class<NodeType>) nodes.get(0).getClass();
				this.nodes = new ArrayList<>();
			}
			if (createNodes)
				this.nodes.addAll(getNodeFactory().createNodes(nodes));
			else
				this.nodes.addAll(nodes);
			this.nodeIndexer = new NodeIndexer<>(getNodes());
		}
	}

	@Override
	public void learnParameters(Dataset dataset) {
		// Only compute the parameters of feature variables. Ignore class variables.
		if (getNodes() != null && !getNodes().isEmpty() && dataset != null) {
			List<NodeType> featureNodes = getNodes().stream().filter(node -> !node.isClassVariable()).collect(
					Collectors.toList());
			this.parameterLearningAlg.learn(featureNodes, dataset);
		}
	}

	/**
	 * Modifies the structure of the continuous-time Bayesian network by changing the parent set of a specified node
	 * and
	 * updates its parameters. This method is necessary to learn the model structure by optimising the parent set of
	 * its
	 * nodes.
	 *
	 * @param nodeIndex       node index whose parent set is modified
	 * @param adjacencyMatrix adjacency matrix of the model containing the new parent set of the specified node
	 */
	@Override
	public void setStructure(int nodeIndex, boolean[][] adjacencyMatrix) {
		Node node = this.nodeIndexer.getNodeByIndex(nodeIndex);
		// Current parents of the node are removed
		node.removeParents();
		for (int i = 0; i < adjacencyMatrix.length; i++)
			if (adjacencyMatrix[i][nodeIndex]) {
				Node parentNode = this.nodeIndexer.getNodeByIndex(i);
				node.setParent(parentNode);
			}
	}

	/**
	 * Modifies the structure of the continuous-time Bayesian network by changing the parent set of some specified
	 * nodes
	 * and updates their parameters.
	 *
	 * @param nodeIndexes     node indexes whose parent sets are modified
	 * @param adjacencyMatrix adjacency matrix of the model containing the new parent set of the specified nodes
	 */
	@Override
	public void setStructure(List<Integer> nodeIndexes, boolean[][] adjacencyMatrix) {
		for (int nodeIndex : nodeIndexes)
			setStructure(nodeIndex, adjacencyMatrix);
	}

	@Override
	public String getModelIdentifier() {
		return "CTBN";
	}

	@Override
	public String getType() {
		return "Continuous-time Bayesian network";
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("--Structure continuous-time Bayesian network--\n");
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

}