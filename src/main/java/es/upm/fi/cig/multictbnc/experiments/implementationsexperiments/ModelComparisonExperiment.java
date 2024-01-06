package es.upm.fi.cig.multictbnc.experiments.implementationsexperiments;

import es.upm.fi.cig.multictbnc.classification.ClassifierFactory;
import es.upm.fi.cig.multictbnc.data.reader.DatasetReader;
import es.upm.fi.cig.multictbnc.data.reader.MultipleCSVReader;
import es.upm.fi.cig.multictbnc.exceptions.ErroneousValueException;
import es.upm.fi.cig.multictbnc.exceptions.UnreadDatasetException;
import es.upm.fi.cig.multictbnc.experiments.AbstractExperiment;
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
import es.upm.fi.cig.multictbnc.performance.CrossValidationBinaryRelevanceMethod;
import es.upm.fi.cig.multictbnc.performance.ValidationMethod;
import es.upm.fi.cig.multictbnc.performance.ValidationMethodFactory;
import es.upm.fi.cig.multictbnc.util.Util;
import es.upm.fi.cig.multictbnc.writers.performance.ExcelExperimentsWriter;
import es.upm.fi.cig.multictbnc.writers.performance.MetricsWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.*;

/**
 * Represents an experiment for comparing different models' performance on datasets with different settings.
 *
 * @author Carlos Villa Blanco
 */
public class ModelComparisonExperiment extends AbstractExperiment {
    private static final Logger logger = LogManager.getLogger(ModelComparisonExperiment.class);
    // ---------------------------- Experiment settings ----------------------------
    // Class subgraph
    static String NAMEBNPLA = "Bayesian estimation"; // "Bayesian estimation", "Maximum likelihood estimation"
    static String NAMEBNSLA = "Hill climbing"; // "Hill climbing", "Random-restart hill climbing"
    // Bridge and feature subgraphs
    static String NAMECTBNPLA = "Bayesian estimation";
    static String NAMECTBNSLA = "Hill climbing";
    static String INITIALSTRUCTURE = "Empty"; // Initial structure: "Empty", "Naive Bayes"
    static String MAXK = "1"; // Maximum number of feature parents (if maxK is used)
    // Evaluation method
    static int FOLDS = 5; // For cross-validation
    static boolean ESTIMATEPROBABILITIES = true;
    static boolean SHUFFLESEQUENCES = true;
    List<List<String>> datasetsAll;
    String nameTimeVariable;
    List<String> nameClassVariables;
    List<String> nameFeatureVariables;
    List<Long> seeds;
    List<String> models;
    String scoreFunction;
    String penalisationFunction;
    Map<String, String> hyperparameters;
    // ---------------------------- Parameter learning algorithms compared models ----------------------------
    BNParameterLearningAlgorithm[] bnPLAModels;
    CTBNParameterLearningAlgorithm[] ctbnPLAModels;

