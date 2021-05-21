package es.upm.fi.cig.mctbnc.data.reader;

import java.io.FileNotFoundException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.upm.fi.cig.mctbnc.exceptions.UnreadDatasetException;

/**
 * Creates dataset readers.
 * 
 * @author Carlos Villa Blanco
 *
 */
public abstract class DatasetReaderFactory {
	static Logger logger = LogManager.getLogger(DatasetReaderFactory.class);

	/**
	 * Generates the correct dataset reader for the given dataset path.
	 * 
	 * @param datasetReader name of the dataset reader to build
	 * @param pathDataset   path of the dataset folder
	 * @param sizeSequence  maximum length of the sequences when they are extracted
	 *                      from a unique file
	 * @return a dataset reader a {@code DatasetReader}
	 * @throws FileNotFoundException  if the provided files were not found
	 * @throws UnreadDatasetException if the provided dataset could not be read
	 * 
	 */
	public static DatasetReader getDatasetReader(String datasetReader, String pathDataset, int sizeSequence)
			throws FileNotFoundException, UnreadDatasetException {
		try {
			switch (datasetReader) {
			case ("Multiple CSV"):
				return new MultipleCSVReader(pathDataset);
			default:
				// Unique CSV
				return new SingleCSVReader(pathDataset, sizeSequence);
			}
		} catch (UnreadDatasetException e) {
			logger.error(e.getMessage());
			return null;
		}
	}

	/**
	 * Returns the name of available dataset readers.
	 * 
	 * @return name of available dataset readers
	 */
	public static List<String> getAvailableDatasetReaders() {
		return List.of("Multiple CSV", "Single CSV");
	}

	/**
	 * Returns the name of available strategies for the extraction of sequences.
	 * 
	 * @return available strategies
	 */
	public static List<String> getAvailableStrategies() {
		return List.of("Fixed size");
	}

}
