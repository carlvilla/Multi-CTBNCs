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
     * @param nameValidationMethod  name of validation method
     * @param datasetReader         a {@code DatasetReader} to read the dataset from the "Dataset" Tab.
     * @param testDatasetReader     a {@code DatasetReader} to read a test dataset
     * @param trainingSize          size of the training dataset (percentage)
     * @param folds                 number of folds
     * @param estimateProbabilities true to estimate the probabilities of the class configurations, false otherwise
     * @param shuffle               true to shuffle the sequences, false otherwise
     * @param seed                  seed used to shuffle the sequences
     * @return a {@code ValidationMethod}
     */
    public static ValidationMethod getValidationMethod(String nameValidationMethod, DatasetReader datasetReader,
                                                       DatasetReader testDatasetReader, double trainingSize, int folds,
                                                       boolean estimateProbabilities, boolean shuffle, Long seed) {
        ValidationMethod validationMethod;
        switch (nameValidationMethod) {
            case "Cross-validation":
                validationMethod = new CrossValidationMethod(datasetReader, folds, estimateProbabilities, shuffle,
                        seed);
                break;
            case "Test dataset":
                validationMethod = new TestDatasetMethod(datasetReader, testDatasetReader, estimateProbabilities,
                        shuffle, seed);
                break;
            default:
                // Hold-out validation
                validationMethod = new HoldOutMethod(datasetReader, trainingSize, estimateProbabilities, shuffle,
                        seed);
        }
        return validationMethod;
    }

}