    /**
     * Constructor for the ModelComparisonExperiment class.
     *
     * @param args command-line arguments passed to the experiment
     */
    public ModelComparisonExperiment(String... args) {
        super(args);
        // Retrieve experiment to perform
        String selectedExperiment = getExperimentConfig().poll();
        this.datasetsAll = getPathDatasets(selectedExperiment);
        // Retrieve models to compare
        String model1 = getExperimentConfig().poll();
        String model2 = getExperimentConfig().poll();
        this.models = List.of(model1, model2);
        // Retrieve score function: "Log-likelihood", "Bayesian Dirichlet equivalent", "Conditional log-likelihood"
        this.scoreFunction = getExperimentConfig().poll();
        // Penalisation (except for "Bayesian Dirichlet equivalent"): "BIC", "AIC", "No"
        this.penalisationFunction = getExperimentConfig().poll();
        // Retrieve hyperparameters prior distributions on model's parameters
        Map<String, Double> priorDistHyper1 = retrieveHyperparametersModel(getExperimentConfig());
        Map<String, Double> priorDistHyper2 = retrieveHyperparametersModel(getExperimentConfig());
        this.nameTimeVariable = getTimeVariable(selectedExperiment);
        this.nameClassVariables = getClassVariables(selectedExperiment);
        this.nameFeatureVariables = getFeatureVariables(selectedExperiment, getExperimentConfig());
        this.seeds = getSeeds(selectedExperiment);
        // Define parameter learning algorithms compared models
        this.bnPLAModels = new BNParameterLearningAlgorithm[2];
        this.ctbnPLAModels = new CTBNParameterLearningAlgorithm[2];
        bnPLAModels[0] = getBNParameterLearningAlg(priorDistHyper1.get("NX"));
        ctbnPLAModels[0] = getCTBNParameterLearningAlg(priorDistHyper1.get("MXY"), priorDistHyper1.get("TX"));
        bnPLAModels[1] = getBNParameterLearningAlg(priorDistHyper2.get("NX"));
        ctbnPLAModels[1] = getCTBNParameterLearningAlg(priorDistHyper2.get("MXY"), priorDistHyper2.get("TX"));
        // Hyperparameters that could be necessary for the generation of the models
        this.hyperparameters = new WeakHashMap<>();
        hyperparameters.put("maxK", MAXK);
    }

    private static List<List<String>> getPathDatasets(String selectedExperiment) {
        switch (selectedExperiment) {
            case ("synthetic"):
                return List.of(
                        List.of("datasets/VillaBlancoEtAl2021/synthetic/Experiment1/dataset0",
                                "datasets/VillaBlancoEtAl2021/synthetic/Experiment1/dataset1",
                                "datasets/VillaBlancoEtAl2021/synthetic/Experiment1/dataset2",
                                "datasets/VillaBlancoEtAl2021/synthetic/Experiment1/dataset3",
                                "datasets/VillaBlancoEtAl2021/synthetic/Experiment1/dataset4"),
                        List.of("datasets/VillaBlancoEtAl2021/synthetic/Experiment2/dataset0",
                                "datasets/VillaBlancoEtAl2021/synthetic/Experiment2/dataset1",
                                "datasets/VillaBlancoEtAl2021/synthetic/Experiment2/dataset2",
                                "datasets/VillaBlancoEtAl2021/synthetic/Experiment2/dataset3",
                                "datasets/VillaBlancoEtAl2021/synthetic/Experiment2/dataset4"),
                        List.of("datasets/VillaBlancoEtAl2021/synthetic/Experiment3/dataset0",
                                "datasets/VillaBlancoEtAl2021/synthetic/Experiment3/dataset1",
                                "datasets/VillaBlancoEtAl2021/synthetic/Experiment3/dataset2",
                                "datasets/VillaBlancoEtAl2021/synthetic/Experiment3/dataset3",
                                "datasets/VillaBlancoEtAl2021/synthetic/Experiment3/dataset4"),
                        List.of("datasets/VillaBlancoEtAl2021/synthetic/Experiment4/dataset0",
                                "datasets/VillaBlancoEtAl2021/synthetic/Experiment4/dataset1",
                                "datasets/VillaBlancoEtAl2021/synthetic/Experiment4/dataset2",
                                "datasets/VillaBlancoEtAl2021/synthetic/Experiment4/dataset3",
                                "datasets/VillaBlancoEtAl2021/synthetic/Experiment4/dataset4"),
                        List.of("datasets/VillaBlancoEtAl2021/synthetic/Experiment5/dataset0",
                                "datasets/VillaBlancoEtAl2021/synthetic/Experiment5/dataset1",
                                "datasets/VillaBlancoEtAl2021/synthetic/Experiment5/dataset2",
                                "datasets/VillaBlancoEtAl2021/synthetic/Experiment5/dataset3",
                                "datasets/VillaBlancoEtAl2021/synthetic/Experiment5/dataset4"),
                        List.of("datasets/VillaBlancoEtAl2021/synthetic/Experiment6/dataset0",
                                "datasets/VillaBlancoEtAl2021/synthetic/Experiment6/dataset1",
                                "datasets/VillaBlancoEtAl2021/synthetic/Experiment6/dataset2",
                                "datasets/VillaBlancoEtAl2021/synthetic/Experiment6/dataset3",
                                "datasets/VillaBlancoEtAl2021/synthetic/Experiment6/dataset4"),
                        List.of("datasets/VillaBlancoEtAl2021/synthetic/Experiment7/dataset0",
                                "datasets/VillaBlancoEtAl2021/synthetic/Experiment7/dataset1",
                                "datasets/VillaBlancoEtAl2021/synthetic/Experiment7/dataset2",
                                "datasets/VillaBlancoEtAl2021/synthetic/Experiment7/dataset3",
                                "datasets/VillaBlancoEtAl2021/synthetic/Experiment7/dataset4"),
                        List.of("datasets/VillaBlancoEtAl2021/synthetic/Experiment8/dataset0",
                                "datasets/VillaBlancoEtAl2021/synthetic/Experiment8/dataset1",
                                "datasets/VillaBlancoEtAl2021/synthetic/Experiment8/dataset2",
                                "datasets/VillaBlancoEtAl2021/synthetic/Experiment8/dataset3",
                                "datasets/VillaBlancoEtAl2021/synthetic/Experiment8/dataset4"),
                        List.of("datasets/VillaBlancoEtAl2021/synthetic/Experiment9/dataset0",
                                "datasets/VillaBlancoEtAl2021/synthetic/Experiment9/dataset1",
                                "datasets/VillaBlancoEtAl2021/synthetic/Experiment9/dataset2",
                                "datasets/VillaBlancoEtAl2021/synthetic/Experiment9/dataset3",
                                "datasets/VillaBlancoEtAl2021/synthetic/Experiment9/dataset4"),
                        List.of("datasets/VillaBlancoEtAl2021/synthetic/Experiment10/dataset0",
                                "datasets/VillaBlancoEtAl2021/synthetic/Experiment10/dataset1",
                                "datasets/VillaBlancoEtAl2021/synthetic/Experiment10/dataset2",
                                "datasets/VillaBlancoEtAl2021/synthetic/Experiment10/dataset3",
                                "datasets/VillaBlancoEtAl2021/synthetic/Experiment10/dataset4"
                        ));
            case ("energy"):
                return List.of(List.of("datasets/VillaBlancoEtAl2021/energy"));
            case ("britishHousehold"):
                return List.of(List.of("datasets/VillaBlancoEtAl2023/section_5_3_british_household_panel_survey" +
                        "/british_household_dataset"));
            default:
                logger.error("Selected experiment was not found");
                return null;
        }
    }

