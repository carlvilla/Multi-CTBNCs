package es.upm.fi.cig.multictbnc.experiments.implementationsexperiments;

import es.upm.fi.cig.multictbnc.classification.ClassifierFactory;
import es.upm.fi.cig.multictbnc.data.reader.DatasetReader;
import es.upm.fi.cig.multictbnc.data.reader.DatasetReaderFactory;
import es.upm.fi.cig.multictbnc.exceptions.ErroneousValueException;
import es.upm.fi.cig.multictbnc.exceptions.UnreadDatasetException;
import es.upm.fi.cig.multictbnc.experiments.implementationsexperiments.featurestreamexperiments.FeatureStreamExperimentFactory;
import es.upm.fi.cig.multictbnc.experiments.implementationsexperiments.featurestreamexperiments.FeatureStreamImplementationExperiment;
import es.upm.fi.cig.multictbnc.learning.BNLearningAlgorithms;
import es.upm.fi.cig.multictbnc.learning.CTBNLearningAlgorithms;
import es.upm.fi.cig.multictbnc.learning.parameters.bn.BNParameterLearningAlgorithm;
import es.upm.fi.cig.multictbnc.learning.parameters.bn.BNParameterLearningAlgorithmFactory;
import es.upm.fi.cig.multictbnc.learning.parameters.ctbn.CTBNParameterLearningAlgorithm;
import es.upm.fi.cig.multictbnc.learning.parameters.ctbn.CTBNParameterLearningAlgorithmFactory;
import es.upm.fi.cig.multictbnc.learning.structure.StructureLearningAlgorithm;
import es.upm.fi.cig.multictbnc.learning.structure.StructureLearningAlgorithmFactory;
import es.upm.fi.cig.multictbnc.models.MultiCTBNC;
import es.upm.fi.cig.multictbnc.nodes.CIMNode;
import es.upm.fi.cig.multictbnc.nodes.CPTNode;
import es.upm.fi.cig.multictbnc.util.Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;


/**
 * Represents an experiment for evaluating continuous-time Bayesian network classifiers on feature streams. This class
 * facilitates conducting experiments on feature streams, handling various aspects such as the evolution of features
 * over time and adapting models to these changes. This class is used to load data and execute the specified
 * implementation of feature stream processing method.
 *
 * @author Carlos Villa Blanco
 */
public class FeatureStreamExperiment {
    // The constraint-based algorithm is executed using different significances
    private static final String SIGNIFICANCEPC = "0.05";
    private static final String SIGTIMETRANSITIONHYPOTHESIS = "0.00001";
    private static final String SIGSTATETOSTATETRANSITIONHYPOTHESIS = "0.00001";
    private static double sigTimeTransitionHypFSS = 0.000005;
    private static double sigStateToStateTransitionHypFSS = 0.000005;
    private static int maxSeparatingSizeFSS = 2;
    private String nameTimeVariable;
    private List<String> nameClassVariables;
    private boolean areModelsValidated;
    // Hyperparameters
    private double nx = 1;
    private double mxy = 1;
    private double tx = 0.001;
    // Bayesian estimation
    private String[] pathsExperiments;
    private BNParameterLearningAlgorithm bnPLA;
    private StructureLearningAlgorithm bnSLA;
    private CTBNParameterLearningAlgorithm ctbnPLA;
    private StructureLearningAlgorithm ctbnSLA;
    private String processingMethod;

