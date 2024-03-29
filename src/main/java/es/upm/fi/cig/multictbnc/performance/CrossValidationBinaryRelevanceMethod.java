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
import es.upm.fi.cig.multictbnc.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.IntStream;

/**
 * Implements a cross-validation method used to learn one CTBNC for each class variable and merge the results.
 *
 * @author Carlos Villa Blanco
 */
public class CrossValidationBinaryRelevanceMethod extends ValidationMethod {
	private final Logger logger = LogManager.getLogger(CrossValidationBinaryRelevanceMethod.class);
	private Dataset dataset;
	private int folds;
	private boolean estimateProbabilities;
	private boolean shuffle;
	private long seed;
	private double lastLearningTimeModelsSeconds;

	/**
	 * Constructor for cross-validation method.
	 *
	 * @param datasetReader         a {@code DatasetReader} to read the dataset
	 * @param folds                 number of folds
	 * @param estimateProbabilities true to estimate the probabilities of the class configurations, false otherwise
	 * @param shuffle               true to shuffle the sequences, false otherwise
	 * @param seed                  seed used to shuffle the sequences
	 * @throws UnreadDatasetException if the provided dataset could not be read
	 */
	public CrossValidationBinaryRelevanceMethod(DatasetReader datasetReader, int folds, boolean estimateProbabilities,
												boolean shuffle, long seed) throws UnreadDatasetException {
		super();
		this.logger.info(
				"Preparing {}-cross-validation for independent CTBNCs / Shuffle: {} / Estimate probabilities: {}",
				folds, shuffle, estimateProbabilities);
		// Obtain the dataset and the number of sequences it contains
		this.dataset = datasetReader.readDataset();
		this.logger.info("Time variable: {}", this.dataset.getNameTimeVariable());
		this.logger.info("Feature variables: {}", this.dataset.getNameFeatureVariables());
		this.logger.info("Class variables: {}", (this.dataset.getNameClassVariables()));
		// Define all the states of the class variables in the dataset
		this.dataset.initialiazeStatesClassVariables();
		// Check that the specified number of folds is valid
		if (folds < 2 || folds > this.dataset.getNumDataPoints())
			this.logger.warn(
					"Number of folds must be between 2 and the dataset size (leave-one-out " + "cross-validation)");
		this.folds = folds;
		this.estimateProbabilities = estimateProbabilities;
		this.shuffle = shuffle;
		this.seed = seed;
	}

	/**
	 * Evaluates the performance of the specified model using cross-validation.
	 *
	 * @param model model to evaluate
	 * @return the results of the cross-validation in a {@code Map}
	 */
	@Override
	public Map<String, Double> evaluate(MultiCTBNC<?, ?> model) {
		// Get sequences from the dataset
		List<Sequence> sequences = getSequencesDataset();
		int numSequences = sequences.size();
		// Obtain the size of each fold
		int[] sizeFolds = defineSizeFolds(numSequences);
		Map<String, Double> resultsCrossValidation = performCrossValidation(model, sequences, sizeFolds);
		return getFinalResults(resultsCrossValidation);
	}

	@Override
	public Map<String, Double> evaluate(MultiCTBNC<?, ?> model, double preprocessingExecutionTime)
			throws UnreadDatasetException, ErroneousValueException {
		// Get sequences from the dataset
		List<Sequence> sequences = getSequencesDataset();
		int numSequences = sequences.size();
		// Obtain the size of each fold
		int[] sizeFolds = defineSizeFolds(numSequences);
		Map<String, Double> resultsCrossValidation = performCrossValidation(model, sequences, sizeFolds);
		logger.info("Adding execution time ({}) to the final learning time", preprocessingExecutionTime);
		resultsCrossValidation.computeIfPresent("Learning time", (k, v) -> v + preprocessingExecutionTime);
		return getFinalResults(resultsCrossValidation);
	}

	private int[] defineSizeFolds(int numSequences) {
		int[] sizeFolds = new int[this.folds];
		Arrays.fill(sizeFolds, numSequences / this.folds);
		// Sequences without fold are added one by one to the first folds
		for (int i = 0; i < numSequences % this.folds; i++)
			sizeFolds[i] += 1;
		return sizeFolds;
	}