    private static String getTimeVariable(String selectedExperiment) {
        switch (selectedExperiment) {
            case ("synthetic"):
            case ("energy"):
            case ("britishHousehold"):
                return "t";
            default:
                logger.error("Selected experiment was not found");
                return null;
        }
    }

    private static List<String> getClassVariables(String selectedExperiment) {
        switch (selectedExperiment) {
            case ("synthetic"):
                return List.of("C1", "C2", "C3", "C4", "C5");
            case ("energy"):
                return List.of("M1", "M2", "M3", "M4", "M5", "M6", "M7", "M8");
            case ("britishHousehold"):
                return List.of("Smoker", "Employment status", "Sex", "Dental check-up", "Responsible adult for child",
                        "Lives with spouse or partner", "Limb, back or neck problems");
            default:
                logger.error("Selected experiment was not found");
                return null;
        }
    }

    private static List<String> getFeatureVariables(String selectedExperiment, Queue<String> experimentConfig) {
        switch (selectedExperiment) {
            case ("synthetic"):
                return List.of("X1", "X2", "X3", "X4", "X5");
            case ("energy"):
                if (experimentConfig.isEmpty())
                    return List.of("IA", "IB", "IC", "VA", "VB", "VC", "SA", "SB", "SC", "PA", "PB", "PC",
                            "QA", "QB", "QC");
                else
                    return Util.stringToList(experimentConfig.poll());
            case ("britishHousehold"):
                return null;
            default:
                logger.error("Selected experiment was not found");
                return null;
        }
    }

