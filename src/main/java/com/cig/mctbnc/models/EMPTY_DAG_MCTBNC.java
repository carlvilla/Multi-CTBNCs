package com.cig.mctbnc.models;

import com.cig.mctbnc.learning.BNLearningAlgorithms;
import com.cig.mctbnc.learning.CTBNLearningAlgorithms;
import com.cig.mctbnc.learning.structure.constraints.StructureConstraints;
import com.cig.mctbnc.learning.structure.constraints.BN.EmptyBN;
import com.cig.mctbnc.nodes.Node;

public class EMPTY_DAG_MCTBNC<NodeTypeBN extends Node, NodeTypeCTBN extends Node>
		extends MCTBNC<NodeTypeBN, NodeTypeCTBN> {

	public EMPTY_DAG_MCTBNC(BNLearningAlgorithms bnLearningAlgs, CTBNLearningAlgorithms ctbnLearningAlgs,
			Class<NodeTypeBN> bnNodeClass, Class<NodeTypeCTBN> ctbnNodeClass) {
		super(bnLearningAlgs, ctbnLearningAlgs, bnNodeClass, ctbnNodeClass);
	}

	@Override
	public StructureConstraints getStructureConstraintsBN() {
		return new EmptyBN();
	}

	@Override
	public String getType() {
		return "Empty-DAG multidimensional continuous time Bayesian network classifier";
	}

}
