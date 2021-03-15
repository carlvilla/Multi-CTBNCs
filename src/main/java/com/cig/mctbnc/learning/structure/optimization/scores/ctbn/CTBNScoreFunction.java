package com.cig.mctbnc.learning.structure.optimization.scores.ctbn;

import com.cig.mctbnc.models.CTBN;
import com.cig.mctbnc.nodes.Node;

public interface CTBNScoreFunction {

	public double compute(CTBN<? extends Node> ctbn);

	public double compute(CTBN<? extends Node> ctbn, int nodeIndex);
	
	public boolean isDecomposable();

}