    /**
     * The main method to execute the feature stream experiments.
     *
     * @param args queue of strings representing parameters needed for setting up and executing experiments.
     */
    public void main(Queue<String> args) {
        setParametersExperiment(args);
        try {
            for (String pathExperiment : this.pathsExperiments) {
                executeExperiment(pathExperiment, this.processingMethod);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets up the parameters for a feature stream experiment using a queue of arguments. This method initializes the
     * experiment with the necessary configurations required for its execution. Additionally, this method initializes
     * the learning algorithms for both parameter and structure learning based on the specified parameters.
     * <p>
     * The method processes the following parameters in sequence:
     * 1. Path to data: extracts the path to the datasets, which includes an initial dataset and a feature stream
     * folder.
     * 2. Class variables: specifies the names of the class variables.
     * 3. Time variable: specifies the name of the time variable.
     * 4. Model validation: a boolean value indicating whether the models should be validated.
     * 5. Processing method: determines the specific implementation of feature stream processing to be applied.
     * <p>
     * After extracting these parameters, the method proceeds to define the parameter learning algorithms and the
     * constraint-based learning algorithms, setting up the experiment for execution.
     *
     * @param args a queue of strings containing the parameters needed to configure the experiment
     */
    public void setParametersExperiment(Queue<String> args) {
        // A "train" folder with the data to learn the initial model and a "data_stream" folder with the data stream
        // are expected
        String pathData = args.poll();
        this.pathsExperiments = getPathsExperiments(pathData);
        // Get class variables
        this.nameClassVariables = Util.stringToList(args.poll());
        // Get time variable
        this.nameTimeVariable = getTimeVariable();
        this.areModelsValidated = Boolean.parseBoolean(args.poll());
        this.processingMethod = args.poll();
        defineParameterLearningAlgorithms();
        defineConstraintBasedLearningAlgorithms();
    }

    /**
     * Executes a single feature stream experiment for a specified path. It involves reading datasets, initializing a
     * Multi-CTBNC and applying the feature stream processing.
     *
     * @param pathExperiment path to the experiment data, including training and streaming data
     * @param typeProcessing the type of feature stream processing to be applied
     * @return the Multi-CTBNC resulting from the experiment
     * @throws IOException             if an error occurs in file reading
     * @throws UnreadDatasetException  if the dataset cannot be read
     * @throws ErroneousValueException if there is an issue with the provided data or configuration settings
     */
    public MultiCTBNC<CPTNode, CIMNode> executeExperiment(String pathExperiment, String typeProcessing)
            throws IOException, ErroneousValueException, UnreadDatasetException {
        String pathInitialDataset = pathExperiment + "/initial_dataset";
        String pathFeatureStream = pathExperiment + "/feature_stream";
        String pathTestDataset = pathExperiment + "/test_dataset";
        int experimentNumber = (int) Util.extractLong(Paths.get(pathExperiment).getFileName().toString());
        int numFeatures = (int) Util.extractLong(Paths.get(pathExperiment).getParent().getFileName().toString());
        DatasetReader initialDatasetReader = getDatasetReader(pathInitialDataset);
        DatasetReader testDatasetReader = getDatasetReader(pathTestDataset);
        MultiCTBNC<CPTNode, CIMNode> model = getMultiCTBNC();
        FeatureStreamImplementationExperiment featureStreamProcessing = FeatureStreamExperimentFactory.getFeatureStreamImplementation(typeProcessing, pathFeatureStream, this.nameTimeVariable, this.nameClassVariables, initialDatasetReader, testDatasetReader, this.ctbnPLA, this.maxSeparatingSizeFSS, experimentNumber, numFeatures, areModelsValidated, sigTimeTransitionHypFSS, sigStateToStateTransitionHypFSS);
        featureStreamProcessing.execute(model);
        featureStreamProcessing.close();
        return model;
    }

    private String[] getPathsExperiments(String pathData) {
        File[] directories = new File(pathData).listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        });
        String[] pathsExperiments = Arrays.stream(directories).map(File::getAbsolutePath).toArray(String[]::new);
        // Sort experiments by number
        Arrays.sort(pathsExperiments, new Comparator<>() {
            @Override
            public int compare(String e1, String e2) {
                return Long.compare(Util.extractLong(e1), Util.extractLong(e2));
            }
        });
        return pathsExperiments;
    }

    private String getTimeVariable() {
        return "t";
    }

    private void defineParameterLearningAlgorithms() {
        // Class subgraph
        String namePLA = "Bayesian estimation";
        this.bnPLA = BNParameterLearningAlgorithmFactory.getAlgorithm(namePLA, nx);
        // Bridge and feature subgraphs
        this.ctbnPLA = CTBNParameterLearningAlgorithmFactory.getAlgorithm(namePLA, mxy, tx);
    }

    private void defineConstraintBasedLearningAlgorithms() {
        Map<String, String> paramSLA = Map.of("significancePC", SIGNIFICANCEPC, "sigTimeTransitionHyp",
                SIGTIMETRANSITIONHYPOTHESIS, "sigStateToStateTransitionHyp", SIGSTATETOSTATETRANSITIONHYPOTHESIS);
        // Class subgraph
        String nameSLA = "CTPC";
        this.bnSLA = StructureLearningAlgorithmFactory.getAlgorithmBN(nameSLA, paramSLA);
        // Bridge and feature subgraphs
        this.ctbnSLA = StructureLearningAlgorithmFactory.getAlgorithmCTBN(nameSLA, paramSLA);
    }

    private DatasetReader getDatasetReader(String pathTestDataset) throws FileNotFoundException {
        DatasetReader datasetReader = DatasetReaderFactory.getDatasetReader("Multiple CSV", pathTestDataset, 0);
        datasetReader.setTimeAndClassVariables(nameTimeVariable, nameClassVariables);
        return datasetReader;
    }

    private MultiCTBNC<CPTNode, CIMNode> getMultiCTBNC() {
        BNLearningAlgorithms bnLearningAlgs = new BNLearningAlgorithms(bnPLA, bnSLA);
        CTBNLearningAlgorithms ctbnLearningAlgs = new CTBNLearningAlgorithms(ctbnPLA, ctbnSLA);
        Map<String, String> hyperparameters = new WeakHashMap<>();
        return ClassifierFactory.getMultiCTBNC("Multi-CTBNC", bnLearningAlgs,
                ctbnLearningAlgs, hyperparameters, CPTNode.class, CIMNode.class);
    }

    /**
     * Executes a single feature stream experiment. This method prepares the datasets for the experiment, initializes
     * the model and then applies the specified feature stream processing.
     *
     * @param pathExperiment                       path to the experiment data, including training and streaming data
     * @param typeProcessing                       type of feature stream processing to be applied
     * @param sigTimeTransitionHypFSSParam         significance level for time transition hypothesis
     * @param sigStateToStateTransitionHypFSSParam significance level for state-to-state transition hypothesis
     * @return Multi-CTBNC resulting from the experiment
     * @throws UnreadDatasetException  if the dataset cannot be read
     * @throws IOException             if an error occurs in file reading
     * @throws ErroneousValueException if there are erroneous values in the data
     */
    public MultiCTBNC<CPTNode, CIMNode> executeExperiment(String pathExperiment, String typeProcessing, double sigTimeTransitionHypFSSParam, double sigStateToStateTransitionHypFSSParam) throws UnreadDatasetException, IOException, ErroneousValueException {
        sigTimeTransitionHypFSS = sigTimeTransitionHypFSSParam;
        sigStateToStateTransitionHypFSS = sigStateToStateTransitionHypFSSParam;
        return executeExperiment(pathExperiment, typeProcessing);
    }

}
