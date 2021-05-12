package es.upm.fi.cig.mctbnc.models;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.upm.fi.cig.mctbnc.data.representation.Dataset;
import es.upm.fi.cig.mctbnc.learning.CTBNLearningAlgorithms;
import es.upm.fi.cig.mctbnc.learning.structure.constraints.StructureConstraints;
import es.upm.fi.cig.mctbnc.nodes.Node;
import es.upm.fi.cig.mctbnc.nodes.NodeFactory;

/**
 * Implements a continuous time Bayesian network (CTBN).
 * 
 * @author Carlos Villa Blanco
 *
 * @param <NodeType> type of the nodes of the CTBN (e.g. nodes that learn a CIM)
 */
public class CTBN<NodeType extends Node> extends AbstractPGM<NodeType> {
	BN<? extends Node> bnClassSubgraph;
	NodeFactory<NodeType> nodeFactory;
	static Logger logger = LogManager.getLogger(CTBN.class);

	/**
	 * Initializes a continuous time Bayesian network by receiving a list of nodes
	 * and a Bayesian network modeling the class subgraph of a MCTBNC. This
	 * constructor is used when the structure of the model is provided and to build
	 * a {@code MCTBNC}.
	 * 
	 * @param nodes           nodes that make up the model and define its structure
	 * @param bnClassSubgraph a Bayesian network modeling the class subgraph of a
	 *                        MCTBNC (necessary to estimate the conditional
	 *                        log-likelihood)
	 */
	public CTBN(List<NodeType> nodes, BN<? extends Node> bnClassSubgraph) {
		super(nodes);
		this.nameVariables = nodes.stream().map(node -> node.getName()).collect(Collectors.toList());
		this.bnClassSubgraph = bnClassSubgraph;
	}

	/**
	 * Initializes a continuous time Bayesian network by receiving a list of nodes,
	 * a Bayesian network modeling the class subgraph of a MCTBNC and a training
	 * dataset. This constructor is used when the structure of the model is provided
	 * and to build a {@code MCTBNC}.
	 * 
	 * @param nodes           nodes that make up the model and define its structure
	 * @param bnClassSubgraph a Bayesian network modeling the class subgraph of a
	 *                        MCTBNC (necessary to estimate the conditional
	 *                        log-likelihood)
	 * @param dataset         dataset used to learn the continuous time Bayesian
	 *                        network
	 */
	public CTBN(List<NodeType> nodes, BN<? extends Node> bnClassSubgraph, Dataset dataset) {
		super(nodes, dataset);
		this.nameVariables = nodes.stream().map(node -> node.getName()).collect(Collectors.toList());
		this.bnClassSubgraph = bnClassSubgraph;
	}

	/**
	 * Initializes a continuous Time Bayesian network given a dataset, the list of
	 * variables to use and the algorithms for parameter and structure learning.
	 * This constructor was thought to be used by a {@code MCTBNC}.
	 * 
	 * @param dataset              dataset used to learn the continuous time
	 *                             Bayesian network
	 * @param nameVariables        variables that make up the model
	 * @param ctbnLearningAlgs     a {@code CTBNLearningAlgorithms} containing the
	 *                             algorithms for parameter and structure learning
	 * @param structureConstraints structure constrains to take into account during
	 *                             the learning of the continuous time Bayesian
	 *                             network
	 * @param bnClassSubgraph      a Bayesian network modeling the class subgraph of
	 *                             a MCTBNC (necessary to estimate the conditional
	 *                             log-likelihood)
	 * @param nodeClass            type of the CTBN nodes
	 */
	public CTBN(Dataset dataset, List<String> nameVariables, CTBNLearningAlgorithms ctbnLearningAlgs,
			StructureConstraints structureConstraints, BN<? extends Node> bnClassSubgraph, Class<NodeType> nodeClass) {
		// Set variables to use
		this.nameVariables = nameVariables;
		// Set node type
		this.nodeClass = nodeClass;
		// Set necessary algorithms to learn the model
		setParameterLearningAlgorithm(ctbnLearningAlgs.getParameterLearningAlgorithm());
		setStructureLearningAlgorithm(ctbnLearningAlgs.getStructureLearningAlgorithm());
		setStructureConstraints(structureConstraints);
		// Save the dataset used to learn the model
		initializeModel(dataset);
		// Set the class subgraph
		this.bnClassSubgraph = bnClassSubgraph;
	}

	/**
	 * Initializes a continuous Time Bayesian network given a dataset, the list of
	 * variables to use and the algorithms for parameter and structure learning.
	 * 
	 * @param dataset              dataset used to learn the continuous time
	 *                             Bayesian network
	 * @param nameVariables        variables that make up the model
	 * @param ctbnLearningAlgs     a {@code CTBNLearningAlgorithms} containing the
	 *                             algorithms for parameter and structure learning
	 * @param structureConstraints structure constrains to take into account during
	 *                             the learning of the continuous time Bayesian
	 *                             network
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
		initializeModel(dataset);
	}

	/**
	 * Constructor to clone a continuous Time Bayesian network.
	 * 
	 * @param ctbn continuous Time Bayesian network to clone
	 */
	public CTBN(CTBN<NodeType> ctbn) {
		// Set variables to use
		this.nameVariables = ctbn.getNameVariables();
		// Set node type
		this.nodeClass = ctbn.getNodeClass();
		// Set necessary algorithms to learn the model
		setParameterLearningAlgorithm(ctbn.getParameterLearningAlg());
		setStructureLearningAlgorithm(ctbn.getStructureLearningAlg());
		setStructureConstraints(ctbn.getStructureConstraints());
		// Set dataset and create model nodes (avoid same reference for nodes)
		initializeModel(ctbn.getDataset());
		// Set the class subgraph
		this.bnClassSubgraph = ctbn.getBnClassSubgraph();
	}

	/**
	 * Modifies the structure of the continuous Time Bayesian network by changing
	 * the parent set of a specified node and updates the parameters. This method is
	 * necessary to learn the structure the model by optimizing the parent set of
	 * its nodes.
	 * 
	 * @param nodeIndex       index of the node whose parent set is modified
	 * @param adjacencyMatrix adjacency matrix of the model containing the new
	 *                        parent set of the specified node
	 */
	public void setStructure(int nodeIndex, boolean[][] adjacencyMatrix) {
		Node node = this.nodeIndexer.getNodeByIndex(nodeIndex);
		// Current parents of the node are removed
		node.removeParents();
		for (int i = 0; i < adjacencyMatrix.length; i++)
			if (adjacencyMatrix[i][nodeIndex]) {
				Node parentNode = this.nodeIndexer.getNodeByIndex(i);
				node.setParent(parentNode);
			}
		this.parameterLearningAlg.learn(node, this.dataset);
	}

	/**
	 * Returns the nodes with the learned parameters. This can be, for example, a
	 * list of CIMNode objects that store conditional intensity matrices.
	 * 
	 * @return nodes with learned parameters
	 */
	public List<NodeType> getLearnedNodes() {
		return this.nodes;
	}

	/**
	 * Returns the class subgraph (Bayesian network). This is necessary when the
	 * structure is defined by optimizing the conditional log-likelihood.
	 * 
	 * @return Bayesian network modelling the class subgraph
	 */
	public BN<? extends Node> getBnClassSubgraph() {
		return this.bnClassSubgraph;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<NodeType> getNodesClassVariables() {
		return (List<NodeType>) this.bnClassSubgraph.getNodes();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("--Structure continuous time Bayesian network--\n");
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
		return "Continuous time Bayesian network";
	}

	@Override
	public String getModelIdentifier() {
		return "CTBN";
	}

}
