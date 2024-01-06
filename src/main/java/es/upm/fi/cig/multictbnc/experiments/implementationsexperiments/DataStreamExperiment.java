package es.upm.fi.cig.multictbnc.experiments.implementationsexperiments;

import es.upm.fi.cig.multictbnc.classification.ClassifierFactory;
import es.upm.fi.cig.multictbnc.classification.Prediction;
import es.upm.fi.cig.multictbnc.conceptdriftdetection.*;
import es.upm.fi.cig.multictbnc.data.reader.DataStreamMultipleCSVReader;
import es.upm.fi.cig.multictbnc.data.reader.DatasetReader;
import es.upm.fi.cig.multictbnc.data.reader.MultipleCSVReader;
import es.upm.fi.cig.multictbnc.data.representation.Dataset;
import es.upm.fi.cig.multictbnc.exceptions.ErroneousValueException;
import es.upm.fi.cig.multictbnc.exceptions.UnreadDatasetException;
import es.upm.fi.cig.multictbnc.gui.XYLineChart;
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
import es.upm.fi.cig.multictbnc.nodes.Node;
import es.upm.fi.cig.multictbnc.performance.Metrics;
import es.upm.fi.cig.multictbnc.util.UserInterfaceUtil;
import es.upm.fi.cig.multictbnc.util.Util;
import es.upm.fi.cig.multictbnc.writers.performance.ExcelExperimentsWriter;
import es.upm.fi.cig.multictbnc.writers.performance.MetricsWriter;
import javafx.application.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Represents an experiment for evaluating continuous-time Bayesian network classifiers on streaming data. This class
 * facilitates conducting experiments on data streams, handling different aspects of data stream processing, concept
 * drift detection and adaptation. This class is configurable with various parameters including batch size, concept
 * drift adaptation strategy, threshold for drift detection, etc., making it versatile for different streaming data
 * scenarios.
 * <p>
 * The class includes functionalities to:
 * 1. Load and process data from specified paths.
 * 2. Train an initial model on a given training dataset.
 * 3. Evaluate and adapt the model in response to incoming data.
 * 4. Implement different strategies for handling concept drift in the data.
 * 5. Calculate and track various performance metrics over data batches.
 * 6. Optionally, display results visually using line charts.
 * 7. Save experimental results to external files for further analysis.
 *
 * @author Carlos Villa Blanco
 */
public class DataStreamExperiment {
    // The constraint-based algorithm is executed using different significances
    static final String SIGNIFICANCEPC = "0.05";
    static final String SIGTIMETRANSITIONHYPOTHESIS = "0.00001";
    static final String SIGSTATETOSTATETRANSITIONHYPOTHESIS = "0.00001";
    String nameTimeVariable;
    List<String> nameClassVariables;
    // Hyperparameters
    double nx = 1;
    double mxy = 1;
    double tx = 0.001;
    // Bayesian estimation
    boolean estimateProbabilities = true;
    XYLineChart lineChartGlobalAccuracy;
    XYLineChart lineChartAccuracies;
    // Concept drift detectors
    ConceptDriftAdaptiveMethod conceptDriftAdapter;
    boolean resetAfterConceptDrift;
    int windowSize;
    ConceptDriftScore conceptDriftScore;
    int batchSize;
    String strategyConceptDriftAdaptation; // "LOCAL", "GLOBAL", "NO" (default)
    List<Double> detectionThresholds;
    double magnitudeThreshold;
    boolean showCharts;
    String[] pathsExperiments;
    int numProcessedBatches;
    double meanGlobalAccuracy;
    double meanMeanAccuracy;
    double meanMacroAveragedPrecision;
    double meanMacroAveragedRecall;
    double meanMacroAveragedF1Score;
    double meanMicroAveragedPrecision;
    double meanMicroAveragedRecall;
    double meanMicroAveragedF1Score;
    double meanGlobalBrierScore;
    long meanUpdatingTimeSeconds;
    long totalUpdatingTimeSeconds;
    int numTimesModelUpdated;
    Logger logger = LogManager.getLogger(DataStreamExperiment.class);
    private BNParameterLearningAlgorithm bnPLA;
    private StructureLearningAlgorithm bnSLA;
    private CTBNParameterLearningAlgorithm ctbnPLA;
    private StructureLearningAlgorithm ctbnSLA;
    private boolean areResultsSaved = true;

