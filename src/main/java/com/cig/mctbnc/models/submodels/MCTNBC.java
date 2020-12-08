package com.cig.mctbnc.models.submodels;

import com.cig.mctbnc.learning.BNLearningAlgorithms;
import com.cig.mctbnc.learning.CTBNLearningAlgorithms;
import com.cig.mctbnc.learning.structure.constraints.StructureConstraints;
import com.cig.mctbnc.learning.structure.constraints.BN.EmptyBN;
import com.cig.mctbnc.learning.structure.constraints.CTBNC.NaiveBayes;
import com.cig.mctbnc.models.MCTBNC;
import com.cig.mctbnc.nodes.Node;

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
		return "Multidimensional continuous time naive Bayes classifier";
	}

}
