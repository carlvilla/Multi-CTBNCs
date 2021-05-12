package es.upm.fi.cig.mctbnc.models.submodels;

import es.upm.fi.cig.mctbnc.learning.BNLearningAlgorithms;
import es.upm.fi.cig.mctbnc.learning.CTBNLearningAlgorithms;
import es.upm.fi.cig.mctbnc.learning.structure.constraints.StructureConstraints;
import es.upm.fi.cig.mctbnc.learning.structure.constraints.BN.EmptyBN;
import es.upm.fi.cig.mctbnc.learning.structure.constraints.CTBNC.NaiveBayes;
import es.upm.fi.cig.mctbnc.models.MCTBNC;
import es.upm.fi.cig.mctbnc.nodes.Node;

/**
 * Specifies the structure constraints of a multidimensional continuous time naive
 * Bayes classifier (MCTBNC) where any subgraph has arcs except the bridge
 * subgraph (fully naive multi-dimensional classifier).
 * 
 * @author Carlos Villa Blanco
 * @param <NodeTypeBN>   type of the nodes of the BN (class subgraph)
 * @param <NodeTypeCTBN> type of the nodes of the CTBN (feature subgraph)
 *
 */
public class MCTNBC<NodeTypeBN extends Node, NodeTypeCTBN extends Node> extends MCTBNC<NodeTypeBN, NodeTypeCTBN> {

	/**
	 * Constructs a multidimensional continuous time naive Bayes classifier given
	 * the learning algorithms for BNs and CTBNs.
	 * 
	 * @param bnLearningAlgs   parameter and structure learning algorithms for a
	 *                         Bayesian network
	 * @param ctbnLearningAlgs parameter and structure learning algorithms for a
	 *                         conntinuous time Bayesian network
	 * @param bnNodeClass      Bayesian network node type
	 * @param ctbnNodeClass    continuous time Bayesian network node type
	 */
	public MCTNBC(BNLearningAlgorithms bnLearningAlgs, CTBNLearningAlgorithms ctbnLearningAlgs,
			Class<NodeTypeBN> bnNodeClass, Class<NodeTypeCTBN> ctbnNodeClass) {
		super(bnLearningAlgs, ctbnLearningAlgs, bnNodeClass, ctbnNodeClass);
	}

	@Override
	public StructureConstraints getStructureConstraintsBN() {
		return new EmptyBN();
	}

	@Override
	public StructureConstraints getStructureConstraintsCTBN() {
		return new NaiveBayes();
	}

	@Override
	public String getType() {
		if (getNodesBN().size() == 1)
			return "Continuous time naive Bayes classifier";
		return "Multidimensional continuous time naive Bayes classifier";
	}

	@Override
	public String getModelIdentifier() {
		return "MCTNBC";
	}

}
