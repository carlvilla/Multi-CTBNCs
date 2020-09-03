package com.cig.mctbnc.data.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.exceptions.VariableNotFoundException;
import com.opencsv.CSVReader;

/**
 * Provides the logic to read separate CSV files. It is possible to find time
 * series data where the sequences are stored in separate CSV files.
 * 
 * @author Carlos Villa Blanco
 *
 */
public class SeparateCSVReader extends AbstractCSVReader {
	File[] files;

	/**
	 * Constructor. Extracts all the CSV files from the specified folder.
	 * 
	 * @param datasetFolder folder path where the csv files are stored
	 * @throws FileNotFoundException
	 */
	public SeparateCSVReader(String datasetFolder) throws FileNotFoundException {
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
		nameAcceptedFiles = new ArrayList<String>();
		for (File file : files) {
			try {
				List<String[]> dataSequence = readCSV(file.getAbsolutePath(), excludeVariables);
				dataset.addSequence(dataSequence);
				nameAcceptedFiles.add(file.getName());
			} catch (VariableNotFoundException e) {
				logger.warn(e.getMessage());
			}
		}
		dataset.removeZeroVarianceFeatures();
		return dataset;
	}
}
