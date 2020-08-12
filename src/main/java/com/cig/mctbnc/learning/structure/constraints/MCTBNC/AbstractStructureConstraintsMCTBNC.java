package com.cig.mctbnc.learning.structure.constraints.MCTBNC;

/**
 * Contains common attributes and methods for classes with structure constrains
 * for MCTBNCs.
 * 
 * @author Carlos Villa Blanco
 *
 */
public abstract class AbstractStructureConstraintsMCTBNC implements StructureConstraintsMCTBNC {
	String penalizationFunction;

	@Override
	public void setPenalizationFunction(String penalizationFunction) {
		this.penalizationFunction = penalizationFunction;
	}

	@Override
	public String getPenalizationFunction() {
		return penalizationFunction;
	}

}
