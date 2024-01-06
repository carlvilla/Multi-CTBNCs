package es.upm.fi.cig.multictbnc.performance;

import es.upm.fi.cig.multictbnc.classification.ClassifierFactory;
import es.upm.fi.cig.multictbnc.classification.Prediction;
import es.upm.fi.cig.multictbnc.data.reader.DatasetReader;
import es.upm.fi.cig.multictbnc.data.representation.Dataset;
import es.upm.fi.cig.multictbnc.data.representation.Sequence;
import es.upm.fi.cig.multictbnc.data.representation.State;
import es.upm.fi.cig.multictbnc.exceptions.ErroneousValueException;
import es.upm.fi.cig.multictbnc.exceptions.UnreadDatasetException;
import es.upm.fi.cig.multictbnc.learning.BNLearningAlgorithms;
import es.upm.fi.cig.multictbnc.learning.CTBNLearningAlgorithms;
import es.upm.fi.cig.multictbnc.learning.parameters.bn.BNParameterLearningAlgorithm;
import es.upm.fi.cig.multictbnc.learning.parameters.bn.BNParameterLearningAlgorithmFactory;
import es.upm.fi.cig.multictbnc.learning.parameters.ctbn.CTBNParameterLearningAlgorithm;
import es.upm.fi.cig.multictbnc.learning.parameters.ctbn.CTBNParameterLearningAlgorithmFactory;
import es.upm.fi.cig.multictbnc.learning.structure.StructureLearningAlgorithm;
import es.upm.fi.cig.multictbnc.learning.structure.StructureLearningAlgorithmFactory;
import es.upm.fi.cig.multictbnc.models.MultiCTBNC;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.IntStream;

/**
 * Implements a validation method for evaluating CTBNCs using a test. This method involves training separate models
 * for each class variable and then combining their predictions to evaluate the overall performance of the model on
 * the test dataset.
 *
 * @author Carlos Villa Blanco
 */
public class TestDatasetBinaryRelevanceMethod extends ValidationMethod {
    private final Logger logger = LogManager.getLogger(TestDatasetBinaryRelevanceMethod.class);
    private DatasetReader trainingDatasetReader;
    private DatasetReader testDatasetReader;
    private boolean estimateProbabilities;
    private double lastLearningTimeModelsSeconds;

    /**
     * Constructor that receives the dataset readers and configuration.
     *
     * @param trainingDatasetReader reader for the training dataset
     * @param testDatasetReader     reader for the test dataset
     * @param estimateProbabilities flag indicating if probabilities should be estimated
     * @throws UnreadDatasetException if the dataset cannot be read
     */
    public TestDatasetBinaryRelevanceMethod(DatasetReader trainingDatasetReader, DatasetReader testDatasetReader, boolean estimateProbabilities) throws UnreadDatasetException {
        super();
        this.logger.info(
                "Preparing test method for independent CTBNCs");
        // Obtain the dataset and the number of sequences it contains
        this.trainingDatasetReader = trainingDatasetReader;
        this.testDatasetReader = testDatasetReader;
        this.testDatasetReader.removeZeroVarianceVariables(false);
        this.estimateProbabilities = estimateProbabilities;
    }

    @Override
    public Map<String, Double> evaluate(MultiCTBNC<?, ?> model) throws UnreadDatasetException,
            ErroneousValueException {
        // Retrieve selected training dataset
        Dataset trainingDataset = this.trainingDatasetReader.readDataset();
        // Retrieve selected test dataset
        Dataset testDataset = this.testDatasetReader.readDataset();
        // Learn one model per class variable in parallel
        List<MultiCTBNC<?, ?>> models = learnModels(model, trainingDataset);
        // Perform predictions with each model and merge the results
        Prediction[] predictions = predict(models, testDataset);
        // Evaluate the performance of the model
        Map<String, Double> results = Metrics.evaluate(predictions, testDataset);
        // Display results
        displayResultsHoldOut(model, results);
        return results;
    }

    @Override
    public Map<String, Double> evaluate(MultiCTBNC<?, ?> model, double preprocessingExecutionTime)
            throws UnreadDatasetException, ErroneousValueException {
        return null;
    }

