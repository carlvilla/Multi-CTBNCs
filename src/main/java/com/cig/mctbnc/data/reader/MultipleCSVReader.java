package com.cig.mctbnc.data.reader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.cig.mctbnc.data.representation.Dataset;
import com.cig.mctbnc.exceptions.UnreadDatasetException;
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
	 * Constructs a {@code MultipleCSVReader} that extracts all the CSV files from
	 * the specified folder.
	 * 
	 * @param datasetFolder folder path where the csv files are stored
	 * @throws FileNotFoundException
	 */
	public MultipleCSVReader(String datasetFolder) throws FileNotFoundException {
		super(datasetFolder);
		logger.info("Generating CSV reader for multiples csv files in {}", datasetFolder);
		// Read all csv files from the specified folder
		File folder = new File(datasetFolder);
		this.files = folder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File folder, String name) {
				return name.endsWith(".csv");
			}
		});
		// Order the files by name
		Arrays.sort(this.files);
		// Extract variables names from first CSV file
		extractVariableNames(this.files[0]);
	}

	@Override
	public Dataset readDataset() throws UnreadDatasetException {
		if (isDatasetOutdated()) {
			this.dataset = new Dataset(this.nameTimeVariable, this.nameClassVariables);
			// Names of the files from which the dataset was read. Some files could be
			// rejected due to problems in its content.
			List<String> nameAcceptedFiles = new ArrayList<String>();
			for (File file : this.files)
				readCSV(file, nameAcceptedFiles);
			// Check if any sequence was added to the dataset
			if (this.dataset.getNumDataPoints() == 0)
				throw new UnreadDatasetException("Any sequence was succesfully processed");
			if (this.dataset.getNumDataPoints() < this.files.length)
				logger.warn("Some sequences not added. They may not have valid variables or enough observations");
			// Remove variables with zero variance
			this.dataset.removeZeroVarianceFeatures();
			// Save in the dataset the files used to extract its sequences
			this.dataset.setNameFiles(nameAcceptedFiles);
			// Set the dataset as not out-of-date
			setDatasetAsOutdated(false);
		}
		return this.dataset;
	}

	/**
	 * Read a single CSV file and .
	 * 
	 * @param nameAcceptedFiles
	 * @param file
	 * @throws UnreadDatasetException
	 */
	private void readCSV(File file, List<String> nameAcceptedFiles) throws UnreadDatasetException {
		try {
			List<String[]> dataSequence = readCSV(file.getAbsolutePath(), this.excludeVariables);
			boolean sequenceAdded = this.dataset.addSequence(dataSequence);
			if (sequenceAdded)
				nameAcceptedFiles.add(file.getName());
		} catch (FileNotFoundException e) {
			throw new UnreadDatasetException("There was an error reading the files of the dataset");
		} catch (VariableNotFoundException e) {
			logger.warn(e.getMessage());
		}
	}

}