    private static List<Long> getSeeds(String selectedExperiment) {
        switch (selectedExperiment) {
            case ("synthetic"):
            case ("britishHousehold"):
                return List.of(10L);
            case ("energy"):
                return List.of(203901165L, 210776381L, 219721216L, 168929L, 71283273L, 154241767L, 61801568L,
                        118950040L, 62100514L, 13014671L, 40044639L, 197151791L, 25959076L, 135446808L, 165931238L);
            default:
                logger.error("Selected experiment was not found");
                return null;
        }
    }

    private static void performExperiment(DatasetReader datasetReader, BNParameterLearningAlgorithm bnPLA,
                                          CTBNParameterLearningAlgorithm ctbnPLA, String selectedModel,
                                          Map<String, String> paramSLA, Map<String, String> hyperparameters, long seed,
                                          MetricsWriter metricsWriter)
            throws UnreadDatasetException, ErroneousValueException {
        // Define structure learning algorithms
        StructureLearningAlgorithm bnSLA = StructureLearningAlgorithmFactory.getAlgorithmBN(NAMEBNSLA, paramSLA);
        StructureLearningAlgorithm ctbnSLA = StructureLearningAlgorithmFactory.getAlgorithmCTBN(NAMECTBNSLA, paramSLA);
        BNLearningAlgorithms bnLearningAlgs = new BNLearningAlgorithms(bnPLA, bnSLA);
        CTBNLearningAlgorithms ctbnLearningAlgs = new CTBNLearningAlgorithms(ctbnPLA, ctbnSLA);
        // Generate selected model and validation method
        MultiCTBNC<CPTNode, CIMNode> model;
        ValidationMethod validationMethod;
        // Cross-validation may be performed using a binary relevance or a unique model
        if (selectedModel.equals("CTBNCs")) {
            model = ClassifierFactory.getMultiCTBNC("Multi-CTBNC", bnLearningAlgs, ctbnLearningAlgs, hyperparameters,
                    CPTNode.class, CIMNode.class);
            validationMethod = new CrossValidationBinaryRelevanceMethod(datasetReader, FOLDS, ESTIMATEPROBABILITIES,
                    SHUFFLESEQUENCES, seed);
        } else if (selectedModel.equals("maxK CTBNCs")) {
            model = ClassifierFactory.getMultiCTBNC("DAG-maxK Multi-CTBNC", bnLearningAlgs, ctbnLearningAlgs,
                    hyperparameters, CPTNode.class, CIMNode.class);
            validationMethod = new CrossValidationBinaryRelevanceMethod(datasetReader, FOLDS, ESTIMATEPROBABILITIES,
                    SHUFFLESEQUENCES, seed);
        } else {
            model = ClassifierFactory.getMultiCTBNC(selectedModel, bnLearningAlgs, ctbnLearningAlgs, hyperparameters,
                    CPTNode.class, CIMNode.class);
            validationMethod = ValidationMethodFactory.getValidationMethod("Cross-validation", datasetReader, null, 0,
                    FOLDS, ESTIMATEPROBABILITIES, SHUFFLESEQUENCES, seed);
        }
        // Set output to show results
        validationMethod.setWriter(metricsWriter);
        // Define initial structure
        model.setInitialStructure(INITIALSTRUCTURE);
        // Evaluate the performance of the model
        validationMethod.evaluate(model);
    }

    private static String getNameExperiment(List<String> datasets) {
        if (datasets.size() > 1)
            return Paths.get(datasets.get(0)).getParent().getFileName().toString();
        return Paths.get(datasets.get(0)).getFileName().toString();
    }

