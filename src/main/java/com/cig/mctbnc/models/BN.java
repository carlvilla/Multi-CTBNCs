package main.java.com.cig.mctbnc.models;

import java.util.List;

import main.java.com.cig.mctbnc.learning.parameters.BNParameterLearning;
import main.java.com.cig.mctbnc.learning.structure.BNStructureLearning;

public class BN extends AbstractPGM {
	
	private List<Node> nodes;
	private BNStructureLearning bnStructureLearning;
	private BNParameterLearning bnParameterLearning;
	private Dataset dataset;
	
	public BN(List<Node> nodes, BNStructureLearning bnStructureLearning,
			BNParameterLearning bnParameterLearning, Dataset dataset) {
		this.nodes = nodes;
		this.bnStructureLearning = bnStructureLearning;
		this.bnParameterLearning = bnParameterLearning;
		this.dataset = dataset;
	}

	@Override
	public void learn() {
		bnStructureLearning.learn(this, bnParameterLearning, dataset);
	}
	
	public List<Node> getNodes(){
		return nodes;
	}
	
	public int getNumNodes() {
		return nodes.size();
	}
	
	/**
	 * Obtain the node with certain index
	 * @param index
	 * @return
	 */
	public Node getNodeByName(String name) {		
		Node selectedNode = nodes.stream()
				  .filter(node -> node.getName().equals(name))
				  .findAny()
				  .orElse(null);
		return selectedNode;
	}
	
	public Dataset getDataset() {
		return dataset;
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
	public Object[][] predict() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void sample() {
		// TODO Auto-generated method stub
		
	}

}
