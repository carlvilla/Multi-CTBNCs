package com.cig.mctbnc.models;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.learning.parameters.ParameterLearningAlgorithm;
import com.cig.mctbnc.learning.structure.HillClimbing;
import com.cig.mctbnc.learning.structure.StructureLearningAlgorithm;
import com.cig.mctbnc.nodes.CIMNode;
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
	static Logger logger = LogManager.getLogger(CTBNC.class);

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
		Graph graph = new SingleGraph("CTBN");
		addNodes(graph, nodes);
		addEdges(graph, nodes);
		graph.display();
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
		// The class variables cannot have parents
		int numNodes = adjacencyMatrix.length;
		for (int i = 0; i < numNodes; i++) {
			for (int j = 0; j < numNodes; j++) {
				// If there is an arc
				if (adjacencyMatrix[i][j]) {
					// If the arc is from a feature to a class variable, the structure is illegal
					if (!getNodeByIndex(i).isClassVariable() && getNodeByIndex(j).isClassVariable()) {
						logger.debug("Illegal structure");
						return false;
					}
				}
			}
		}
		return true;
	}

	public List<N> getLearnedNodes() {
		return learnedNodes;
	}

}
