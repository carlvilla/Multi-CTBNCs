package es.upm.fi.cig.multictbnc.experiments.implementationsexperiments.featurestreamexperiments;

import es.upm.fi.cig.multictbnc.data.reader.DatasetReader;
import es.upm.fi.cig.multictbnc.data.representation.Dataset;
import es.upm.fi.cig.multictbnc.exceptions.ErroneousValueException;
import es.upm.fi.cig.multictbnc.exceptions.UnreadDatasetException;
import es.upm.fi.cig.multictbnc.models.MultiCTBNC;
import es.upm.fi.cig.multictbnc.nodes.CIMNode;
import es.upm.fi.cig.multictbnc.nodes.CPTNode;
import es.upm.fi.cig.multictbnc.performance.TestDatasetMethod;
import es.upm.fi.cig.multictbnc.performance.ValidationMethod;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents an experiment for processing a feature stream dataset without online feature subset selection but with
 * model updates.
 *
 * @author Carlos Villa Blanco
 */
public class FeatureStreamWithoutFSSExperiment extends FeatureStreamImplementationExperiment {

    /**
     * Initializes a FeatureStreamWithoutFSSExperiment with the provided configuration parameters.
     *
     * @param pathFeatureStream    path to the feature stream
     * @param nameTimeVariable     name of the time variable
     * @param nameClassVariables   list of names of class variables
     * @param initialDatasetReader DatasetReader for the initial dataset
     * @param testDatasetReader    DatasetReader for the test dataset
     * @param experimentNumber     number identifying the experiment
     * @param numFeatures          number of features in the dataset (used to identify results files)
     * @param areModelsValidated   boolean indicating whether the models should be validated
     * @throws UnreadDatasetException if there is an issue reading the dataset
     * @throws IOException            if there is an I/O error
     */
    public FeatureStreamWithoutFSSExperiment(String pathFeatureStream, String nameTimeVariable, List<String> nameClassVariables, DatasetReader initialDatasetReader, DatasetReader testDatasetReader, int experimentNumber, int numFeatures, boolean areModelsValidated) throws UnreadDatasetException, IOException {
        super(pathFeatureStream, nameTimeVariable, nameClassVariables, initialDatasetReader, testDatasetReader, experimentNumber, numFeatures, areModelsValidated);
    }

    @Override
    public String getFilenameResults() {
        return "NO_FSS";
    }

    @Override
    public void execute(MultiCTBNC<CPTNode, CIMNode> model) throws IOException {
        logger.info("Iterating over feature stream...");
        List<Map<String, Double>> resultsBatch = new ArrayList<>();
        double fssExecutionTime = 0;
        double timeIncludeFeatureToModelSeconds = 0;
        try {
            while (featureStreamReader.isDataArriving())
                timeIncludeFeatureToModelSeconds += processNewFeatrueVariable(model);
            if (this.areModelsValidated)
                validateFinalModel(model, resultsBatch, fssExecutionTime, timeIncludeFeatureToModelSeconds);
        } catch (ErroneousValueException | UnreadDatasetException | IOException e) {
            logger.error("An error occurred while processing the feature stream");
        } catch (Error e) {
            // Catch OutOfMemoryError to register that this way of processing the feature stream incurs in memory errors
            this.fileSelectedFeatures.write("A memory error occurred!");
        }
        logger.info("The end of the feature stream has been reached");
        metricsWriter.write(resultsBatch);
    }

    private double processNewFeatrueVariable(MultiCTBNC<CPTNode, CIMNode> model) throws UnreadDatasetException, FileNotFoundException, ErroneousValueException {
        Dataset currentDatasetFeatureStream = featureStreamReader.readDataset();
        if (currentDatasetFeatureStream.getNumDataPoints() > 0) {
            // Incorporate new feature variable into the model. Affected feature variables should be updated
            String nameNewFeatureVariable = featureStreamReader.getNameLastFeatureReceived();
            logger.info("Feature variable {} was received from the feature stream", nameNewFeatureVariable);
            // Perform FSS to decide if new feature variable should be included into the model
            logger.info("Updating model with new feature variable");
        }
        return model.updateBridgeAndFeatureSubgraph(currentDatasetFeatureStream) / 1000.f;
    }

    private void validateFinalModel(MultiCTBNC<CPTNode, CIMNode> model, List<Map<String, Double>> resultsBatch, double fssExecutionTime, double timeIncludeFeatureToModelSeconds) throws UnreadDatasetException, ErroneousValueException, IOException {
        // Validate model after receiving all feature variables, performing the FSS and updating the model in every batch
        ValidationMethod validationMethod = new TestDatasetMethod(testDatasetReader, estimateProbabilities);
        Map<String, Double> results = validationMethod.evaluate(model);
        results.put("Learning time", timeIncludeFeatureToModelSeconds + fssExecutionTime);
        resultsBatch.add(results);
        for (String featureName : model.getNameFeatureVariables())
            fileSelectedFeatures.write(featureName);
    }
}
