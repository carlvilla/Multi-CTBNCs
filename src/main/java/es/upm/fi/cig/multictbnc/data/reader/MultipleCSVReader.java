package es.upm.fi.cig.multictbnc.data.reader;

import es.upm.fi.cig.multictbnc.data.representation.Dataset;
import es.upm.fi.cig.multictbnc.exceptions.UnreadDatasetException;
import es.upm.fi.cig.multictbnc.exceptions.VariableNotFoundException;
import es.upm.fi.cig.multictbnc.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Provides the logic to read separate CSV files. It is possible to find time series data where the sequences are stored
 * in separate CSV files.
 *
 * @author Carlos Villa Blanco
 */
public class MultipleCSVReader extends AbstractCSVReader {
	private static final Logger logger = LogManager.getLogger(MultipleCSVReader.class);
	File[] files;
	int numReadFiles = 0;

	/**
	 * Constructs a {@code MultipleCSVReader} that extracts all the CSV files from the specified folder.
	 *
	 * @param datasetFolder folder path where the CSV files are stored
	 * @throws FileNotFoundException  if the CSV files were not found
	 * @throws UnreadDatasetException if the provided dataset could not be read
	 */
	public MultipleCSVReader(String datasetFolder) throws FileNotFoundException, UnreadDatasetException {
		super(datasetFolder);
		// Read all CSV files from the specified folder
		File folder = new File(datasetFolder);
		this.files = folder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File folder, String name) {
				return name.endsWith(".csv");
			}
		});
		if (this.files == null || this.files.length == 0)
			throw new UnreadDatasetException("No CSV files found in the specified folder");
		logger.info("Generating CSV reader for multiples CSV files in {}", datasetFolder);
		// Order the files by name
		Arrays.sort(this.files, new Comparator<File>() {
			@Override
			public int compare(File f1, File f2) {
				return Long.compare(Util.extractLong(f1.toString()), Util.extractLong(f2.toString()));
			}
		});
		// Extract variables names from first CSV file
		extractVariableNames(this.files[0]);
	}

	@Override
	public Dataset readDataset() throws UnreadDatasetException {
		if (isDatasetOutdated()) {
			logger.info("Reading dataset from multiples CSV files in {}", this.datasetFolder);
			this.dataset = new Dataset(this.nameTimeVariable, this.nameClassVariables);
			for (File file : this.files)
				readCSV(file);
			// Check if any sequence was added to the dataset
			if (this.dataset.getNumDataPoints() == 0)
				throw new UnreadDatasetException("Any sequence was succesfully processed");
			// Remove variables with zero variance
			this.dataset.checkVarianceFeatures(this.removeZeroVarianceVariables);
			// Set the dataset as not out-of-date
			setDatasetAsOutdated(false);
		}
		return this.dataset;
	}

	@Override
	public Dataset readDataset(int numFiles) throws UnreadDatasetException {
		logger.info("Reading dataset from {} CSV files in {}", numFiles, this.datasetFolder);
		this.dataset = new Dataset(this.nameTimeVariable, this.nameClassVariables);
		for (int i = numReadFiles; i < numReadFiles + numFiles; i++)
			if (i < this.files.length)
				readCSV(this.files[i]);
			else
				// There are no more files to read
				break;
		// Check if any sequence was added to the dataset
		if (this.dataset.getNumDataPoints() == 0)
			throw new UnreadDatasetException("Any sequence was succesfully processed");
		numReadFiles += numFiles;
		return this.dataset;
	}

	/**
	 * Reads a single CSV file.
	 *
	 * @param file CSV file
	 * @throws UnreadDatasetException thrown if the dataset could not be read
	 */
	private void readCSV(File file) throws UnreadDatasetException {
		try {
			List<String[]> dataSequence = readCSV(file.getAbsolutePath(), this.excludeVariables);
			this.dataset.addSequence(dataSequence, file.getAbsolutePath());
		} catch (FileNotFoundException fnfe) {
			throw new UnreadDatasetException("An error occurred while reading the files of the dataset");
		} catch (VariableNotFoundException vnfe) {
			logger.warn(vnfe.getMessage());
		}
	}

}