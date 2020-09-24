package com.cig.mctbnc.data.reader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.exceptions.VariableNotFoundException;

/**
 * Read time series data contained in a single CSV. It divide the dataset into
 * several sequences depeding on the selected strategy.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class SingleCSVReader extends AbstractCSVReader {
	File file;
	int sizeSequence;

	/**
	 * Constructor. Extract CSV file from the specified folder.
	 * 
	 * @param datasetFolder folder path where the CSV file is stored
	 * @param sizeSequence
	 * @throws FileNotFoundException
	 */
	public SingleCSVReader(String datasetFolder, int sizeSequence) throws FileNotFoundException {
		super(datasetFolder);
		this.sizeSequence = sizeSequence;
		logger.info("Generating CSV reader for single csv file in {}", datasetFolder);
		// Read all csv files from the specified folder
		File folder = new File(datasetFolder);
		file = folder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File folder, String name) {
				return name.endsWith(".csv");
			}
		})[0];
		// Extract variables names from the CSV file
		extractVariableNames(file);
	}

	@Override
	public Dataset readDataset() {
		Dataset dataset = new Dataset(nameTimeVariable, nameClassVariables);
		try {
			// Read the entire CSV
			List<String[]> dataCSV = readCSV(file.getAbsolutePath(), excludeVariables);
			// Extract the sequences from the CSV by using the selected strategy
			extractFixedSequences(dataset, dataCSV);
			// Remove zero variance features
			dataset.removeZeroVarianceFeatures();
			// Save name of the file in the dataset
			dataset.setNameFiles(List.of(file.getName()));
		} catch (VariableNotFoundException e) {
			logger.warn(e.getMessage());
		}
		return dataset;
	}

	/**
	 * Extract sequences that have the same length, which is specified by the user,
	 * and add it to the specified dataset. As there cannot be different class
	 * configurations in one sequence, sequences could contain less observation if a
	 * transition of a class variable occurs before reaching the limit. It is
	 * assumed that the names of the variables are in the first array of "dataCSV".
	 * As there cannot be different class configurations in one sequence, sequences
	 * could contain less observation.
	 * 
	 * @return dataset
	 */
	public Dataset extractFixedSequences(Dataset dataset, List<String[]> dataCSV) {
		int numInstances = dataCSV.size();
		String[] namesVariables = dataCSV.get(0);

		// Obtain the indexes of the class variables in the CSV
		int[] indexClassVariables = nameClassVariables.stream()
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

			if (dataSequence.size() == 0) {
				// Add names of the variables
				dataSequence.add(namesVariables);
			}

			// Check if any of the class variables transitioned to another state
			boolean sameClassConfiguration = sameClassConfiguration(indexClassVariables, observation,
					currentClassConfiguration);

			if (dataSequence.size() < sizeSequence && sameClassConfiguration) {
				// Add transition
				dataSequence.add(observation);
			} else {
				// Add sequence to dataset
				dataset.addSequence(dataSequence);
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

		return dataset;
	}

	/**
	 * CHeck if the class configuration of an observation is equal to another that
	 * is given.
	 * 
	 * @param indexClassVariables
	 * @param observation
	 * @param classConfiguration
	 * @return
	 */
	private boolean sameClassConfiguration(int[] indexClassVariables, String[] observation,
			String[] classConfiguration) {
		// Extract class configuration from the observation
		String[] classConfigurationObservation = extractClassConfigurationObservation(indexClassVariables, observation);
		// Compare class configuration of the observation and the given one.
		return Arrays.equals(classConfiguration, classConfigurationObservation);
	}

	/**
	 * Extract class configuration from the given observation.
	 * 
	 * @param indexClassVariables
	 * @param observation
	 * @return
	 */
	private String[] extractClassConfigurationObservation(int[] indexClassVariables, String[] observation) {
		String[] classConfigurationObservation = new String[indexClassVariables.length];
		for (int i = 0; i < indexClassVariables.length; i++)
			classConfigurationObservation[i] = observation[indexClassVariables[i]];
		return classConfigurationObservation;
	}

}
