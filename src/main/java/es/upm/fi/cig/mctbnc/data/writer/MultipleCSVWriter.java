package es.upm.fi.cig.mctbnc.data.writer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.opencsv.CSVWriter;

import es.upm.fi.cig.mctbnc.data.representation.Dataset;
import es.upm.fi.cig.mctbnc.data.representation.Observation;
import es.upm.fi.cig.mctbnc.data.representation.Sequence;
import es.upm.fi.cig.mctbnc.util.Util;

/**
 * Manage the writing of datasets into CSVs
 * 
 * @author Carlos Villa Blanco
 *
 */
public class MultipleCSVWriter {
	static Logger logger = LogManager.getLogger(MultipleCSVWriter.class);

	/**
	 * Write the sequences of the provided dataset into multiple CSVs to the
	 * specified directory.
	 * 
	 * @param dataset
	 * @param destinationPath
	 */
	public static void write(Dataset dataset, String destinationPath) {
		logger.info("Writing dataset with {} sequences into {}", dataset.getNumDataPoints(), destinationPath);
		List<Sequence> sequences = dataset.getSequences();
		List<String> header = dataset.getNameAllVariables();
		// Create folders of provided path if they do not exist
		File f = new File(destinationPath);
		f.mkdirs();
		for (int i = 0; i < sequences.size(); i++) {
			// Get a sequence
			Sequence sequence = sequences.get(i);
			// Define the name of the CSV for the sequence
			String nameFile = "Sequence" + i;
			// Define the path of the CSV
			String pathSequence = Paths.get(destinationPath, nameFile + ".csv").toString();
			// Get the observations of the sequence
			List<Observation> observations = sequence.getObservations();
			// Store the data of the sequences
			List<String[]> dataSequence = new ArrayList<String[]>();
			try {
				CSVWriter csvWriter = new CSVWriter(new FileWriter(pathSequence), ',');
				// Write header
				dataSequence.add(Util.listToArray(header));
				// Write observations
				for (Observation observation : observations) {
					// Extract time and state of features during the observation
					String time = String.valueOf(observation.getTimeValue());
					String[] valuesFeatures = observation.getValues();
					// Create an array to store the extracted data
					String[] dataObservation = new String[valuesFeatures.length + 1];
					dataObservation[0] = time;
					System.arraycopy(valuesFeatures, 0, dataObservation, 1, valuesFeatures.length);
					dataSequence.add(dataObservation);
				}
				csvWriter.writeAll(dataSequence);
				csvWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
				logger.error("Error creating CSV to write sequence");
			}

		}

	}

}
