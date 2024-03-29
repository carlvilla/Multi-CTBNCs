package es.upm.fi.cig.multictbnc.models.submodels;

import es.upm.fi.cig.multictbnc.learning.BNLearningAlgorithms;
import es.upm.fi.cig.multictbnc.learning.CTBNLearningAlgorithms;
import es.upm.fi.cig.multictbnc.learning.structure.constraints.BN.EmptyBN;
import es.upm.fi.cig.multictbnc.learning.structure.constraints.CTBNC.NaiveBayes;
import es.upm.fi.cig.multictbnc.learning.structure.constraints.StructureConstraints;
import es.upm.fi.cig.multictbnc.models.MultiCTBNC;
import es.upm.fi.cig.multictbnc.nodes.Node;

/**
 * Specifies the structure constraints of a multidimensional continuous-time naive Bayes classifier (Multi-CTBNC) where
 * any subgraph has arcs except the bridge subgraph (fully naive multi-dimensional classifier).
 *
 * @param <NodeTypeBN>   type of the nodes of the BN (class subgraph)
 * @param <NodeTypeCTBN> type of the nodes of the CTBN (feature subgraph)
 * @author Carlos Villa Blanco
 */
public class MultiCTNBC<NodeTypeBN extends Node, NodeTypeCTBN extends Node> extends
		MultiCTBNC<NodeTypeBN, NodeTypeCTBN> {

	/**
	 * Constructs a multidimensional continuous-time naive Bayes classifier given the learning algorithms for BNs and
	 * CTBNs.
	 *
	 * @param bnLearningAlgs   parameter and structure learning algorithms for a Bayesian network
	 * @param ctbnLearningAlgs parameter and structure learning algorithms for a continuous-time Bayesian network
	 * @param bnNodeClass      Bayesian network node type
	 * @param ctbnNodeClass    continuous-time Bayesian network node type
	 */
	public MultiCTNBC(BNLearningAlgorithms bnLearningAlgs, CTBNLearningAlgorithms ctbnLearningAlgs,
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
	public String getModelIdentifier() {
		return "Multi-CTNBC";
	}

	@Override
	public String getType() {
		if (getNodesClassVariables().size() == 1)
			return "Continuous-time naive Bayes classifier";
		return "Multidimensional continuous-time naive Bayes classifier";
	}

}