    private List<MultiCTBNC<?, ?>> learnModels(MultiCTBNC<?, ?> model, Dataset trainingDataset) {
        // Get name class variables
        List<String> nameCVs = trainingDataset.getNameClassVariables();
        // Create as many models as class variables. Required for parallelisation
        List<MultiCTBNC<?, ?>> models = new ArrayList<>();
        // Training datasets for each class variable. Sequences are not duplicated
        List<Dataset> datasets = new ArrayList<>();
        for (int i = 0; i < nameCVs.size(); i++) {
            // An instance of the parameter and structure learning algorithms for each model
            BNLearningAlgorithms bnLearningAlgs = getLearningAlgorithmsBN(model);
            CTBNLearningAlgorithms ctbnLearningAlgs = getLearningAlgorithmsCTBN(model);
            // Define model for one class variable
            MultiCTBNC<?, ?> modelCV = ClassifierFactory.getMultiCTBNC(model.getModelIdentifier(), bnLearningAlgs,
                    ctbnLearningAlgs, model.getHyperparameters(), model.getTypeNodeClassVariable(),
                    model.getTypeNodeFeature());
            modelCV.setInitialStructure(model.getInitialStructure());
            models.add(modelCV);
            // Define a training dataset that ignores all class variables except one
            Dataset dataset = new Dataset(trainingDataset.getSequences());
            List<String> nameClassVariables = new ArrayList<>(nameCVs);
            nameClassVariables.remove(nameCVs.get(i));
            dataset.setIgnoredClassVariables(nameClassVariables);
            datasets.add(dataset);
        }
        // Train models in parallel
        Instant start = Instant.now();
        IntStream.range(0, nameCVs.size()).parallel().forEach(indexModel -> {
            try {
                // Train the model
                models.get(indexModel).learn(datasets.get(indexModel));
            } catch (ErroneousValueException eve) {
                logger.error("Model for class variable " + nameCVs + "could not be learnt." + eve.getMessage());
            }
        });
        Instant end = Instant.now();
        this.lastLearningTimeModelsSeconds = Duration.between(start, end).toMillis() / 1000.f;
        this.logger.info("All classifiers learnt in {}", this.lastLearningTimeModelsSeconds);
        return models;
    }

    private Prediction[] predict(List<MultiCTBNC<?, ?>> models, Dataset testingDataset) {
        List<String> nameCVs = testingDataset.getNameClassVariables();
        Prediction[] predictionsAllClassVariables = null;
        for (int i = 0; i < models.size(); i++) {
            // Display learnt model
            System.out.println(MessageFormat.format(
                    "------------------------Model for class variable " + "{0}------------------------",
                    nameCVs.get(i)));
            displayModel(models.get(i));
            System.out.println("---------------------------------------------------------------------------");
            Prediction[] predictionsOneClassVariable = models.get(i).predict(testingDataset, this.estimateProbabilities);
            predictionsAllClassVariables = updatePredictions(predictionsAllClassVariables, nameCVs.get(i),
                    predictionsOneClassVariable);
        }
        return predictionsAllClassVariables;
    }

    private void displayResultsHoldOut(MultiCTBNC<?, ?> model, Map<String, Double> results) {
        System.out.println("--------------------------Results hold-out validation--------------------------");
        displayResults(results);
        displayModel(model);
        System.out.println("-------------------------------------------------------------------------------");
    }

    private BNLearningAlgorithms getLearningAlgorithmsBN(MultiCTBNC<?, ?> model) {
        // Retrieve learning algorithms
        BNLearningAlgorithms bnLA = model.getLearningAlgsBN();
        String nameBnPLA = bnLA.getParameterLearningAlgorithm().getIdentifier();
        String nameBnSLA = bnLA.getStructureLearningAlgorithm().getIdentifier();
        // Retrieve parameters of the parameter learning algorithm
        Map<String, String> parametersBnPLA = bnLA.getParameterLearningAlgorithm().getParametersAlgorithm();
        double nx = Double.valueOf(parametersBnPLA.getOrDefault("nx", "0"));
        // Retrieve parameters of the structure learning algorithm
        Map<String, String> parametersBnSLA = bnLA.getStructureLearningAlgorithm().getParametersAlgorithm();
        // Create learning algorithms
        BNParameterLearningAlgorithm bnPLA = BNParameterLearningAlgorithmFactory.getAlgorithm(nameBnPLA, nx);
        StructureLearningAlgorithm bnSLA = StructureLearningAlgorithmFactory.getAlgorithmBN(nameBnSLA,
                parametersBnSLA);

        return new BNLearningAlgorithms(bnPLA, bnSLA);
    }

