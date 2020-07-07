package main.java.com.cig.mctbnc.models;

import java.util.List;

public interface Node {
	
	public int getIndex();
	public String getName();
	public List<Node> getChildren();
	public List<Node> getParents();
	public void setChild(Node nodeChildren);
	
}
