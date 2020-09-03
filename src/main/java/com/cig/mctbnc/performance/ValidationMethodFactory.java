package com.cig.mctbnc.performance;

import com.cig.mctbnc.data.reader.DatasetReader;

/**
 * Build validation methods.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class ValidationMethodFactory {

	/**
	 * Build the specified validation method.
	 * 
	 * @param nameValidationMethod
	 * @param dR
	 * @param shuffle
	 * @param trainingSize
	 * @param folds
	 * @return validation method
	 */
	public static ValidationMethod getValidationMethod(String nameValidationMethod, DatasetReader dR, boolean shuffle,
			double trainingSize, int folds) {
		ValidationMethod validationMethod;
		switch (nameValidationMethod) {
		case "Cross-validation":
			validationMethod = new CrossValidation(dR, folds, shuffle);
			break;
		default:
			// Hold-out validation
			validationMethod = new HoldOut(dR, trainingSize, shuffle);
		}
		return validationMethod;
	}

}