	private void displayResultsCV(Map<String, Double> resultsCrossValidation) {
		System.out.println("----------------------------Results cross-validation----------------------------");
		displayResults(resultsCrossValidation);
		System.out.println("--------------------------------------------------------------------------------");
	}

	private void displayResultsFold(int foldNumber, Map<String, Double> resultsFold) {
		System.out.println(MessageFormat.format(
				"---------------------------------Results fold {0}---------------------------------", foldNumber));
		resultsFold.forEach((metric, value) -> System.out.println(metric + " = " + value));
		System.out.println("--------------------------------------------------------------------------------");
	}

	/**
	 * Given all the sequences of a dataset, creates a test dataset using the sequences between some specified indexes.
	 *
	 * @param sequences sequences of the dataset
	 * @param fromIndex index of the first sequence of the extracted dataset
	 * @param toIndex   index of the last sequence of the extracted dataset
	 * @return test dataset
	 */
	private Dataset extractTestingDataset(List<Sequence> sequences, int fromIndex, int toIndex) {
		List<Sequence> testingSequences = sequences.subList(fromIndex, toIndex);
		return new Dataset(testingSequences);
	}

	/**
	 * Given all the sequences of a dataset, creates a training dataset that includes all the sequences but those
	 * between some specified indexes.
	 *
	 * @param sequences sequences of the dataset
	 * @param fromIndex index of the first sequence to ignore
	 * @param toIndex   index of the last sequence to ignore
	 * @return training dataset
	 */
	private Dataset extractTrainingDataset(List<Sequence> sequences, int fromIndex, int toIndex) {
		List<Sequence> trainingSequences = new ArrayList<>(sequences);
		// Remove instances that will be used for testing
		trainingSequences.subList(fromIndex, toIndex).clear();
		return new Dataset(trainingSequences);
	}

