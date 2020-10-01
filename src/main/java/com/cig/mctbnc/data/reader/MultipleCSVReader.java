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
 * Provides the logic to read separate CSV files. It is possible to find time
 * series data where the sequences are stored in separate CSV files.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class MultipleCSVReader extends AbstractCSVReader {
	File[] files;

	/**
	 * Constructor. Extracts all the CSV files from the specified folder.
	 * 
	 * @param datasetFolder folder path where the csv files are stored
	 * @throws FileNotFoundException
	 */
	public MultipleCSVReader(String datasetFolder) throws FileNotFoundException {
		super(datasetFolder);
		logger.info("Generating CSV reader for multiples csv files in {}", datasetFolder);
		// Read all csv files from the specified folder
		File folder = new File(datasetFolder);
		files = folder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File folder, String name) {
				return name.endsWith(".csv");
			}
		});
		// Order the files by name
		Arrays.sort(files);
		// Extract variables names from first CSV file
		extractVariableNames(files[0]);
	}

	@Override
	public Dataset readDataset() {
		Dataset dataset = new Dataset(nameTimeVariable, nameClassVariables);
		// Names of the files from which the dataset was read. Some files could be
		// rejected due to problems in its content.
		List<String> nameAcceptedFiles = new ArrayList<String>();
		for (File file : files) {
			try {
				List<String[]> dataSequence = readCSV(file.getAbsolutePath(), excludeVariables);
				boolean sequenceAdded = dataset.addSequence(dataSequence);
				if (sequenceAdded)
					nameAcceptedFiles.add(file.getName());
			} catch (VariableNotFoundException e) {
				logger.warn(e.getMessage());
			}
		}
		// Remove variables with zero variance
		dataset.removeZeroVarianceFeatures();
		// Save in the dataset the files used to extract its sequences
		dataset.setNameFiles(nameAcceptedFiles);
		return dataset;
	}
}
