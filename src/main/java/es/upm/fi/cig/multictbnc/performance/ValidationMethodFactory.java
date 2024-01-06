package es.upm.fi.cig.multictbnc.performance;

import es.upm.fi.cig.multictbnc.data.reader.DatasetReader;
import es.upm.fi.cig.multictbnc.exceptions.UnreadDatasetException;

/**
 * Builds validation methods.
 *
 * @author Carlos Villa Blanco
 */
public class ValidationMethodFactory {

    /**
     * Builds the specified validation method.
     *
     * @param nameValidationMethod  name of the validation method
     * @param datasetReader         a {@code DatasetReader} to read the dataset
     * @param testDatasetReader     a {@code DatasetReader} to read a test dataset
     * @param trainingSize          size of the training dataset (percentage)
     * @param folds                 number of folds
     * @param estimateProbabilities {@code true} to estimate the probabilities of the class configurations,
     *                              {@code false} otherwise
     * @param shuffle               {@code true} to shuffle the sequences, {@code false} otherwise
     * @param seed                  seed used to shuffle the sequences
     * @return a {@code ValidationMethod}
     * @throws UnreadDatasetException if a provided dataset could not be read
     */
    public static ValidationMethod getValidationMethod(String nameValidationMethod, DatasetReader datasetReader,
                                                       DatasetReader testDatasetReader, double trainingSize, int folds,
                                                       boolean estimateProbabilities, boolean shuffle, Long seed)
            throws UnreadDatasetException {
        switch (nameValidationMethod) {
            case "Cross-validation":
                return new CrossValidationMethod(datasetReader, folds, estimateProbabilities, shuffle, seed);
            case "Binary relevance cross-validation":
                return new CrossValidationBinaryRelevanceMethod(datasetReader, folds, estimateProbabilities, shuffle,
                        seed);
            case "Test dataset":
                return new TestDatasetMethod(datasetReader, testDatasetReader, estimateProbabilities);
            default:
                // Hold-out
                return new HoldOutMethod(datasetReader, trainingSize, estimateProbabilities, shuffle, seed);
        }
    }

}