package es.upm.fi.cig.mctbnc.data.reader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import es.upm.fi.cig.mctbnc.data.representation.Dataset;
import es.upm.fi.cig.mctbnc.exceptions.UnreadDatasetException;
import es.upm.fi.cig.mctbnc.exceptions.VariableNotFoundException;

/**
 * Reads time series data contained in a single CSV. It divides the dataset into
 * several sequences depending on the selected strategy.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class SingleCSVReader extends AbstractCSVReader {
	File file;
	int sizeSequence;

	/**
	 * Constructs a {@code SingleCSVReader} that extracts a CSV file from the
	 * specified folder.
	 * 
	 * @param datasetFolder folder path where the CSV file is stored
	 * @param sizeSequence  maximum size of the sequences
	 * @throws FileNotFoundException if the CSV file was not found
	 */
	public SingleCSVReader(String datasetFolder, int sizeSequence) throws FileNotFoundException {
		super(datasetFolder);
		this.sizeSequence = sizeSequence;
		logger.info("Generating CSV reader for single csv file in {}", datasetFolder);
		// Read all csv files from the specified folder
		File folder = new File(datasetFolder);
		this.file = folder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File folder, String name) {
				return name.endsWith(".csv");
			}
		})[0];
		// Extract variables names from the CSV file
		extractVariableNames(this.file);
	}

	@Override
	public Dataset readDataset() throws UnreadDatasetException {
		if (isDatasetOutdated()) {
			logger.info("Reading dataset with sequences of size {} from the csv file {}", this.sizeSequence, this.file);
			this.dataset = new Dataset(this.nameTimeVariable, this.nameClassVariables);
			try {
				// Read the entire CSV
				List<String[]> dataCSV = readCSV(this.file.getAbsolutePath(), this.excludeVariables);
				// Extract the sequences from the CSV by using the selected strategy
				extractFixedSequences(this.dataset, dataCSV);
				// Remove zero variance features
				this.dataset.removeZeroVarianceFeatures();
				// Set the dataset as not out-of-dates
				setDatasetAsOutdated(false);
			} catch (FileNotFoundException e) {
				throw new UnreadDatasetException("There was an error reading the file of the dataset");
			} catch (VariableNotFoundException e) {
				logger.warn(e.getMessage());
			}
		}
		return this.dataset;
	}

	/**
	 * Extracts sequences that have the same maximum length and add them to the
	 * specified dataset. As there cannot be different class configurations in one
	 * sequence, sequences could contain less observation if a transition of a class
	 * variable occurs before reaching the limit. It is assumed that the names of
	 * the variables are in the first array of "dataCSV".
	 * 
	 * @param dataset a {@code Dataset} where sequences are stored
	 * @param dataCSV list of String arrays with the data extracted from the CSV
	 *                file. First array in the list must contain the name of the
	 *                variables
	 */
	public void extractFixedSequences(Dataset dataset, List<String[]> dataCSV) {
		int numInstances = dataCSV.size();
		String[] namesVariables = dataCSV.get(0);
		// Obtain the indexes of the class variables in the CSV
		int[] indexClassVariables = this.nameClassVariables.stream()
				.mapToInt(nameClassVariable -> List.of(namesVariables).indexOf(nameClassVariable)).toArray();
		// Store the class configuration of the first sequence.
		String[] currentClassConfiguration = extractClassConfigurationObservation(indexClassVariables, dataCSV.get(1));
		// Store the transitions for a sequence
		List<String[]> dataSequence = new ArrayList<String[]>();
		// Iterate over all the observations
		for (int i = 1; i < numInstances; i++) {
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
			if (dataSequence.size() < this.sizeSequence && sameClassConfiguration)
				// Add transition
				dataSequence.add(observation);
			else {
				// Add sequence to dataset
				dataset.addSequence(dataSequence,
						this.file.getAbsolutePath() + "(Row " + (i - dataSequence.size()) + " to " + i + ")");
				if (!sameClassConfiguration)
					// The class configuration changed
					currentClassConfiguration = extractClassConfigurationObservation(indexClassVariables, observation);
				// Create new sequence
				dataSequence = new ArrayList<String[]>();
				// Add names of the variables
				dataSequence.add(namesVariables);
				// The observation is added to the new sequence
				dataSequence.add(observation);
			}
		}
	}

	/**
	 * Checks if the class configuration of an observation is equal to another that
	 * is given.
	 * 
	 * @param indexClassVariables indexes of the class variables
	 * @param observation         evaluated observation
	 * @param classConfiguration  class configuration for the comparison
	 * @return true if the observation has the provided class configuration, false
	 *         otherwise
	 */
	private boolean sameClassConfiguration(int[] indexClassVariables, String[] observation,
			String[] classConfiguration) {
		// Extract class configuration from the observation
		String[] classConfigurationObservation = extractClassConfigurationObservation(indexClassVariables, observation);
		// Compare class configuration of the observation and the given one.
		return Arrays.equals(classConfiguration, classConfigurationObservation);
	}

	/**
	 * Extracts the class configuration of the specified class variables from the
	 * given observation.
	 * 
	 * @param indexClassVariables indexes of the class variables
	 * @param observation         evaluated observation
	 * @return class configuration of the provided class variables in the given
	 *         observation
	 */
	private String[] extractClassConfigurationObservation(int[] indexClassVariables, String[] observation) {
		String[] classConfigurationObservation = new String[indexClassVariables.length];
		for (int i = 0; i < indexClassVariables.length; i++)
			classConfigurationObservation[i] = observation[indexClassVariables[i]];
		return classConfigurationObservation;
	}

}
