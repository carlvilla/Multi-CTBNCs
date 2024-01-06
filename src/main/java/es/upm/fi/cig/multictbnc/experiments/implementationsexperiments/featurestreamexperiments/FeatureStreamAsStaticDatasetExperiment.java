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
import java.util.List;
import java.util.Map;

/**
 * Implements an experiment where a feature stream is treated as a static dataset. This class is responsible for
 * training and validating a Multi-CTBNC.
 *
 * @author Carlos Villa Blanco
 */
public class FeatureStreamAsStaticDatasetExperiment extends FeatureStreamImplementationExperiment {

    /**
     * Constructs a FeatureStreamAsStaticDatasetExperiment with the specified parameters.
     *
     * @param pathFeatureStream    path to the feature stream
     * @param nameTimeVariable     name of the time variable
     * @param nameClassVariables   list of names of class variables
     * @param initialDatasetReader reader for the initial dataset
     * @param testDatasetReader    reader for the test dataset
     * @param experimentNumber     identifier of the experiment
     * @param numFeatures          number of features in the dataset
     * @param areModelsValidated   boolean flag indicating if models should be validated
     * @throws UnreadDatasetException if the dataset cannot be read
     * @throws IOException            if an error occurs in file reading
     */
    public FeatureStreamAsStaticDatasetExperiment(String pathFeatureStream, String nameTimeVariable, List<String> nameClassVariables, DatasetReader initialDatasetReader, DatasetReader testDatasetReader, int experimentNumber, int numFeatures, boolean areModelsValidated) throws UnreadDatasetException, IOException {
        super(pathFeatureStream, nameTimeVariable, nameClassVariables, initialDatasetReader, testDatasetReader, experimentNumber, numFeatures, areModelsValidated);
    }

    @Override
    public String getFilenameResults() {
        return "static";
    }

    @Override
    public void execute(MultiCTBNC<CPTNode, CIMNode> model) throws UnreadDatasetException, IOException, ErroneousValueException {
        try {
            double learningTimeSeconds = trainModel(model);
            ValidationMethod validationMethod = new TestDatasetMethod(this.testDatasetReader, this.estimateProbabilities);
            if (this.areModelsValidated) {
                Map<String, Double> resultsLearningStaticDataset = validateModel(model, learningTimeSeconds, validationMethod);
                metricsWriter.write(resultsLearningStaticDataset);
            }
        } catch (Error e) {
            // Catch OutOfMemoryError to register that this way of processing the feature stream incurs in memory errors
            this.fileSelectedFeatures.write("A memory error occurred!");
        }
    }

    private double trainModel(MultiCTBNC<CPTNode, CIMNode> model) throws UnreadDatasetException, FileNotFoundException, ErroneousValueException {
        // Retrieve complete feature stream
        Dataset completeDataset = this.featureStreamReader.readCompleteDataset();
        // Learn model from static dataset
        double learningTimeSeconds = model.learn(completeDataset) / 1000.f;
        // Remove dataset from model to avoid memory problems
        model.setDataset(null);
        return learningTimeSeconds;
    }

    private static Map<String, Double> validateModel(MultiCTBNC<CPTNode, CIMNode> model, double learningTimeSeconds, ValidationMethod validationMethod) throws UnreadDatasetException, ErroneousValueException {
        Map<String, Double> resultsLearningStaticDataset = validationMethod.evaluate(model);
        resultsLearningStaticDataset.put("Learning time", learningTimeSeconds);
        return resultsLearningStaticDataset;
    }
}