	private Map<String, Double> getFinalResults(Map<String, Double> resultsCrossValidation) {
		// The average of each metric is computed
		resultsCrossValidation.forEach((metric, value) -> resultsCrossValidation.put(metric, value / this.folds));
		displayResultsCV(resultsCrossValidation);
		return resultsCrossValidation;
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

	private List<Sequence> getSequencesDataset() {
		// Get sequences from the dataset
		List<Sequence> sequences = this.dataset.getSequences();
		if (this.shuffle) {
			// Shuffle the sequences before performing the cross-validation
			Util.shuffle(sequences, this.seed);
			this.logger.info("Sequences shuffled");
		}
		return sequences;
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
			// Warn the new dataset about all the possible states the variables can take
			// (categorical variables are assumed for now)
			dataset.setStatesVariables(trainingDataset.getStatesVariables());
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

	private Map<String, Double> performCrossValidation(MultiCTBNC<?, ?> model, List<Sequence> sequences,
													   int[] sizeFolds) {
		// Save evaluation metrics obtained with the cross-validation
		Map<String, Double> resultsCrossValidation = new LinkedHashMap<>();
		// Iterate over each fold
		int fromIndex = 0;
		for (int i = 0; i < this.folds; i++) {
			this.logger.info("Testing model on fold " + i);
			// Prepare training and test datasets for the current fold
			int toIndex = fromIndex + sizeFolds[i];
			// Prepare training dataset for current fold
			Dataset trainingDataset = extractTrainingDataset(sequences, fromIndex, toIndex);
			// Prepare test dataset for current fold
			Dataset testingDataset = extractTestingDataset(sequences, fromIndex, toIndex);
			// Warn both the training and testing set about all the possible states the
			// variables can take (categorical variables are assumed for now)
			trainingDataset.setStatesVariables(this.dataset.getStatesVariables());
			testingDataset.setStatesVariables(this.dataset.getStatesVariables());
			// Learn one model per class variable in parallel
			List<MultiCTBNC<?, ?>> models = learnModels(model, trainingDataset);
			// Perform predictions with each model and merge the results
			Prediction[] predictionsFold = predict(models, testingDataset);
			// Result of performance metrics when evaluating the model with the current fold
			Map<String, Double> resultsFold = Metrics.evaluate(predictionsFold, testingDataset);
			// Add learning time to the results
			resultsFold.put("Learning time", this.lastLearningTimeModelsSeconds);
			// Update the final results of the metrics after seeing all the folds
			resultsFold.forEach((metric, value) -> resultsCrossValidation.merge(metric, value, (a, b) -> a + b));
			displayResultsFold(i, resultsFold);
			fromIndex += sizeFolds[i];
		}
		return resultsCrossValidation;
	}

	private Prediction[] predict(List<MultiCTBNC<?, ?>> models, Dataset testingDataset) {
		List<String> nameCVs = testingDataset.getNameClassVariables();
		Prediction[] predictionsFold = null;
		for (int i = 0; i < models.size(); i++) {
			// Display learnt model
			System.out.println(MessageFormat.format(
					"------------------------Model for class variable " + "{0}------------------------",
					nameCVs.get(i)));
			displayModel(models.get(i));
			System.out.println("---------------------------------------------------------------------------");
			Prediction[] predictionsCV = models.get(i).predict(testingDataset, this.estimateProbabilities);
			predictionsFold = updatePredictionsFold(predictionsFold, this.dataset.getNameClassVariables().get(i),
					predictionsCV);
		}
		return predictionsFold;
	}

	/**
	 * Updates the current predictions of a fold with the predictions for a new class variable.
	 *
	 * @param predictionsFold current predictions of the fold
	 * @param nameCV          name of the class variable whose predictions are included in the predictions of the fold
	 * @param predictionsCV   predictions of the class variable
	 * @return updated predictions for the fold
	 */
	private Prediction[] updatePredictionsFold(Prediction[] predictionsFold, String nameCV,
											   Prediction[] predictionsCV) {
		if (predictionsFold == null)
			// First class is predicted
			return predictionsCV;
		// Update the predicted class configuration for each sequence and the
		// probabilities given to each class configuration (necessary for Brier score)
		for (int i = 0; i < predictionsFold.length; i++) {
			// Get prediction current class variable and sequence
			State prediction = predictionsCV[i].getPredictedClasses();
			// Update current class configuration (CC) of the sequence
			State predictedCC = predictionsFold[i].getPredictedClasses();
			predictedCC.addEvent(nameCV, prediction.getValueVariable(nameCV));
			// Define probabilities of each class configuration for the current sequence
			Map<State, Double> newProbabilitiesCCs = new HashMap<>();
			if (this.estimateProbabilities) {
				// Iterate over class configurations including the states of the current class
				// variable
				Set<State> statesCC = predictionsFold[i].getProbabilities().keySet();
				Set<State> statesCV = predictionsCV[i].getProbabilities().keySet();
				for (State stateCC : statesCC)
					for (State classCV : statesCV) {
						// Get a new class configuration that includes the state of the class variable
						State newStateCC = new State(stateCC.getEvents());
						newStateCC.addEvents(classCV.getEvents());
						// Get probability new class configuration
						double previousProbCC = predictionsFold[i].getProbabilities().get(stateCC);
						double newProbCC = previousProbCC * predictionsCV[i].getProbabilities().get(classCV);
						// Save new class configuration and its probability
						newProbabilitiesCCs.put(newStateCC, newProbCC);
					}
				// Save probabilities class configurations for the sequence
				predictionsFold[i].setProbabilities(newProbabilitiesCCs);
				// Get probability predicted class configuration for the sequence
				double probabilityPredictedCC = predictionsFold[i].getProbabilityPrediction();
				// Get probability predicted class for the current class variable
				double probabilityPredictedClass = predictionsCV[i].getProbabilityPrediction();
				// Save new probability predicted class configuration
				double probabilityNewPredictedCC = probabilityPredictedCC * probabilityPredictedClass;
				predictionsFold[i].setProbabilityPrediction(probabilityNewPredictedCC);
			}
		}
		return predictionsFold;
	}

}