    /**
     * Retrieves the mean global accuracy across all processed data batches.
     *
     * @return mean global accuracy
     */
    public double getMeanGlobalAccuracy() {
        return meanGlobalAccuracy;
    }

    /**
     * Retrieves the average mean accuracy across all processed data batches.
     *
     * @return average mean accuracy
     */
    public double getMeanMeanAccuracy() {
        return meanMeanAccuracy;
    }

    /**
     * Retrieves the mean macro-averaged F1 score across all processed data batches.
     *
     * @return mean macro-averaged F1 score
     */
    public double getMeanMacroAveragedF1Score() {
        return meanMacroAveragedF1Score;
    }

    /**
     * Retrieves the mean micro-averaged F1 score across all processed data batches.
     *
     * @return mean micro-averaged F1 score
     */
    public double getMeanMicroAveragedF1Score() {
        return meanMicroAveragedF1Score;
    }

    /**
     * Retrieves the mean global Brier score across all processed data batches.
     *
     * @return mean global Brier score
     */
    public double getMeanGlobalBrierScore() {
        return meanGlobalBrierScore;
    }

    /**
     * Retrieves the number of times the model was updated during the experiment.
     *
     * @return number of times the model was updated
     */
    public int getNumTimesModelUpdated() {
        return numTimesModelUpdated;
    }

    /**
     * The main method to execute the data stream experiments.
     *
     * @param args queue of strings representing parameters needed for setting up and executing experiments.
     * @throws ErroneousValueException if there is an issue with the values of the provided parameters
     */
    public void main(Queue<String> args) throws ErroneousValueException {
        setParametersExperiment(args);
        if (!this.strategyConceptDriftAdaptation.equals("NO_UPDATE")) {
            for (Double detectionThreshold : detectionThresholds) {
                logger.info("Performing data stream experiments with {} detection threshold", detectionThreshold);
                for (String pathExperiment : getPathsExperiments()) {
                    executeExperiment(pathExperiment, detectionThreshold);
                }
            }
        } else {
            for (String pathExperiment : getPathsExperiments()) {
                executeExperiment(pathExperiment, 0);
            }
        }
        if (!showCharts)
            Platform.exit();
    }

    /**
     * Sets up the parameters for a data stream experiment using a queue of arguments. This method initializes the
     * experiment with the necessary configurations required for its execution. Additionally, this method initializes
     * the learning algorithms for both parameter and structure learning based on the specified parameters.
     * <p>
     * The method processes the following parameters in sequence:
     * 1. Path to data: extracts the path to the dataset, which includes a training set and a data stream folder.
     * 2. Batch size: determines the size of the data batches to be processed in each iteration of the experiment.
     * 3. Concept drift adaptation strategy: specifies the method of adaptation to concept drift, such as 'LOCAL',
     * 'GLOBAL' or no adaptation.
     * 4. Detection thresholds: sets the thresholds for the Page-Hinkley test used in concept drift detection.
     * 5. Magnitude threshold: defines the magnitude threshold for concept drift detection.
     * 6. Reset after concept drift: a boolean value indicating whether the model should be reset after detecting
     * concept drift.
     * 7. Window size: determines the window size for the concept drift detection algorithm.
     * 8. Class variables: specifies the names of the class variables.
     * 9. Time Variable: specifies the name of the time variable.
     * 10. Concept drift score function: defines the scoring function to be used in concept drift detection.
     * 11. Display charts: a boolean value indicating whether to display charts for visualizing the results.
     *
     * @param args a queue of strings containing the parameters needed to configure the experiment
     */
    public void setParametersExperiment(Queue<String> args) {
        // A "train" folder with the data to learn the initial model and a "data_stream" folder with the data stream
        // are expected
        String pathData = args.poll();
        this.pathsExperiments = getPathsExperiments(pathData);
        // Size of the processed batches
        this.batchSize = Integer.parseInt(args.poll());
        // How the model is adapted to concept drifts (LOCAL, GLOBAL or no adaption)
        this.strategyConceptDriftAdaptation = args.poll();
        // Parameters of the Page-Hinkley test
        this.detectionThresholds = Util.stringToList(args.poll()).stream().map(Double::parseDouble).collect(Collectors.toList());
        this.magnitudeThreshold = Double.parseDouble(args.poll());
        this.resetAfterConceptDrift = Boolean.parseBoolean(args.poll());
        this.windowSize = Integer.parseInt(args.poll());
        // Get class variables
        this.nameClassVariables = Util.stringToList(args.poll());
        // Get time variable
        this.nameTimeVariable = getTimeVariable();
        // Define score used to detect concept drifts in a given data batch
        String penalisationFunctionConceptDriftScore = args.poll();
        this.conceptDriftScore = new AverageLocalLogLikelihood(penalisationFunctionConceptDriftScore);
        // Show charts
        this.showCharts = Boolean.parseBoolean(args.poll());
        defineParameterLearningAlgorithms();
        defineConstraintBasedLearningAlgorithms();
    }