    private CTBNLearningAlgorithms getLearningAlgorithmsCTBN(MultiCTBNC<?, ?> model) {
        // Retrieve learning algorithms
        CTBNLearningAlgorithms ctbnLA = model.getLearningAlgsCTBN();
        String nameCtbnPLA = ctbnLA.getParameterLearningAlgorithm().getIdentifier();
        String nameCtbnSLA = ctbnLA.getStructureLearningAlgorithm().getIdentifier();
        // Retrieve parameters of the parameter learning algorithm
        Map<String, String> parametersCtbnPLA = ctbnLA.getParameterLearningAlgorithm().getParametersAlgorithm();
        double mxy = Double.valueOf(parametersCtbnPLA.getOrDefault("mxy", "0"));
        double tx = Double.valueOf(parametersCtbnPLA.getOrDefault("tx", "0"));
        // Retrieve parameters of the structure learning algorithm
        Map<String, String> parametersCtbnSLA = ctbnLA.getStructureLearningAlgorithm().getParametersAlgorithm();
        // Create learning algorithms
        CTBNParameterLearningAlgorithm ctbnPLA = CTBNParameterLearningAlgorithmFactory.getAlgorithm(nameCtbnPLA, mxy,
                tx);
        StructureLearningAlgorithm ctbnSLA = StructureLearningAlgorithmFactory.getAlgorithmCTBN(nameCtbnSLA,
                parametersCtbnSLA);
        return new CTBNLearningAlgorithms(ctbnPLA, ctbnSLA);
    }

    private Prediction[] updatePredictions(Prediction[] predictionsAllClassVariables, String nameCV,
                                           Prediction[] predictionsOneClassVariable) {
        if (predictionsAllClassVariables == null)
            // First class is predicted
            return predictionsOneClassVariable;
        // Update the predicted class configuration for each sequence and the
        // probabilities given to each class configuration (necessary for Brier score)
        for (int i = 0; i < predictionsAllClassVariables.length; i++) {
            // Get prediction current class variable and sequence
            State prediction = predictionsOneClassVariable[i].getPredictedClasses();
            // Update current class configuration (CC) of the sequence
            State predictedCC = predictionsAllClassVariables[i].getPredictedClasses();
            predictedCC.addEvent(nameCV, prediction.getValueVariable(nameCV));
            // Define probabilities of each class configuration for the current sequence
            Map<State, Double> newProbabilitiesCCs = new HashMap<>();
            if (this.estimateProbabilities) {
                // Iterate over class configurations including the states of the current class
                // variable
                Set<State> statesCC = predictionsAllClassVariables[i].getProbabilities().keySet();
                Set<State> statesCV = predictionsOneClassVariable[i].getProbabilities().keySet();
                for (State stateCC : statesCC)
                    for (State classCV : statesCV) {
                        // Get a new class configuration that includes the state of the class variable
                        State newStateCC = new State(stateCC.getEvents());
                        newStateCC.addEvents(classCV.getEvents());
                        // Get probability new class configuration
                        double previousProbCC = predictionsAllClassVariables[i].getProbabilities().get(stateCC);
                        double newProbCC = previousProbCC * predictionsOneClassVariable[i].getProbabilities().get(classCV);
                        // Save new class configuration and its probability
                        newProbabilitiesCCs.put(newStateCC, newProbCC);
                    }
                // Save probabilities class configurations for the sequence
                predictionsAllClassVariables[i].setProbabilities(newProbabilitiesCCs);
                // Get probability predicted class configuration for the sequence
                double probabilityPredictedCC = predictionsAllClassVariables[i].getProbabilityPrediction();
                // Get probability predicted class for the current class variable
                double probabilityPredictedClass = predictionsOneClassVariable[i].getProbabilityPrediction();
                // Save new probability predicted class configuration
                double probabilityNewPredictedCC = probabilityPredictedCC * probabilityPredictedClass;
                predictionsAllClassVariables[i].setProbabilityPrediction(probabilityNewPredictedCC);
            }
        }
        return predictionsAllClassVariables;
    }

}