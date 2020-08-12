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

	/**
	 * Establish the penalization function used for the structure complexity of the
	 * BNs and CTBNs.
	 * 
	 * @param penalizationFunction name of the penalization function
	 */
	public void setPenalizationFunction(String penalizationFunction);

	/**
	 * Return the penalization function that is used for the structure complexity of
	 * the BNs and CTBNs.
	 * 
	 * @return name of the penalization function
	 */
	String getPenalizationFunction();

}
