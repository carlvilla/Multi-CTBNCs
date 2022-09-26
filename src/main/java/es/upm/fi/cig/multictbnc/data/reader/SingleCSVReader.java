package es.upm.fi.cig.multictbnc.data.reader;

import es.upm.fi.cig.multictbnc.data.representation.Dataset;
import es.upm.fi.cig.multictbnc.exceptions.UnreadDatasetException;
import es.upm.fi.cig.multictbnc.exceptions.VariableNotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Reads time series data contained in a single CSV. It divides the dataset into several sequences depending on the
 * selected strategy.
 *
 * @author Carlos Villa Blanco
 */
public class SingleCSVReader extends AbstractCSVReader {
	private static final Logger logger = LogManager.getLogger(SingleCSVReader.class);
	File file;
	int sizeSequence;

	/**
	 * Constructs a {@code SingleCSVReader} that extracts a CSV file from the specified folder.
	 *
	 * @param datasetFolder folder path where the CSV file is stored
	 * @param sizeSequence  maximum size of the sequences
	 * @throws FileNotFoundException  if the CSV file was not found
	 * @throws UnreadDatasetException if a dataset could not be read from the specified folder
	 */
	public SingleCSVReader(String datasetFolder, int sizeSequence)
			throws FileNotFoundException, UnreadDatasetException {
		super(datasetFolder);
		// Read all CSV files from the specified folder
		this.file = new File(datasetFolder);
		if (this.file == null)
			throw new UnreadDatasetException("No CSV file found in the specified folder");
		// Only the first CSV found will be used
		logger.info("Generating CSV reader for single CSV file {}", this.file.getAbsoluteFile());
		// Check if the size of the sequences is correct
		if (sizeSequence > 1)
			this.sizeSequence = sizeSequence;
		else {
			logger.warn(
					"The size of the extracted sequences must be greater than 1. Ten observations will be extracted " +
							"per sequence");
			this.sizeSequence = 10;
		}
		// Extract variables names from the CSV file
		extractVariableNames(this.file);
	}

	/**
	 * Extracts sequences that have the same maximum length and add them to the specified dataset. It is assumed that
	 * the names of the variables are in the first array of "dataCSV".
	 *
	 * @param dataset a {@code Dataset} where sequences are stored
	 * @param dataCSV list of String arrays with the data extracted from the CSV file. The first array in the list must
	 *                contain the name of the variables
	 */
	public void extractFixedSequences(Dataset dataset, List<String[]> dataCSV) {
		// Extract number of observations
		int numObservations = dataCSV.size() - 1;
		String[] namesVariables = dataCSV.get(0);
		// Store the transitions for a sequence
		List<String[]> dataSequence = new ArrayList<>();
		// Iterate over all the observations
		for (int i = 1; i <= numObservations; i++) {
			// Add observations to the sequence until the size limit is reached
			// Extract observation
			String[] observation = dataCSV.get(i);
			if (dataSequence.size() == 0)
				// Add names of the variables
				dataSequence.add(namesVariables);
			if (dataSequence.size() <= this.sizeSequence)
				// Add transition
				dataSequence.add(observation);
			else {
				// Add sequence to dataset
				String filePath =
						this.file.getAbsolutePath() + "(Row " + (i - dataSequence.size()) + " to " + (i - 2) + ")";
				dataset.addSequence(dataSequence, filePath);
				// Create new sequence
				dataSequence = new ArrayList<>();
				// Add names of the variables
				dataSequence.add(namesVariables);
				// The observation is added to the new sequence
				dataSequence.add(observation);
			}
		}
		addRemainingObservations(dataset, numObservations, dataSequence);
	}

