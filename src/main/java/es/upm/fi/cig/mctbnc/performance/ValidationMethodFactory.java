package es.upm.fi.cig.mctbnc.performance;

import es.upm.fi.cig.mctbnc.data.reader.DatasetReader;
import es.upm.fi.cig.mctbnc.exceptions.UnreadDatasetException;

/**
 * Builds validation methods.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class ValidationMethodFactory {

	/**
	 * Builds the specified validation method.
	 * 
	 * @param nameValidationMethod  name of validation method
	 * @param datasetReader         a {@code DatasetReader} to read the dataset
	 * @param trainingSize          size of the training dataset (percentage)
	 * @param folds                 number of folds
	 * @param estimateProbabilities true to estimate the probabilities of the class
	 *                              configurations, false otherwise
	 * @param shuffle               true to shuffle the sequences, false otherwise
	 * @param seed                  seed used to shuffle the sequences
	 * @return a {@code ValidationMethod}
	 * @throws UnreadDatasetException if the provided dataset could not be read
	 */
	public static ValidationMethod getValidationMethod(String nameValidationMethod, DatasetReader datasetReader,
			double trainingSize, int folds, boolean estimateProbabilities, boolean shuffle, Long seed)
			throws UnreadDatasetException {
		ValidationMethod validationMethod;
		switch (nameValidationMethod) {
		case "Cross-validation":
			validationMethod = new CrossValidation(datasetReader, folds, estimateProbabilities, shuffle, seed);
			break;
		default:
			// Hold-out validation
			validationMethod = new HoldOut(datasetReader, trainingSize, estimateProbabilities, shuffle, seed);
		}
		return validationMethod;
	}

}
