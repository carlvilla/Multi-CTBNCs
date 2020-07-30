package com.cig.mctbnc.data.reader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.server.LogStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
public class SeparateCSVReader implements DatasetReader {

	File[] files;
	String nameTimeVariable;
	String[] nameClassVariables;
	String[] excludeVariables;
	static Logger logger = LogManager.getLogger(SeparateCSVReader.class);

	public SeparateCSVReader(File[] files, String nameTimeVariable, String[] nameClassVariables,
			String[] excludeVariables) {
		this.files = files;
		this.nameTimeVariable = nameTimeVariable;
		this.nameClassVariables = nameClassVariables;
		this.excludeVariables = excludeVariables;
	}

	@Override
	public Dataset readDataset() {
		Dataset dataset = new Dataset(nameTimeVariable, nameClassVariables);
		for (File file : files) {
			try {
				List<String[]> dataSequence = readCSV(file.getAbsolutePath(), excludeVariables);
				dataset.addSequence(dataSequence);
			} catch (VariableNotFoundException e) {
				logger.warn(e.getMessage());
			}
		}
		return dataset;
	}

	/**
	 * Reads a CSV file.
	 * 
	 * @param pathFile
	 *            path to the CSV file
	 * @param excludeVariables
	 * @return list with the rows (lists of strings) of the CSV
	 * @throws VariableNotFoundException
	 */
	public List<String[]> readCSV(String pathFile, String[] excludeVariables) throws VariableNotFoundException {
		FileReader reader;
		List<String[]> list = new ArrayList<String[]>();
		try {
			reader = new FileReader(pathFile);
			CSVReader csvReader = new CSVReader(reader);
			// If it was specified variables to ignore
			if (excludeVariables.length > 0) {
				// Obtain name of the variables
				List<String> head = new ArrayList<String>(Arrays.asList(csvReader.readNext()));
				// Obtain the index of the variables to ignore
				List<Integer> indexesToIgnore = new ArrayList<Integer>();
				for (String excludeVariable : excludeVariables) {
					int index = head.indexOf(excludeVariable);
					if (index == -1) {
						// The variable to exclude does not exist in the analysed CSV
						String message = String.format("Variable %s not found in file %s. The file will be ignored.", excludeVariable, pathFile);
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

}