    /**
     * Check if hyperparameters for the prior distribution on the model's parameter are provided. If hyperparameters
     * are not provided, default values are used.
     *
     * @param experimentConfig queue of experiment configuration parameters
     * @return map of hyperparameters for the model's prior distribution
     */
    private Map<String, Double> retrieveHyperparametersModel(Queue<String> experimentConfig) {
        Map<String, Double> hyperparametersParametersPriorDistribution = new HashMap<String, Double>();
        if (!experimentConfig.isEmpty()) {
            hyperparametersParametersPriorDistribution = Util.stringToMap(experimentConfig.poll());
        } else {
            hyperparametersParametersPriorDistribution.put("NX", 1.0);
            hyperparametersParametersPriorDistribution.put("MXY", 100.0);
            hyperparametersParametersPriorDistribution.put("TX", 10.0);
        }
        return hyperparametersParametersPriorDistribution;
    }

    private BNParameterLearningAlgorithm getBNParameterLearningAlg(Double NX) {
        // Define parameter learning algorithms
        return BNParameterLearningAlgorithmFactory.getAlgorithm(NAMEBNPLA, NX);
    }

    private CTBNParameterLearningAlgorithm getCTBNParameterLearningAlg(Double MXY, Double TX) {
        return CTBNParameterLearningAlgorithmFactory.getAlgorithm(NAMECTBNPLA, MXY, TX);
    }

    @Override
    public void execute() {
        // Iterate over experiments
        for (List<String> datasets : getAllDatasets()) {
            MetricsWriter metricsWriter = getMetricsWriter(datasets);
            // Iterate over different permutations of the same dataset
            for (long seed : seeds) {
                logger.info("---------- Score function: {} ----------", scoreFunction);
                Map<String, String> paramSLA = Map.of("scoreFunction", scoreFunction, "penalisationFunction",
                        penalisationFunction);
                // Iterate over the datasets that are evaluated
                for (String pathDataset : datasets) {
                    if (seeds.size() > 1)
                        logger.info("########## DATASET: {} (Shuffling seed: {}) " + "##########", pathDataset, seed);
                    else
                        logger.info("########## DATASET: {} ##########", pathDataset);
                    // Retrieve dataset with the necessary variables for the model
                    DatasetReader datasetReader;
                    try {
                        datasetReader = new MultipleCSVReader(pathDataset);
                        datasetReader.setTimeAndClassVariables(nameTimeVariable, nameClassVariables);
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    } catch (UnreadDatasetException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        for (int idxModel = 0; idxModel < models.size(); idxModel++) {
                            String selectedModel = models.get(idxModel);

                            logger.info("********** MODEL: {} " + "**********", selectedModel);
                            performExperiment(datasetReader, bnPLAModels[idxModel], ctbnPLAModels[idxModel],
                                    selectedModel, paramSLA, hyperparameters, seed, metricsWriter);
                        }
                    } catch (Exception e) {
                        logger.error(e.getMessage());
                    }
                }
            }
            metricsWriter.close();
        }
    }

    private List<List<String>> getAllDatasets() {
        return this.datasetsAll;
    }

    private MetricsWriter getMetricsWriter(List<String> datasets) {
        // Define output to store the results of the experiments
        String nameExperiment = getNameExperiment(datasets);
        String filename = getNameResultFile(nameExperiment);
        filename = filename + "_" + bnPLAModels[0].getParametersAlgorithm().get("nx") + "_" +
                ctbnPLAModels[0].getParametersAlgorithm().get("mxy") + "_" +
                ctbnPLAModels[0].getParametersAlgorithm().get("tx");
        return new ExcelExperimentsWriter(List.of(this.scoreFunction), datasets, this.models, this.nameClassVariables,
                this.nameFeatureVariables, this.bnPLAModels[0], this.ctbnPLAModels[0], this.penalisationFunction,
                this.INITIALSTRUCTURE, this.seeds, filename);
    }

    private String getNameResultFile(String nameExperiment) {
        String filename = nameExperiment + "_" + models.get(0).replaceAll("\\s+", "_") + "_" +
                models.get(1).replaceAll("\\s+", "_") + "_" + scoreFunction.replaceAll("\\s+", "_");
        return filename;
    }

}
