package es.upm.fi.cig.mctbnc.models.submodels;

import es.upm.fi.cig.mctbnc.learning.BNLearningAlgorithms;
import es.upm.fi.cig.mctbnc.learning.CTBNLearningAlgorithms;
import es.upm.fi.cig.mctbnc.learning.structure.constraints.StructureConstraints;
import es.upm.fi.cig.mctbnc.learning.structure.constraints.BN.EmptyBN;
import es.upm.fi.cig.mctbnc.models.MCTBNC;
import es.upm.fi.cig.mctbnc.nodes.Node;

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
		if (getNodesBN().size() == 1)
			return "Continuous time Bayesian network classifier";
		return "Empty-digraph multidimensional continuous time Bayesian network classifier";		
	}
	
	@Override
	public String getModelIdentifier() {
		return "Empty-digraph MCTBNC";
	}

}