    /**
     * Retrieves the array of dataset paths for the experiments.
     *
     * @return an array of strings, each representing a path to datasets
     */
    public String[] getPathsExperiments() {
        return this.pathsExperiments;
    }

    /**
     * Executes a single data stream experiment for a specified path and detection threshold. This method encompasses
     * the entire process of running an experiment, including training the initial model, setting up concept drift
     * adaptation strategies, iterating over the data stream and saving the results.
     *
     * @param pathExperiment     path to the experiment data, including training and streaming data
     * @param detectionThreshold threshold value used for detecting concept drift
     * @return list of maps, each map containing performance metrics for a data batch in the stream
     * @throws ErroneousValueException if there is an issue with the provided data or configuration settings
     */
    public List<Map<String, Double>> executeExperiment(String pathExperiment, double detectionThreshold) throws ErroneousValueException {
        String pathTrainDataset = pathExperiment + "/train";
        String pathDataStream = pathExperiment + "/data_stream";
        logger.info("Execution of: " + this.strategyConceptDriftAdaptation + " - " + pathDataStream + " - " + this.batchSize + " - " + detectionThreshold);
        MultiCTBNC<CPTNode, CIMNode> model = trainInitialModel(pathTrainDataset, nameTimeVariable,
                nameClassVariables);
        String titleExperiment =
                Paths.get(pathDataStream).getParent().toString() + ", " + strategyConceptDriftAdaptation + ", " +
                        batchSize + ", " + detectionThreshold;
        switch (strategyConceptDriftAdaptation) {
            case ("LOCAL") -> conceptDriftAdapter = new ConceptDriftLocallyAdaptiveMethod(model.getNameVariables(),
                    conceptDriftScore, magnitudeThreshold, detectionThreshold, resetAfterConceptDrift, windowSize,
                    showCharts, titleExperiment);
            case ("GLOBAL") -> conceptDriftAdapter = new ConceptDriftGloballyAdaptiveMethod(model.getNameVariables(),
                    conceptDriftScore, magnitudeThreshold, detectionThreshold, resetAfterConceptDrift,
                    windowSize, showCharts, titleExperiment);
        }
        Object[] results = iterateOverDataStream(model, pathDataStream);
        List<Map<String, Double>> resultsPerBatch = (List<Map<String, Double>>) results[0];
        List<List<Node>> changedNodes = (List<List<Node>>) results[1];
        saveResults(pathExperiment, detectionThreshold, resultsPerBatch, changedNodes);
        return resultsPerBatch;
    }

