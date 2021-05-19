package es.upm.fi.cig.mctbnc.data.reader;

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

import com.opencsv.CSVReader;

import es.upm.fi.cig.mctbnc.data.representation.Dataset;
import es.upm.fi.cig.mctbnc.exceptions.VariableNotFoundException;

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
	 * Receives the path to the dataset folder and initialize the reader as
	 * out-of-date. In that way, the dataset will be loaded when it is requested.
	 * 
	 * @param datasetFolder path to the dataset folder
	 */
	public AbstractCSVReader(String datasetFolder) {
		this.datasetFolder = datasetFolder;
		this.outdatedDataset = true;
	}

	/**
	 * Extracts the names of the variables from a CSV file. It is assumed that the
	 * names are in the first row.
	 * 
	 * @param csvFile CSV file
	 * @throws FileNotFoundException if the CSV file was not found
	 */
	public void extractVariableNames(File csvFile) throws FileNotFoundException {
		if (csvFile.isFile()) {
			FileReader reader = new FileReader(csvFile);
			CSVReader csvReader = new CSVReader(reader);
			try {
				this.nameVariables = Arrays.asList(csvReader.readNext());
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
	 * @param excludeVariables names of variables to ignore when reading the CSV
	 * @return list with the rows (lists of strings) of the CSV
	 * @throws VariableNotFoundException if a specified variable was not found in
	 *                                   the provided files
	 * @throws FileNotFoundException     if the CSV file was not found
	 */
	public List<String[]> readCSV(String pathFile, List<String> excludeVariables)
			throws VariableNotFoundException, FileNotFoundException {
		List<String[]> list = new ArrayList<String[]>();
		FileReader reader = new FileReader(pathFile);
		CSVReader csvReader = new CSVReader(reader);
		try {
			// If it was specified variables to ignore
			if (excludeVariables != null && excludeVariables.size() > 0) {
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
	public void setVariables(String nameTimeVariable, List<String> nameFeatureVariables) {
		this.nameTimeVariable = nameTimeVariable;
		// Variables that should be ignored
		this.excludeVariables = new ArrayList<String>(getNameVariables());
		this.excludeVariables.remove(nameTimeVariable);
		this.excludeVariables.removeAll(nameFeatureVariables);
	}

	@Override
	public void setVariables(String nameTimeVariable, List<String> nameClassVariables,
			List<String> nameFeatureVariables) {
		this.nameTimeVariable = nameTimeVariable;
		this.nameClassVariables = nameClassVariables;
		// Variables that should be ignored
		this.excludeVariables = new ArrayList<String>(getNameVariables());
		this.excludeVariables.remove(nameTimeVariable);
		this.excludeVariables.removeAll(nameClassVariables);
		this.excludeVariables.removeAll(nameFeatureVariables);
	}

	@Override
	public String getNameTimeVariable() {
		return this.nameTimeVariable;
	}

	@Override
	public List<String> getNameClassVariables() {
		return this.nameClassVariables;
	}

	@Override
	public List<String> getNameFeatureVariables() {
		List<String> nameFeatureVariables = new ArrayList<String>(this.nameVariables);
		nameFeatureVariables.remove(this.nameTimeVariable);
		nameFeatureVariables.removeAll(this.nameClassVariables);
		nameFeatureVariables.removeAll(this.excludeVariables);
		return nameFeatureVariables;
	}

	@Override
	public List<String> getNameVariables() {
		return this.nameVariables;
	}

	@Override
	public void setDatasetAsOutdated(boolean outdated) {
		this.outdatedDataset = outdated;
	}

	@Override
	public boolean isDatasetOutdated() {
		return this.outdatedDataset;
	}

	private void closeReader(Closeable reader) {
		try {
			reader.close();
		} catch (IOException e) {
			logger.error("An error occurred while closing the CSV reader: {}", e.getMessage());
		}
	}

}
