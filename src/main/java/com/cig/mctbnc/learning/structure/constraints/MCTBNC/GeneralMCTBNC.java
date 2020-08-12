package com.cig.mctbnc.learning.structure.constraints.MCTBNC;

import com.cig.mctbnc.learning.structure.constraints.StructureConstraints;
import com.cig.mctbnc.learning.structure.constraints.BN.GeneralDAG;
import com.cig.mctbnc.learning.structure.constraints.CTBNC.GeneralCTBNC;

/**
 * Returns the structure constraint for BNs and CTBNCs that are used by MCTBNC
 * without any special constraint in its structure.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class GeneralMCTBNC extends AbstractStructureConstraintsMCTBNC {

	@Override
	public StructureConstraints getStructureConstraintsBN() {
		StructureConstraints structureConstrintsBN = new GeneralDAG();
		structureConstrintsBN.setPenalizationFunction(getPenalizationFunction());
		return structureConstrintsBN;
	}

	@Override
	public StructureConstraints getStructureConstraintsCTBN() {
		StructureConstraints structureConstrintsCTBN = new GeneralCTBNC();
		structureConstrintsCTBN.setPenalizationFunction(getPenalizationFunction());
		return structureConstrintsCTBN;
	}

}
