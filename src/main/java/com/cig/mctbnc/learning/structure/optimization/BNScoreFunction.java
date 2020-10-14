package com.cig.mctbnc.learning.structure.optimization;

import com.cig.mctbnc.models.BN;
import com.cig.mctbnc.nodes.Node;

public interface BNScoreFunction {

	public double compute(BN<? extends Node> bn);

}
