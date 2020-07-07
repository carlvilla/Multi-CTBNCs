package main.java.com.cig.mctbnc.models;

public class DiscreteNode extends AbstractNode{
	
	private String[] states;
	
	public DiscreteNode(int index, String name, String[] states) {
		super(index, name);
		this.states = states;
	}
	
	public String[] getPossibleStates() {
		return states;
	}
	
	public String[] getStates(){
		return states;
	}

}
