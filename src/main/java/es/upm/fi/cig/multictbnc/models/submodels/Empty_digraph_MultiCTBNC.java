package es.upm.fi.cig.multictbnc.models.submodels;

import es.upm.fi.cig.multictbnc.learning.BNLearningAlgorithms;
import es.upm.fi.cig.multictbnc.learning.CTBNLearningAlgorithms;
import es.upm.fi.cig.multictbnc.learning.structure.constraints.BN.EmptyBN;
import es.upm.fi.cig.multictbnc.learning.structure.constraints.StructureConstraints;
import es.upm.fi.cig.multictbnc.models.MultiCTBNC;
import es.upm.fi.cig.multictbnc.nodes.Node;

/**
 * Implements a Multi-CTBNC with an empty class subgraph.
 *
 * @param <NodeTypeBN>   type of the nodes of the BN (class subgraph)
 * @param <NodeTypeCTBN> type of the nodes of the CTBN (feature subgraph)
 * @author Carlos Villa Blanco
 */
public class Empty_digraph_MultiCTBNC<NodeTypeBN extends Node, NodeTypeCTBN extends Node> extends
		MultiCTBNC<NodeTypeBN, NodeTypeCTBN> {

	/**
	 * Constructs a {@code Empty_digraph_MultiCTBNC} by receiving the learning algorithms for Bayesian networks and
	 * continuous-time Bayesian networks.
	 *
	 * @param bnLearningAlgs   parameter and structure learning algorithms for Bayesian networks
	 * @param ctbnLearningAlgs parameter and structure learning algorithms for continuous-time Bayesian networks
	 * @param bnNodeClass      Bayesian network node type
	 * @param ctbnNodeClass    continuous-time Bayesian network node type
	 */
	public Empty_digraph_MultiCTBNC(BNLearningAlgorithms bnLearningAlgs, CTBNLearningAlgorithms ctbnLearningAlgs,
									Class<NodeTypeBN> bnNodeClass, Class<NodeTypeCTBN> ctbnNodeClass) {
		super(bnLearningAlgs, ctbnLearningAlgs, bnNodeClass, ctbnNodeClass);
	}

	@Override
	public StructureConstraints getStructureConstraintsBN() {
		return new EmptyBN();
	}

	@Override
	public String getModelIdentifier() {
		return "Empty-digraph Multi-CTBNC";
	}

	@Override
	public String getType() {
		if (getNodesClassVariables().size() == 1)
			return "Continuous-time Bayesian network classifier";
		return "Empty-digraph multidimensional continuous-time Bayesian network classifier";
	}

}