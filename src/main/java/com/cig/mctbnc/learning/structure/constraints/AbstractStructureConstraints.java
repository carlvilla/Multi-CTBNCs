package com.cig.mctbnc.learning.structure.constraints;

/**
 * Contains common attributes and methods for classes that determine the
 * structure constraints of PFG.
 * 
 * @author Carlos Villa Blanco
 *
 */
public abstract class AbstractStructureConstraints implements StructureConstraints {
	private String penalizationFunction;

	@Override
	public void setPenalizationFunction(String penalizationFunction) {
		this.penalizationFunction = penalizationFunction;
	}

	@Override
	public String getPenalizationFunction() {
		return penalizationFunction;
	}

}
