package com.cig.mctbnc.learning.structure.constraints.MCTBNC;

import com.cig.mctbnc.learning.structure.constraints.StructureConstraints;

/**
 * Interface to define classes with the structure restrictions of a MCTBNC. As a
 * MCTBNC is formed by a BN and a CTBN, the classes return the constraints for
 * the structures of these models.
 * 
 * @author Carlos Villa Blanco
 *
 */
public interface StructureConstraintsMCTBNC {

	/**
	 * Return the structure constraints for the BN.
	 * 
	 * @return StructureConstraint object
	 */
	public StructureConstraints getStructureConstraintsBN();

	/**
	 * Return the structure constraints for the CTBN.
	 * 
	 * @return StructureConstraint object
	 */
	public StructureConstraints getStructureConstraintsCTBN();

}