    private String[] getPathsExperiments(String pathData) {
        File file = new File(pathData);
        File[] directories = file.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        });
        String[] pathsExperiments = Arrays.stream(directories).map(File::getAbsolutePath).toArray(String[]::new);
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
        String nameSLA = "Online-MB-CTPC";
        // Class subgraph
        this.bnSLA = StructureLearningAlgorithmFactory.getAlgorithmBN(nameSLA, paramSLA);
        // Bridge and feature subgraphs
        this.ctbnSLA = StructureLearningAlgorithmFactory.getAlgorithmCTBN(nameSLA, paramSLA);
    }

    private MultiCTBNC<CPTNode, CIMNode> trainInitialModel(String pathTrainDataset, String nameTimeVariable,
                                                           List<String> nameClassVariables)
            throws ErroneousValueException {
        // Train model with training dataset
        BNLearningAlgorithms bnLearningAlgs = new BNLearningAlgorithms(bnPLA, bnSLA);
        CTBNLearningAlgorithms ctbnLearningAlgs = new CTBNLearningAlgorithms(ctbnPLA, ctbnSLA);
        Map<String, String> hyperparameters = new WeakHashMap<String, String>();
        MultiCTBNC<CPTNode, CIMNode> model = ClassifierFactory.<CPTNode, CIMNode>getMultiCTBNC("Multi-CTBNC",
                bnLearningAlgs, ctbnLearningAlgs, hyperparameters, CPTNode.class, CIMNode.class);
        // Read datasets
        DatasetReader trainDatasetReader;
        Dataset trainDataset = null;
        try {
            trainDatasetReader = new MultipleCSVReader(pathTrainDataset);
            // Set the variables that will be used
            trainDatasetReader.setTimeAndClassVariables(nameTimeVariable, nameClassVariables);
            trainDataset = trainDatasetReader.readDataset();
        } catch (FileNotFoundException | UnreadDatasetException e) {
            e.printStackTrace();
        }
        model.learn(trainDataset);
        if (showCharts)
            System.out.println(model);
        return model;
    }

    private Object[] iterateOverDataStream(MultiCTBNC<CPTNode, CIMNode> model, String pathDataStream) {
        this.numProcessedBatches = 0;
        this.numTimesModelUpdated = 0;
        Dataset batchDataStream;
        List<List<Node>> changedNodes = new ArrayList<>();
        List<Map<String, Double>> resultsPerBatch = new ArrayList<>();
        try {
            DataStreamMultipleCSVReader dataStreamReader = new DataStreamMultipleCSVReader(pathDataStream,
                    nameTimeVariable, nameClassVariables);
            while (dataStreamReader.isDataArriving()) {
                batchDataStream = dataStreamReader.readDataset(batchSize);
                if (batchDataStream.getNumDataPoints() > 0) {
                    // Prequential evaluation. Batch is used to test the model, then to update it if concept drift
                    // detected
                    Prediction[] predictionsBatch = model.predict(batchDataStream, estimateProbabilities);
                    boolean isModelUpdated = false;
                    double timeLastUpdateSeconds = 0;
                    if (conceptDriftAdapter != null)
                        isModelUpdated = conceptDriftAdapter.adaptModel(model, batchDataStream);
                    if (isModelUpdated) {
                        // Update time metrics
                        timeLastUpdateSeconds = conceptDriftAdapter.getUpdatingTime() / 1000.f;
                        totalUpdatingTimeSeconds += timeLastUpdateSeconds;
                        meanUpdatingTimeSeconds =
                                meanUpdatingTimeSeconds * (numProcessedBatches) / (numProcessedBatches + 1) +
                                        conceptDriftAdapter.getUpdatingTime() /
                                                (numProcessedBatches + 1);
                        numTimesModelUpdated++;
                        changedNodes.add(conceptDriftAdapter.getLastChangedNodes());
                    } else {
                        changedNodes.add(List.of());
                    }
                    numProcessedBatches++;
                    int numSequencesBatch = batchDataStream.getNumDataPoints();
                    Map<String, Double> results = Metrics.evaluate(predictionsBatch, batchDataStream);
                    results.put("Learning time", timeLastUpdateSeconds);
                    displayResults(results, conceptDriftAdapter, null, numSequencesBatch);
                    resultsPerBatch.add(results);
                }
            }
        } catch (FileNotFoundException | UnreadDatasetException | ErroneousValueException e) {
            logger.error("An error occurred while reading the data stream");
        }
        logger.info("The end of the data stream has been reached");
        return new Object[]{resultsPerBatch, changedNodes};
    }

    private void saveResults(String pathExperiment, double detectionThreshold, List<Map<String, Double>> resultsPerBatch, List<List<Node>> changedNodes) {
        if (areResultsSaved) {
            List<String> nameDatasets = new ArrayList<>();
            for (int i = 0; i < resultsPerBatch.size(); i++) {
                String nameDataset = "Batch " + i;
                if (changedNodes != null && changedNodes.size() > i && changedNodes.get(i) != null) {
                    nameDataset = nameDataset +
                            changedNodes.get(i).stream().map(Node::getName).collect(Collectors.joining(","));
                }
                nameDatasets.add(nameDataset);
            }
            // Save results in a file
            String nameDataStream = Paths.get(pathExperiment).getParent().getFileName().toString();
            String nameExperiment = Paths.get(pathExperiment).getFileName().toString();
            String experimentName;
            if (strategyConceptDriftAdaptation.equals("NO_UPDATE"))
                experimentName = strategyConceptDriftAdaptation + "_" + nameDataStream + "_" + nameExperiment;
            else
                experimentName = strategyConceptDriftAdaptation + "_" + nameDataStream + "_" + nameExperiment + "_detection_threshold_" + detectionThreshold;
            MetricsWriter metricsWriter = getMetricsWriter(nameDatasets, experimentName);
            metricsWriter.write(resultsPerBatch);
            metricsWriter.close();
        }
    }

    private void displayResults(Map<String, Double> results, ConceptDriftAdaptiveMethod conceptDriftAdapter,
                                Entry<List<String>, Long> newFeaturesUpdateTime, int numSequencesBatch) {
        updateLineCharts(results);
        printResults(results, conceptDriftAdapter, newFeaturesUpdateTime, numSequencesBatch);
    }

    private MetricsWriter getMetricsWriter(List<String> datasets, String filename) {
        // Define output to store the results of the experiments
        return new ExcelExperimentsWriter(List.of(""), datasets, List.of("Multi-CTBNC"), this.nameClassVariables,
                null, this.bnPLA, this.ctbnPLA, "", "Empty", null, filename);
    }

    private void updateLineCharts(Map<String, Double> results) {
        if (showCharts) {
            createLineCharts();
            int idxCurrentBatch = numProcessedBatches;// - 1;
            if (results.containsKey("Global accuracy")) {
                // Multi-dimensional classification
                lineChartGlobalAccuracy.update(idxCurrentBatch, results.get("Global accuracy"));

                double[] accuracies = nameClassVariables.stream().map(
                        nameClassVariable -> "Accuracy " + nameClassVariable).map(results::get).mapToDouble(
                        acc -> acc).toArray();

                lineChartAccuracies.update(idxCurrentBatch, accuracies);
            } else
                // One-dimensional classification
                lineChartGlobalAccuracy.update(idxCurrentBatch, results.get("Accuracy"));
        }
    }

    private void printResults(Map<String, Double> results, ConceptDriftAdaptiveMethod conceptDriftAdapter,
                              Entry<List<String>, Long> newFeaturesUpdateTime, int numSequencesBatch) {
        System.out.println("--------------------------Results Chunk " + (numProcessedBatches - 1) * batchSize + "-" +
                ((numProcessedBatches - 1) * batchSize + numSequencesBatch) + "--------------------------");
        // Result concept drift adapter
        if (conceptDriftAdapter != null)
            System.out.println(conceptDriftAdapter.getResults());
        // Batch results
        System.out.println("##### Results batch #####");
        results.forEach((metric, value) -> System.out.println(metric + " = " + value));
        // Results addition of new features
        if (newFeaturesUpdateTime != null)
            System.out.println("Time to include new variables " + newFeaturesUpdateTime.getKey() + " to the model: " +
                    newFeaturesUpdateTime.getValue());
        // Results taking into account the results in previous batches
        // Update mean global accuracy
        System.out.println("##### Mean results data stream #####");
        double accuracyBatch;
        if (results.containsKey("Global accuracy"))
            accuracyBatch = results.get("Global accuracy");
        else
            accuracyBatch = results.get("Accuracy");
        meanGlobalAccuracy = meanGlobalAccuracy * (numProcessedBatches - 1) / numProcessedBatches +
                accuracyBatch / numProcessedBatches;
        System.out.println("Mean global accuracy: " + meanGlobalAccuracy);
        if (results.containsKey("Mean accuracy")) {
            meanMeanAccuracy = meanMeanAccuracy * (numProcessedBatches - 1) / numProcessedBatches +
                    results.get("Mean accuracy") / numProcessedBatches;
            System.out.println("Mean mean accuracy: " + meanMeanAccuracy);
        }
        meanMacroAveragedPrecision = meanMacroAveragedPrecision * (numProcessedBatches - 1) / numProcessedBatches +
                results.get("Macro-averaged precision") / numProcessedBatches;
        meanMacroAveragedRecall = meanMacroAveragedRecall * (numProcessedBatches - 1) / numProcessedBatches +
                results.get("Macro-averaged recall") / numProcessedBatches;
        meanMacroAveragedF1Score = meanMacroAveragedF1Score * (numProcessedBatches - 1) / numProcessedBatches +
                results.get("Macro-averaged F1 score") / numProcessedBatches;
        meanMicroAveragedPrecision = meanMicroAveragedPrecision * (numProcessedBatches - 1) / numProcessedBatches +
                results.get("Micro-averaged precision") / numProcessedBatches;
        meanMicroAveragedRecall = meanMicroAveragedRecall * (numProcessedBatches - 1) / numProcessedBatches +
                results.get("Micro-averaged recall") / numProcessedBatches;
        meanMicroAveragedF1Score = meanMicroAveragedF1Score * (numProcessedBatches - 1) / numProcessedBatches +
                results.get("Micro-averaged F1 score") / numProcessedBatches;
        System.out.println("Macro-averaged precision: " + meanMacroAveragedPrecision);
        System.out.println("Macro-averaged recall: " + meanMacroAveragedRecall);
        System.out.println("Macro-averaged F1 score: " + meanMacroAveragedF1Score);
        System.out.println("Micro-averaged precision: " + meanMicroAveragedPrecision);
        System.out.println("Micro-averaged recall: " + meanMicroAveragedRecall);
        System.out.println("Micro-averaged F1 score: " + meanMicroAveragedF1Score);
        if (results.containsKey("Global Brier score")) {
            meanGlobalBrierScore = meanGlobalBrierScore * (numProcessedBatches - 1) / numProcessedBatches +
                    results.get("Global Brier score") / numProcessedBatches;
            System.out.println("Mean global Brier score: " + meanGlobalBrierScore);
        } else if (results.containsKey("Brier score")) {
            meanGlobalBrierScore = meanGlobalBrierScore * (numProcessedBatches - 1) / numProcessedBatches +
                    results.get("Brier score") / numProcessedBatches;
            System.out.println("Mean Brier score: " + meanGlobalBrierScore);
        }
        // Running time results
        System.out.println("Average time updating model (ms): " + meanUpdatingTimeSeconds);
        System.out.println("Total time updating model (ms): " + totalUpdatingTimeSeconds);
        System.out.println("Times the model was updated: " + numTimesModelUpdated);
        System.out.println(
                "------------------------------------------------------------------------------------------------------");
    }

    private void createLineCharts() {
        if (lineChartGlobalAccuracy == null)
            lineChartGlobalAccuracy = UserInterfaceUtil.createXYLineChart("Global accuracy Evolution", "Batch Number",
                    "Global Accuracy", new int[]{0, 1}, "Global accuracy");
        if (lineChartAccuracies == null)
            lineChartAccuracies = UserInterfaceUtil.createXYLineChart("Accuracy Evolution", "Batch Number", "Accuracy",
                    new int[]{0, 1}, nameClassVariables.toArray(new String[0]));
    }

    /**
     * Sets the flag indicating whether the results of the experiment should be saved.
     *
     * @param areResultsSaved a boolean value specifying whether to save the experiment results. `true` to save
     *                        results, `false` otherwise.
     */
    public void setAreResultsSaved(boolean areResultsSaved) {
        this.areResultsSaved = areResultsSaved;
    }

}
