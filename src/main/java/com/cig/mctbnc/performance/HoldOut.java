package com.cig.mctbnc.performance;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cig.mctbnc.classification.Prediction;
import com.cig.mctbnc.data.reader.DatasetReader;
import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.data.representation.Sequence;
import com.cig.mctbnc.exceptions.UnreadDatasetException;
import com.cig.mctbnc.models.MCTBNC;
import com.cig.mctbnc.util.Util;

/**
 * Implements hold-out validation method.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class HoldOut extends ValidationMethod {
	Dataset trainingDataset;
	Dataset testingDataset;
	Logger logger = LogManager.getLogger(HoldOut.class);

	/**
	 * Constructs a {@code HoldOut} by receiving a {@code DatasetReader}, the size
	 * of the training set and if the data should be shuffled.
	 * 
	 * @param datasetReader
	 * @param trainingSize
	 * @param shuffle
	 * @throws UnreadDatasetException
	 */
	public HoldOut(DatasetReader datasetReader, double trainingSize, boolean shuffle) throws UnreadDatasetException {
		DecimalFormat df = new DecimalFormat("##.00");
		this.logger.info("Generating training ({}%) and testing ({}%) datasets (Hold-out validation)",
				df.format(trainingSize * 100), df.format((1 - trainingSize) * 100));
		generateTrainAndTest(datasetReader, trainingSize, shuffle);
		this.logger.info("Time variable: {}", this.trainingDataset.getNameTimeVariable());
		this.logger.info("Features: {}", this.trainingDataset.getNameFeatures());
		this.logger.info("Class variables: {}", (this.trainingDataset.getNameClassVariables()));
		this.logger.info("Sequences for training {}", this.trainingDataset.getNumDataPoints());
		this.logger.info("Sequences for testing {}", this.testingDataset.getNumDataPoints());
	}

	/**
	 * Evaluate the performance of the specified model using hold-out validation.
	 * 
	 * @param model model to evaluate
	 */
	@Override
	public void evaluate(MCTBNC<?, ?> model) {
		// Train the model
		model.learn(this.trainingDataset);
		// Make predictions with the model
		Prediction[] predictions = model.predict(this.testingDataset, true);
		// Evaluate the performance of the model
		Map<String, Double> results = Metrics.evaluate(predictions, this.testingDataset);
		// Display results
		System.out.println("--------------------Results hold-out validation--------------------");
		displayResults(results);
		displayModel(model);
		System.out.println("-------------------------------------------------------------------");
		// Display learned model
		model.display();
	}

	/**
	 * Generate a training and a testing dataset.
	 * 
	 * @param datasetReader read the dataset
	 * @param trainingSize  size of the training dataset (percentage)
	 * @param shuffle       determines if the data is shuffled before splitting into
	 *                      training and testing
	 * @throws UnreadDatasetException
	 */
	public void generateTrainAndTest(DatasetReader datasetReader, double trainingSize, boolean shuffle)
			throws UnreadDatasetException {
		// Obtain entire dataset
		Dataset dataset = datasetReader.readDataset();
		// Obtain files names from which the dataset was read
		List<String> fileNames = new ArrayList<String>(dataset.getNameFiles());
		// Obtain sequences of the dataset
		List<Sequence> sequences = new ArrayList<Sequence>(dataset.getSequences());
		if (shuffle) {
			// Sequences and their files are shuffled before splitting into train and test
			Integer seed = 10;
			Util.shuffle(sequences, seed);
			Util.shuffle(fileNames, seed);
			this.logger.info("Sequences shuffled");
		}
		// Define training and testing sequences
		int lastIndexTraining = (int) (trainingSize * sequences.size());
		List<Sequence> trainingSequences = sequences.subList(0, lastIndexTraining);
		List<Sequence> testingSequences = sequences.subList(lastIndexTraining, sequences.size());
		// Define training and testing datasets
		this.trainingDataset = new Dataset(trainingSequences);
		this.testingDataset = new Dataset(testingSequences);
		// Set in the datasets the names of the files from which the data was extracted
		this.trainingDataset.setNameFiles(fileNames.subList(0, lastIndexTraining));
		this.testingDataset.setNameFiles(fileNames.subList(lastIndexTraining, sequences.size()));
		// Warn the training set about the possible states the variables can take (for
		// now, categorical variable are assumed)
		this.trainingDataset.setStatesVariables(dataset.getStatesVariables());
	}

	/**
	 * Return the training dataset.
	 * 
	 * @return training dataset
	 */
	public Dataset getTraining() {
		if (this.trainingDataset == null) {
			this.logger.warn("The training dataset was not generated.");
		}
		return this.trainingDataset;
	}

	/**
	 * Return the testing dataset.
	 * 
	 * @return testing dataset
	 */
	public Dataset getTesting() {
		if (this.trainingDataset == null) {
			this.logger.warn("The testing dataset was not generated.");
		}
		return this.testingDataset;
	}

}
