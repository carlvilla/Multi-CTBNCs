package es.upm.fi.cig.multictbnc.data.reader;

import es.upm.fi.cig.multictbnc.exceptions.UnreadDatasetException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.util.List;

/**
 * Creates dataset readers.
 *
 * @author Carlos Villa Blanco
 */
public abstract class DatasetReaderFactory {
	static Logger logger = LogManager.getLogger(DatasetReaderFactory.class);

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

	/**
	 * Generates the correct dataset reader for the given dataset path.
	 *
	 * @param datasetReader name of the dataset reader to build
	 * @param pathDataset   path of the dataset folder
	 * @param sizeSequence  maximum length of the sequences when they are extracted from a unique file
	 * @return a dataset reader a {@code DatasetReader}
	 * @throws FileNotFoundException if the provided files were not found
	 */
	public static DatasetReader getDatasetReader(String datasetReader, String pathDataset, int sizeSequence)
			throws FileNotFoundException {
		try {
			if ("Multiple CSV".equals(datasetReader)) {
				return new MultipleCSVReader(pathDataset);
			}// Unique CSV
			return new SingleCSVReader(pathDataset, sizeSequence);
		} catch (UnreadDatasetException ude) {
			logger.error(ude.getMessage());
			return null;
		}
	}

}