	/**
	 * Extracts sequences that have the same maximum length and add them to the specified dataset. Observations of a
	 * sequence must belong to the same class configuration. Therefore, sequences could contain less observation if a
	 * transition of a class variable occurs before reaching the maximum sequence size. It is assumed that the names of
	 * the variables are in the first array of "dataCSV".
	 *
	 * @param dataset a {@code Dataset} where sequences are stored
	 * @param dataCSV list of String arrays with the data extracted from the CSV file. The first array in the list must
	 *                contain the name of the variables
	 */
	public void extractFixedSequencesSameCC(Dataset dataset, List<String[]> dataCSV) {
		// Extract number of observations
		int numObservations = dataCSV.size() - 1;
		String[] namesVariables = dataCSV.get(0);
		// Obtain the indexes of the class variables in the CSV
		int[] indexClassVariables = this.nameClassVariables.stream().mapToInt(
				nameClassVariable -> List.of(namesVariables).indexOf(nameClassVariable)).toArray();
		// Store the class configuration of the first sequence.
		String[] currentClassConfiguration = extractClassConfigurationObservation(indexClassVariables, dataCSV.get(1));
		// Store the transitions for a sequence
		List<String[]> dataSequence = new ArrayList<>();
		// Iterate over all the observations
		for (int i = 1; i <= numObservations; i++) {
			// Add observations to the sequence until the size limit is reached or the class
			// variables transition
			// Extract observation
			String[] observation = dataCSV.get(i);
			if (dataSequence.size() == 0)
				// Add names of the variables
				dataSequence.add(namesVariables);
			// Check if any of the class variables transitioned to another state
			boolean sameClassConfiguration = sameClassConfiguration(indexClassVariables, observation,
					currentClassConfiguration);
			if (dataSequence.size() <= this.sizeSequence && sameClassConfiguration)
				// Add transition
				dataSequence.add(observation);
			else {
				// Add sequence to dataset
				String filePath =
						this.file.getAbsolutePath() + "(Row " + (i - dataSequence.size()) + " to " + (i - 2) + ")";
				dataset.addSequence(dataSequence, filePath);
				if (!sameClassConfiguration)
					// The class configuration changed
					currentClassConfiguration = extractClassConfigurationObservation(indexClassVariables, observation);
				// Create new sequence
				dataSequence = new ArrayList<>();
				// Add names of the variables
				dataSequence.add(namesVariables);
				// The observation is added to the new sequence
				dataSequence.add(observation);
			}
		}
		// Add observations that could not make a complete sequence
		addRemainingObservations(dataset, numObservations, dataSequence);
	}

	@Override
	public Dataset readDataset() throws UnreadDatasetException {
		if (isDatasetOutdated()) {
			logger.info("Reading dataset with sequences of size {} from the CSV file {}", this.sizeSequence,
					this.file);
			this.dataset = new Dataset(this.nameTimeVariable, this.nameClassVariables);
			try {
				// Read the entire CSV
				List<String[]> dataCSV = readCSV(this.file.getAbsolutePath(), this.excludeVariables);
				// Extract the sequences from the CSV
				if (this.nameClassVariables != null)
					extractFixedSequencesSameCC(this.dataset, dataCSV);
				else
					extractFixedSequences(this.dataset, dataCSV);
				// Remove zero variance feature variables
				this.dataset.checkVarianceFeatures(this.removeZeroVarianceVariables);
				// Set the dataset as not out-of-dates
				setDatasetAsOutdated(false);
			} catch (FileNotFoundException fnfe) {
				throw new UnreadDatasetException("An error occurred while reading the file of the dataset");
			} catch (VariableNotFoundException vnfe) {
				logger.warn(vnfe.getMessage());
			}
		}
		return this.dataset;
	}

	/**
	 * Adds the remaining observations to the dataset.
	 *
	 * @param dataset         a {@code Dataset} where sequences are stored
	 * @param numObservations total number of observations to add to the dataset
	 * @param dataSequence    data of the remaining observations
	 */
	private void addRemainingObservations(Dataset dataset, int numObservations, List<String[]> dataSequence) {
		if (dataSequence.size() > 0) {
			String filePath =
					this.file.getAbsolutePath() + "(Row " + (numObservations - dataSequence.size() + 1) + " to " +
							(numObservations - 1) + ")";
			dataset.addSequence(dataSequence, filePath);
		}
	}

	/**
	 * Extracts the class configuration of the specified class variables from the given observation.
	 *
	 * @param indexClassVariables indexes of the class variables
	 * @param observation         evaluated observation
	 * @return class configuration of the provided class variables in the given observation
	 */
	private String[] extractClassConfigurationObservation(int[] indexClassVariables, String[] observation) {
		String[] classConfigurationObservation = new String[indexClassVariables.length];
		for (int i = 0; i < indexClassVariables.length; i++)
			classConfigurationObservation[i] = observation[indexClassVariables[i]];
		return classConfigurationObservation;
	}

	/**
	 * Checks if the class configuration of an observation is equal to another that is given.
	 *
	 * @param indexClassVariables indexes of the class variables
	 * @param observation         evaluated observation
	 * @param classConfiguration  class configuration for the comparison
	 * @return true if the observation has the provided class configuration, false otherwise
	 */
	private boolean sameClassConfiguration(int[] indexClassVariables, String[] observation,
										   String[] classConfiguration) {
		// Extract class configuration from the observation
		String[] classConfigurationObservation = extractClassConfigurationObservation(indexClassVariables,
				observation);
		// Compare the class configuration of the observation and the given one.
		return Arrays.equals(classConfiguration, classConfigurationObservation);
	}

}