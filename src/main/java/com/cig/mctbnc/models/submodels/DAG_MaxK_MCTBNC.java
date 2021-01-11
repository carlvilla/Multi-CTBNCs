package com.cig.mctbnc.models.submodels;

import com.cig.mctbnc.learning.BNLearningAlgorithms;
import com.cig.mctbnc.learning.CTBNLearningAlgorithms;
import com.cig.mctbnc.learning.structure.constraints.StructureConstraints;
import com.cig.mctbnc.learning.structure.constraints.BN.DAG;
import com.cig.mctbnc.learning.structure.constraints.CTBNC.MaxKCTBNC;
import com.cig.mctbnc.models.MCTBNC;
import com.cig.mctbnc.nodes.Node;

/**
 * Implements a MCTBNC where the class subgraph is formed by a Bayesian network,
 * while the feature subgraph is a K-dependence continuous time Bayesian
 * network. In other words, the feature nodes are limited to have K parents
 * (apart of the class variables).
 * 
 * @author Carlos Villa Blanco
 * @param <NodeTypeBN>
 * @param <NodeTypeCTBN>
 *
 */
public class DAG_MaxK_MCTBNC<NodeTypeBN extends Node, NodeTypeCTBN extends Node>
		extends MCTBNC<NodeTypeBN, NodeTypeCTBN> {
	int maxK;

	public DAG_MaxK_MCTBNC(BNLearningAlgorithms bnLearningAlgs, CTBNLearningAlgorithms ctbnlearningAlgs, int maxK,
			Class bnNodeClass, Class ctbnNodeClass) {
		super(bnLearningAlgs, ctbnlearningAlgs, bnNodeClass, ctbnNodeClass);
		this.maxK = maxK;
	}

	@Override
	public StructureConstraints getStructureConstraintsBN() {
		return new DAG();
	}

	@Override
	public StructureConstraints getStructureConstraintsCTBN() {
		return new MaxKCTBNC(maxK);
	}

	@Override
	public String getType() {
		return "DAG-Max" + maxK + " MCTBNC";
	}

}
