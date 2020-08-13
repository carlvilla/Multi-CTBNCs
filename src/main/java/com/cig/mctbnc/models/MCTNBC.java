package com.cig.mctbnc.models;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.learning.parameters.ParameterLearningAlgorithm;
import com.cig.mctbnc.learning.structure.StructureLearningAlgorithm;
import com.cig.mctbnc.learning.structure.constraints.StructureConstraints;
import com.cig.mctbnc.learning.structure.constraints.BN.EmptyBN;
import com.cig.mctbnc.learning.structure.constraints.CTBNC.NaiveBayes;
import com.cig.mctbnc.nodes.Node;

/**
 * Specify the structure constraints of a multidimensional continuous time naive
 * Bayes classifier (MCTBNC) where any subgraph has arcs except the bridge
 * subgraph (fully naive multi-dimensional classifier).
 * 
 * @author Carlos Villa Blanco
 *
 */
public class MCTNBC<NodeTypeBN extends Node, NodeTypeCTBN extends Node> extends MCTBNC<NodeTypeBN, NodeTypeCTBN> {

	/**
	 * @param dataset
	 * @param ctbnParameterLearningAlgorithm
	 * @param ctbnStructureLearningAlgorithm
	 * @param bnParameterLearningAlgorithm
	 * @param bnStructureLearningAlgorithm
	 * @param bnNodeClass
	 * @param ctbnNodeClass
	 */
	public MCTNBC(Dataset dataset, ParameterLearningAlgorithm ctbnParameterLearningAlgorithm,
			StructureLearningAlgorithm ctbnStructureLearningAlgorithm,
			ParameterLearningAlgorithm bnParameterLearningAlgorithm,
			StructureLearningAlgorithm bnStructureLearningAlgorithm, Class<NodeTypeBN> bnNodeClass,
			Class<NodeTypeCTBN> ctbnNodeClass) {
		super(dataset, ctbnParameterLearningAlgorithm, ctbnStructureLearningAlgorithm, bnParameterLearningAlgorithm,
				bnStructureLearningAlgorithm, bnNodeClass, ctbnNodeClass);
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
	public void display() {
		Graph graph = new SingleGraph("MCTNBC");
		addNodes(graph, nodes);
		addEdges(graph, nodes);
		graph.display();
	}

}
