package com.cig.mctbnc.learning.structure;

import com.cig.mctbnc.models.PGM;

public interface MCTBNCStructureLearning {

	public void getBestNeighbor();
	public void learn();
	public double evaluateStructure(PGM model); 
	
	
}
