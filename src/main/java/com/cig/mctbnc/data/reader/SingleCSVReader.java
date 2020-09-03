package com.cig.mctbnc.data.reader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.ArrayList;
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
		nameAcceptedFiles = new ArrayList<String>();
		try {
			List<String[]> dataCSV = readCSV(file.getAbsolutePath(), excludeVariables);
			// Save name of the file
			nameAcceptedFiles.add(file.getName());
			// Extract the sequences from the CSV by using the selected strategy
			extractFixedSequences(dataset, dataCSV);
			// Remove zero variance features
			dataset.removeZeroVarianceFeatures();
		} catch (VariableNotFoundException e) {
			logger.warn(e.getMessage());
		}
		return dataset;
	}

	/**
	 * Extract sequences that have the same length, which is specified by the user,
	 * and add it to the specified dataset. As there cannot be different class
	 * configurations in one sequence, the most common class configuration will be
	 * assigned to the sequence. It is assumed that the names of the variables are
	 * in the first array of "dataCSV".
	 * 
	 * @return
	 */
	public Dataset extractFixedSequences(Dataset dataset, List<String[]> dataCSV) {
		int numInstances = dataCSV.size();

		// Get variables' names
		String[] namesVariables = dataCSV.get(0);

		for (int i = 1; i < numInstances / sizeSequence; i = i + sizeSequence) {
			List<String[]> dataSequence = new ArrayList<String[]>();
			dataSequence.add(namesVariables);
			System.out.println(i);
			System.out.println(i + sizeSequence - 1);
			dataSequence.addAll(dataCSV.subList(i, i + sizeSequence - 1));
			dataset.addSequence(dataSequence);
		}

		return dataset;
	}

}
