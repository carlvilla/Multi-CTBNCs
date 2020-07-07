package main.java.com.cig.mctbnc.learning.parameters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import main.java.com.cig.mctbnc.models.BN;
import main.java.com.cig.mctbnc.models.Dataset;
import main.java.com.cig.mctbnc.models.Node;
import main.java.com.cig.mctbnc.models.Sequence;

public class BNParameterMLE implements BNParameterLearning {

	double[][] CPT;

	@Override
	public void learn(BN bn) {

		Dataset dataset = bn.getDataset();
		double[][][] ss = sufficientStatistics(bn, dataset);

	}

	public double[][] getCPT() {
		return null;

	}

	public double[][][] sufficientStatistics(BN bn, Dataset dataset) {
		
		int[][][] N;
		
		int numVariables = bn.getNumNodes();
		String[] nameVariables = dataset.getNameClassVariables();
		String[][] valuesVariables = dataset.getValuesClassVariables();
		
		
		for(int i=0; i<numVariables;i++) {
			String nameVariable = nameVariables[i];
			Node node = bn.getNodeByName(nameVariable);
			List<Node> parents = node.getParents();
			String[] nameParents = parents.stream().map(Node::getName).toArray(String[]::new);
			String[] statesVariable = dataset.getStatesVariable(nameVariable);
			String[][] statesParents = dataset.getStatesVariables(nameParents);
			
			for(int j=0;j<statesParents.length;j++)
				for(int k=0;k<statesVariable.length;k++) {
					// Get number of times variable "nameVariable" has value statesVariable[k]
					// while its parents have values statesParents[j]
					String valueVariable = statesVariable[k];
					String[] valueParents = statesParents[j];
					
					// Create hashmap to query the dataset
					Map<String, String> query = new HashMap<String, String>();
					query.put(nameVariable, valueVariable);	
					for(int z=0;z<valueParents.length;z++)
						query.put(nameParents[z], valueParents[z]);
					
					N[i,j,k] = dataset.getNumOccurrences(query);
					
					
					for(String valueParent:valueParents)
						
					
					
				}
				
				
	
			
			
			
		}
		
		for(String classVariable:nameClassVariables) {
			Node node = bn.getNodeByName(classVariable);
			statesStudiedVariable = dataset.getStatesVariable(classVariable);
			for(String i:statesStudiedVariable) {
				List<Node> parents = node.getParents();
				
			}
			
			classVariable
			
			nijk
			
			
			ss[i,j,k]
		}
		
		num dataset[0].length; 
		
		
		dataset.getStatesVariable()
		
	}

}
