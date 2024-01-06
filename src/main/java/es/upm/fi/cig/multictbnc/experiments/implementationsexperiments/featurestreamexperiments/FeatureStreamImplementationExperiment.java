package es.upm.fi.cig.multictbnc.experiments.implementationsexperiments.featurestreamexperiments;

import es.upm.fi.cig.multictbnc.data.reader.DatasetReader;
import es.upm.fi.cig.multictbnc.data.reader.FeatureStreamMultipleCSVReader;
import es.upm.fi.cig.multictbnc.exceptions.ErroneousValueException;
import es.upm.fi.cig.multictbnc.exceptions.UnreadDatasetException;
import es.upm.fi.cig.multictbnc.experiments.implementationsexperiments.DataStreamExperiment;
import es.upm.fi.cig.multictbnc.fss.SubsetSelectedFeatures;
import es.upm.fi.cig.multictbnc.models.MultiCTBNC;
import es.upm.fi.cig.multictbnc.nodes.CIMNode;
import es.upm.fi.cig.multictbnc.nodes.CPTNode;
import es.upm.fi.cig.multictbnc.writers.performance.ExcelExperimentsWriter;
import es.upm.fi.cig.multictbnc.writers.performance.MetricsWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Abstract class representing an implementation of an experiment with feature streams.
 *
 * @author Carlos Villa Blanco
 */
public abstract class FeatureStreamImplementationExperiment {
    boolean areModelsValidated;
    String nameTimeVariable;
    List<String> nameClassVariables;
    DatasetReader initialDatasetReader;
    DatasetReader testDatasetReader;
    MetricsWriter metricsWriter;
    FileWriter fileSelectedFeatures;
    FeatureStreamMultipleCSVReader featureStreamReader;
    Boolean estimateProbabilities = true;
    Logger logger = LogManager.getLogger(DataStreamExperiment.class);

    /**
     * Constructs a FeatureStreamImplementationExperiment with the specified parameters.
     *
     * @param pathFeatureStream    path to the feature stream
     * @param nameTimeVariable     name of the time variable
     * @param nameClassVariables   list of names of class variables
     * @param initialDatasetReader reader for the initial dataset
     * @param testDatasetReader    reader for the test dataset
     * @param experimentNumber     identifier of the experiment
     * @param numFeatures          number of features in the dataset
     * @param areModelsValidated   boolean flag indicating if models should be validated
     * @throws IOException            if an error occurs in file handling
     * @throws UnreadDatasetException if the dataset cannot be read
     */
    public FeatureStreamImplementationExperiment(String pathFeatureStream, String nameTimeVariable, List<String> nameClassVariables, DatasetReader initialDatasetReader, DatasetReader testDatasetReader, int experimentNumber, int numFeatures, boolean areModelsValidated) throws IOException, UnreadDatasetException {
        this.nameTimeVariable = nameTimeVariable;
        this.nameClassVariables = nameClassVariables;
        this.initialDatasetReader = initialDatasetReader;
        this.testDatasetReader = testDatasetReader;
        this.areModelsValidated = areModelsValidated;
        featureStreamReader = new FeatureStreamMultipleCSVReader(pathFeatureStream,
                initialDatasetReader.readDataset(), nameTimeVariable);
        String filenameExperiment = "feature_stream_" + numFeatures + "_" + getFilenameResults() + "_experiment" + experimentNumber;
        if (areModelsValidated) {
            this.metricsWriter = getResultsWriter(filenameExperiment);
            this.fileSelectedFeatures = new FileWriter("results/experiments/" + filenameExperiment + "_selected_features.txt");
        }
    }

    /**
     * Provides the filename for storing results.
     *
     * @return the filename as a string
     */
    public abstract String getFilenameResults();

    private MetricsWriter getResultsWriter(String filenameExperiment) {
        return new ExcelExperimentsWriter(List.of(""), List.of("Results feature stream"), List.of("Multi-CTBNC"), this.nameClassVariables,
                null, null, null, "", "Empty", null, filenameExperiment);
    }

    /**
     * Executes the experiment.
     *
     * @param model Multi-CTBNC used in the experiment
     * @throws UnreadDatasetException  if the dataset cannot be read
     * @throws IOException             if an error occurs in file handling
     * @throws ErroneousValueException if there are erroneous values in the data
     */
    public abstract void execute(MultiCTBNC<CPTNode, CIMNode> model) throws UnreadDatasetException, IOException, ErroneousValueException;


    /**
     * Closes the resources used for the experiment. This includes the file writers for metrics and selected features.
     *
     * @throws IOException if an error occurs in file handling
     */
    public void close() throws IOException {
        if (this.areModelsValidated) {
            this.fileSelectedFeatures.close();
            this.metricsWriter.close();
        }
    }

    /**
     * Records the subset of selected features used in the experiment. The method writes the names of these features
     * to a file for later analysis.
     *
     * @param subsetSelectedFeatures subset of selected features
     * @throws IOException if an error occurs in file writing
     */
    void recordSubsetSelectedFeatures(SubsetSelectedFeatures subsetSelectedFeatures) throws IOException {
        if (this.fileSelectedFeatures != null) {
            for (String featureName : subsetSelectedFeatures.getFeatures())
                this.fileSelectedFeatures.write(featureName + ", ");
            this.fileSelectedFeatures.write("\r\n");
        }
    }
}
