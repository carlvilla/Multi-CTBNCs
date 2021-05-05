package es.upm.fi.cig.mctbnc.models.submodels;

import java.util.Map;

import es.upm.fi.cig.mctbnc.learning.BNLearningAlgorithms;
import es.upm.fi.cig.mctbnc.learning.CTBNLearningAlgorithms;
import es.upm.fi.cig.mctbnc.learning.structure.constraints.StructureConstraints;
import es.upm.fi.cig.mctbnc.learning.structure.constraints.BN.EmptyBN;
import es.upm.fi.cig.mctbnc.learning.structure.constraints.CTBNC.MaxKCTBNC;
import es.upm.fi.cig.mctbnc.models.MCTBNC;
import es.upm.fi.cig.mctbnc.nodes.Node;

/**
 * Implements a MCTBNC with empty class subgraph and a K-dependence continuous
 * time Bayesian network for the feature subgraph, i.e., the feature nodes are
 * limited to have K parents (apart of the class variables).
 * 
 * @author Carlos Villa Blanco
 *
 * @param <NodeTypeBN>
 * @param <NodeTypeCTBN>
 */
public class Empty_maxK_MCTBNC<NodeTypeBN extends Node, NodeTypeCTBN extends Node>
		extends MCTBNC<NodeTypeBN, NodeTypeCTBN> {
	int maxK;

	/**
	 * Constructs a {@code Empty_maxK_MCTBNC} by receiving the learning algorithms
	 * for Bayesian networks and continuous time Bayesian networks and the maximum
	 * number of parents of the features (apart of the class variables).
	 * 
	 * @param bnLearningAlgs
	 * @param ctbnLearningAlgs
	 * @param maxK
	 * @param bnNodeClass
	 * @param ctbnNodeClass
	 */
	public Empty_maxK_MCTBNC(BNLearningAlgorithms bnLearningAlgs, CTBNLearningAlgorithms ctbnLearningAlgs, int maxK,
			Class<NodeTypeBN> bnNodeClass, Class<NodeTypeCTBN> ctbnNodeClass) {
		super(bnLearningAlgs, ctbnLearningAlgs, bnNodeClass, ctbnNodeClass);
		this.maxK = maxK;
	}

	@Override
	public StructureConstraints getStructureConstraintsBN() {
		return new EmptyBN();
	}

	@Override
	public StructureConstraints getStructureConstraintsCTBN() {
		return new MaxKCTBNC(this.maxK);
	}

	@Override
	public Map<String, String> getHyperparameters() {
		return Map.of("maxK", String.valueOf(this.maxK));
	}

	@Override
	public String getType() {
		if (getNodesBN().size() == 1)
			return String.format("Max%d continuous time Bayesian network classifier", this.maxK);
		return String.format("Empty-max%d multidimensional continuous time Bayesian network classifier", this.maxK);
	}

	@Override
	public String getModelIdentifier() {
		return "Empty-maxK MCTBNC";
	}

}
