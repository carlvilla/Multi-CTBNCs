package es.upm.fi.cig.multictbnc.models.submodels;

import es.upm.fi.cig.multictbnc.learning.BNLearningAlgorithms;
import es.upm.fi.cig.multictbnc.learning.CTBNLearningAlgorithms;
import es.upm.fi.cig.multictbnc.learning.structure.constraints.BN.EmptyBN;
import es.upm.fi.cig.multictbnc.learning.structure.constraints.CTBNC.MaxKCTBNC;
import es.upm.fi.cig.multictbnc.learning.structure.constraints.StructureConstraints;
import es.upm.fi.cig.multictbnc.models.MultiCTBNC;
import es.upm.fi.cig.multictbnc.nodes.Node;

import java.util.Map;

/**
 * Implements a Multi-CTBNC with an empty class subgraph and a K-dependence continuous-time Bayesian network for the
 * feature subgraph, i.e., the feature nodes are limited to having K parents (apart of the class variables).
 *
 * @param <NodeTypeBN>   type of the nodes of the BN (class subgraph)
 * @param <NodeTypeCTBN> type of the nodes of the CTBN (feature subgraph)
 * @author Carlos Villa Blanco
 */
public class Empty_maxK_MultiCTBNC<NodeTypeBN extends Node, NodeTypeCTBN extends Node> extends
		MultiCTBNC<NodeTypeBN, NodeTypeCTBN> {
	int maxK;

	/**
	 * Constructs a {@code Empty_maxK_Multi-CTBNC} by receiving the learning algorithms for Bayesian networks and
	 * continuous-time Bayesian networks and the maximum number of parents of the features (apart of the class
	 * variables).
	 *
	 * @param bnLearningAlgs   parameter and structure learning algorithms for Bayesian networks
	 * @param ctbnLearningAlgs parameter and structure learning algorithms for continuous-time Bayesian networks
	 * @param maxK             maximum number of parents the nodes of feature variables can have (without including
	 *                         nodes of class variables)
	 * @param bnNodeClass      Bayesian network node type
	 * @param ctbnNodeClass    continuous-time Bayesian network node type
	 */
	public Empty_maxK_MultiCTBNC(BNLearningAlgorithms bnLearningAlgs, CTBNLearningAlgorithms ctbnLearningAlgs,
								 int maxK,
								 Class<NodeTypeBN> bnNodeClass, Class<NodeTypeCTBN> ctbnNodeClass) {
		super(bnLearningAlgs, ctbnLearningAlgs, bnNodeClass, ctbnNodeClass);
		this.maxK = maxK;
	}

	@Override
	public Map<String, String> getHyperparameters() {
		return Map.of("maxK", String.valueOf(this.maxK));
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
	public String getModelIdentifier() {
		return "Empty-maxK Multi-CTBNC";
	}

	@Override
	public String getType() {
		if (getNodesClassVariables().size() == 1)
			return String.format("Max%d continuous-time Bayesian network classifier", this.maxK);
		return String.format("Empty-max%d multidimensional continuous-time Bayesian network classifier", this.maxK);
	}

}