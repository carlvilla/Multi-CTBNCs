package es.upm.fi.cig.multictbnc.experiments.implementationsexperiments.featurestreamexperiments;

import es.upm.fi.cig.multictbnc.data.reader.DatasetReader;
import es.upm.fi.cig.multictbnc.data.representation.Dataset;
import es.upm.fi.cig.multictbnc.exceptions.ErroneousValueException;
import es.upm.fi.cig.multictbnc.exceptions.UnreadDatasetException;
import es.upm.fi.cig.multictbnc.fss.ConInd;
import es.upm.fi.cig.multictbnc.fss.OnlineFeatureSubsetSelection;
import es.upm.fi.cig.multictbnc.fss.SubsetSelectedFeatures;
import es.upm.fi.cig.multictbnc.learning.parameters.ctbn.CTBNParameterLearningAlgorithm;
import es.upm.fi.cig.multictbnc.models.MultiCTBNC;
import es.upm.fi.cig.multictbnc.nodes.CIMNode;
import es.upm.fi.cig.multictbnc.nodes.CPTNode;
import es.upm.fi.cig.multictbnc.performance.TestDatasetMethod;
import es.upm.fi.cig.multictbnc.performance.ValidationMethod;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents an experiment that processes a feature stream with online feature subset selection using a MultiCTBNC. It
 * iteratively updates the model with new features and validates the final model's performance.
 *
 * @author Carlos Villa Blanco
 */
public class FeatureStreamWithFSSExperiment extends FeatureStreamImplementationExperiment {
    OnlineFeatureSubsetSelection onlineFeatureSubsetSelection;

    /**
     * Initializes a FeatureStreamWithFSSExperiment with the provided configuration parameters.
     *
     * @param pathFeatureStream            path to the feature stream
     * @param nameTimeVariable             name of the time variable
     * @param nameClassVariables           list of names of class variables
     * @param initialDatasetReader         DatasetReader for the initial dataset
     * @param testDatasetReader            DatasetReader for the test dataset
     * @param ctbnPLA                      parameter learning algorithm for the CIM nodes
     * @param maxSeparatingSizeFSS         maximum separating size for the feature subset selection
     * @param experimentNumber             number identifying the experiment
     * @param numFeatures                  number of features in the dataset (used to identify results files)
     * @param areModelsValidated           boolean indicating whether the models should be validated
     * @param sigTimeTransitionHyp         significance level for time transition hypothesis
     * @param sigStateToStateTransitionHyp significance level for state-to-state transition hypothesis
     * @throws UnreadDatasetException if there is an issue reading the dataset
     * @throws IOException            if there is an I/O error
     */
    public FeatureStreamWithFSSExperiment(String pathFeatureStream, String nameTimeVariable, List<String> nameClassVariables, DatasetReader initialDatasetReader, DatasetReader testDatasetReader, CTBNParameterLearningAlgorithm ctbnPLA, int maxSeparatingSizeFSS, int experimentNumber, int numFeatures, boolean areModelsValidated, double sigTimeTransitionHyp, double sigStateToStateTransitionHyp) throws UnreadDatasetException, IOException {
        super(pathFeatureStream, nameTimeVariable, nameClassVariables, initialDatasetReader, testDatasetReader, experimentNumber, numFeatures, areModelsValidated);
        this.onlineFeatureSubsetSelection = new ConInd(this.nameClassVariables, ctbnPLA, maxSeparatingSizeFSS, sigTimeTransitionHyp, sigStateToStateTransitionHyp);
    }

    @Override
    public String getFilenameResults() {
        return "FSS";
    }

    @Override
    public void execute(MultiCTBNC<CPTNode, CIMNode> model) throws ErroneousValueException, UnreadDatasetException, IOException {
        try {
            // We treat the data as a feature stream
            double timeLearningInitialModel = trainModel(model);
            List<Map<String, Double>> resultsBatch = iterateOverFeatureStream(model, timeLearningInitialModel);
            if (this.areModelsValidated) {
                // Save results in a file
                this.metricsWriter.write(resultsBatch);
            }
        } catch (Error e) {
            if (this.areModelsValidated) {
                // Catch OutOfMemoryError to register that this way of processing the feature stream incurs in memory errors
                this.fileSelectedFeatures.write("A memory error occurred!");
            }
        }
    }

