package com.cig.mctbnc.models;

import java.util.List;

import com.cig.mctbnc.nodes.Node;

public class CTBN<T extends Node> extends AbstractPGM<T> {

	public CTBN(List<Node> nodes) {
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
