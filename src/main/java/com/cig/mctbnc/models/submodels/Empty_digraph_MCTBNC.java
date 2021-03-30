package com.cig.mctbnc.models.submodels;

import com.cig.mctbnc.learning.BNLearningAlgorithms;
import com.cig.mctbnc.learning.CTBNLearningAlgorithms;
import com.cig.mctbnc.learning.structure.constraints.StructureConstraints;
import com.cig.mctbnc.learning.structure.constraints.BN.EmptyBN;
import com.cig.mctbnc.models.MCTBNC;
import com.cig.mctbnc.nodes.Node;

/**
 * Implements a MCTBNC with empty class subgraph.
 * 
 * @author Carlos Villa Blanco
 *
 * @param <NodeTypeBN>
 * @param <NodeTypeCTBN>
 */
public class Empty_digraph_MCTBNC<NodeTypeBN extends Node, NodeTypeCTBN extends Node>
		extends MCTBNC<NodeTypeBN, NodeTypeCTBN> {

	/**
	 * Constructs a {@code Empty_digraph_MCTBNC} by receiving the learning
	 * algorithms for Bayesian networks and continuous time Bayesian networks.
	 * 
	 * @param bnLearningAlgs
	 * @param ctbnLearningAlgs
	 * @param bnNodeClass
	 * @param ctbnNodeClass
	 */
	public Empty_digraph_MCTBNC(BNLearningAlgorithms bnLearningAlgs, CTBNLearningAlgorithms ctbnLearningAlgs,
			Class<NodeTypeBN> bnNodeClass, Class<NodeTypeCTBN> ctbnNodeClass) {
		super(bnLearningAlgs, ctbnLearningAlgs, bnNodeClass, ctbnNodeClass);
	}

	@Override
	public StructureConstraints getStructureConstraintsBN() {
		return new EmptyBN();
	}

	@Override
	public String getType() {
		return "Empty-digraph multidimensional continuous time Bayesian network classifier";
	}
	
	@Override
	public String getModelIdentifier() {
		return "Empty-digraph MCTBNC";
	}

}
