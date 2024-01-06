package es.upm.fi.cig.multictbnc.performance;

import es.upm.fi.cig.multictbnc.classification.Prediction;
import es.upm.fi.cig.multictbnc.data.reader.DatasetReader;
import es.upm.fi.cig.multictbnc.data.representation.Dataset;
import es.upm.fi.cig.multictbnc.data.representation.Sequence;
import es.upm.fi.cig.multictbnc.exceptions.ErroneousValueException;
import es.upm.fi.cig.multictbnc.exceptions.UnreadDatasetException;
import es.upm.fi.cig.multictbnc.models.MultiCTBNC;
import es.upm.fi.cig.multictbnc.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class allows specifying different training and test datasets.
 *
 * @author Carlos Villa Blanco
 */
public class TestDatasetMethod extends ValidationMethod {
    private final Logger logger = LogManager.getLogger(TestDatasetMethod.class);
    private DatasetReader trainingDatasetReader;
    private DatasetReader testDatasetReader;
    private boolean estimateProbabilities;

    /**
     * Constructor that receives two {@code DatasetReader} for the training and test datasets, whether the test dataset
     * should be shuffled, a seed for shuffling and whether the probabilities of each class configuration should be
     * estimated during the testing.
     *
     * @param trainingDatasetReader {@code DatasetReader} for the training dataset
     * @param testDatasetReader     {@code DatasetReader} for the test dataset
     * @param estimateProbabilities {@code true} if the probabilities of class configurations are estimated,
     *                              {@code false} otherwise
     */
    public TestDatasetMethod(DatasetReader trainingDatasetReader, DatasetReader testDatasetReader,
                             boolean estimateProbabilities) {
        this.trainingDatasetReader = trainingDatasetReader;
        this.testDatasetReader = testDatasetReader;
        this.testDatasetReader.removeZeroVarianceVariables(false);
        this.estimateProbabilities = estimateProbabilities;
    }

    /**
     * Constructor that receives a {@code DatasetReader} for the test dataset, whether the dataset should be
     * shuffled, a
     * seed for shuffling and whether the probabilities of each class configuration should be estimated during the
     * testing.
     *
     * @param testDatasetReader     {@code DatasetReader} for the test dataset
     * @param estimateProbabilities {@code true} if the probabilities of class configurations are estimated,
     *                              {@code false} otherwise
     */
    public TestDatasetMethod(DatasetReader testDatasetReader, boolean estimateProbabilities) {
        this.testDatasetReader = testDatasetReader;
        this.testDatasetReader.removeZeroVarianceVariables(false);
        this.estimateProbabilities = estimateProbabilities;
    }

    @Override
    public Map<String, Double> evaluate(MultiCTBNC<?, ?> model) throws UnreadDatasetException,
            ErroneousValueException {
        // If not training dataset is supplied, the model is assumed to be already trained
        if (this.trainingDatasetReader != null) {
            // Retrieve selected training dataset
            Dataset trainingDataset = this.trainingDatasetReader.readDataset();
            // Train model
            model.learn(trainingDataset);
        }
        // Retrieve selected test dataset
        Dataset testDataset = this.testDatasetReader.readDataset();
        // Make predictions with the model
        Prediction[] predictions = model.predict(testDataset, this.estimateProbabilities);
        if (Util.isArrayEmpty(predictions))
            throw new ErroneousValueException("Any sequence of the test dataset could be predicted.");
        // Evaluate the performance of the model
        Map<String, Double> results = Metrics.evaluate(predictions, testDataset);
        // Display results
        displayResultsHoldOut(model, results);
        return results;
    }

    @Override
    public Map<String, Double> evaluate(MultiCTBNC<?, ?> model, double preprocessingExecutionTime)
            throws UnreadDatasetException, ErroneousValueException {
        // Retrieve selected training dataset
        Dataset trainingDataset = this.trainingDatasetReader.readDataset();
        // Train model
        model.learn(trainingDataset);
        // Retrieve selected test dataset
        Dataset testDataset = this.testDatasetReader.readDataset();
        // Make predictions with the model
        Prediction[] predictions = model.predict(testDataset, this.estimateProbabilities);
        if (Util.isArrayEmpty(predictions))
            throw new ErroneousValueException("Any sequence of the test dataset could be predicted.");
        // Evaluate the performance of the model
        Map<String, Double> results = Metrics.evaluate(predictions, testDataset);
        logger.info("Adding execution time ({}) to the final learning time", preprocessingExecutionTime);
        results.computeIfPresent("Learning time", (k, v) -> v + preprocessingExecutionTime);
        // Display results
        displayResultsHoldOut(model, results);
        return results;
    }

    private void displayResultsHoldOut(MultiCTBNC<?, ?> model, Map<String, Double> results) {
        System.out.println("--------------------------Results hold-out validation--------------------------");
        displayResults(results);
        displayModel(model);
        System.out.println("-------------------------------------------------------------------------------");
    }

}