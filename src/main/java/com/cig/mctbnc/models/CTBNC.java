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
 * @param <T>
 */
public class CTBNC<T extends Node> extends AbstractPGM implements Classifier {
	private List<T> learnedNodes;
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
	}

	@Override
	public void learn() {
		structureLearningAlg.learn(this, dataset, parameterLearningAlg);
	}

	@Override
	public void setStructure(boolean[][] adjacencyMatrix) {
		// TODO Auto-generated method stub

	}

	@Override
	public void display() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean[][] getAdjacencyMatrix() {
		// TODO Auto-generated method stub
		return null;
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
		return false;
	}

}
