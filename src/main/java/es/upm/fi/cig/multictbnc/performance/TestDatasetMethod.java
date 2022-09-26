package es.upm.fi.cig.multictbnc.performance;

import es.upm.fi.cig.multictbnc.classification.Prediction;
import es.upm.fi.cig.multictbnc.data.reader.DatasetReader;
import es.upm.fi.cig.multictbnc.data.representation.Dataset;
import es.upm.fi.cig.multictbnc.data.representation.Sequence;
import es.upm.fi.cig.multictbnc.exceptions.ErroneousValueException;
import es.upm.fi.cig.multictbnc.exceptions.UnreadDatasetException;
import es.upm.fi.cig.multictbnc.models.MultiCTBNC;
import es.upm.fi.cig.multictbnc.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class allows specifying different training and test datasets.
 *
 * @author Carlos Villa Blanco
 */
public class TestDatasetMethod extends ValidationMethod {
	private final Logger logger = LogManager.getLogger(TestDatasetMethod.class);
	private DatasetReader trainingDatasetReader;
	private DatasetReader testDatasetReader;
	private boolean estimateProbabilities;
	private boolean shuffle;
	private Long seed;

	/**
	 * Constructor that receives two {@code DatasetReader} for the training and test datasets, whether the test dataset
	 * should be shuffled, a seed for shuffling and whether the probabilities of each class configuration should be
	 * estimated during the testing.
	 *
	 * @param trainingDatasetReader {@code DatasetReader} for the training dataset
	 * @param testDatasetReader     {@code DatasetReader} for the test dataset
	 * @param estimateProbabilities {@code true} if the probabilities of class configurations are estimated, {@code
	 *                              false} otherwise
	 * @param shuffle               {@code true} to shuffle the test dataset, {@code false} otherwise
	 * @param seed                  a seed for shuffling
	 */
	public TestDatasetMethod(DatasetReader trainingDatasetReader, DatasetReader testDatasetReader,
							 boolean estimateProbabilities, boolean shuffle, Long seed) {
		this.trainingDatasetReader = trainingDatasetReader;
		this.testDatasetReader = testDatasetReader;
		this.testDatasetReader.removeZeroVarianceVariables(false);
		this.estimateProbabilities = estimateProbabilities;
		this.shuffle = shuffle;
		this.seed = seed;
	}

	@Override
	public Map<String, Double> evaluate(MultiCTBNC<?, ?> model) throws UnreadDatasetException,
			ErroneousValueException {
		// Retrieve selected training dataset
		Dataset trainingDataset = this.trainingDatasetReader.readDataset();
		// Train model
		model.learn(trainingDataset);
		// Retrieve selected test dataset
		Dataset testDataset = this.testDatasetReader.readDataset();
		// Obtain sequences of the dataset
		List<Sequence> sequences = new ArrayList<>(testDataset.getSequences());
		if (this.shuffle) {
			Util.shuffle(sequences, this.seed);
			this.logger.info("Test sequences shuffled");
		}
		// Make predictions with the model
		Prediction[] predictions = model.predict(testDataset, this.estimateProbabilities);
		if (Util.isArrayEmpty(predictions))
			throw new ErroneousValueException("Any sequence of the test dataset could be predicted.");
		// Evaluate the performance of the model
		Map<String, Double> results = Metrics.evaluate(predictions, testDataset);
		// Display results
		System.out.println("--------------------------Results hold-out validation--------------------------");
		displayResults(results);
		displayModel(model);
		System.out.println("-------------------------------------------------------------------------------");
		return results;
	}

}