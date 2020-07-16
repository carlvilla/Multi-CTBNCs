package com.cig.mctbnc.models;

import java.util.List;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.learning.parameters.ParameterLearningAlgorithm;
import com.cig.mctbnc.learning.structure.StructureLearningAlgorithm;
import com.cig.mctbnc.nodes.Node;

/**
 * Implements a continuous time Bayesian network classifier. This is a modified
 * version of the original where there can be more than one class variable.
 * 
 * @author Carlos Villa Blanco
 *
 * @param <N>
 *            type of the learned nodes (e.g. nodes that learn a CIM)
 */
public class CTBNC<N extends Node> extends AbstractPGM implements Classifier {
	private List<N> learnedNodes;
	private ParameterLearningAlgorithm parameterLearningAlg;
	private StructureLearningAlgorithm structureLearningAlg;
	private Dataset dataset;

	/**
	 * Initialize a continuous Time Bayesian network given a list of nodes, a
	 * dataset and the algorithms for parameter and structure learning. This
	 * constructor was thought to be used by the MCTBNC.
	 * 
	 * @param nodes
	 * @param dataset
	 * @param parameterLearningAlg
	 * @param structureLearningAlg
	 */
	public CTBNC(List<Node> nodes, Dataset dataset, ParameterLearningAlgorithm parameterLearningAlg,
			StructureLearningAlgorithm structureLearningAlg) {
		super(nodes);
		setParameterLearningAlgorithm(parameterLearningAlg);
		setStructureLearningAlgorithm(structureLearningAlg);
		this.dataset = dataset;
	}

	private void setParameterLearningAlgorithm(ParameterLearningAlgorithm parameterLearningAlg) {
		this.parameterLearningAlg = parameterLearningAlg;
	}

	private void setStructureLearningAlgorithm(StructureLearningAlgorithm structureLearningAlg) {
		this.structureLearningAlg = structureLearningAlg;

	}

	@Override
	public void learn() {
		structureLearningAlg.learn(this, dataset, parameterLearningAlg);
	}

	@Override
	public void setStructure(boolean[][] adjacencyMatrix) {
		// Current edges are removed
		for (Node node : nodes) {
			node.removeAllEdges();
		}
		for (int i = 0; i < adjacencyMatrix.length; i++) {
			Node node = nodeIndexer.getNodeByIndex(i);

			for (int j = 0; j < adjacencyMatrix.length; j++) {
				if (adjacencyMatrix[i][j]) {
					Node childNode = nodeIndexer.getNodeByIndex(j);
					node.setChild(childNode);
				}
			}
		}
		parameterLearningAlg.learn(nodes, dataset);
		this.learnedNodes = (List<N>) parameterLearningAlg.getParameters();
	}

	@Override
	public void display() {
		// TODO Auto-generated method stub

	}

	@Override
	public String[][] predict() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getType() {
		return "Continuous time Bayesian network";
	}

	@Override
	public boolean isStructureLegal(boolean[][] adjacencyMatrix) {
		// TODO Auto-generated method stub
		return true;
	}

}
