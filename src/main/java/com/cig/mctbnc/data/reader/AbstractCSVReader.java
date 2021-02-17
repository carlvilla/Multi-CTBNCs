package com.cig.mctbnc.data.reader;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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
 * Common attributes and methods for dataset readers.
 * 
 * @author Carlos Villa Blanco
 *
 */
public abstract class AbstractCSVReader implements DatasetReader {
	String datasetFolder;
	List<String> nameVariables;
	String nameTimeVariable;
	List<String> nameClassVariables;
	List<String> excludeVariables;
	// Stores a copy of the loaded dataset to avoid reloading it if not necessary
	Dataset dataset;
	// Flag variable to know if the dataset needs to be reloaded
	boolean outdatedDataset;
	static Logger logger = LogManager.getLogger(AbstractCSVReader.class);

	/**
	 * Receives the path to the folder of the dataset and initialize the reader as
	 * out-of-date. In that way, the dataset will be loaded when it is requested.
	 * 
	 * @param datasetFolder
	 */
	public AbstractCSVReader(String datasetFolder) {
		this.datasetFolder = datasetFolder;
		outdatedDataset = true;
	}

	/**
	 * Extract the names of the variables from a CSV file. It is assumed that the
	 * names are in the first row.
	 * 
	 * @param csvFile
	 * @throws FileNotFoundException
	 */
	public void extractVariableNames(File csvFile) throws FileNotFoundException {
		if (csvFile.isFile()) {
			FileReader reader = new FileReader(csvFile);
			CSVReader csvReader = new CSVReader(reader);
			try {
				nameVariables = Arrays.asList(csvReader.readNext());
			} catch (NullPointerException | IOException e) {
				throw new FileNotFoundException("Impossible to read CSV file");
			} finally {
				closeReader(csvReader);
			}
		} else {
			throw new FileNotFoundException("Impossible to read CSV file");
		}
	}

	/**
	 * Reads a CSV file.
	 * 
	 * @param pathFile         path to the CSV file
	 * @param excludeVariables
	 * @return list with the rows (lists of strings) of the CSV
	 * @throws VariableNotFoundException
	 * @throws FileNotFoundException 
	 */
	public List<String[]> readCSV(String pathFile, List<String> excludeVariables) throws VariableNotFoundException, FileNotFoundException {
		List<String[]> list = new ArrayList<String[]>();
		FileReader reader = new FileReader(pathFile);
		CSVReader csvReader = new CSVReader(reader);
		try {
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
		} catch (NullPointerException | IOException e) {
			logger.warn("Impossible to read file {}", pathFile);
		} finally {
			closeReader(csvReader);
		}
		return list;
	}

	@Override
	public void setVariables(String nameTimeVariable, List<String> nameClassVariables, List<String> nameFeatures) {
		this.nameTimeVariable = nameTimeVariable;
		this.nameClassVariables = nameClassVariables;
		// Variables that should be ignored
		excludeVariables = new ArrayList<String>(getAllVariablesDataset());
		excludeVariables.remove(nameTimeVariable);
		excludeVariables.removeAll(nameClassVariables);
		excludeVariables.removeAll(nameFeatures);
	}

	@Override
	public List<String> getAllVariablesDataset() {
		return nameVariables;
	}

	@Override
	public void setDatasetAsOutdated(boolean outdated) {
		outdatedDataset = outdated;
	}

	@Override
	public boolean isDatasetOutdated() {
		return outdatedDataset;
	}

	private void closeReader(Closeable reader) {
		try {
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
