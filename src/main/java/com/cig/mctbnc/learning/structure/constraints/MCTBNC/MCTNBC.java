package com.cig.mctbnc.learning.structure.constraints.MCTBNC;

import com.cig.mctbnc.learning.structure.constraints.StructureConstraints;
import com.cig.mctbnc.learning.structure.constraints.BN.EmptyBN;
import com.cig.mctbnc.learning.structure.constraints.CTBNC.NaiveBayes;

/**
 * Specify the restrictions of a multi-dimensional continuous time naive Bayes
 * classifier (MCTBNC) where any subgraph has arcs except the bridge subgraph.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class MCTNBC implements StructureConstraintsMCTBNC {

	@Override
	public StructureConstraints getStructureConstraintsBN() {
		return new EmptyBN();
	}

	@Override
	public StructureConstraints getStructureConstraintsCTBN() {
		return new NaiveBayes();
	}

}
