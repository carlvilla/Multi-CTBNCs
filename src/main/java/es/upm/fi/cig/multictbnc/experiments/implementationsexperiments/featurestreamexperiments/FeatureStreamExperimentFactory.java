package es.upm.fi.cig.multictbnc.experiments.implementationsexperiments.featurestreamexperiments;

import es.upm.fi.cig.multictbnc.data.reader.DatasetReader;
import es.upm.fi.cig.multictbnc.exceptions.UnreadDatasetException;
import es.upm.fi.cig.multictbnc.learning.parameters.ctbn.CTBNParameterLearningAlgorithm;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;

/**
 * Factory class for creating specific types of feature stream experiments.
 */
public class FeatureStreamExperimentFactory {
    private static Logger logger = LogManager.getLogger(FeatureStreamExperimentFactory.class);

    /**
     * Creates an instance of a feature stream experiment based on the specified type of processing. Different types
     * of processing methods like 'Retraining(FSS)', 'Batch(FSS)', 'Batch', and 'Retraining' are supported, each
     * corresponding to a different implementation strategy for handling feature streams.
     *
     * @param typeFeatureStreamProcessing  type of feature stream processing method
     * @param pathFeatureStream            path to the feature stream
     * @param nameTimeVariable             name of the time variable
     * @param nameClassVariables           list of names of class variables
     * @param initialDatasetReader         reader for the initial dataset
     * @param testDatasetReader            reader for the test dataset
     * @param ctbnPLA                      CTBN parameter learning algorithm
     * @param maxSeparatingSizeFSS         maximum separating size for feature subset selection
     * @param experimentNumber             identifier of the experiment
     * @param numFeatures                  number of features in the dataset
     * @param areModelsValidated           boolean flag indicating if models should be validated
     * @param sigTimeTransitionHyp         significance level for time transition hypothesis
     * @param sigStateToStateTransitionHyp significance level for state-to-state transition hypothesis
     * @return instance of FeatureStreamImplementationExperiment
     * @throws UnreadDatasetException if the dataset cannot be read
     * @throws IOException            if an error occurs in file handling
     */
    public static FeatureStreamImplementationExperiment getFeatureStreamImplementation(String typeFeatureStreamProcessing, String pathFeatureStream, String nameTimeVariable, List<String> nameClassVariables, DatasetReader initialDatasetReader, DatasetReader testDatasetReader, CTBNParameterLearningAlgorithm ctbnPLA, int maxSeparatingSizeFSS, int experimentNumber, int numFeatures, boolean areModelsValidated, double sigTimeTransitionHyp, double sigStateToStateTransitionHyp) throws UnreadDatasetException, IOException {
        switch (typeFeatureStreamProcessing) {
            case ("Retraining(FSS)"):
                logger.info("Processing feature stream using model retraining with online FSS");
                return new FeatureStreamWithFSSExperiment(pathFeatureStream, nameTimeVariable, nameClassVariables, initialDatasetReader, testDatasetReader, ctbnPLA, maxSeparatingSizeFSS, experimentNumber, numFeatures, areModelsValidated, sigTimeTransitionHyp, sigStateToStateTransitionHyp);
            case ("Batch(FSS)"):
                logger.info("Processing feature stream using batch training with online FSS");
                return new FeatureStreamWithFSSWithoutUpdatingExperiment(pathFeatureStream, nameTimeVariable, nameClassVariables, initialDatasetReader, testDatasetReader, ctbnPLA, maxSeparatingSizeFSS, experimentNumber, numFeatures, areModelsValidated, sigTimeTransitionHyp, sigStateToStateTransitionHyp);
            case ("Batch"):
                logger.info("Processing feature stream using batch training");
                return new FeatureStreamAsStaticDatasetExperiment(pathFeatureStream, nameTimeVariable, nameClassVariables, initialDatasetReader, testDatasetReader, experimentNumber, numFeatures, areModelsValidated);
            case ("Retraining"):
                logger.info("Processing feature stream using model retraining");
                return new FeatureStreamWithoutFSSExperiment(pathFeatureStream, nameTimeVariable, nameClassVariables, initialDatasetReader, testDatasetReader, experimentNumber, numFeatures, areModelsValidated);
            default:
                logger.error("Feature stream processing method {} is not implemented", typeFeatureStreamProcessing);
                return null;

        }
    }

}
