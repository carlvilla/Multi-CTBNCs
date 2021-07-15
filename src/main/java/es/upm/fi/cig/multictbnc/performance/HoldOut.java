package es.upm.fi.cig.multictbnc.performance;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.upm.fi.cig.multictbnc.classification.Prediction;
import es.upm.fi.cig.multictbnc.data.reader.DatasetReader;
import es.upm.fi.cig.multictbnc.data.representation.Dataset;
import es.upm.fi.cig.multictbnc.data.representation.Sequence;
import es.upm.fi.cig.multictbnc.exceptions.UnreadDatasetException;
import es.upm.fi.cig.multictbnc.models.MultiCTBNC;
import es.upm.fi.cig.multictbnc.util.Util;

/**
 * Implements hold-out validation method.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class HoldOut extends ValidationMethod {
	DatasetReader datasetReader;
	double trainingSize;
	boolean estimateProbabilities;
	boolean shuffle;
	Long seed;
	Dataset trainingDataset;
	Dataset testingDataset;
	Logger logger = LogManager.getLogger(HoldOut.class);

	/**
	 * Constructs a {@code HoldOut} by receiving a {@code DatasetReader}, the size
	 * of the training set and if the data should be shuffled.
	 * 
	 * @param datasetReader         a {@code DatasetReader} to read the dataset
	 * @param trainingSize          size of the training dataset (percentage)
	 * @param estimateProbabilities true to estimate the probabilities of the class
	 *                              configurations, false otherwise
	 * @param shuffle               true to shuffle the sequences before splitting
	 *                              them into training and testing datasets, false
	 *                              otherwise
	 * @param seed                  seed used to shuffle the sequences
	 */
	public HoldOut(DatasetReader datasetReader, double trainingSize, boolean estimateProbabilities, boolean shuffle,
			Long seed) {
		DecimalFormat df = new DecimalFormat("##.00");
		this.logger.info(
				"Generating training ({}%) and testing ({}%) datasets (Hold-out validation) / Shuffle: {} / Estimate probabilities: {}",
				df.format(trainingSize * 100), df.format((1 - trainingSize) * 100), shuffle, estimateProbabilities);
		this.datasetReader = datasetReader;
		this.trainingSize = trainingSize;
		this.estimateProbabilities = estimateProbabilities;
		this.shuffle = shuffle;
		this.seed = seed;
	}

	/**
	 * Evaluates the performance of the specified model using hold-out validation.
	 * 
	 * @param model model to evaluate
	 * @throws UnreadDatasetException if the provided dataset could not be read
	 */
	@Override
	public void evaluate(MultiCTBNC<?, ?> model) throws UnreadDatasetException {
		// Generate training and testing datasets (if it was not done before)
		generateTrainAndTest();
		// Train the model
		model.learn(this.trainingDataset);
		// Make predictions with the model
		Prediction[] predictions = model.predict(this.testingDataset, true);
		// Evaluate the performance of the model
		Map<String, Double> results = Metrics.evaluate(predictions, this.testingDataset);
		// Display results
		System.out.println("--------------------------Results hold-out validation--------------------------");
		displayResults(results);
		displayModel(model);
		System.out.println("-------------------------------------------------------------------------------");
	}

	/**
	 * Generates a training and a testing dataset.
	 * 
	 * @throws UnreadDatasetException if the provided dataset could not be read
	 */
	public void generateTrainAndTest() throws UnreadDatasetException {
		// Obtain entire dataset
		Dataset dataset = this.datasetReader.readDataset();
		// Obtain sequences of the dataset
		List<Sequence> sequences = new ArrayList<Sequence>(dataset.getSequences());
		if (this.shuffle) {
			// Sequences are shuffled before splitting into train and test
			Util.shuffle(sequences, this.seed);
			this.logger.info("Sequences shuffled");
		}
		// Define training and testing sequences
		int lastIndexTraining = (int) (this.trainingSize * sequences.size());
		List<Sequence> trainingSequences = sequences.subList(0, lastIndexTraining);
		List<Sequence> testingSequences = sequences.subList(lastIndexTraining, sequences.size());
		// Define training and testing datasets
		this.trainingDataset = new Dataset(trainingSequences);
		this.testingDataset = new Dataset(testingSequences);
		// Warn the training set about the possible states the variables can take (for
		// now, categorical variable are assumed)
		this.trainingDataset.setStatesVariables(dataset.getStatesVariables());
		// Warn both the training and testing set about all the possible states the
		// variables can take (categorical variable are assumed for now)
		this.trainingDataset.setStatesVariables(dataset.getStatesVariables());
		this.testingDataset.setStatesVariables(dataset.getStatesVariables());
		this.logger.info("Time variable: {}", this.trainingDataset.getNameTimeVariable());
		this.logger.info("Feature variables: {}", this.trainingDataset.getNameFeatureVariables());
		this.logger.info("Class variables: {}", (this.trainingDataset.getNameClassVariables()));
		this.logger.info("Sequences for training {}", this.trainingDataset.getNumDataPoints());
		this.logger.info("Sequences for testing {}", this.testingDataset.getNumDataPoints());
	}

	/**
	 * Returns the training dataset.
	 * 
	 * @return training dataset
	 */
	public Dataset getTraining() {
		if (this.trainingDataset == null) {
			this.logger.warn("The training dataset was not generated");
		}
		return this.trainingDataset;
	}

	/**
	 * Returns the testing dataset.
	 * 
	 * @return testing dataset
	 */
	public Dataset getTesting() {
		if (this.trainingDataset == null) {
			this.logger.warn("The testing dataset was not generated");
		}
		return this.testingDataset;
	}

}
