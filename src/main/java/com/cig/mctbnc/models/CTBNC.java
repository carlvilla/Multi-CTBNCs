package com.cig.mctbnc.models;

import java.util.List;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.learning.parameters.CTBNParameterLearning;
import com.cig.mctbnc.learning.structure.CTBNCStructureLearning;
import com.cig.mctbnc.nodes.Node;

/**
 * Implements a continuous time Bayesian network classifier. This is a modified version 
 * of the original where there can be more than one class variable.
 * 
 * @author Carlos Villa Blanco
 *
 * @param <T>
 */
public class CTBNC<T extends Node> extends AbstractPGM<T> {
	private List<T> learnedNodes;
	private CTBNParameterLearning ctbnParameterLearning;
	private CTBNCStructureLearning ctbnStructureLearning;
	private Dataset dataset;

	public CTBNC(List<Node> nodes) {
		super(nodes);
	}
	
	@Override
	public void learn() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setStructure(boolean[][] adjacencyMatrix) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public T[][] predict() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void sample() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void display() {
		// TODO Auto-generated method stub
		
	}

}
