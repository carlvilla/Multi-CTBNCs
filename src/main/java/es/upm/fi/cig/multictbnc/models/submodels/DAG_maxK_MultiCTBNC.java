package es.upm.fi.cig.multictbnc.models.submodels;

import java.util.Map;

import es.upm.fi.cig.multictbnc.learning.BNLearningAlgorithms;
import es.upm.fi.cig.multictbnc.learning.CTBNLearningAlgorithms;
import es.upm.fi.cig.multictbnc.learning.structure.constraints.StructureConstraints;
import es.upm.fi.cig.multictbnc.learning.structure.constraints.BN.DAG;
import es.upm.fi.cig.multictbnc.learning.structure.constraints.CTBNC.MaxKCTBNC;
import es.upm.fi.cig.multictbnc.models.MultiCTBNC;
import es.upm.fi.cig.multictbnc.nodes.Node;

/**
 * Implements a Multi-CTBNC where the class subgraph is formed by a Bayesian network,
 * while the feature subgraph is a K-dependence continuous time Bayesian
 * network, i.e., the feature nodes are limited to have K parents (apart of the
 * class variables).
 * 
 * @author Carlos Villa Blanco
 * @param <NodeTypeBN>   type of the nodes of the BN (class subgraph)
 * @param <NodeTypeCTBN> type of the nodes of the CTBN (feature subgraph)
 *
 */
public class DAG_maxK_MultiCTBNC<NodeTypeBN extends Node, NodeTypeCTBN extends Node>
		extends MultiCTBNC<NodeTypeBN, NodeTypeCTBN> {
	int maxK;

	/**
	 * Constructs a {@code DAG_maxK_MultiCTBNC} by receiving the learning algorithms for
	 * Bayesian networks and continuous time Bayesian networks and the maximum
	 * number of parents of the features (apart of the class variables).
	 * 
	 * @param bnLearningAlgs   parameter and structure learning algorithms for
	 *                         Bayesian networks
	 * @param ctbnlearningAlgs parameter and structure learning algorithms for
	 *                         continuous time Bayesian networks
	 * @param maxK             maximum number of parents the nodes of feature
	 *                         variables can have (without including nodes of class
	 *                         variables)
	 * @param bnNodeClass      Bayesian network node type
	 * @param ctbnNodeClass    continuous time Bayesian network node type
	 */
	public DAG_maxK_MultiCTBNC(BNLearningAlgorithms bnLearningAlgs, CTBNLearningAlgorithms ctbnlearningAlgs, int maxK,
			Class<NodeTypeBN> bnNodeClass, Class<NodeTypeCTBN> ctbnNodeClass) {
		super(bnLearningAlgs, ctbnlearningAlgs, bnNodeClass, ctbnNodeClass);
		this.maxK = maxK;
	}

	@Override
	public StructureConstraints getStructureConstraintsBN() {
		return new DAG();
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
		return String.format("DAG-max%d multidimensional continuous time Bayesian network classifier", this.maxK);
	}

	@Override
	public String getModelIdentifier() {
		return "DAG-maxK Multi-CTBNC";
	}

}
