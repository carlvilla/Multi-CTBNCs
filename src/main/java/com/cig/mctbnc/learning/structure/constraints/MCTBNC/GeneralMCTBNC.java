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
public class GeneralMCTBNC implements StructureConstraintsMCTBNC {

	@Override
	public StructureConstraints getStructureConstraintsBN() {
		return new GeneralDAG();
	}

	@Override
	public StructureConstraints getStructureConstraintsCTBN() {
		return new GeneralCTBNC();
	}

}
