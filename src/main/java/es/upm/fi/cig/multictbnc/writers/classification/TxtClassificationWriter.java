package es.upm.fi.cig.multictbnc.writers.classification;

import es.upm.fi.cig.multictbnc.classification.Prediction;
import es.upm.fi.cig.multictbnc.data.representation.Dataset;
import es.upm.fi.cig.multictbnc.data.representation.State;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Class to write the predictions made on a dataset in a TXT file.
 *
 * @author Carlos Villa Blanco
 */
public class TxtClassificationWriter {
	private static final Logger logger = LogManager.getLogger(TxtClassificationWriter.class);

	/**
	 * Writes predictions of a dataset in the specified folder.
	 *
	 * @param predictions {@code Prediction} array
	 * @param dataset     dataset from which predictions were made
	 * @param pathFolder  folder where the predictions will be stored
	 */
	public static void writePredictions(Prediction[] predictions, Dataset dataset, String pathFolder) {
		String fileName = new SimpleDateFormat("yyyyMMddHHmmSS'.txt'").format(new Date());
		File file = new File(pathFolder + fileName);
		file.getParentFile().mkdirs();
		try {
			PrintWriter writer = new PrintWriter(file, "UTF-8");
			for (int i = 0; i < predictions.length; i++) {
				String fileSequence = dataset.getSequences().get(i).getFilePath();
				State predictedCC = predictions[i].getPredictedClasses();
				double probabilityCC = predictions[i].getProbabilityPrediction();
				writer.print(fileSequence + " / Predicted classes: " + predictedCC);
				if (predictions[i].getProbabilities() != null)
					writer.print(" / Probability class configuration: " + probabilityCC + "%");
				writer.println();
			}
			writer.close();
			logger.info("Classifications saved in {}", file.getAbsoluteFile());
		} catch (IOException e) {
			logger.error("An error occurred while creating the file for the classifications");
		}
	}
}