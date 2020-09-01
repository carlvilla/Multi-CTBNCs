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
public class SeparateCSVReader extends AbstractDatasetReader {
	File[] files;

	/**
	 * Constructor. Extracts all the csv files from the specified folder.
	 * 
	 * @param datasetFolder      folder path where the csv files are stored
	 * @param nameTimeVariable
	 * @param nameClassVariables
	 * @param excludeVariables
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

	/**
	 * Reads a CSV file.
	 * 
	 * @param pathFile         path to the CSV file
	 * @param excludeVariables
	 * @return list with the rows (lists of strings) of the CSV
	 * @throws VariableNotFoundException
	 */
	public List<String[]> readCSV(String pathFile, List<String> excludeVariables) throws VariableNotFoundException {
		List<String[]> list = new ArrayList<String[]>();
		try {
			FileReader reader = new FileReader(pathFile);
			CSVReader csvReader = new CSVReader(reader);
			// If it was specified variables to ignore
			if (excludeVariables.size() > 0) {
				// Obtain name of the variables
				List<String> head = new ArrayList<String>(Arrays.asList(csvReader.readNext()));
				// Obtain the index of the variables to ignore
				List<Integer> indexesToIgnore = new ArrayList<Integer>();
				for (String excludeVariable : excludeVariables) {
					int index = head.indexOf(excludeVariable);
					if (index == -1) {
						// The variable to exclude does not exist in the analysed CSV
						String message = String.format("Variable %s not found in file %s. The file will be ignored.",
								excludeVariable, pathFile);
						throw new VariableNotFoundException(message);
					}
					indexesToIgnore.add(index);
				}
				// List is sorted so last elements of the lists are removed first
				Collections.sort(indexesToIgnore, Collections.reverseOrder());
				// Remove names of the variables to ignore
				for (int index : indexesToIgnore)
					head.remove(index);
				// Add names of the variables to the final list
				list.add(head.stream().toArray(String[]::new));
				// Read data of the csv
				String[] nextLine;
				while ((nextLine = csvReader.readNext()) != null) {
					List<String> row = new LinkedList<String>(Arrays.asList(nextLine));
					// Remove values of the variables to ignore
					for (int index : indexesToIgnore)
						row.remove(index);
					list.add(row.stream().toArray(String[]::new));
				}
			} else {
				list = csvReader.readAll();
			}
			reader.close();
			csvReader.close();
		} catch (IOException e) {
			logger.warn("Impossible to read file {}", pathFile);
		}
		return list;
	}

	/**
	 * Extract the names of the variables from a CSV file. It is assumed that the
	 * names are in the first row.
	 * 
	 * @param csvFile
	 * @throws FileNotFoundException
	 */
	private void extractVariableNames(File csvFile) throws FileNotFoundException {
		if (csvFile.isFile()) {
			FileReader reader = new FileReader(csvFile);
			CSVReader csvReader = new CSVReader(reader);			
			try {
				nameVariables = Arrays.asList(csvReader.readNext());
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else {
			throw new FileNotFoundException("Impossible to read CSV file");
		}
	}

}
