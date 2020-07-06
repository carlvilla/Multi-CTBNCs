package main.java.com.cig.mctbnc.models;

import java.util.List;

public class DiscreteNode implements Node{
	
	private String name;
	private List<Node> children;
	private List<Node> parents;
	private String[] states;
	
	public DiscreteNode(String name, String[] states) {
		this.name = name;
		this.states = states;
	}
	
	public String[] getPossibleStates() {
		return states;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public List<Node> getChildren() {
		return children;
	}

	@Override
	public List<Node> getParents() {
		return parents;		
	}
	
	public String[] getStates(){
		return states;
	}

}
