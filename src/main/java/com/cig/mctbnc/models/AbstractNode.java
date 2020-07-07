package main.java.com.cig.mctbnc.models;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractNode implements Node {
	private int index;
	private String name;
	private List<Node> children;
	private List<Node> parents;
	
	public AbstractNode(int index, String name) {
		this.index = index;
		this.name = name;
		children = new ArrayList<Node>();
		parents = new ArrayList<Node>();
	}
	
	@Override
	public int getIndex() {
		return index;
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
	
	@Override
	public void setChild(Node nodeChild) {
		children.add(nodeChild);
		
	}
	
}
