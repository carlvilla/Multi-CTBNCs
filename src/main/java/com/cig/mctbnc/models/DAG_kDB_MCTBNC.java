package com.cig.mctbnc.models;

import com.cig.mctbnc.learning.BNLearningAlgorithms;
import com.cig.mctbnc.learning.CTBNLearningAlgorithms;
import com.cig.mctbnc.learning.structure.constraints.StructureConstraints;
import com.cig.mctbnc.learning.structure.constraints.BN.DAG;
import com.cig.mctbnc.learning.structure.constraints.CTBNC.MaxKCTBNC;
import com.cig.mctbnc.nodes.Node;

/**
 * Implements a KDependenceMCTBNC
 * 
 * @author Carlos Villa Blanco
 * @param <NodeTypeBN> 
 * @param <NodeTypeCTBN> 
 *
 */
public class DAG_kDB_MCTBNC<NodeTypeBN extends Node, NodeTypeCTBN extends Node> extends MCTBNC<NodeTypeBN, NodeTypeCTBN>  {
	int maxK;

	public DAG_kDB_MCTBNC(BNLearningAlgorithms bnLearningAlgs, CTBNLearningAlgorithms ctbnlearningAlgs, int maxK,
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
		return "Max-" + maxK + " multidimensional continuous time Bayesian network classifier";
	}

}
