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
public class MCTNBC extends AbstractStructureConstraintsMCTBNC {

	@Override
	public StructureConstraints getStructureConstraintsBN() {
		StructureConstraints structureConstrintsBN = new EmptyBN();
		structureConstrintsBN.setPenalizationFunction(getPenalizationFunction());
		return structureConstrintsBN;
	}

	@Override
	public StructureConstraints getStructureConstraintsCTBN() {
		StructureConstraints structureConstrintsCTBN = new NaiveBayes();
		structureConstrintsCTBN.setPenalizationFunction(getPenalizationFunction());
		return structureConstrintsCTBN;
	}

}
