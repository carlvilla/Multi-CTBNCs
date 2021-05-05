package es.upm.fi.cig.mctbnc.models.submodels;

import es.upm.fi.cig.mctbnc.learning.BNLearningAlgorithms;
import es.upm.fi.cig.mctbnc.learning.CTBNLearningAlgorithms;
import es.upm.fi.cig.mctbnc.learning.structure.constraints.StructureConstraints;
import es.upm.fi.cig.mctbnc.learning.structure.constraints.BN.EmptyBN;
import es.upm.fi.cig.mctbnc.learning.structure.constraints.CTBNC.NaiveBayes;
import es.upm.fi.cig.mctbnc.models.MCTBNC;
import es.upm.fi.cig.mctbnc.nodes.Node;

/**
 * Specify the structure constraints of a multidimensional continuous time naive
 * Bayes classifier (MCTBNC) where any subgraph has arcs except the bridge
 * subgraph (fully naive multi-dimensional classifier).
 * 
 * @author Carlos Villa Blanco
 * @param <NodeTypeBN>
 * @param <NodeTypeCTBN>
 *
 */
public class MCTNBC<NodeTypeBN extends Node, NodeTypeCTBN extends Node> extends MCTBNC<NodeTypeBN, NodeTypeCTBN> {

	/**
	 * Construct a multidimensional continuous time naive Bayes classifier given the
	 * learning algorithms for BNs and CTBNs.
	 * 
	 * @param bnLearningAlgs
	 * @param ctbnLearningAlgs
	 * @param bnNodeClass
	 * @param ctbnNodeClass
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