    private double trainModel(MultiCTBNC<CPTNode, CIMNode> model) throws UnreadDatasetException, ErroneousValueException {
        Dataset trainDataset = this.initialDatasetReader.readDataset();
        return model.learn(trainDataset) / 1000.f;
    }

    private List<Map<String, Double>> iterateOverFeatureStream(MultiCTBNC<CPTNode, CIMNode> model, double timeLearningInitialModel) {
        logger.info("Iterating over feature stream...");
        List<Map<String, Double>> resultsBatch = new ArrayList<>();
        double fssExecutionTime = 0;
        double timeIncludeFeatureToModelSeconds = timeLearningInitialModel;
        try {
            while (this.featureStreamReader.isDataArriving()) {
                Dataset currentDatasetFeatureStream = this.featureStreamReader.readDataset();
                if (currentDatasetFeatureStream.getNumDataPoints() > 0) {
                    // Incorporate new feature variable into the model. Affected feature variables should be updated
                    String nameNewFeatureVariable = this.featureStreamReader.getNameLastFeatureReceived();
                    logger.info("Feature variable {} was received from the feature stream", nameNewFeatureVariable);
                    // Perform FSS to decide if new feature variable should be included into the model
                    List<String> currentFeatureVariables = new ArrayList<>(
                            currentDatasetFeatureStream.getNameFeatureVariables());
                    currentFeatureVariables.remove(nameNewFeatureVariable);
                    Dataset datasetFSS = currentDatasetFeatureStream.getLabelPowerset();
                    onlineFeatureSubsetSelection.setCurrentFeatureVariables(currentFeatureVariables);
                    SubsetSelectedFeatures selectedFeatureVariables = onlineFeatureSubsetSelection.execute(
                            nameNewFeatureVariable, datasetFSS);
                    fssExecutionTime += selectedFeatureVariables.getExecutionTime();
                    if (onlineFeatureSubsetSelection.getLastExecutionYieldAnyChange()) {
                        // Remove irrelevant and redundant variables from the dataset
                        List<String> irrelevantFeatureVariables = model.getNameFeatureVariables().stream().filter(
                                nameFeaturevariable -> !selectedFeatureVariables.containsFeature(
                                        nameFeaturevariable)).collect(Collectors.toList());
                        currentDatasetFeatureStream.removeFeatureVariables(irrelevantFeatureVariables);
                        logger.info("Updating model with new feature variable");
                        timeIncludeFeatureToModelSeconds +=
                                model.updateBridgeAndFeatureSubgraph(currentDatasetFeatureStream) / 1000.f;
                        recordSubsetSelectedFeatures(selectedFeatureVariables);
                    } else {
                        // Remove only the irrelevant new feature
                        currentDatasetFeatureStream.removeFeatureVariables(List.of(nameNewFeatureVariable));
                    }
                }
            }
            if (this.areModelsValidated)
                // Validate model after receiving all feature variables, performing the FSS and updating the model in every batch
                validateFinalModel(model, resultsBatch, fssExecutionTime, timeIncludeFeatureToModelSeconds);
        } catch (ErroneousValueException | UnreadDatasetException | IOException e) {
            logger.error("An error occurred while processing the feature stream");
        }
        logger.info("The end of the feature stream has been reached");
        return resultsBatch;
    }

    private void validateFinalModel(MultiCTBNC<CPTNode, CIMNode> model, List<Map<String, Double>> resultsBatch, double fssExecutionTime, double timeIncludeFeatureToModelSeconds) throws UnreadDatasetException, ErroneousValueException {
        ValidationMethod validationMethod = new TestDatasetMethod(testDatasetReader, estimateProbabilities);
        Map<String, Double> results = validationMethod.evaluate(model);
        results.put("Learning time", timeIncludeFeatureToModelSeconds + fssExecutionTime);
        resultsBatch.add(results);
    }
}
