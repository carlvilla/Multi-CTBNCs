package es.upm.fi.cig.mctbnc.performance;

import es.upm.fi.cig.mctbnc.data.reader.DatasetReader;
import es.upm.fi.cig.mctbnc.exceptions.UnreadDatasetException;

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
	 * @param trainingSize
	 * @param folds
	 * @param estimateProbabilities
	 * @param shuffle
	 * @param seed
	 * @return a {@code ValidationMethod}
	 * @throws UnreadDatasetException
	 */
	public static ValidationMethod getValidationMethod(String nameValidationMethod, DatasetReader dR,
			double trainingSize, int folds, boolean estimateProbabilities, boolean shuffle, Long seed)
			throws UnreadDatasetException {
		ValidationMethod validationMethod;
		switch (nameValidationMethod) {
		case "Cross-validation":
			validationMethod = new CrossValidation(dR, folds, estimateProbabilities, shuffle, seed);
			break;
		default:
			// Hold-out validation
			validationMethod = new HoldOut(dR, trainingSize, estimateProbabilities, shuffle, seed);
		}
		return validationMethod;
	}

}
